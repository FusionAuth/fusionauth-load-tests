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

import java.time.Instant;
import java.util.Collection;
import java.util.stream.IntStream;

/**
 * @author Troy Hill
 */
public class WorkerExecutor implements Runnable {
  public final Collection<SampleListener> listeners;

  public final int loopCount;

  public final Worker worker;

  public WorkerExecutor(Worker worker, int loopCount, Collection<SampleListener> listeners) {
    this.worker = worker;
    this.loopCount = loopCount;
    this.listeners = listeners;
  }

  @Override
  public void run() {
    IntStream.range(0, loopCount).forEachOrdered((n) -> {
      worker.prepare();
      Instant start = Instant.now();
      boolean success = worker.execute();
      Instant stop = Instant.now();

      Sample sample = new Sample(success, start, stop);
      this.listeners.forEach((l) -> l.handle(sample));
    });

    worker.finished();
  }
}
