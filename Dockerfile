FROM nginx

RUN apt-get update && apt-get -y install iputils-ping telnet

CMD ["nginx", "-g", "daemon off;"]

#FROM adoptopenjdk/openjdk16:ubi
#
#RUN mkdir /opt/app
#COPY out/artifacts/ec_network_node_jar /opt/app
#EXPOSE 5000
#CMD ["java", "-jar", "/opt/app/ec-network-node.jar"]