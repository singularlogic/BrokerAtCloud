#!/bin/bash
source $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/settings.sh

clpath="$TARGET_DIR/classes:$TARGET_DIR/dependency/*:$TARGET_DIR/PuLSaR/WEB-INF/lib/*"

$JVM $JETTY_LOGGING_LEVEL -classpath "$clpath" eu.brokeratcloud.opt.engine.CLI "$@"
