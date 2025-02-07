#!/bin/sh
# collect_jstack.sh classpath main-class jstack

java -cp $1 $2 &
PID=$!
sleep 1
jstack -l $PID | tee $3
kill -9 $PID
