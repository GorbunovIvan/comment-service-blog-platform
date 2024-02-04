FROM openjdk

WORKDIR /app

COPY target/comment-service-blog-platform-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]

# Run:
#   'docker build -t comment-service-blog-platform-image .'
