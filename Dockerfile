# Usamos una imagen base con Maven y JDK 17
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los archivos necesarios para compilar
COPY pom.xml .
COPY src ./src

# Ejecutamos la compilación con el perfil 'production', sin tests para acelerar
RUN mvn clean package -Pproduction -DskipTests

# Segunda etapa: imagen más ligera para correr la aplicación
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiamos el JAR generado en la etapa build
COPY --from=build /app/target/my-app-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
