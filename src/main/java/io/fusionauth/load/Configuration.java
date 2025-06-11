/*
 * Copyright (c) 2012-2025, FusionAuth, All Rights Reserved
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

import java.util.HashMap;
import java.util.Map;

import com.inversoft.json.ToString;

/**
 * @author Daniel DeGroff
 */
public class Configuration {
  public Map<String, Object> attributes = new HashMap<>();

  public String className;

  /**
   * Gets an optional boolean property from the JSON configuration object.
   *
   * @param property     The property.
   * @param defaultValue The default value;
   * @return The value if it is in the configuration, otherwise the default.
   */
  public boolean getBoolean(String property, boolean defaultValue) {
    Object value = attributes.get(property);
    if (value == null) {
      return defaultValue;
    }

    return (Boolean) value;
  }

  /**
   * Gets a required boolean property from the JSON configuration object.
   *
   * @param property The property.
   * @return The value and never null.
   * @throws IllegalArgumentException If the property is not in the configuration.
   */
  public boolean getBoolean(String property) {
    Object value = attributes.get(property);
    if (value == null) {
      throw new IllegalArgumentException("Missing required configuration property [" + property + "]");
    }

    return (Boolean) value;
  }

  /**
   * Gets a required Integer property from the JSON configuration object.
   *
   * @param property The property.
   * @return The value and never null.
   * @throws IllegalArgumentException If the property is not in the configuration.
   */
  public int getInteger(String property) {
    Object value = attributes.get(property);
    if (value == null) {
      throw new IllegalArgumentException("Missing required configuration property [" + property + "]");
    }

    return ((Number) value).intValue();
  }

  /**
   * Gets an optional Integer property from the JSON configuration object.
   *
   * @param property     The property.
   * @param defaultValue The default value.
   * @return The value if it is in the configuration, otherwise the default.
   */
  public int getInteger(String property, int defaultValue) {
    Object value = attributes.get(property);
    if (value == null) {
      return defaultValue;
    }

    return ((Number) value).intValue();
  }

  /**
   * Gets a required String property from the JSON configuration object.
   *
   * @param property The property.
   * @return The value and never null.
   * @throws IllegalArgumentException If the property is not in the configuration.
   */
  public String getString(String property) {
    Object value = attributes.get(property);
    if (value == null) {
      throw new IllegalArgumentException("Missing required configuration property [" + property + "]");
    }

    return value.toString();
  }

  /**
   * Gets an optional String property from the JSON configuration object.
   *
   * @param property     The property.
   * @param defaultValue The default value;
   * @return The value if not null, or the defaultValue
   */
  public String getString(String property, String defaultValue) {
    Object value = attributes.get(property);
    if (value == null) {
      return defaultValue;
    }

    return value.toString();
  }

  /**
   * Checks if the configuration has a property.
   *
   * @param property The property to check.
   * @return true if the property exists in the configuration, false otherwise.
   */
  public boolean hasProperty(String property) {
    Object value = attributes.get(property);
    return value != null;
  }

  @Override
  public String toString() {
    return ToString.toString(this);
  }
}
