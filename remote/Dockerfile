FROM debian

RUN apt-get update && apt-get install openvpn net-tools default-jdk -y
COPY launch.sh /root/launch.sh

RUN mkdir /opt/app

ENTRYPOINT ["/root/launch.sh"]