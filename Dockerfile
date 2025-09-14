# Usa una imagen base de Java
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR ejecutable de la carpeta de construcción de Gradle
COPY applications/app-service/build/libs/solicitudes.jar solicitudes.jar

# Expone el puerto del servicio
EXPOSE 8081

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "solicitudes.jar"]