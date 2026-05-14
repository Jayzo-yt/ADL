FROM eclipse-temurin:17

RUN apt-get update && \
    apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

RUN cp target/*.jar app.jar

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

CMD ["java","-jar","app.jar"]