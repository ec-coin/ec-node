#!/bin/bash
echo "Copying JAR file to remote VM"
scp -P 2222 ../target/ec-network-node-1.0-jar-with-dependencies.jar dylan@178.63.163.115:/home/dylan/ec-nodes

echo "Launching containers on remote"
ssh -p 2222 dylan@178.63.163.115 'cd ./ec-nodes && docker-compose kill && docker-compose build && docker-compose up -d'

echo "Connecting API to client PC"
ssh -p 2222 -N -L 4567:localhost:4567 dylan@178.63.163.115