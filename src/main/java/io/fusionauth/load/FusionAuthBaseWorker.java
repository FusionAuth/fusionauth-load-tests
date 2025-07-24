/*
 * Copyright (c) 2025, FusionAuth, All Rights Reserved
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

  private final int applicationCount;

  private final int tenantCount;

  protected UUID applicationId;

  protected int applicationIndex;

  protected UUID tenantId;

  protected int tenantIndex;

  protected FusionAuthClient tenantScopedClient;

  protected int userIndex;

  public FusionAuthBaseWorker(FusionAuthClient client, Configuration configuration) {
    super(configuration);
    this.client = client;
    this.applicationCount = configuration.getInteger("applicationCount", 0);
    this.tenantCount = configuration.getInteger("tenantCount", 0);
    this.tenantScopedClient = client;
  }

  public static UUID applicationUUID(int index) {
    return new UUID(1, index);
  }

  public static UUID tenantUUID(int index) {
    return new UUID(0, index);
  }

  protected void setApplicationIndex(int index) {
    this.applicationIndex = index;
    if (tenantCount > 0) {
      int tenantIndex = applicationIndex % tenantCount;
      if (tenantIndex == 0) {
        tenantIndex = tenantCount; // indexes are 1-based
      }
      applicationId = applicationUUID(applicationIndex);
      setTenantIndex(tenantIndex);
    }
  }

  protected void setTenantIndex(int index) {
    this.tenantIndex = index;
    tenantId = tenantUUID(tenantIndex);
    tenantScopedClient = client.setTenantId(tenantId); // this returns a new client with the tenant set
  }

  protected void setUserIndex(int index) {
    this.userIndex = index;
    if (applicationCount > 0 && tenantCount > 0) {
      applicationIndex = userIndex % applicationCount;
      if (applicationIndex == 0) {
        applicationIndex = applicationCount; // indexes are 1-based
      }
      setApplicationIndex(applicationIndex);
    }
  }

}
