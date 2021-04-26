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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.inversoft.rest.ClientResponse;
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

    String authCode = null;

    try {
      int statusCode = getRequestToAuthorization();

      // 200 means we are not logged into SSO yet
      if (statusCode == 200) {
        authCode = postRequestToAuthorization(email);
      } else if (statusCode == 302) {
        System.out.println("Whoops, not coded for SSO redirect yet. ");
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Exchange the auth code for a token
    ClientResponse<AccessToken, OAuthError> tokenResponse = client.exchangeOAuthCodeForAccessToken(authCode, this.clientId, this.clientSecret, this.redirectURI);
    if (!tokenResponse.wasSuccessful()) {
      System.out.println("/oauth2/token Fail [" + tokenResponse.status + "]");
    }

    return false;
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }

  private int getRequestToAuthorization() throws IOException {
    StringBuilder queryString = new StringBuilder();
    Map<String, List<Object>> parameters = new LinkedHashMap<>();
    parameters.put("client_id", List.of(this.clientId));
    parameters.put("client_secret", List.of(this.clientSecret));
    parameters.put("response_type", List.of("code"));
    parameters.put("redirect_uri", List.of(this.redirectURI));
    withParameters(queryString, parameters);

    URL url = new URL(this.baseURL + "/oauth2/authorize" + queryString.toString());

    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
    huc.setDoOutput(false);
    huc.setConnectTimeout(10_000);
    huc.setReadTimeout(10_000);
    huc.setRequestMethod("GET");

    huc.connect();

    return huc.getResponseCode();
  }

  private String postRequestToAuthorization(String email) throws Exception {
    URL url = new URL(this.baseURL + "/oauth2/authorize");
    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
    huc.setDoOutput(true);
    huc.setConnectTimeout(10_000);
    huc.setReadTimeout(10_000);
    huc.setRequestMethod("POST");

    String postBody = "client_id=" + this.clientId +
                      "&client_secret=" + URLEncoder.encode(this.clientSecret, StandardCharsets.UTF_8) +
                      "&loginId=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
                      "&password=" + URLEncoder.encode(BaseWorker.Password, StandardCharsets.UTF_8) +
                      "&redirect_uri=" + URLEncoder.encode(this.redirectURI, StandardCharsets.UTF_8) +
                      "&response_type=code";
    byte[] bodyBytes = postBody.getBytes(StandardCharsets.UTF_8);

    huc.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    huc.addRequestProperty("Content-Length", bodyBytes.length + "");

    huc.setInstanceFollowRedirects(false);
    huc.connect();

    try (OutputStream os = huc.getOutputStream()) {
      os.write(bodyBytes);
      os.flush();
    }

    int postStatus = huc.getResponseCode();
    String location = huc.getHeaderField("Location");
    if (postStatus == 302) {
      int index = location.indexOf("?code=") + 6;
      return location.substring(index, location.indexOf("&", index));
    } else if (postStatus == 200) {
      try (InputStream postIs = huc.getInputStream()) {
        TextResponseHandler htmlResponse = new TextResponseHandler();
        String postResponseBody = htmlResponse.apply(postIs);
        System.out.println(postResponseBody);
      }
    }

    return null;
  }

  private void withParameters(StringBuilder urlString, Map<String, List<Object>> parameters) throws UnsupportedEncodingException {
    if (parameters.size() > 0) {
      if (urlString.indexOf("?") == -1) {
        urlString.append("?");
      }

      for (Iterator<Entry<String, List<Object>>> i = parameters.entrySet().iterator(); i.hasNext(); ) {
        Entry<String, List<Object>> entry = i.next();

        for (Iterator<Object> j = entry.getValue().iterator(); j.hasNext(); ) {
          Object value = j.next();
          urlString.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(value.toString(), "UTF-8"));
          if (j.hasNext()) {
            urlString.append("&");
          }
        }

        if (i.hasNext()) {
          urlString.append("&");
        }
      }
    }
  }
}
