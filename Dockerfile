FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Avval pom.xml ni nusxalab, qaramliklarni (dependencies) yuklab olamiz
# Bu qadam Docker keshidan unumli foydalanish imkonini beradi
COPY pom.xml .
RUN mvn dependency:go-offline

# Endi kodni nusxalab loyihani build qilamiz
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage (Faqatgina ishlashi uchun kerakli JRE)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Build stage'dan olingan .jar faylni ko'chirib olamiz
COPY --from=build /app/target/*.jar app.jar

# Ilova 9092 portda ishlaydi
EXPOSE 9092

# Ilovani ishga tushirish
ENTRYPOINT ["java", "-jar", "app.jar"]
