#!/bin/bash

CONTAINER_NAME="mt-api-java"
IMAGE_NAME="bendb1993/mt-api-java"


# Step 1: Clean and package the code (skip tests)
echo "Packaging the project with Maven..."
mvn clean package -DskipTests

# Step 2a: Build the Docker image v1 and latest
echo "Building the Docker image v1 ..."
docker build -f Dockerfile.v1 -t $IMAGE_NAME:v1 -t $IMAGE_NAME .

# Step 2a: Build the Docker image v2
echo "Building the Docker image v2 ..."
docker build -f Dockerfile.v2 -t $IMAGE_NAME:v2 .

# Step 3: Remove the existing Docker container if it exists
echo "Removing existing container (if any)..."
docker rm -f $CONTAINER_NAME

# Step 4: Run the new Docker container
echo "Running the new Docker container..."
docker run -d \
    --name $CONTAINER_NAME \
    -p 8080:8080 \
    -e DB_CONNECTION_STRING=mongodb://172.17.0.2:27017/mt-api \
    $IMAGE_NAME

# Step 5: Output the status of the container
echo "Container is up and running!"
docker ps -f name=$CONTAINER_NAME

