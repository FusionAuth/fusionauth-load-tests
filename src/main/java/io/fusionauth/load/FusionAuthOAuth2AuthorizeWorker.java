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

import java.util.Random;

import io.fusionauth.client.FusionAuthClient;

/**
 * Worker to test OAuth2 Authorize.
 *
 * @author Daniel DeGroff
 */
public class FusionAuthOAuth2AuthorizeWorker extends BaseWorker {
  private final String baseURL;

  private final FusionAuthClient client;

  private final String clientId;

  private final String clientSecret;

  private final int loginLowerBound;

  private final int loginUpperBound;

  private final String redirectURI;

  public FusionAuthOAuth2AuthorizeWorker(FusionAuthClient client, Configuration configuration) {
    super(configuration);
    this.baseURL = configuration.getString("url");
    this.client = client;
    this.clientId = configuration.getString("client_id");
    this.clientSecret = configuration.getString("client_secret");
    this.redirectURI = configuration.getString("redirect_uri");
    this.loginLowerBound = configuration.getInteger("loginLowerBound", 0);
    this.loginUpperBound = configuration.getInteger("loginUpperBound", 1_000_000);
  }

  @Override
  public boolean execute() {
    int random = new Random().nextInt((loginUpperBound - loginLowerBound) + 1) + loginLowerBound;
    String email = "load_user_" + random + "@fusionauth.io";

    // Build the Authorize action

    // 1. Call GET to /oauth2/authorize to render the form and get the CSRF value
    // 2. POST the form w/ the CSRF value
    // 3. Consume the response to get the Location HTTP response header which will contain the auth code
    // 4. Take the response code and make a POST request to the /oauth2/token endpoint to complete the login.

    return false;
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }
}
