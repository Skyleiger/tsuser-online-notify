FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Dominic Wienzek"

# Copy tsuser-online-notify files to image
COPY target/tsuser-online-notify.jar /tsuser-online-notify/tsuser-online-notify.jar

# Set working directory and entrypoint
WORKDIR /tsuser-online-notify
ENTRYPOINT java -jar tsuser-online-notify.jar