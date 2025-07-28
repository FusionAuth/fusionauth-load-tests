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

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;

/**
 * Worker to test creating users and registrations. The user data includes a randomly generated external Id.
 *
 * @author Daniel DeGroff
 */
public class FusionAuthRegistrationWorker extends FusionAuthBaseWorker {
  private final UUID configuredApplicationId;

  private final AtomicInteger counter;

  private final String encryptionScheme;

  private final int factor;

  public FusionAuthRegistrationWorker(FusionAuthClient client, Configuration configuration, AtomicInteger counter) {
    super(client, configuration);
    this.counter = counter;
    if (configuration.hasProperty("applicationId")) {
      this.configuredApplicationId = UUID.fromString(configuration.getString("applicationId"));
    } else {
      this.configuredApplicationId = null;
    }
    this.factor = configuration.getInteger("factor");
    this.encryptionScheme = configuration.getString("encryptionScheme");
  }

  @Override
  public boolean execute() {
    setUserIndex(counter.incrementAndGet());

    User user = new User();
    user.email = "load_user_" + userIndex + "@fusionauth.io";
    user.password = Password;
    user.encryptionScheme = encryptionScheme;
    user.factor = factor;
    user.data.put("externalId", secureString(20, ALPHA_NUMERIC_CHARACTERS));
    user.tenantId = tenantId;

    UUID registrationAppId = configuredApplicationId != null ? configuredApplicationId : applicationId;
    UserRegistration userRegistration = new UserRegistration().with(r -> r.applicationId = registrationAppId)
                                                              .with(r -> r.roles.add("user"));
    ClientResponse<RegistrationResponse, Errors> result = tenantScopedClient.register(null, new RegistrationRequest(null, user, userRegistration));
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
