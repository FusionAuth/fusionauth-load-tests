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

/**
 * @author Brian Pontarelli
 */
@SuppressWarnings("unused")
public class HTTPWorkerFactory implements WorkerFactory {
  private final Configuration configuration;

  private final String directive;

  @ConfigurationInjected
  public HTTPWorkerFactory(Configuration configuration) {
    this.configuration = configuration;
    this.directive = configuration.getString("directive", "java-http-load-test");
  }

  @Override
  public Worker createWorker() {
    return switch (directive) {
      case "java-http-load-test" -> new JavaHTTPLoadTestWorker(configuration);
      default -> throw new IllegalArgumentException("Invalid directive [" + directive + "]");
    };
  }

  @Override
  public void prepare(LoadDefinition loadDefinition) {
  }
}
