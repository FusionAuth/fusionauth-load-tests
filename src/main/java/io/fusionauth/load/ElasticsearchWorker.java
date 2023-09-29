/*
 * Copyright (c) 2023, FusionAuth, All Rights Reserved
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

import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.rest.ByteArrayBodyHandler;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.RESTClient;

/**
 * Worker to test search.
 *
 * @author Daniel DeGroff
 */
public class ElasticsearchWorker extends BaseWorker {
  private final String indexName;

  private final int loginLowerBound;

  private final int loginUpperBound;

  private final String queryString;

  private final String url;

  public ElasticsearchWorker(Configuration configuration) {
    super(configuration);
    this.queryString = configuration.getString("queryString");
    this.loginLowerBound = configuration.getInteger("loginLowerBound", 0);
    this.loginUpperBound = configuration.getInteger("loginUpperBound", 1_000_000);
    this.indexName = configuration.getString("indexName", null);
    this.url = configuration.getString("url", null);
  }

  @Override
  public boolean execute() {
    int random = new Random().nextInt((loginUpperBound - loginLowerBound) + 1) + loginLowerBound;
    String email = "load_user_" + random + "@fusionauth.io";

    String query = queryString.replace("${email}", email);
    ClientResponse<JsonNode, JsonNode> response = new RESTClient<>(JsonNode.class, JsonNode.class)
        .url(this.url + "/" + indexName + "/_search/")
        .bodyHandler(new ByteArrayBodyHandler(
            """
                {
                  "query": {
                    "query_string": {
                      "query": "${query}"
                    }
                  }
                }
                """
                .replace("${query}", query)
                .getBytes(StandardCharsets.UTF_8)))
        .setHeader("Content-Type", "application/json")
        .connectTimeout(2_000)
        .readTimeout(2_000)
        .successResponseHandler(new JSONResponseHandler<>(JsonNode.class))
        .errorResponseHandler(new JSONResponseHandler<>(JsonNode.class))
        .get()
        .go();

    if (!response.wasSuccessful()) {
      System.out.println(response.errorResponse);
    }

    return response.wasSuccessful();
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }
}
