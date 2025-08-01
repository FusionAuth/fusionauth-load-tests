/*
 * Copyright (c) 2025, FusionAuth, All Rights Reserved
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
package io.fusionauth.load.http;

import java.nio.charset.StandardCharsets;

import com.inversoft.rest.ByteArrayBodyHandler;

/**
 * A simple Fixed Length body handler.
 *
 * @author Daniel DeGroff
 */
public class FixedLengthRequestHandler extends ByteArrayBodyHandler {
  public FixedLengthRequestHandler(String body) {
    super(body.getBytes(StandardCharsets.UTF_8));
  }
}
