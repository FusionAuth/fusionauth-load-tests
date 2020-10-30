/*
 * Copyright (c) 2012-2020, FusionAuth, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package io.fusionauth.load;

import java.util.concurrent.atomic.AtomicInteger;

import io.fusionauth.load.Configuration;
import io.fusionauth.load.ConfigurationInjected;
import io.fusionauth.load.LoadDefinition;
import io.fusionauth.load.Worker;
import io.fusionauth.load.WorkerFactory;
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
