FROM openjdk:8-jdk-alpine3.8

ADD ./lib /app/lib/
ADD ./app/*.jar /app/

EXPOSE 8082

WORKDIR /app

CMD java -cp acinonyx-server.jar:lib/* acinonyx.Main

