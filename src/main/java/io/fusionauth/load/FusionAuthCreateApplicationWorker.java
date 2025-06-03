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

import java.util.concurrent.atomic.AtomicInteger;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;

/**
 * @author Daniel DeGroff
 */
public class FusionAuthCreateApplicationWorker extends BaseWorker {
  private final FusionAuthClient client;

  private final AtomicInteger counter;

  private final int numberOfTenants;

  public FusionAuthCreateApplicationWorker(FusionAuthClient client, Configuration configuration, AtomicInteger counter) {
    super(configuration);
    this.client = client;
    this.counter = counter;
    this.numberOfTenants = configuration.getInteger("numberOfTenants", 0);
  }

  @Override
  public boolean execute() {
    int applicationIndex = counter.incrementAndGet();
    FusionAuthClient scopedClient = client;

    Application application = new Application().with(t -> t.name = "application_" + applicationIndex)
                                               .with(a -> a.roles.add(new ApplicationRole("admin")))
                                               .with(a -> a.roles.add(new ApplicationRole("user")));

    if (numberOfTenants > 0) {
      int tenantIndex = applicationIndex % numberOfTenants;
      if (tenantIndex == 0) {
        tenantIndex = numberOfTenants; // indexes are 1-based
      }
      application.tenantId = UUIDTools.tenantUUID(tenantIndex);
      scopedClient = client.setTenantId(application.tenantId);
    }
    ClientResponse<ApplicationResponse, Errors> result = scopedClient.createApplication(UUIDTools.applicationUUID(applicationIndex), new ApplicationRequest(null, application));
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
