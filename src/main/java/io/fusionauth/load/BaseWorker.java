/*
 * Copyright (c) 2020, FusionAuth, All Rights Reserved
 */
package io.fusionauth.load;

import com.inversoft.error.Errors;
import com.inversoft.load.Configuration;
import com.inversoft.load.Worker;
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
