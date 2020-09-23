#!/bin/bash


id=$1
file=$2
repDeg=$3


if [ $# -ne 3 ]; then
    echo "Usage: $0 <peerID> <fileName> <replicationDegree>"
    exit 1
fi

java TestApp peer$id:110$id BACKUP $file $repDeg