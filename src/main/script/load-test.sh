#!/bin/sh

# Grab the path
HOME=$(dirname $0)/../../..

if [ ! -d $HOME/lib ]; then
  echo "Unable to locate library files needed to run the load tests. [$HOME/lib]"
  exit 1
fi

CLASSPATH=
for f in $(ls $HOME/lib | grep .jar); do
  CLASSPATH=$CLASSPATH:$HOME/lib/$f
done

CLASSPATH=$CLASSPATH:$HOME/build/classes/main

java -cp $CLASSPATH com.inversoft.load.LoadRunner $@
