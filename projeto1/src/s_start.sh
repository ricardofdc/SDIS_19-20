#!/bin/bash


numPeers=$1


if [ $# -ne 1 ]; then
    echo "Usage: $0 <number of peers>"
    exit 1
fi

for ((i = 1 ; i <= $numPeers ; i++)); do
    konsole --separate --hold -e "java Peer 1.0 $i peer$i:110$i 224.0.0.0 8001 224.0.0.1 8002 224.0.0.2 8003" &
done