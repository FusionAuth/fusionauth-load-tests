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
package io.fusionauth.load.config;

import java.nio.file.Path;

import io.fusionauth.load.BaseIntegrationTest;
import io.fusionauth.load.JSONConfigurator;
import io.fusionauth.load.MockListener;
import io.fusionauth.load.MockWorkerFactory;
import io.fusionauth.load.listeners.SystemOutListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * @author Troy Hill
 */
@Test(groups = {"integration"})
public class JSONConfiguratorTest extends BaseIntegrationTest {
  @Test
  public void configuration() throws Exception {
    Path configFile = projectDir.resolve("src/test/resources/json/jsonConfigurationTest.json");
    JSONConfigurator configurator = new JSONConfigurator(configFile);
    configurator.foreman.workerFactory = new MockWorkerFactory();
    configurator.foreman.initialize();

    assertEquals(configurator.foreman.workers.size(), 1);
    assertEquals(configurator.foreman.listeners.size(), 2);
    assertEquals(configurator.foreman.listeners.get(0).getClass(), SystemOutListener.class);
    assertEquals(configurator.foreman.listeners.get(1).getClass(), MockListener.class);
  }

  @BeforeMethod
  public void reset() {
    MockListener.listenCount = 0;
  }
}
