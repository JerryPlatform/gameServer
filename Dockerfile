FROM openjdk:8-jdk-alpine
ARG JAR_FILE=build/libs/*.jar
RUN apk --no-cache add tzdata && \
        cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
        echo "Asia/Seoul" > /etc/timezone
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Seoul
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -Dtomcat.util.http.parser.HttpParser.requestTargetAllow="|{}" -jar /app.jar