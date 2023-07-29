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

FROM ballerina/ballerina:2201.7.0 as ballerina-pack

# Set the current working directory inside the image
WORKDIR /work-dir

# Copy the jar file from builder image, into the current image
COPY --chown=ballerina:troupe --from=builder /app/build/libs/MicroRTS-1.0.0.jar ./app.jar

# Specify the command to run on container start
CMD ["java", "-jar", "./app.jar"]
