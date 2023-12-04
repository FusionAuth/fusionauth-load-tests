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

import java.util.List;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.search.UserSearchCriteria;

/**
 * Worker to test searching based on an external Id in user data.
 *
 * @author Spencer Witt
 */
public class FusionAuthSearchDataWorker extends BaseWorker {
  private final FusionAuthClient client;

  // Populate this with a subset of valid external Ids from the database and randomly select one to return valid search results
  private final List<String> externalIds = List.of();

  private final int numberOfResults;

  // You can simulate tests using the query search parameter rather than query string by using this value with UserSearchCriteria.query
  private final String query;

  private final String queryString;

  public FusionAuthSearchDataWorker(FusionAuthClient client, Configuration configuration) {
    super(configuration);
    this.client = client;
    this.numberOfResults = configuration.getInteger("numberOfResults");
    this.queryString = configuration.getString("queryString");
    this.query = configuration.getString("query");
  }

  @Override
  public boolean execute() {
    // By default, this test will generate a random external Id, which is unlikely to return any results.
    // Populate externalIds with a set of valid Ids and randomly select one of those to simulate finding a user.
    String externalId = secureString(20, ALPHA_NUMERIC_CHARACTERS);

    ClientResponse<SearchResponse, Errors> result = client.searchUsersByQuery(new SearchRequest(new UserSearchCriteria()
                                                                                                    .with(c -> c.numberOfResults = numberOfResults)
                                                                                                    .with(c -> c.queryString = queryString.replace("${externalId}", externalId))));
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
