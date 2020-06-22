FROM ubuntu:18.04

RUN apt-get clean && apt-get update && apt-get install -y --no-install-recommends software-properties-common
RUN apt-get install -y --no-install-recommends openjdk-8-jdk ca-certificates && apt-get install -y --no-install-recommends ca-certificates-java bash curl tzdata iproute2 zip unzip wget


EXPOSE 8080
EXPOSE 8081

VOLUME /var/log/campaignmanager

ADD configs/docker.yml docker.yml
ADD target/campaign-manager*.jar campaignmanager.jar

CMD sh -c "java -Ddb.shards=${SHARDS-2} -Dfile.encoding=utf-8 -XX:+${GC_ALGO-UseG1GC} -Xms${JAVA_PROCESS_MIN_HEAP-1g} -Xmx${JAVA_PROCESS_MAX_HEAP-1g} ${JAVA_OPTS} -jar campaignmanager.jar server docker.yml"

