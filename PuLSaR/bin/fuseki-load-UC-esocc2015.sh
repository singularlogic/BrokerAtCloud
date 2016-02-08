#!/bin/bash
source $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/settings.sh

FUSEKI_URL=http://$FUSEKI_HOST:$FUSEKI_PORT/$FUSEKI_SERVICE/data?default
echo "Appending to previous Fuseki contents..."

RDF_FILE="$BASEDIR/var/TTL files/UC-esocc2015/BP_ESOCC2015_tutorial.ttl"
echo "Loading $RDF_FILE..."
$CURL -X POST --data-binary @"$RDF_FILE" -H "Content-Type: text/turtle" $FUSEKI_URL

RDF_FILE="$BASEDIR/var/TTL files/UC-esocc2015/SD-providerA-service1-GOLD.ttl"
echo "Loading $RDF_FILE..."
$CURL -X POST --data-binary @"$RDF_FILE" -H "Content-Type: text/turtle" $FUSEKI_URL

RDF_FILE="$BASEDIR/var/TTL files/UC-esocc2015/SD-providerB-service1-HIGH.ttl"
echo "Loading $RDF_FILE..."
$CURL -X POST --data-binary @"$RDF_FILE" -H "Content-Type: text/turtle" $FUSEKI_URL

echo "Loading completed"
