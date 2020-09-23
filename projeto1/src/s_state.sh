#!/bin/bash


id=$1


if [ $# -ne 1 ]; then
    echo "Usage: $0 <peerID>"
    exit 1
fi

java TestApp peer$id:110$id STATEs_state.sh