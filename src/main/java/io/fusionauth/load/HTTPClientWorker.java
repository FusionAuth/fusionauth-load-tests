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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import io.fusionauth.http.server.HTTPServer;
import io.fusionauth.load.http.client.SimpleNIOClient;
import io.fusionauth.load.http.server.NettyHTTPClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/**
 * Worker to test status endpoint.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("IfCanBeSwitch")
public class HTTPClientWorker extends BaseWorker {
  private static final byte[] Response = "{\"version\":\"42\"}".getBytes();

  private static final HttpClient Client;

  private static final HttpRequest Request;

  private static HTTPServer Server;

  private final String clientType;

  public HTTPClientWorker(Configuration configuration) {
    super(configuration);
    this.clientType = configuration.getString("client");
  }

  @Override
  public boolean execute() {
    if (clientType.equals("restify")) {
      var response = new RESTClient<>(String.class, String.class)
          .url("http://localhost:8080/api/status")
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
        var response = Client.send(Request, BodyHandlers.ofString());
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
    } else if (clientType.equals("fusionauth")) {
      SimpleNIOClient client = new SimpleNIOClient();
      int result = client.url("http://localhost:9011/api/system/version")
                         .get()
                         .go();
      return result == 200;
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
    Client = HttpClient.newBuilder()
                       .connectTimeout(Duration.ofSeconds(3))
                       .build();
    Request = HttpRequest.newBuilder(URI.create("http://localhost:8080/api/status"))
                         .header("Connection", "keep-alive")
                         .GET()
                         .build();

//    HTTPHandler handler = (req, res) -> {
//      res.setContentLength(Response.length);
//      res.setContentType("application/json");
//      res.getOutputStream().write(Response);
//      res.getOutputStream().close();
//    };

//    Server = new HTTPServer().withListener(new HTTPListenerConfiguration()).withNumberOfWorkerThreads(200).withHandler(handler);
//    Server.start();
  }
}
