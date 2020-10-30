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
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.fusionauth.load.CSVSampleListener;
import io.fusionauth.load.Sample;
import io.fusionauth.load.SampleListener;
import io.fusionauth.load.ThrowingConsumer;

/**
 * @author Troy Hill
 */
public class TimerListener implements SampleListener, CSVSampleListener {
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private long count;

  private Instant start;

  private Instant stop;

  @Override
  public void done() {
    stop = Instant.now();
  }

  @Override
  public void handle(Sample sample) {
    lock.writeLock().lock();
    try {
      if (start == null) {
        start = Instant.now();
      }
      count++;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public String outputFileName() {
    return "timerListenerResults";
  }

  /**
   * Write the output as a record for a CSV output.
   * <pre>
   *   duration,count,average per second
   * </pre>
   *
   * @param writer The output writer
   * @throws Exception
   */
  @Override
  public void report(BufferedWriter writer) throws Exception {
    buildReport((duration) -> {
      writer.write(
          new StringBuilder()
              .append(String.valueOf(duration.toMillis()))
              .append(",")
              .append(String.valueOf(count))
              .append(",")
              .append(String.valueOf(count / TimeUnit.MILLISECONDS.toSeconds(duration.toMillis()))).toString());
      writer.newLine();
    });
  }

  @Override
  public void report(Appendable writer) throws Exception {
    buildReport((duration) ->
        writer.append("\n=== TimerListener ===\n")
              .append("Overall duration: " + duration.toMillis() + "ms\n")
              .append("Count: " + count + "\n")
              .append("Avg: " + (count / TimeUnit.MILLISECONDS.toSeconds(duration.toMillis())) + " per second\n"));
  }

  @Override
  public void reportHeader(BufferedWriter writer) throws Exception {
    writer.write("Total Duration, Total Count, Average Per Second");
    writer.newLine();
  }

  private void buildReport(ThrowingConsumer<Duration> consumer) throws Exception {
    lock.readLock().lock();
    try {
      if (stop != null) {
        Duration duration = Duration.between(start, stop);
        consumer.accept(duration);
      }
    } finally {
      lock.readLock().unlock();
    }
  }
}
