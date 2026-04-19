# Stage 1: Build the application package with SBT
FROM sbtscala/scala-sbt:eclipse-temurin-alpine-17.0.15_6_1.12.9_3.8.3 AS builder

WORKDIR /app
COPY project/build.properties project/
COPY project/plugins.sbt project/
COPY build.sbt .

RUN sbt update

COPY src ./src

# This command builds the application bundle in target/universal/stage
RUN sbt stage

# Stage 2: Create the minimal runnable image with Java 17
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the bundled application from the builder stage
COPY --from=builder /app/target/universal/stage /app

# Expose the API port
EXPOSE 8080

RUN apk add --no-cache bash

# Run the app. bin/flux is created by JavaAppPackaging since project name := "flux" in build.sbt
CMD ["bin/flux"]
