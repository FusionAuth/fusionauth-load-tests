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

import java.util.Random;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.search.UserSearchCriteria;

/**
 * Worker to test search.
 *
 * @author Daniel DeGroff
 */
public class FusionAuthSearchWorker extends BaseWorker {
  private final FusionAuthClient client;

  private final int loginLowerBound;

  private final int loginUpperBound;

  private final int numberOfResults;

  private final String queryString;

  public FusionAuthSearchWorker(FusionAuthClient client, Configuration configuration) {
    super(configuration);
    this.client = client;
    this.numberOfResults = configuration.getInteger("numberOfResults");
    this.queryString = configuration.getString("queryString");
    this.loginLowerBound = configuration.getInteger("loginLowerBound", 0);
    this.loginUpperBound = configuration.getInteger("loginUpperBound", 1_000_000);
  }

  @Override
  public boolean execute() {
    int random = new Random().nextInt((loginUpperBound - loginLowerBound) + 1) + loginLowerBound;
    String email = "load_user_" + random + "@fusionauth.io";

    String query = queryString.replace("${email}", email);
    ClientResponse<SearchResponse, Errors> result = client.searchUsersByQuery(new SearchRequest(new UserSearchCriteria()
                                                                                                    .with(c -> c.numberOfResults = numberOfResults)
                                                                                                    .with(c -> c.queryString = query)));
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
