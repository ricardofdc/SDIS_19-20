#!/bin/bash


id=$1
repDeg=$2


if [ $# -ne 2 ]; then
    echo "Usage: $0 <peerID> <replicationDegree>"
    exit 1
fi

java TestApp peer$id:110$id BACKUP back2.txt $repDeg
java TestApp peer$id:110$id BACKUP feup.png $repDeg
java TestApp peer$id:110$id BACKUP resumo.pdf $repDeg
java TestApp peer$id:110$id BACKUP test1.jpg $repDeg