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

/**
 * @author Troy Hill
 */
public interface Worker {
  /**
   * Preform the timed action and return true if successful.
   *
   * @return True if the execution was successful, false otherwise.
   */
  boolean execute();

  /**
   * Called when the worker has completed all of its work.
   */
  void finished();

  /**
   * Implement this method to do any work before the timing occurs.
   */
  void prepare();
}
