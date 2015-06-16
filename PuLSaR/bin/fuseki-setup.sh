#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
BASEDIR=$( cd "$SCRIPT_DIR/.." && pwd )

cat $BASEDIR/fuseki/tdb-assembler.ttl.TEMPLATE > $BASEDIR/fuseki/tdb-assembler.ttl
echo "    tdb:location \"$BASEDIR/fuseki/DB\" ; " >> $BASEDIR/fuseki/tdb-assembler.ttl
echo "    . " >> $BASEDIR/fuseki/tdb-assembler.ttl
echo Done
