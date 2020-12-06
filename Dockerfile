FROM tomcat:8.0.51-jre8-alpine
RUN apk add --update --no-cache openssh sshpass
COPY target/*.war /usr/local/tomcat/webapps/

