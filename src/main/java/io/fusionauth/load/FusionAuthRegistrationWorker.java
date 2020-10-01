/*
 * Copyright (c) 2018, FusionAuth, All Rights Reserved
 */
package io.fusionauth.load;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.inversoft.error.Errors;
import com.inversoft.load.Configuration;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;

/**
 * @author Daniel DeGroff
 */
public class FusionAuthRegistrationWorker extends BaseWorker {
  private final UUID applicationId;

  private final FusionAuthClient client;

  private final AtomicInteger counter;

  private final String encryptionScheme;

  private final int factor;

  public FusionAuthRegistrationWorker(FusionAuthClient client, Configuration configuration, AtomicInteger counter) {
    super(configuration);
    this.client = client;
    this.counter = counter;
    this.applicationId = UUID.fromString(configuration.getString("applicationId"));
    this.factor = configuration.getInteger("factor");
    this.encryptionScheme = configuration.getString("encryptionScheme");
  }

  @Override
  public boolean execute() {
    User user = new User();
    user.email = "load_user_" + counter.incrementAndGet() + "@fusionauth.io";
    user.password = Password;
    user.encryptionScheme = encryptionScheme;
    user.factor = factor;

    UserRegistration userRegistration = new UserRegistration().with((r) -> r.applicationId = applicationId)
                                                              .with((r) -> r.roles.add("user"));
    ClientResponse<RegistrationResponse, Errors> result = client.register(null, new RegistrationRequest(user, userRegistration));
    if (result.wasSuccessful()) {
      return true;
    }

    printErrors(result);
    return false;
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }
}
