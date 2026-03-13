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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderRequest;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.provider.IdentityProviderLimitUserLinkingPolicy;
import io.fusionauth.domain.provider.IdentityProviderLinkingStrategy;
import io.fusionauth.domain.provider.IdentityProviderOauth2Configuration;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.domain.provider.OpenIdConnectApplicationConfiguration;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;

/**
 * Makes a PUT /api/identity-provider/{idpId} call, enabling the identity provider for
 * a random selection of applications and tenants.
 * Useful for finding race conditions (like DB deadlock).
 *
 * @author Brent Halsey
 */
public class FusionAuthPutIdentityProviderWorker extends FusionAuthBaseWorker {
  private final AtomicInteger counter;

  private final UUID idpId;

  private final int maxAttempts;

  private final int numEnabledApplications;

  private final int numEnabledTenants;

  public FusionAuthPutIdentityProviderWorker(FusionAuthClient client, Configuration configuration, AtomicInteger counter) {
    super(client, configuration);
    this.counter = counter;
    if (configuration.hasProperty("idpId")) {
      this.idpId = UUID.fromString(configuration.getString("idpId"));
    } else {
      this.idpId = null;
    }

    if (configuration.hasProperty("numEnabledApplications")) {
      this.numEnabledApplications = configuration.getInteger("numEnabledApplications");
    } else {
      this.numEnabledApplications = Integer.min(10, applicationCount);
    }

    if (configuration.hasProperty("numEnabledTenants")) {
      this.numEnabledTenants = configuration.getInteger("numEnabledTenants");
    } else {
      this.numEnabledTenants = Integer.min(10, tenantCount);
    }

    this.maxAttempts = configuration.getInteger("maxAttempts", 10);
  }

  @Override
  public boolean execute() {
    setApplicationIndex(counter.incrementAndGet());

    // build application configs for a random selection of applications
    List<Integer> allApps = IntStream.rangeClosed(1, applicationCount).boxed().collect(Collectors.toList());
    Collections.shuffle(allApps);
    Map<UUID, OpenIdConnectApplicationConfiguration> appConfig = new HashMap<>();
    for (int i = 0; i < numEnabledApplications; i++) {
      appConfig.put(applicationUUID(allApps.get(i)), new OpenIdConnectApplicationConfiguration().with(a -> a.enabled = true));
    }

    // build tenant configs for a random selection of tenants
    List<Integer> allTenants = IntStream.rangeClosed(1, tenantCount).boxed().collect(Collectors.toList());
    Collections.shuffle(allTenants);
    Map<UUID, IdentityProviderTenantConfiguration> tenantConfig = new HashMap<>();
    for (int i = 0; i < numEnabledTenants; i++) {
      tenantConfig.put(tenantUUID(allTenants.get(i)), new IdentityProviderTenantConfiguration()
          .with(t -> t.limitUserLinkCount = new IdentityProviderLimitUserLinkingPolicy()
              .with(p -> p.enabled = true)));
    }

    OpenIdConnectIdentityProvider idp = new OpenIdConnectIdentityProvider()
        .with(i -> i.name = "load-test-oidc-idp")
        .with(i -> i.enabled = true)
        .with(i -> i.linkingStrategy = IdentityProviderLinkingStrategy.LinkByEmail)
        .with(i -> i.applicationConfiguration = appConfig)
        .with(i -> i.tenantConfiguration = tenantConfig)
        .with(i -> i.oauth2 = new IdentityProviderOauth2Configuration()
            .with(o -> o.client_id = "client_id")
            .with(o -> o.client_secret = "client_secret")
            .with(o -> o.authorization_endpoint = URI.create("https://idp.fusionauth.io/p/oauth2/authorize"))
            .with(o -> o.token_endpoint = URI.create("https://idp.fusionauth.io/p/oauth2/token"))
            .with(o -> o.userinfo_endpoint = URI.create("https://idp.fusionauth.io/p/oauth2/userinfo"))
            .with(o -> o.emailClaim = "email"));

    ClientResponse<IdentityProviderResponse, Errors> result = retryablePut(idpId, new IdentityProviderRequest(idp));
    if (result.wasSuccessful()) {
      return true;
    }

    printErrors(result);
    return false;
  }

  private ClientResponse<IdentityProviderResponse, Errors> retryablePut(UUID idpId, IdentityProviderRequest idpRequest) {
    return retryablePut(idpId, idpRequest, 1);
  }

  // Do our own retry logic (until we add retry support in the java client)
  private ClientResponse<IdentityProviderResponse, Errors> retryablePut(UUID idpId, IdentityProviderRequest idpRequest, int attempt) {
    ClientResponse<IdentityProviderResponse, Errors> result = client.updateIdentityProvider(idpId, idpRequest);
    if (result.wasSuccessful() || attempt == maxAttempts) {
      return result;
    } else if (result.status == 409) {
      try {
        long backoff = (long) (500 * Math.pow(1.5, attempt));
        long jitter = (long) (Math.random() * 0.10 * backoff);
        System.out.printf("Got 409 on attempt %d, sleeping for %d ms + %d ms and retrying\n", attempt, backoff, jitter);
        Thread.sleep(backoff + jitter);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return retryablePut(idpId, idpRequest, attempt + 1);
    } else {
      printErrors(result);
      return result;
    }
  }
}
