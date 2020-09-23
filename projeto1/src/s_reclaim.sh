#!/bin/bash


id=$1
space=$2


if [ $# -ne 2 ]; then
    echo "Usage: $0 <peerID> <space(Kbytes)>"
    exit 1
fi

java TestApp peer$id:110$id RECLAIM $space