FROM ubuntu:18.04

RUN apt-get clean && apt-get update && apt-get install -y --no-install-recommends software-properties-common
RUN apt-get install -y --no-install-recommends openjdk-8-jdk ca-certificates && apt-get install -y --no-install-recommends ca-certificates-java bash curl tzdata iproute2 zip unzip wget


EXPOSE 8080
EXPOSE 8081

VOLUME /var/log/campaignmanager

ADD configs/docker.yml config/docker.yml
ADD target/campaign-manager*.jar campaignmanager.jar
ADD startup.sh startup.sh

CMD ./startup.sh
