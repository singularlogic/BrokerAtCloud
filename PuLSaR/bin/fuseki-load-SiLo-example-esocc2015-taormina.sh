#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
BASEDIR=$( cd "$SCRIPT_DIR/.." && pwd )
CURL=curl

echo "Appending to previous Fuseki contents..."

RDF_FILE="$BASEDIR/var/TTL files/UC SiLo - esocc2015 tutorial/preparation/20150904_BP_ESOCC2015_tutorial_v4 + SMI-attr-mappings.ttl"
echo "Loading $RDF_FILE..."
$CURL -X POST --data-binary @"$RDF_FILE" -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default

RDF_FILE="$BASEDIR/var/TTL files/UC SiLo - esocc2015 tutorial/preparation/SD-providerA-service1-GOLD.ttl"
echo "Loading $RDF_FILE..."
$CURL -X POST --data-binary @"$RDF_FILE" -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default

RDF_FILE="$BASEDIR/var/TTL files/UC SiLo - esocc2015 tutorial/preparation/SD-providerB-service1-HIGH.ttl"
echo "Loading $RDF_FILE..."
$CURL -X POST --data-binary @"$RDF_FILE" -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default

echo "Loading completed"
