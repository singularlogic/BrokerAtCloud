#!/bin/bash
# THIS FILE CONTAINS PULSAR, FUSEKI AND CLI LAUNCH SETTINGS
# THIS FILE IS USED BY ALL OTHER .SH FILES

# Basic folders
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CURDIR=$SCRIPT_DIR
BASEDIR=$( cd "$SCRIPT_DIR/.." && pwd )
TARGET_DIR=$( cd "$BASEDIR/target" && pwd )

# Java executables
JVM=java

# cURL executable
CURL=curl

# PuLSaR settings
PULSAR_PORT=9090
PULSAR_CONTEXT=$BASEDIR/target/PuLSaR/
# PULSAR_CONTEXT=$BASEDIR/target/PuLSaR.war
# PULSAR_CONTEXT=$BASEDIR/bin/PuLSaR.war

# Jetty settings
JETTY_LOGGING_LEVEL=-DVERBOSE
# options: -DDEBUG  -DVERBOSE -DIGNORED

# Fuseki settings
FUSEKI_SERVICE=BrokerAtCloudStore
FUSEKI_HOST=localhost
FUSEKI_PORT=3030
