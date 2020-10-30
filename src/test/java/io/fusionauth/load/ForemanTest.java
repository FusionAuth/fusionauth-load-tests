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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * @author Troy Hill
 */
@Test(groups = "unit")
public class ForemanTest {
  @Test
  public void loopTest() throws InterruptedException {
    new Foreman().with((f) -> f.workers.add(new MockWorker()))
                 .with((f) -> f.workerFactory = new MockWorkerFactory())
                 .with((f) -> f.listeners.add(new MockListener()))
                 .with((f) -> f.loopCount = 10)
                 .execute();

    Thread.sleep(1000);
    assertEquals(MockListener.listenCount, 10);
  }

  @Test
  public void multipleListenerTest() throws InterruptedException {
    new Foreman().with((f) -> f.workers.add(new MockWorker()))
                 .with((f) -> f.workerFactory = new MockWorkerFactory())
                 .with((f) -> f.listeners.add(new MockListener()))
                 .with((f) -> f.listeners.add(new MockListener()))
                 .with((f) -> f.loopCount = 10)
                 .execute();

    Thread.sleep(1000);
    assertEquals(MockListener.listenCount, 20);
  }

  @BeforeMethod
  public void reset() {
    MockListener.listenCount = 0;
  }

  @Test
  public void simpleTest() throws InterruptedException {

    new Foreman().with((f) -> f.workers.add(new MockWorker()))
                 .with((f) -> f.workerFactory = new MockWorkerFactory())
                 .with((f) -> f.listeners.add(new MockListener()))
                 .with((f) -> f.loopCount = 1)
                 .execute();

    Thread.sleep(1000);
    assertEquals(MockListener.listenCount, 1);
  }
}
