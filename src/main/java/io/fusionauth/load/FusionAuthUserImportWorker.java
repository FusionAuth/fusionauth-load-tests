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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.user.ImportRequest;

/**
 * Worker to test importing users
 *
 * @author Brent Halsey
 */
public class FusionAuthUserImportWorker extends FusionAuthBaseWorker {
  private final int batchSize;

  private final UUID configuredApplicationId;

  private final AtomicInteger counter;

  private final int factor;

  public FusionAuthUserImportWorker(FusionAuthClient client, Configuration configuration, AtomicInteger counter) {
    super(client, configuration);
    this.counter = counter;
    if (configuration.hasProperty("applicationId")) {
      this.configuredApplicationId = UUID.fromString(configuration.getString("applicationId"));
    } else {
      this.configuredApplicationId = null;
    }
    this.batchSize = configuration.getInteger("batchSize");
    this.factor = configuration.getInteger("factor");
  }

  @Override
  public boolean execute() {

    int offset = counter.get(); // Only run this single thread/worker
    System.out.println("Importing users with offset: " + offset);

    // do a batch for each tenant. ti = tenant index
    for (int ti = 1; ti <= tenantCount; ti++) {
      List<User> users = new ArrayList<>();
      for (int j = 0; j < batchSize; j++) {
        setUserIndex(offset + ti + j * tenantCount);

        User user = new User();
        user.active = true;
        user.email = "load_user_" + userIndex + "@fusionauth.io";
        user.password = Password;
        user.factor = factor;
        user.data.put("externalId", secureString(20, ALPHA_NUMERIC_CHARACTERS));
        user.tenantId = tenantId;

        UUID registrationAppId = configuredApplicationId != null ? configuredApplicationId : applicationId;
        UserRegistration userRegistration = new UserRegistration().with(r -> r.applicationId = registrationAppId)
                                                                  .with(r -> r.roles.add("user"));
        user.getRegistrations().add(userRegistration);
        users.add(user);
      }

      ClientResponse<Void, Errors> result = tenantScopedClient.importUsers(new ImportRequest(users));
      if (!result.wasSuccessful()) {
        // keep going if we have an error, but log it
        printErrors(result);
      } else {
        System.out.println("Imported " + users.size() + " users for tenant " + tenantIndex);
      }
    }
    counter.addAndGet(batchSize * tenantCount);
    return true;
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }
}
