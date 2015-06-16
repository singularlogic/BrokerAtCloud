#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
BASEDIR=$( cd "$SCRIPT_DIR/.." && pwd )

JVM=java

$JVM -DVERBOSE -Dfile.encoding=UTF-8 -jar $BASEDIR/bin/jetty-runner+ssi.jar --port 9090 $BASEDIR/target/PuLSaR.war &> $BASEDIR/OUTPUT.txt 
#$JVM -DVERBOSE -Dfile.encoding=UTF-8 -jar $BASEDIR/bin/jetty-runner+ssi.jar --port 9090 $BASEDIR/bin/PuLSaR.war &> $BASEDIR/OUTPUT.txt 

# JETTY LOGGING OPTIONS: -DDEBUG  -DVERBOSE -DIGNORED



