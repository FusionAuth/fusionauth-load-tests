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

import io.fusionauth.client.FusionAuthClient;

/**
 * Base worker to create deterministic Ids for FusionAuth load tests.
 *
 * @author Brent Halsey
 */
public abstract class FusionAuthBaseWorker extends BaseWorker {
  protected final FusionAuthClient client;

  private final int numberOfApplications;

  private final int numberOfTenants;

  protected UUID applicationId;

  protected int applicationIndex;

  protected UUID tenantId;

  protected int tenantIndex;

  protected FusionAuthClient tenantScopedClient;

  public FusionAuthBaseWorker(FusionAuthClient client, Configuration configuration) {
    super(configuration);
    this.client = client;
    this.numberOfApplications = configuration.getInteger("numberOfApplications", 0);
    this.numberOfTenants = configuration.getInteger("numberOfTenants", 0);
    this.tenantScopedClient = client;
  }

  public static UUID applicationUUID(int index) {
    return new UUID(1, tenantIndex);
  }

  public static UUID tenantUUID(int tenantIndex) {
    return new UUID(0, tenantIndex);
  }

  protected void setApplicationIndex(int applicationIndex) {
    this.applicationIndex = applicationIndex;
    if (numberOfTenants > 0) {
      int tenantIndex = applicationIndex % numberOfTenants;
      if (tenantIndex == 0) {
        tenantIndex = numberOfTenants; // indexes are 1-based
      }
      applicationId = applicationUUID(applicationIndex);
      setTenantIndex(tenantIndex);
    }
  }

  protected void setTenantIndex(int tenantIndex) {
    this.tenantIndex = tenantIndex;
    tenantId = tenantUUID(tenantIndex);
    tenantScopedClient = client.setTenantId(tenantId); // this returns a new client with the tenant set
  }

  protected void setUserIndex(int userIndex) {
    if (numberOfApplications > 0 && numberOfTenants > 0) {
      applicationIndex = userIndex % numberOfApplications;
      if (applicationIndex == 0) {
        applicationIndex = numberOfApplications; // indexes are 1-based
      }
      setApplicationIndex(applicationIndex);
    }
  }

}
