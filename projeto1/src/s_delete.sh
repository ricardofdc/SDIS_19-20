#!/bin/bash


id=$1
file=$2


if [ $# -ne 2 ]; then
    echo "Usage: $0 <peerID> <fileName>"
    exit 1
fi

java TestApp peer$id:110$id DELETE $file