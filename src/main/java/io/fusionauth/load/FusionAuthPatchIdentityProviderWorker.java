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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderResponse;

/**
 * Makes a PATCH /api/identity-provider/{idpId} call, enabling the identity provider for
 * the application corresponding to the counter.
 * <br/>
 * You likely want to reset the idp after a load test to clear the enabled applications.
 * Counting the applications provides a count of successful calls.
 * E.g.,
 * <pre>
 *   curl -s https://local.fusionauth.io/api/identity-provider/{idpId} -H "Authorization: {api_key}" | jq '.identityProvider.applicationConfiguration | length'
 * </pre>
 *
 * @author Brent Halsey
 */
public class FusionAuthPatchIdentityProviderWorker extends FusionAuthBaseWorker {
  private final AtomicInteger counter;

  private final UUID idpId;

  private final int maxAttempts;

  public FusionAuthPatchIdentityProviderWorker(FusionAuthClient client, Configuration configuration, AtomicInteger counter) {
    super(client, configuration);
    this.counter = counter;
    if (configuration.hasProperty("idpId")) {
      this.idpId = UUID.fromString(configuration.getString("idpId"));
    } else {
      this.idpId = null;
    }
    this.maxAttempts = configuration.getInteger("maxAttempts", 10);
  }

  @Override
  public boolean execute() {
    setApplicationIndex(counter.incrementAndGet());

    Map<String, Object> patch = Map.of("identityProvider",
                                       Map.of("applicationConfiguration",
                                              Map.of(applicationId.toString(),
                                                     Map.of("createRegistration", Boolean.TRUE, "enabled", Boolean.TRUE))));
    ClientResponse<IdentityProviderResponse, Errors> result = retryablePatch(idpId, patch);
    if (result.wasSuccessful()) {
      return true;
    }

    printErrors(result);
    return false;
  }

  private ClientResponse<IdentityProviderResponse, Errors> retryablePatch(UUID idpId, Map<String, Object> patch) {
    return retryablePatch(idpId, patch, 1);
  }

  // Do our own retry logic (until we add retry support in the java client)
  private ClientResponse<IdentityProviderResponse, Errors> retryablePatch(UUID idpId, Map<String, Object> patch, int attempt ) {

    ClientResponse<IdentityProviderResponse, Errors> result = client.patchIdentityProvider(idpId, patch);
    if (result.wasSuccessful() || attempt == maxAttempts) {
      return result;
    } else if (result.status == 409) {
      try {
        long backoff = 500L * attempt;
        long jitter = (long) (Math.random() * 0.10 * backoff);
        System.out.printf("Got 409 on attempt %d, sleeping for %d ms + %d ms and retrying\n", attempt, backoff, jitter);
        Thread.sleep(backoff + jitter);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return retryablePatch(idpId, patch, attempt + 1);
    } else {
      printErrors(result);
      return result;
    }
  }
}
