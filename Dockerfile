# Use the official Gradle image as a build environment.
FROM gradle:6.8-jdk11 as builder

# Set the current working directory inside the image
WORKDIR /app

# Copy the build file to download dependencies
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy the source code into the container
COPY ./src ./src

# Build the application
RUN gradle bootJar --no-daemon

FROM ballerina/ballerina:2201.7.0
# FROM openjdk:11-jre-slim

RUN mkdir -p /work-dir \
    && addgroup troupe \
    && adduser -S -s /bin/bash -g 'ballerina' -G troupe -D ballerina \
    && apk upgrade \
    && chown -R ballerina:troupe /work-dir \


# Set the current working directory inside the image
WORKDIR /work-dir

# Copy the jar file from builder image, into the current image
COPY --from=builder /app/build/libs/MicroRTS-1.0.0.jar ./app.jar

# Specify the command to run on container start
CMD ["java", "-jar", "./app.jar"]
