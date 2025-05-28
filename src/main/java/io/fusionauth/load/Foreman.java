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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author Troy Hill
 */
public class Foreman implements Buildable<Foreman> {
  public final List<SampleListener> listeners = new ArrayList<>();

  public final List<Worker> workers = new ArrayList<>();

  public boolean done;

  public LoadDefinition loadDefinition;

  public int loopCount;

  public Reporter reporter;

  public int workerCount;

  public WorkerFactory workerFactory;

  private boolean initialized;

  public Foreman execute() throws InterruptedException {
    initialize();
    try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {

      // Gradually build up workers, to reduce the chance of failures while we get going.
      for (Worker worker : workers) {
        WorkerExecutor executor = new WorkerExecutor(worker, loopCount, listeners);
        pool.execute(executor);
      }

      if (this.reporter != null) {
        this.reporter.schedule();
      }

      pool.shutdown();
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

      listeners.forEach(SampleListener::done);

      if (reporter != null) {
        reporter.report();
      }

      if (reporter != null) {
        reporter.stop();
      }

      // Temp hack to get some general timings on the OAuth2 Authorize worker broken down by component
      if (workers.get(0) instanceof FusionAuthOAuth2AuthorizeWorker) {
        System.out.println("\n\n");
        long total = FusionAuthOAuth2AuthorizeWorker.timing.render + FusionAuthOAuth2AuthorizeWorker.timing.post + FusionAuthOAuth2AuthorizeWorker.timing.token;
        long iterationCount = (long) workers.size() * loopCount;

        int renderPercent = (int) (FusionAuthOAuth2AuthorizeWorker.timing.render * 100.0 / total + 0.5);
        int postPercent = (int) (FusionAuthOAuth2AuthorizeWorker.timing.post * 100.0 / total + 0.5);
        int tokenPercent = (int) (FusionAuthOAuth2AuthorizeWorker.timing.token * 100.0 / total + 0.5);

        System.out.println("Render: " + FusionAuthOAuth2AuthorizeWorker.timing.render + " ms, Average: " + FusionAuthOAuth2AuthorizeWorker.timing.render / (iterationCount) + " ms, " + (renderPercent) + "%");
        System.out.println("Post: " + FusionAuthOAuth2AuthorizeWorker.timing.post + " ms, Average: " + FusionAuthOAuth2AuthorizeWorker.timing.post / (iterationCount) + " ms, " + (postPercent) + "%");
        System.out.println("Token: " + FusionAuthOAuth2AuthorizeWorker.timing.token + " ms, Average: " + FusionAuthOAuth2AuthorizeWorker.timing.token / (iterationCount) + " ms, " + (tokenPercent) + "%");
        System.out.println("\n\n");
      }

      done = true;
      initialized = true;
      return this;
    }
  }

  public void initialize() {
    if (initialized) {
      return;
    }

    DecimalFormat df = new DecimalFormat("#,###");

    System.out.println("Foreman Initialized");

    loopCount = Math.max(1, loopCount);
    System.out.println("  --> Loop count:\t" + df.format(loopCount));

    if (workerCount > 0 && workerFactory == null) {
      throw new IllegalStateException("Please provide a worker factory if you are going to specify a worker count");
    }

    System.out.println("  --> Worker count:\t" + df.format(workerCount));
    System.out.println("  --> Total iterations:\t" + df.format(workerCount * loopCount));
    System.out.println("  --> Worker factory:\t" + workerFactory.getClass().getCanonicalName());

    System.out.println("  --> Prepare the factory for production....");
    workerFactory.prepare(loadDefinition);

    IntStream.range(0, this.workerCount).forEachOrdered((n) ->
                                                            workers.add(this.workerFactory.createWorker()));

    if (reporter != null) {
      reporter.addSampleListeners(listeners);
    }
  }
}
