#!/bin/bash


num=$1


if [ $# -ne 1 ]; then
    echo "Usage: $0 <numberOfPeers>"
    exit 1
fi

for ((i = 1 ; i <= $num; i++)); do
    java TestApp peer$i:110$i STATE
done