#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
BASEDIR=$( cd "$SCRIPT_DIR/.." && pwd )
TRGDIR=$( cd "$BASEDIR/target" && pwd )

JVM=java
clpath="$TRGDIR/classes:$TRGDIR/dependency/*:$TRGDIR/OptWebapp/WEB-INF/lib/*"

$JVM -DVERBOSE -classpath "$clpath" eu.brokeratcloud.opt.engine.CLI "$@"
