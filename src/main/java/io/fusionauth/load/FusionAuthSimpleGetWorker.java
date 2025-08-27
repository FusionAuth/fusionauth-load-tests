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

import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;

/**
 * Worker to test OAuth2 Authorize.
 *
 * @author Daniel DeGroff
 */
public class FusionAuthSimpleGetWorker extends BaseWorker {
  private final String url;

  public FusionAuthSimpleGetWorker(Configuration configuration) {
    super(configuration);
    this.url = configuration.getString("url");
  }

  @Override
  public boolean execute() {
    ClientResponse<String, Void> getResponse = new RESTClient<>(String.class, Void.TYPE)
        .url(this.url)
        .connectTimeout(7_000)
        .readTimeout(7_000)
        .successResponseHandler(new TextResponseHandler())
        .get()
        .go();
    return getResponse.wasSuccessful();
  }
}
