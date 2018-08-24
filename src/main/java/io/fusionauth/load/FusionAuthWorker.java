/*
 * Copyright (c) 2018, FusionAuth, All Rights Reserved
 */
package io.fusionauth.load;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.inversoft.error.Errors;
import com.inversoft.load.Configuration;
import com.inversoft.load.Worker;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;
import com.inversoft.rest.ClientResponse;

/**
 * @author Daniel DeGroff
 */
public class FusionAuthWorker implements Worker {

  public final static String Password = "11e7ea7b-784d-4687-bf2d-4f8ee479a4dd11e7ea7b-784d-4687-bf2d-4f8ee479a4dd";

  private final UUID applicationId;

  private final FusionAuthClient client;

  private final boolean debug;

  private final String directive;

  private final String encryptionScheme;

  private final int factor;

  private final int loginLowerBound;

  private final int loginUpperBound;

  private AtomicInteger counter;

  public FusionAuthWorker(FusionAuthClient client, Configuration configuration, AtomicInteger counter, String directive) {
    this.client = client;
    this.directive = directive;
    this.counter = counter;
    applicationId = UUID.fromString(configuration.getString("applicationId"));
    loginLowerBound = configuration.getInteger("loginLowerBound", 0);
    loginUpperBound = configuration.getInteger("loginUpperBound", 1_000_000);
    // This speeds up login and registration quite a bit. Default salted-sha256 is a factor of 20,000.
    factor = configuration.getInteger("factor", 1);
    encryptionScheme = configuration.getString("encryptionScheme", "salted-sha256");
    debug = configuration.getBoolean("debug", false);
  }

  @Override
  public boolean execute() {
    User user = new User();

    if (directive.equals("register")) {
      user.email = "load_user_" + counter.incrementAndGet() + "@fusionauth.io";
      user.password = Password;
      user.encryptionScheme = this.encryptionScheme;
      user.factor = this.factor;

      ClientResponse<RegistrationResponse, Errors> result = client.register(null,
                                                                            new RegistrationRequest(user, new UserRegistration()
                                                                                .with((r) -> r.applicationId = applicationId)
                                                                                .with((r) -> r.roles.add("user"))));
      if (result.wasSuccessful()) {
        return true;
      }

      printErrors(result);
    } else if (directive.equals("login")) {
      int random = new Random().nextInt(/**/(loginUpperBound - loginLowerBound) + 1) + loginLowerBound;
//      int random = counter.incrementAndGet();
      user.email = "load_user_" + random + "@fusionauth.io";
      user.password = Password;

      ClientResponse<LoginResponse, Errors> result = client.login(new LoginRequest(applicationId, user.email, user.password));
      if (result.wasSuccessful()) {
        return true;
      }

      printErrors(result);
    }

    return false;
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }

  private void printErrors(ClientResponse result) {
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
