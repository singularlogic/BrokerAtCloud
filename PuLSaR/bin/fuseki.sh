#!/bin/bash
source $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/settings.sh

cd $BASEDIR/fuseki

$JVM -Xmx1000M -jar fuseki-server.jar --port=$FUSEKI_PORT -desc=tdb-assembler.ttl --update /$FUSEKI_SERVICE
