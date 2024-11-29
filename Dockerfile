# Use the Java 8 base image
FROM eclipse-temurin:8-jdk-slim

# Accept build-time arguments
ARG PROFILE
ARG MONGO_INITDB_DATABASE
ARG AUTH_HOST
ARG HOST_URL
ARG VOCAB_URL

# Set environment variables using build-time arguments
ENV PROFILE=$PROFILE
ENV MONGO_INITDB_DATABASE=$MONGO_INITDB_DATABASE
ENV AUTH_HOST=$AUTH_HOST
ENV HOST_URL=$HOST_URL
ENV VOCAB_URL=$VOCAB_URL

# Create application directory
RUN mkdir -p /usr/local/hl7-igamt

# Copy the generated JAR file into the container
COPY hl7-igamt.jar /usr/local/hl7-igamt/hl7-igamt.jar


# Define the default command to run the application
CMD ["java", "-Xmx900m", "-Xss256k", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=${PROFILE}", "-Ddb.name=${MONGO_INITDB_DATABASE}", "-Ddb.host=hl7-igamt-db", "-Ddb.port=27017", "-Dauth.url=${AUTH_HOST}", "-Dhost.url=${HOST_URL}", "-Dvocabulary.url=${VOCAB_URL}", "-jar", "/usr/local/hl7-igamt/hl7-igamt.jar"]
