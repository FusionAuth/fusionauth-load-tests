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

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Identical to the {@link Consumer} interface except that each method may throw {@link Exception}.
 *
 * @author Daniel DeGroff
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {

  /**
   * Identical to {@link Consumer#accept(Object)} except that it throws {@link Exception}.
   *
   * @param t the input argument
   * @see Consumer#accept(Object)
   */
  void accept(T t) throws Exception;

  /**
   * Identical to {@link Consumer#andThen(Consumer)} except that it throws {@link Exception}.
   * <p>
   *
   * @param after
   * @return
   * @throws Exception
   * @see Consumer#andThen(Consumer)
   */
  default ThrowingConsumer<T> andThen(ThrowingConsumer<? super T> after) throws Exception {
    Objects.requireNonNull(after);
    return (T t) -> {
      accept(t);
      after.accept(t);
    };
  }
}
