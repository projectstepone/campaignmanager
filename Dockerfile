FROM openjdk:11

EXPOSE 8080
EXPOSE 8081

VOLUME /var/log/campaignmanager

ADD configs/docker.yml config/docker.yml
ADD target/campaign-manager*.jar campaignmanager.jar
ADD startup.sh startup.sh

CMD ./startup.sh
