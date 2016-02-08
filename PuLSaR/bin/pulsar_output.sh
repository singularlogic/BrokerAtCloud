#!/bin/bash
source $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/settings.sh

PWD = $( pwd )
cd $BASEDIR
$JVM $JETTY_LOGGING_LEVEL -Dfile.encoding=UTF-8 -jar $BASEDIR/bin/jetty-runner+ssi.jar --port $PULSAR_PORT $PULSAR_CONTEXT &> $BASEDIR/logs/OUTPUT.txt 
cd $PWD
