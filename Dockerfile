FROM openjdk:8
VOLUME /tmp
EXPOSE 8090
ADD ./target/socialweb-zuul-0.0.1-SNAPSHOT.jar socialweb-zuul.jar
ENTRYPOINT ["java","-jar","/socialweb-zuul.jar"]