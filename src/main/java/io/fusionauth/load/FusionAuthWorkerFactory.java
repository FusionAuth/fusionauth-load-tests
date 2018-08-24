/*
 * Copyright (c) 2018, FusionAuth, All Rights Reserved
 */
package io.fusionauth.load;

import java.util.concurrent.atomic.AtomicInteger;

import com.inversoft.load.Configuration;
import com.inversoft.load.ConfigurationInjected;
import com.inversoft.load.LoadDefinition;
import com.inversoft.load.Worker;
import com.inversoft.load.WorkerFactory;
import io.fusionauth.client.FusionAuthClient;

/**
 * @author Daniel DeGroff
 */
public class FusionAuthWorkerFactory implements WorkerFactory {
  private static final AtomicInteger counter = new AtomicInteger(-1);

  private final Configuration configuration;

  private FusionAuthClient client;

  private String directive;

  @ConfigurationInjected
  public FusionAuthWorkerFactory(Configuration configuration) {
    client = new FusionAuthClient(configuration.getString("apiKey"), configuration.getString("url"), 5_000, 10_000);
    this.configuration = configuration;
    directive = configuration.getString("directive", "register");
    if (counter.intValue() == -1) {
      counter.set(configuration.getInteger("counter", 0));
    }
  }

  @Override
  public Worker createWorker() {
    return new FusionAuthWorker(client, configuration, counter, directive);
  }

  @Override
  public void prepare(LoadDefinition loadDefinition) {
  }
}
