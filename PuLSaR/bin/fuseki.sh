#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
BASEDIR=$( cd "$SCRIPT_DIR/.." && pwd )

cd $BASEDIR/fuseki

java -Xmx1000M -jar fuseki-server.jar --port=3030 -desc=tdb-assembler.ttl --update /BrokerAtCloudStore
