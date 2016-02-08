#!/bin/bash
source $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/settings.sh

path="$path:$BASEDIR/bin"
export path

cd $BASEDIR
