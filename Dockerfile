FROM nginx

RUN apt-get update && apt-get -y install iputils-ping telnet

CMD ["nginx", "-g", "daemon off;"]