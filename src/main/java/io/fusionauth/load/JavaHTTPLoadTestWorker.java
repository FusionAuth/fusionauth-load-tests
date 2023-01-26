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
  private final String url;

  public JavaHTTPLoadTestWorker(Configuration configuration) {
    super(configuration);
    this.url = configuration.getString("url");
  }

  @Override
  public boolean execute() {
    ClientResponse<String, String> response = new RESTClient<>(String.class, String.class)
        .url(this.url)
        .bodyHandler(new ByteArrayBodyHandler("This is a body that is hashed".getBytes()))
        .connectTimeout(7_000)
        .readTimeout(7_000)
        .successResponseHandler(new TextResponseHandler())
        .errorResponseHandler(new TextResponseHandler())
        .get()
        .go();
    return response.wasSuccessful();
  }

  @Override
  public void finished() {
  }

  @Override
  public void prepare() {
  }
}
