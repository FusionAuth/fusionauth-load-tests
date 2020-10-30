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

import java.io.BufferedWriter;

/**
 * Listener interface that identifies a sample listener as being able to output CSV.
 *
 * @author Daniel DeGroff
 */
public interface CSVSampleListener {

  /**
   * Return the name of the output file.
   *
   * @return
   */
  String outputFileName();

  /**
   * Write the next record for CSV output ending with a new line.
   *
   * @param writer The writer.
   * @throws Exception
   */
  void report(BufferedWriter writer) throws Exception;

  /**
   * Write the header row in the CSV output, this should only be called once.
   *
   * @param writer The writer.
   * @throws Exception
   */
  void reportHeader(BufferedWriter writer) throws Exception;
}
