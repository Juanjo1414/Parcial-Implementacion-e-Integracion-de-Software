# ===== ETAPA 1: Build =====
# Usamos una imagen con JDK + Gradle solo para compilar. Esta imagen
# NUNCA llega a producción; solo genera el .jar y se descarta.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiamos primero el wrapper y los archivos de configuración de Gradle
# (NO el código fuente todavía). Esto es un truco de cache de Docker:
# mientras no cambien las dependencias, Docker reutiliza esta capa y no
# vuelve a descargar todo Internet cada vez que editas una línea de Java.
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# Ahora sí copiamos el código fuente y compilamos el .jar final.
COPY src src
RUN ./gradlew bootJar --no-daemon -x test
# -x test: los tests ya los corremos en la Etapa 8 de forma independiente;
# no tiene sentido volver a ejecutarlos dentro del build de la imagen
# (alarga el build de Docker y duplica trabajo).

# ===== ETAPA 2: Runtime =====
# Imagen final: SOLO el JRE (no el JDK completo) + el .jar ya compilado.
# Resultado: una imagen mucho más liviana y con menos superficie de ataque
# (no tiene compilador, ni Gradle, ni el código fuente).
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Buena práctica de seguridad: nunca correr la aplicación como root dentro
# del contenedor. Creamos un usuario dedicado sin privilegios.
RUN groupadd -r camelracing && useradd -r -g camelracing camelracing

COPY --from=build /app/build/libs/*.jar app.jar
RUN chown camelracing:camelracing app.jar

USER camelracing

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]