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
package io.fusionauth.load.reporters;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.fusionauth.load.CSVSampleListener;
import io.fusionauth.load.Configuration;
import io.fusionauth.load.ConfigurationInjected;
import io.fusionauth.load.Reporter;
import io.fusionauth.load.SampleListener;

/**
 * @author Troy Hill
 */
public class DefaultReporter implements Reporter {
  public final List<SampleListener> listeners = new ArrayList<>();

  private boolean csvOutput;

  /**
   * Interval in seconds.
   */
  private int interval;

  private Map<String, BufferedWriter> outputWriters = new HashMap<>(1);

  private ScheduledExecutorService scheduler;

  @ConfigurationInjected
  public DefaultReporter(Configuration configuration) {
    this();
    csvOutput = configuration.getBoolean("csvOutput", true);
    interval = configuration.getInteger("interval", 5);
  }

  public DefaultReporter() {
    // Add a shutdown hook to make sure we close the writers.
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
  }

  public void addSampleListeners(Collection<SampleListener> listeners) {
    this.listeners.addAll(listeners);
  }

  public void report() {
    try {
      StringBuilder builder = new StringBuilder();
      buildReport(builder);
      System.out.println(builder.toString());
    } catch (Exception e) {
      System.out.println("Reporting failure \n  " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public void schedule() {
    this.scheduler = Executors.newScheduledThreadPool(1);
    this.scheduler.scheduleAtFixedRate(this::report, interval, interval, TimeUnit.SECONDS);
  }

  public void stop() {
    try {
      System.out.println("\nStop was called. Wait up to 5 seconds for everything to wrap up.");
      this.scheduler.shutdown();
      this.scheduler.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      System.out.println("Tried to Wait 5 seconds for scheduler to shutdown and a InterruptedException was thrown. Call scheduler.shutdownNow()");
      this.scheduler.shutdownNow();
    }
    System.out.println("  --> Reporter Shutting down. Cleaning up...");
    shutdown();
  }

  private void buildReport(StringBuilder builder) throws Exception {
    for (SampleListener listener : listeners) {
      listener.report(builder);
      if (csvOutput && listener instanceof CSVSampleListener) {
        CSVSampleListener csvListener = (CSVSampleListener) listener;
        BufferedWriter bufferedWriter = getOutputWriter(csvListener);
        csvListener.report(bufferedWriter);
      }
    }
  }

  /**
   * Retrieve the ouptut writer for this listener, the writer will be created upon first request.
   *
   * @param csvListener The listener supporting CSV output.
   * @return a buffered writer to be passed to the listener.
   *
   * @throws Exception
   */
  private BufferedWriter getOutputWriter(CSVSampleListener csvListener) throws Exception {
    String fileName = csvListener.outputFileName();
    if (!outputWriters.containsKey(fileName)) {
      Path file = Paths.get("./" + fileName + ".csv");
      int count = 0;
      while (Files.exists(file)) {
        file = Paths.get("./" + fileName + "." + ++count + ".csv");
      }
      Files.createFile(file);
      outputWriters.put(fileName, new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file.getFileName().toString(), false)))));
      // Initialize the output with headers
      csvListener.reportHeader(outputWriters.get(fileName));
    }
    return outputWriters.get(fileName);
  }

  private void shutdown() {
    Iterator<Map.Entry<String, BufferedWriter>> iterator = outputWriters.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, BufferedWriter> entry = iterator.next();
      try {
        System.out.println("  --> Closing writer [" + entry.getKey() + "]");
        entry.getValue().close();
      } catch (IOException ignore) {
      }
      iterator.remove();
    }
  }
}
