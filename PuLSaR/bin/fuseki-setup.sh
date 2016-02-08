#!/bin/bash
source $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/settings.sh

cat $BASEDIR/fuseki/tdb-assembler.ttl.TEMPLATE > $BASEDIR/fuseki/tdb-assembler.ttl
echo "    tdb:location \"$BASEDIR/fuseki/DB\" ; " >> $BASEDIR/fuseki/tdb-assembler.ttl
echo "    . " >> $BASEDIR/fuseki/tdb-assembler.ttl
echo Done
