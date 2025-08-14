/*
 * Copyright (c) 2024, FusionAuth, All Rights Reserved
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

import java.util.Random;
import java.util.UUID;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.jwt.JWTRefreshResponse;
import io.fusionauth.domain.api.jwt.RefreshRequest;

/**
 * Worker to test JWT refresh.
 *
 * @author Daniel DeGroff
 */
public class FusionAuthRefreshWorker extends BaseWorker {
  private final UUID applicationId;

  private final FusionAuthClient client;

  private final int loginLowerBound;

  private final int loginUpperBound;

  private String refreshToken;

  public FusionAuthRefreshWorker(FusionAuthClient client, Configuration configuration) {
    super(configuration);
    this.client = client;
    this.applicationId = UUID.fromString(configuration.getString("applicationId"));
    this.loginLowerBound = configuration.getInteger("loginLowerBound", 0);
    this.loginUpperBound = configuration.getInteger("loginUpperBound", 1_000_000);
  }

  @Override
  public boolean execute() {
    if (refreshToken == null) {
      int random = new Random().nextInt((loginUpperBound - loginLowerBound) + 1) + loginLowerBound;
      String email = "load_user_" + random + "@fusionauth.io";

      ClientResponse<LoginResponse, Errors> result = client.login(new LoginRequest(applicationId, email, Password));
      if (result.wasSuccessful()) {
        refreshToken = result.successResponse.refreshToken;
      } else {
        printErrors(result);
        return false;
      }
    }

    ClientResponse<JWTRefreshResponse, Errors> result = client.exchangeRefreshTokenForJWT(new RefreshRequest(refreshToken));
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
