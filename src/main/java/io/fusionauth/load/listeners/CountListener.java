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
package io.fusionauth.load.listeners;

import java.io.BufferedWriter;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.fusionauth.load.CSVSampleListener;
import io.fusionauth.load.Sample;
import io.fusionauth.load.SampleListener;
import io.fusionauth.load.ThrowingConsumer;

/**
 * Simple counting listener, counts successes and failures and totals.
 * <p>
 * Use Atomics instead?  needed if we're synchronizing?
 *
 * @author Troy Hill
 */
public class CountListener implements SampleListener, CSVSampleListener {
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private long failures;

  private long successes;

  private long totals;

  @Override
  public void done() {
  }

  @Override
  public void handle(Sample sample) {
    lock.writeLock().lock();
    try {
      if (sample.succeeded) {
        successes++;
      } else {
        failures++;
      }
      totals++;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public String outputFileName() {
    return "countListenerResults";
  }

  /**
   * Write the output as a record for a CSV output.
   * <pre>
   *   successes,failures,totals
   * </pre>
   *
   * @param writer The output writer
   * @throws Exception
   */
  @Override
  public void report(BufferedWriter writer) throws Exception {
    buildReport((totals) -> {
      writer.write(
          new StringBuilder()
              .append(totals.currentSuccesses)
              .append(",")
              .append(totals.currentFailures)
              .append(",")
              .append(totals.currentTotals).toString());
      writer.newLine();
    });
  }

  @Override
  public void report(Appendable writer) throws Exception {
    buildReport((totals) ->
        writer.append("Successes: " + totals.currentSuccesses + "\n")
              .append("Failures: " + totals.currentFailures + "\n")
              .append("Totals: " + totals.currentTotals + "\n"));
  }

  @Override
  public void reportHeader(BufferedWriter writer) throws Exception {
    writer.write("Successes, Failures, Totals");
    writer.newLine();
  }

  private void buildReport(ThrowingConsumer<Totals> consumer) throws Exception {
    long currentSuccesses;
    long currentFailures;
    long currentTotals;
    lock.readLock().lock();
    try {
      currentSuccesses = successes;
      currentFailures = failures;
      currentTotals = totals;
    } finally {
      lock.readLock().unlock();
    }

    consumer.accept(new Totals(currentSuccesses, currentFailures, currentTotals));
  }

  private static class Totals {
    long currentFailures;

    long currentSuccesses;

    long currentTotals;

    Totals(long currentSuccesses, long currentFailures, long currentTotals) {
      this.currentSuccesses = currentSuccesses;
      this.currentFailures = currentFailures;
      this.currentTotals = currentTotals;
    }
  }
}
