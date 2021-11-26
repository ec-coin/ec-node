#!/bin/bash

cd ../
sudo docker-compose run --rm openvpn ovpn_genconfig -u udp://127.0.0.1
sudo docker-compose run --rm openvpn ovpn_initpki
sudo chown -R $(whoami): ./openvpn-data
echo "push \"route 172.16.238.0 255.255.255.0\"" >> ./openvpn-data/conf/openvpn.conf
sudo docker-compose run --rm openvpn easyrsa build-client-full ENTRY nopass
sudo docker-compose run --rm openvpn ovpn_getclient ENTRY > ENTRY.ovpn
