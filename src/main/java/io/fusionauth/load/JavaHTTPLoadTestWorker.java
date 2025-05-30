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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import com.inversoft.rest.ByteArrayBodyHandler;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;

/**
 * Worker to test our HTTP server implementations.
 *
 * @author Daniel DeGroff
 */
public class JavaHTTPLoadTestWorker extends BaseWorker {
  private final HttpClient javaHTTPClient = HttpClient.newBuilder()
                                                      .connectTimeout(Duration.ofSeconds(10))
                                                      .followRedirects(Redirect.ALWAYS)
                                                      .build();

  private final String restClient;

  private final String url;

  public JavaHTTPLoadTestWorker(Configuration configuration) {
    super(configuration);
    this.url = configuration.getString("url");
    this.restClient = configuration.getString("restClient");
  }

  @Override
  public boolean execute() {
    if ("restify".equals(restClient)) {
      ClientResponse<String, String> response = new RESTClient<>(String.class, String.class)
          .url(this.url)
          .setHeader("Content-Type", "text/plain")
          .bodyHandler(new ByteArrayBodyHandler("This is a body that is hashed".getBytes()))
          .connectTimeout(15_000)
          .readTimeout(15_000)
          .successResponseHandler(new TextResponseHandler())
          .errorResponseHandler(new TextResponseHandler())
          .get()
          .go();

      if (response.wasSuccessful()) {
        return true;
      }

      if (response.exception != null) {
        System.out.println(response.exception.getClass().getSimpleName() + " [" + response.exception.getMessage() + "]");
      } else {
        System.out.println(response.status);
        System.out.println(response.errorResponse);
      }
      return false;
    } else if ("java".equals(restClient)) {
      var request = HttpRequest.newBuilder()
                               .method("GET", BodyPublishers.ofByteArray("This is a body that is hashed".getBytes()))
                               .timeout(Duration.ofSeconds(10))
                               .uri(URI.create(this.url))
                               .build();

      try {
        var response = javaHTTPClient.send(request, BodyHandlers.ofByteArray());
        return response.statusCode() >= 200 && response.statusCode() <= 399;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    throw new IllegalStateException("Unknown value [" + restClient + "] for [restClient]. Supported values include [restify, java]. Default is restify.");
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }
}
