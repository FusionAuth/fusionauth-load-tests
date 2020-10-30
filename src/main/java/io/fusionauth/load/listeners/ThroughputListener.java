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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.fusionauth.load.CSVSampleListener;
import io.fusionauth.load.Configuration;
import io.fusionauth.load.ConfigurationInjected;
import io.fusionauth.load.LoadTools;
import io.fusionauth.load.Sample;
import io.fusionauth.load.SampleListener;
import io.fusionauth.load.ThrowingConsumer;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Troy Hill
 */
@SuppressWarnings("unused")
public class ThroughputListener implements SampleListener, CSVSampleListener {
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private long failures;

  private int multiplier;

  private Instant start;

  private Instant stop;

  private long successes;

  private long totalLatency;

  @ConfigurationInjected
  public ThroughputListener(Configuration configuration) {
    multiplier = configuration.getInteger("multiplier", 1);
  }

  @Override
  public void done() {
    stop = Instant.now();
  }

  @Override
  public void handle(Sample sample) {
    LoadTools.writeLock(lock, () -> {
      // Start begins when we receive the first sample.
      if (start == null) {
        start = Instant.now();
      }

      if (sample.succeeded) {
        successes++;
      } else {
        failures++;
      }

      totalLatency += sample.duration().toMillis();
    });
  }

  @Override
  public String outputFileName() {
    return "throughputListenerResults";
  }

  /**
   * Write the output as a record for a CSV output.
   * <pre>
   *   total duration,total count,average latency,average success, average failures
   * </pre>
   *
   * @param writer The output writer
   */
  @Override
  public void report(BufferedWriter writer) throws Exception {
    buildReport((totals) -> {
      writer.write(
          new StringBuilder()
              .append(String.valueOf(totals.duration.toMillis()))
              .append(",")
              .append(String.valueOf(totals.total))
              .append(",")
              .append(totals.df.format(totals.averageLatency))
              .append(",")
              .append(totals.df.format(totals.averageSuccesses))
              .append(",")
              .append(totals.df.format(totals.averageFailures)).toString());
      writer.newLine();
    });
  }

  @Override
  public void report(Appendable writer) throws Exception {
    buildReport((totals) ->
        writer.append("\nThroughput Report\n")
              .append(" Total duration:\t" + totals.duration.toMillis() + " ms (~" + MILLISECONDS.toSeconds(totals.duration.toMillis()) + " s)\n")
              .append(" Total count:\t\t" + totals.total + "\n")
              .append(" Avg. latency:\t\t" + totals.df.format(totals.averageLatency) + " ms\n")
              .append(" Avg. success:\t\t" + totals.df.format(totals.averageSuccesses) + " per second\n")
              .append(" Avg. failure:\t\t" + ((totals.averageFailures == 0) ? "-" : (totals.df.format(totals.averageFailures) + " per second\n"))));
  }

  @Override
  public void reportHeader(BufferedWriter writer) throws Exception {
    writer.write("Total Duration, Total Count, Average Latency, Average Success, Average Failures");
    writer.newLine();
  }

  private void buildReport(ThrowingConsumer<Totals> consumer) throws Exception {
    long total;
    double averageFailures;
    double averageSuccesses;
    double averageLatency;

    Duration duration;

    lock.readLock().lock();
    try {
      Instant end = (stop != null) ? stop : Instant.now();
      duration = Duration.between(start, end);

      total = (failures + successes) * multiplier;
      long currentFailures = failures * multiplier;
      long currentSuccesses = successes * multiplier;
      averageFailures = (double) currentFailures / ((double) duration.toMillis() / 1000.0d);
      averageSuccesses = (double) currentSuccesses / ((double) duration.toMillis() / 1000.0d);
      averageLatency = (double) totalLatency / (double) total;
    } finally {
      lock.readLock().unlock();
    }

    DecimalFormat df = new DecimalFormat("#.###");
    df.setRoundingMode(RoundingMode.CEILING);

    consumer.accept(new Totals(averageLatency, averageSuccesses, averageFailures, total, duration, df));
  }

  private static class Totals {

    double averageFailures;

    double averageLatency;

    double averageSuccesses;

    DecimalFormat df;

    Duration duration;

    long total;

    Totals(double averageLatency, double averageSuccesses, double averageFailures, long total, Duration duration, DecimalFormat df) {
      this.averageLatency = averageLatency;
      this.averageSuccesses = averageSuccesses;
      this.averageFailures = averageFailures;
      this.total = total;
      this.duration = duration;
      this.df = df;
    }
  }
}
