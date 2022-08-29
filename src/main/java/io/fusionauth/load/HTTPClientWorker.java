/*
 * Copyright (c) 2012-2022, FusionAuth, All Rights Reserved
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import io.fusionauth.load.http.server.NettyHTTPClient;
import io.fusionauth.load.http.server.SimpleNIOServer;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/**
 * Worker to test status endpoint.
 *
 * @author Daniel DeGroff
 */
@SuppressWarnings("IfCanBeSwitch")
public class HTTPClientWorker extends BaseWorker {
  public static final HttpRequest httpRequest;

  private static final HttpClient httpClient;

//  private static final byte[] httpResponse = "{\"version\":\"42\"}".getBytes();

//  private static final NettyHTTPClient nettyClient = new NettyHTTPClient();

//  private static final HttpServer server;

  private static final SimpleNIOServer nioServer;

  private final String clientType;

  public HTTPClientWorker(Configuration configuration) {
    super(configuration);
    this.clientType = configuration.getString("client");
  }

  @Override
  public boolean execute() {
    if (clientType.equals("restify")) {
      var response = new RESTClient<>(String.class, String.class)
          .url("http://localhost:9011/api/system/version")
          .authorization("bf69486b-4733-4470-a592-f1bfce7af580")
          .header("Connection", "keep-alive")
          .successResponseHandler(new TextResponseHandler())
          .errorResponseHandler(new TextResponseHandler())
          .readTimeout(2_000)
          .connectTimeout(5_000)
          .get()
          .go();
      if (response.wasSuccessful()) {
        return true;
      }

      printErrors(response);
    } else if (clientType.equals("jdk")) {
      try {
        var response = httpClient.send(httpRequest, BodyHandlers.ofString());
        if (response.statusCode() == 200) {
          return true;
        }

        System.out.println(response.statusCode());
      } catch (Exception e) {
        System.out.println(e.getMessage());
        return false;
      }
    } else if (clientType.equals("apache")) {
      try (var client = HttpClients.createDefault()) {
        var request = new HttpGet("http://localhost:9011/api/system/version");
        var response = client.execute(request, r -> {
          if (r.getCode() == 200) {
            return EntityUtils.toString(r.getEntity());
          }

          System.out.println(r.getCode());
          return null;
        });

        return response != null;
      } catch (Exception e) {
        System.out.println(e.getMessage());
        return false;
      }
    } else if (clientType.equals("netty")) {
      try (var nettyClient = new NettyHTTPClient()) {
        return nettyClient.go();
      }
    } else {
      System.out.println("Invalid HTTP client type [" + clientType + "]");
    }

    return false;
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }

  static {
    System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection");
    httpClient = HttpClient.newBuilder()
                           .connectTimeout(Duration.ofSeconds(3))
                           .build();
    httpRequest = HttpRequest.newBuilder(URI.create("http://localhost:9011/api/system/version"))
                             .header("Authorization", "bf69486b-4733-4470-a592-f1bfce7af580")
                             .header("Connection", "keep-alive")
                             .GET()
                             .build();

    try {
      nioServer = new SimpleNIOServer();
      nioServer.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

//    try {
//      server = HttpServer.create(new InetSocketAddress(9011), 1024);
//      server.createContext("/", exchange -> {
//        exchange.getResponseHeaders().add("Content-Type", "application/json");
//        exchange.getResponseHeaders().add("Content-Length", "" + httpResponse.length);
//        exchange.sendResponseHeaders(200, httpResponse.length);
//        exchange.getResponseBody().write(httpResponse);
//        exchange.getResponseBody().flush();
//        exchange.getResponseBody().close();
//      });
//      server.start();
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
  }
}
