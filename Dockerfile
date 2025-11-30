# Используем официальный образ Gradle
FROM gradle:8.10-jdk17-alpine AS build
WORKDIR /app
# Копируем файлы сборки и зависимости
COPY build.gradle settings.gradle ./
# Копируем исходный код
COPY src ./src
# Собираем JAR (gradle bootJar)
RUN gradle bootJar --no-daemon

# Используем официальный образ OpenJDK для запуска
FROM openjdk:17-jdk-slim
WORKDIR /app
# Копируем JAR из stage сборки
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]