FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN chmod +x mvnw
EXPOSE 8080
ENTRYPOINT ["./mvnw", "spring-boot:run"]