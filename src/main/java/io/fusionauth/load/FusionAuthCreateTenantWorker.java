/*
 * Copyright (c) 2018, FusionAuth, All Rights Reserved
 */
package io.fusionauth.load;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.inversoft.error.Errors;
import com.inversoft.load.Configuration;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.RefreshTokenExpirationPolicy;
import io.fusionauth.domain.RefreshTokenRevocationPolicy;
import io.fusionauth.domain.RefreshTokenUsagePolicy;
import io.fusionauth.domain.SecureGeneratorConfiguration;
import io.fusionauth.domain.SecureGeneratorType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.TenantRequest;
import io.fusionauth.domain.api.TenantResponse;

/**
 * @author Daniel DeGroff
 */
public class FusionAuthCreateTenantWorker extends BaseWorker {
  private final FusionAuthClient client;

  private final AtomicInteger counter;

  public FusionAuthCreateTenantWorker(FusionAuthClient client, Configuration configuration, AtomicInteger counter) {
    super(configuration);
    this.client = client;
    this.counter = counter;
  }

  @Override
  public boolean execute() {
    Tenant tenant = new Tenant().with(t -> t.name = "tenant_" + counter.incrementAndGet())
                                .with(t -> t.emailConfiguration.host = "localhost")
                                .with(t -> t.emailConfiguration.port = 25)
                                .with(t -> t.externalIdentifierConfiguration.authorizationGrantIdTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.changePasswordIdGenerator = new SecureGeneratorConfiguration(10, SecureGeneratorType.randomAlphaNumeric))
                                .with(t -> t.externalIdentifierConfiguration.changePasswordIdTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.deviceCodeTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.deviceUserCodeIdGenerator = new SecureGeneratorConfiguration(10, SecureGeneratorType.randomAlphaNumeric))
                                .with(t -> t.externalIdentifierConfiguration.emailVerificationIdGenerator = new SecureGeneratorConfiguration(10, SecureGeneratorType.randomAlphaNumeric))
                                .with(t -> t.externalIdentifierConfiguration.emailVerificationIdTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.externalAuthenticationIdTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.oneTimePasswordTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.passwordlessLoginGenerator = new SecureGeneratorConfiguration(10, SecureGeneratorType.randomAlphaNumeric))
                                .with(t -> t.externalIdentifierConfiguration.passwordlessLoginTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.registrationVerificationIdGenerator = new SecureGeneratorConfiguration(10, SecureGeneratorType.randomAlphaNumeric))
                                .with(t -> t.externalIdentifierConfiguration.registrationVerificationIdTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.samlv2AuthNRequestIdTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.setupPasswordIdGenerator = new SecureGeneratorConfiguration(10, SecureGeneratorType.randomAlphaNumeric))
                                .with(t -> t.externalIdentifierConfiguration.setupPasswordIdTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.twoFactorIdTimeToLiveInSeconds = 60)
                                .with(t -> t.externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds = 60)
                                .with(t -> t.issuer = "example.com")
                                .with(t -> t.jwtConfiguration.accessTokenKeyId = UUID.fromString(configuration.getString("keyId")))
                                .with(t -> t.jwtConfiguration.idTokenKeyId = UUID.fromString(configuration.getString("keyId")))
                                .with(t -> t.jwtConfiguration.refreshTokenExpirationPolicy = RefreshTokenExpirationPolicy.Fixed)
                                .with(t -> t.jwtConfiguration.refreshTokenRevocationPolicy = new RefreshTokenRevocationPolicy(true, true))
                                .with(t -> t.jwtConfiguration.refreshTokenTimeToLiveInMinutes = 60)
                                .with(t -> t.jwtConfiguration.refreshTokenUsagePolicy = RefreshTokenUsagePolicy.OneTimeUse)
                                .with(t -> t.jwtConfiguration.timeToLiveInSeconds = 60)
                                .with(t -> t.passwordValidationRules.maxLength = 200)
                                .with(t -> t.passwordValidationRules.minLength = 10)
                                .with(t -> t.passwordValidationRules.requireMixedCase = true)
                                .with(t -> t.passwordValidationRules.requireNonAlpha = true)
                                .with(t -> t.passwordValidationRules.requireNumber = true)
                                .with(t -> t.passwordValidationRules.validateOnLogin = true)
                                .with(t -> t.themeId = UUID.fromString(configuration.getString("themeId")));
    ClientResponse<TenantResponse, Errors> result = client.createTenant(null, new TenantRequest(tenant));
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
