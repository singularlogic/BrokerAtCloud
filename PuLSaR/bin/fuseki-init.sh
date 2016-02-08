#!/bin/bash
source $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/settings.sh

RDF_FILE="$BASEDIR/var/TTL files/ontologies/all-in-one.ttl"

echo "Previous Fuseki contents will be discarded..."
echo "Loading $RDF_FILE..."
$CURL -X PUT --data-binary @"$RDF_FILE" -H "Content-Type: text/turtle" http://$FUSEKI_HOST:$FUSEKI_PORT/$FUSEKI_SERVICE/data?default
echo "Loading completed"
