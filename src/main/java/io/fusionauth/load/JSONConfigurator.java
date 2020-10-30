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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.load.Configuration;
import io.fusionauth.load.Foreman;
import io.fusionauth.load.LoadDefinition;
import io.fusionauth.load.Reporter;
import io.fusionauth.load.SampleListener;
import io.fusionauth.load.WorkerFactory;
import io.fusionauth.load.reporters.DefaultReporter;

/**
 * @author Troy Hill
 */
public class JSONConfigurator {
  private final LoadDefinition definition;

  public Foreman foreman;

  public JSONConfigurator(URL configurationUrl) throws Exception {
    this(Paths.get(configurationUrl.toURI()));
  }

  public JSONConfigurator(Path configurationFile) throws Exception {
    if (!Files.exists(configurationFile) || !Files.isRegularFile(configurationFile)) {
      throw new InvalidParameterException("Configuration file does not exist or is not a file.");
    }

    ObjectMapper objectMapper = new ObjectMapper();
    this.definition = objectMapper.readValue(configurationFile.toFile(), LoadDefinition.class);
    this.foreman = new Foreman().with((f) -> f.loadDefinition = definition);

    configureWorkerFactory();
    configureReporter();
    configureListeners();
  }

  private void configureListeners() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
    for (Configuration configuration : definition.listeners) {
      Object instance = newInstance(configuration);
      if (instance instanceof SampleListener) {
        foreman.listeners.add((SampleListener) instance);
      } else {
        throw new IllegalArgumentException("class in configuration does not extend SampleListener [" + configuration.className + "]");
      }
    }
  }

  private void configureReporter() throws InvocationTargetException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    if (definition.reporter != null) {
      Object instance = newInstance(definition.reporter);
      if (instance instanceof Reporter) {
        foreman.reporter = (Reporter) instance;
      } else {
        throw new IllegalArgumentException("class in configuration is not a Reporter or subclass [" + definition.reporter.className + "]");
      }
    } else {
      foreman.reporter = new DefaultReporter();
    }
  }

  private void configureWorkerFactory() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
    Object instance = newInstance(definition.workerFactory);
    if (instance instanceof WorkerFactory) {
      foreman.with((f) -> f.workerFactory = (WorkerFactory) instance)
             .with((f) -> f.workerCount = definition.workerCount)
             .with((f) -> f.loopCount = definition.loopCount);
    } else {
      throw new IllegalArgumentException("class in configuration does not extend WorkerFactory [" + definition.workerFactory.className + ']');
    }
  }

  private Object newInstance(Configuration configuration) {
    try {
      try {
        return Class.forName(configuration.className).getConstructor(Configuration.class).newInstance(configuration);
      } catch (NoSuchMethodException e) {
        return Class.forName(configuration.className).getConstructor().newInstance();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
