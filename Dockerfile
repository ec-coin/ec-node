FROM adoptopenjdk/openjdk16:ubi

RUN mkdir /opt/app
# COPY out/artifacts/ec_network_node_jar /opt/app
EXPOSE 5000
CMD ["java", "-jar", "/opt/app/ec-network-node.jar"]