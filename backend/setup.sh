#!/bin/bash

if [ ! -f .env ]; then
    echo ".env not found. Creating from env.sample..."
    cp env.sample .env
    echo "Please review .env and adjust credentials if needed."
else
    echo ".env found. Using existing file."
fi


# export $(grep -v '^#' .env | xargs)
# safer: source .env with automatic export
if [ -f .env ]; then
    set -o allexport
    . ./.env
    set +o allexport
fi



# 3. Start Docker Compose
echo "Starting PostgreSQL container..."
docker compose up -d

# 4. Finished
echo "✅ Setup complete!"
echo "Database is running on port $DB_PORT."
echo "Use DB_USER=${DB_USER}, DB_PASS=${DB_PASS}, DB_NAME=${DB_NAME} in your Spring Boot application.yml"
