# Usamos una imagen oficial que ya trae Java 17 y Maven listos
FROM maven:3.8.5-openjdk-17
WORKDIR /app

# Copiamos solo el pom.xml primero para descargar dependencias
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copiamos tu código fuente y compilamosgit add Dockerfile
COPY src ./src
RUN mvn package -DskipTests

# Ejecutamos la aplicación
CMD ["sh", "-c", "java -jar target/*.jar"]