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

  private final FusionAuthClient client;

  private final Configuration configuration;

  private final String directive;

  @ConfigurationInjected
  public FusionAuthWorkerFactory(Configuration configuration) {
    this.client = new FusionAuthClient(configuration.getString("apiKey"), configuration.getString("url"), 5_000, 10_000);
    this.configuration = configuration;
    this.directive = configuration.getString("directive", "register");
    if (counter.intValue() == -1) {
      counter.set(configuration.getInteger("counter", 0));
    }
  }

  @Override
  public Worker createWorker() {
    return switch (directive) {
      case "create-tenant" -> new FusionAuthCreateTenantWorker(client, configuration, counter);
      case "login" -> new FusionAuthLoginWorker(client, configuration);
      case "register" -> new FusionAuthRegistrationWorker(client, configuration, counter);
      default -> throw new IllegalArgumentException("Invalid directive [" + directive + "]");
    };
  }

  @Override
  public void prepare(LoadDefinition loadDefinition) {
  }
}
