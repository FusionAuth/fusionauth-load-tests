/*
 * Copyright (c) 2012-2025, FusionAuth, All Rights Reserved
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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.fusionauth.client.FusionAuthClient;

/**
 * @author Daniel DeGroff
 */
@SuppressWarnings("unused")
public class FusionAuthWorkerFactory implements WorkerFactory {
  private static final AtomicInteger counter = new AtomicInteger(-1);

  private final FusionAuthClient client;

  private final Configuration configuration;

  private final String directive;

  @ConfigurationInjected
  public FusionAuthWorkerFactory(Configuration configuration) {
    String apiKey = configuration.getString("apiKey", null);
    String tenantId = configuration.getString("tenantId", null);
    String url = configuration.getString("url", null);
    if (apiKey != null && url != null) {
      this.client = new FusionAuthClient(apiKey, url, 5_000, 10_000);

      if (tenantId != null) {
        this.client.setTenantId(UUID.fromString(tenantId));
      }
    } else {
      this.client = null;
    }

    this.configuration = configuration;
    this.directive = configuration.getString("directive", "register");
    if (counter.intValue() == -1) {
      counter.set(configuration.getInteger("counter", 0));
    }
  }

  @Override
  public Worker createWorker() {
    return switch (directive) {
      case "create-application" -> new FusionAuthCreateApplicationWorker(client, configuration, counter);
      case "create-tenant" -> new FusionAuthCreateTenantWorker(client, configuration, counter);
      case "email-verification" -> new FusionAuthEmailVerificationIdWorker(client, configuration, counter);
      case "simple-get" -> new FusionAuthSimpleGetWorker(configuration);
      case "login" -> new FusionAuthLoginWorker(client, configuration);
      case "update-password" -> new FusionAuthUpdatePasswordWorker(client, configuration);
      case "oauth2/authorize" -> new FusionAuthOAuth2AuthorizeWorker(client, configuration);
      case "refresh" -> new FusionAuthRefreshWorker(client, configuration);
      case "register" -> new FusionAuthRegistrationWorker(client, configuration, counter);
      case "search" -> new FusionAuthSearchWorker(client, configuration);
      case "search-data" -> new FusionAuthSearchDataWorker(client, configuration);
      case "retrieve-email" -> new FusionAuthRetrieveEmailWorker(client, configuration);
      case "user-import" -> new FusionAuthUserImportWorker(client, configuration, counter);
      case "elasticsearch" -> new ElasticsearchWorker(configuration);
      default -> throw new IllegalArgumentException("Invalid directive [" + directive + "]");
    };
  }

  @Override
  public void prepare(LoadDefinition loadDefinition) {
    String directive = null;
    String url = null;
    String apiKey = null;
    if (loadDefinition != null && loadDefinition.workerFactory != null) {
      directive = loadDefinition.workerFactory.getString("directive", null);
      url = loadDefinition.workerFactory.getString("url", null);
      apiKey = loadDefinition.workerFactory.getString("apiKey", null);
    }
    System.out.println("  --> Worker directive:\t" + directive);
    System.out.println("  --> Worker url:\t" + url);
    System.out.println("  --> Target version:\t" + fetchVersion(url, apiKey));
  }

  protected String fetchVersion(String url, String apiKey) {
    if (apiKey != null && url != null) {
      var client = new FusionAuthClient(apiKey, url, 5_000, 10_000);
      var response = client.retrieveSystemStatusUsingAPIKey();
      if (response.wasSuccessful() && response.successResponse != null) {
        return response.successResponse.getOrDefault("version", "unavailable").toString();
      }
    }
    return "unavailable";
  }
}
