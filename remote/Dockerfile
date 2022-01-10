FROM debian

RUN apt-get update && apt-get install openvpn net-tools default-jdk -y
COPY launch.sh /root/launch.sh

RUN mkdir /opt/app

EXPOSE 5000
ENTRYPOINT ["/root/launch.sh"]