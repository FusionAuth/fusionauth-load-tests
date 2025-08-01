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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;

import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import io.fusionauth.load.http.ChunkedBodyHandler;
import io.fusionauth.load.http.FixedLengthRequestHandler;

/**
 * Worker to test our HTTP server implementations.
 *
 * @author Daniel DeGroff
 */
public class JavaHTTPLoadTestWorker extends BaseWorker {
  private final String body = "This is a small body for a load test request.";

  private final byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

  private final boolean chunked;

  private final HttpClient javaRESTClient;

  private final String restClient;

  private final String url;

  public JavaHTTPLoadTestWorker(Configuration configuration) {
    super(configuration);
    this.url = configuration.getString("url");
    this.chunked = configuration.getBoolean("chunked", false);
    this.restClient = configuration.getString("restClient", "restify");
    if (!restClient.equals("restify") && !restClient.equals("java")) {
      throw new IllegalArgumentException("Invalid restClient: " + restClient + ". Must be 'restify' or 'java'");
    }

    // Build a single REST client for this worker.
    javaRESTClient = restClient.equals("java")
        ? HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    // Use virtual threads
                    // - This seems to improve the performance of this REST client by nearly 100%.
                    //   With 100 workers, I could get ~ 25-30k RPS, with this change I can get 58k.
                    .executor(Executors.newVirtualThreadPerTaskExecutor())
                    .followRedirects(Redirect.ALWAYS)
                    .build()
        : null;
  }

  @Override
  public boolean execute() {
    if ("restify".equals(restClient)) {
      ClientResponse<String, String> response = new RESTClient<>(String.class, String.class)
          .url(this.url)
          .setHeader("Content-Type", "text/plain")
          .bodyHandler(chunked ? new ChunkedBodyHandler(body) : new FixedLengthRequestHandler(body))
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
      // This isn't very explicit, but in my testing I have proven that using the ofByteArray() will add a Content-Length
      // request header, and the ofInputStream() will not.
      var publisher = chunked
          ? BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(bodyBytes))
          : BodyPublishers.ofByteArray(bodyBytes);
      var request = HttpRequest.newBuilder()
                               .method("GET", publisher)
                               .timeout(Duration.ofSeconds(10))
                               .uri(URI.create(this.url))
                               .build();
      try {
        var response = javaRESTClient.send(request, BodyHandlers.ofByteArray());
        return response.statusCode() >= 200 && response.statusCode() <= 399;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    // Note, this is not expected since we are validating this in the constructor.
    return false;
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }
}
