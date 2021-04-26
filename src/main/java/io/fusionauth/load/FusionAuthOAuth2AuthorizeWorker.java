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

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.OAuthError;

/**
 * Worker to test OAuth2 Authorize.
 *
 * @author Daniel DeGroff
 */
public class FusionAuthOAuth2AuthorizeWorker extends BaseWorker {
  public static OAUth2Timing timing = new OAUth2Timing();

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
    //
    // 1. Call GET to /oauth2/authorize to render the form and get the CSRF value
    // 2. POST the form w/ the CSRF value
    // 3. Consume the response to get the Location HTTP response header which will contain the auth code
    // 4. Take the response code and make a POST request to the /oauth2/token endpoint to complete the login.
    //
    // Can we test these individually?
    // 1. Render the authorize page
    // 2. Post the page
    // 3. Exchange token
    //


    try {
      long start = System.currentTimeMillis();
      ClientResponse<Void, Void> getResponse = new RESTClient<>(Void.TYPE, Void.TYPE)
          .url(this.baseURL + "/oauth2/authorize")
          .urlParameter("client_id", clientId)
          .urlParameter("response_type", "code")
          .urlParameter("redirect_uri", redirectURI)
          .followRedirects(false)
          .connectTimeout(7_000)
          .readTimeout(7_000)
          .get()
          .go();

      timing.render += (System.currentTimeMillis() - start);

      if (getResponse.status == 200) {
        // Submit the form
        start = System.currentTimeMillis();
        ClientResponse<Void, String> postResponse = new RESTClient<>(Void.TYPE, String.class)
            .url(this.baseURL + "/oauth2/authorize")
            .bodyHandler(new FormDataBodyHandler(Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "loginId", email,
                "password", Password,
                "redirect_uri", redirectURI,
                "response_type", "code")))
            .errorResponseHandler(new TextResponseHandler())
            .followRedirects(false)
            .connectTimeout(7_000)
            .readTimeout(7_000)
            .post()
            .go();

        timing.post += (System.currentTimeMillis() - start);

        if (postResponse.status == 302) {
          List<String> header = postResponse.headers.get("Location");
          if (header != null) {
            String location = header.get(0);
            int index = location.indexOf("?code=") + 6;
            String authCode = location.substring(index, location.indexOf("&", index));

            // Exchange auth code for a token
            start = System.currentTimeMillis();

            client.connectTimeout = 7_000;
            client.readTimeout = 7_000;

            ClientResponse<AccessToken, OAuthError> tokenResponse = client.exchangeOAuthCodeForAccessToken(authCode, clientId, clientSecret, redirectURI);
            if (tokenResponse.wasSuccessful()) {
              timing.token += (System.currentTimeMillis() - start);
              return true;
            }

            System.out.println("/oauth2/token Fail [" + tokenResponse.status + "]");
          }
        } else {
          System.out.println("Whoops! Post to /oauth2/authorized returned [" + postResponse.status + "]");
          System.out.println(postResponse.errorResponse);
          System.out.println(postResponse.exception);
        }

      } else if (getResponse.status == 302) {
        System.out.println("Whoops, not coded for SSO redirect yet.");
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return false;
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }

  public static class OAUth2Timing {
    public long post;

    public long render;

    public long token;
  }
}
