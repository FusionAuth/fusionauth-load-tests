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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.fusionauth.load.JSONConfigurator;

/**
 * @author Troy Hill
 */
public class LoadRunner {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: io.fusionauth.load.Runner <path to config file.json>");
      System.exit(1);
    }

    Path configFile = Paths.get(args[0]);
    if (Files.notExists(configFile) || !Files.isRegularFile(configFile)) {
      System.err.println("Invalid config file location [" + args[0] + "]");
      System.exit(2);
    }

    JSONConfigurator configurator = new JSONConfigurator(configFile);
    configurator.foreman.execute();
  }
}
