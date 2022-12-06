FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar app.jar
#RUN apk add --no-cache tzdata
#ENV TZ Europe/Moscow
ENTRYPOINT ["java","-jar","/app.jar"]