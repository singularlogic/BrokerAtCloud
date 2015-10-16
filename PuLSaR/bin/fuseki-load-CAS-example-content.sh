#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
BASEDIR=$( cd "$SCRIPT_DIR/.." && pwd )

RDF_FILE="$BASEDIR/var/TTL files/UC CAS - review/2015-01-21-export-FINAL.ttl"
CURL=curl

echo "Previous Fuseki contents will be discarded..."
echo "Loading $RDF_FILE..."
$CURL -X PUT --data-binary @"$RDF_FILE" -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default
echo "Loading completed"
