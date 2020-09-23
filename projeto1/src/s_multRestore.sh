#!/bin/bash


id=$1


if [ $# -ne 1 ]; then
    echo "Usage: $0 <peerID>"
    exit 1
fi

java TestApp peer$id:110$id BACKUP back2.txt
java TestApp peer$id:110$id BACKUP feup.png
java TestApp peer$id:110$id BACKUP resumo.pdf
java TestApp peer$id:110$id BACKUP test1.jpg