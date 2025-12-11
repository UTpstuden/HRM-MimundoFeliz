# --------------------------------------------------------------------------------
# ETAPA 1: BUILD - Compila la aplicación Spring Boot
# --------------------------------------------------------------------------------
FROM maven:3.9.6-amazoncorretto-17 AS build
# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app
# Copia los archivos de configuración de Maven (pom.xml) para aprovechar el caché
# Si el pom.xml no cambia, Docker no recompilará todas las dependencias.
COPY pom.xml .
# Descarga todas las dependencias (solo si el pom.xml no ha cambiado)
RUN mvn dependency:go-offline
# Copia el código fuente restante
COPY src /app/src
# Empaqueta la aplicación en un archivo JAR ejecutable
# El comando 'package' compilará la aplicación y ejecutará el plugin de Spring Boot
# para crear un JAR con todas las dependencias incluidas.
RUN mvn clean package -DskipTests
# --------------------------------------------------------------------------------
# ETAPA 2: RUNTIME - Crea la imagen final ligera
# --------------------------------------------------------------------------------
# Usamos un JRE base minimalista para reducir el tamaño de la imagen final
FROM amazoncorretto:17-alpine
# Etiqueta para metadatos (opcional, pero buena práctica)
# LABEL maintainer="Tu Nombre <tu.email@ejemplo.com>"
# Expone el puerto por defecto de Spring Boot
# Esto es solo documentación. Render usa una variable de entorno para el puerto.
EXPOSE 8080
# Establece el directorio de trabajo
WORKDIR /app
# Copia el JAR ejecutable de la etapa 'build'
# El nombre 'target/*.jar' asume que usaste el nombre por defecto.
COPY --from=build /app/target/*.jar app.jar
# ENTRYPOINT para ejecutar la aplicación Spring Boot
# Usar el formato 'exec' es la forma recomendada
ENTRYPOINT ["java", "-jar", "app.jar"]