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

import com.inversoft.error.Errors;
import io.fusionauth.load.Configuration;
import io.fusionauth.load.Worker;
import com.inversoft.rest.ClientResponse;

/**
 * Base worker with error handling and the password.
 *
 * @author Brian Pontarelli
 */
public abstract class BaseWorker implements Worker {
  public final static String Password = "11e7ea7b-784d-4687-bf2d-4f8ee479a4dd11e7ea7b-784d-4687-bf2d-4f8ee479a4dd";

  final Configuration configuration;

  private final boolean debug;

  protected BaseWorker(Configuration configuration) {
    this.debug = configuration.getBoolean("debug", false);
    this.configuration = configuration;
  }

  void printErrors(ClientResponse<?, Errors> result) {
    if (debug || result.status == 400) {
      if (result.exception != null) {
        System.out.println(result.exception.getMessage());
      } else if (result.errorResponse != null) {
        System.out.println(result.errorResponse);
      } else {
        System.out.println(result.status);
      }

      if (result.status == 500) {
        throw new RuntimeException("Stopping load testing. Did not expect a status of 500, check your configuration.");
      }
    }
  }
}
