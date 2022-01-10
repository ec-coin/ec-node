FROM debian
RUN apt-get update && apt-get install default-jdk -y

RUN mkdir /opt/app
# COPY out/artifacts/ec_network_node_jar /opt/app
EXPOSE 5000
CMD ["java", "-jar", "/opt/app/ec-network-node-1.0-jar-with-dependencies.jar"]