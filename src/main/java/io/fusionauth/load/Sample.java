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

import java.time.Duration;
import java.time.Instant;

import com.inversoft.json.ToString;

/**
 * @author Troy Hill
 */
public class Sample {
  /**
   * Optional message to describe the sample.
   */
  public String message;

  public Instant start;

  public Instant stop;

  // Expected values
  public boolean succeeded;

  public Sample(boolean succeeded, Instant start, Instant stop) {
    this(succeeded, start, stop, null);
  }

  public Sample(boolean succeeded, Instant start, Instant stop, String message) {
    this.succeeded = succeeded;
    this.start = start;
    this.stop = stop;
    this.message = message;
  }

  public static Sample fail(Instant start, Instant stop) {
    return pass(start, stop, null);
  }

  public static Sample fail(Instant start, Instant stop, String message) {
    return new Sample(false, start, stop, message);
  }

  public static Sample pass(Instant start, Instant stop) {
    return pass(start, stop, null);
  }

  public static Sample pass(Instant start, Instant stop, String message) {
    return new Sample(true, start, stop, message);
  }

  public Duration duration() {
    return Duration.between(start, stop);
  }

  @Override
  public String toString() {
    return ToString.toString(this);
  }
}
