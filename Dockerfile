FROM eclipse-temurin:17

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

COPY target/*.jar app.jar

CMD ["java","-jar","app.jar"]