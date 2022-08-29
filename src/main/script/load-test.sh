#!/usr/bin/env bash

#
# Copyright (c) 2022-2022, FusionAuth, All Rights Reserved
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

# Magic sauce
SOURCE="${BASH_SOURCE[0]}"
while [[ -h ${SOURCE} ]]; do # resolve $SOURCE until the file is no longer a symlink
  SCRIPT_DIR="$(cd -P "$(dirname "${SOURCE}")" >/dev/null && pwd)"
  SOURCE="$(readlink "${SOURCE}")"
  [[ ${SOURCE} != /* ]] && SOURCE="${SCRIPT_DIR}/${SOURCE}" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SCRIPT_DIR="$(cd -P "$(dirname "${SOURCE}")" > /dev/null && pwd)"

# Grab the path
if [[ ! -d ${SCRIPT_DIR}/lib ]]; then
  echo "Unable to locate library files needed to run the load tests. [lib]"
  exit 1
fi

CLASSPATH=.
for f in ${SCRIPT_DIR}/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f
done

suspend=""
if (( $# > 1 )) && [[ $1 == "--suspend" ]]; then
  suspend="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000"
  shift
fi

~/dev/java/current17/bin/java ${suspend} -cp "${CLASSPATH}" io.fusionauth.load.LoadRunner $@
