FROM eclipse-temurin:17-jdk
RUN apt-get update && apt-get install -y gradle && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . .
RUN ./gradlew build -x test
EXPOSE 8090
ENTRYPOINT ["sh", "-c", "java -jar build/libs/*SNAPSHOT.jar"]
