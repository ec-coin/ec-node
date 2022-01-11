#!/bin/bash

nohup openvpn --config /etc/openvpn/client/docker.conf &
java -jar /opt/app/ec-network-node-1.0-jar-with-dependencies.jar --max-peers 50
