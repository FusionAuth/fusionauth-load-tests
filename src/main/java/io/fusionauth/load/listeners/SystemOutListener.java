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

import io.fusionauth.load.Sample;
import io.fusionauth.load.SampleListener;

/**
 * @author Troy Hill
 */
public class SystemOutListener implements SampleListener {
  @Override
  public void done() {
    System.out.println("---- Done ----");
  }

  @Override
  public void handle(Sample sample) {
    System.out.println(sample);
  }

  @Override
  public void report(Appendable writer) throws Exception {
    writer.append("SystemOutListener report empty");
  }
}
