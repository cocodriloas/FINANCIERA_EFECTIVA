FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copiamos los archivos de Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Damos permisos de ejecución al wrapper (Regla de la guía)
RUN chmod +x mvnw

# Descargamos dependencias
RUN ./mvnw dependency:go-offline

# Copiamos el código fuente y compilamos
COPY src ./src
RUN ./mvnw package -DskipTests

# Ejecutamos la aplicación
CMD ["sh", "-c", "java -jar target/*.jar"]