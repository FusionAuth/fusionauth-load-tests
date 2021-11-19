#!/usr/bin/env bash

# Grab the path
if [[ ! -d lib ]]; then
  echo "Unable to locate library files needed to run the load tests. [lib]"
  exit 1
fi

CLASSPATH=.
for f in $(ls lib | grep .jar); do
  CLASSPATH=${CLASSPATH}:lib/$f
done

~/dev/java/current17/bin/java -cp ${CLASSPATH} io.fusionauth.load.LoadRunner $@
