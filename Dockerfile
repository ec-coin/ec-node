FROM adoptopenjdk/openjdk16:ubi

RUN mkdir /opt/app
EXPOSE 5000
CMD ["java", "-jar", "/opt/app/ec-network-node-1.0-jar-with-dependencies.jar"]