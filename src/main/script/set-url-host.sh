#!/usr/bin/env bash

#
# Copyright (c) 2025, FusionAuth, All Rights Reserved
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied. See the License for the specific
# language governing permissions and limitations under the License.
#

# Set the url host value across all the json config files
# Usage: cd build/dist; ./set-url-host.sh <new_url>

if [ $# -ne 1 ]; then
  echo "Usage: cd build/dist; $0 <new_url>"
  exit 1
fi

NEW_URL="$1"
find . -type f -name "*.json" -print0 | xargs -0 sed -i '' "s|local\.fusionauth\.io|$NEW_URL|g"
