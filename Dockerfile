FROM adoptopenjdk/openjdk16:ubi

RUN mkdir /opt/app
# COPY out/artifacts/ec_network_node_jar /opt/app
EXPOSE 5000
CMD ["java", "-jar", "/opt/app/ec-network-node-1.0-jar-with-dependencies.jar", "--debug-db-seeding", "true"]