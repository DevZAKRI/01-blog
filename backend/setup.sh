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
# By default we don't remove volumes to preserve data between restarts.
# Pass the `reset` argument or set RESET_DB=true to force a reset (this will remove the postgres_data volume).
if [ "$1" = "reset" ] || [ "${RESET_DB:-false}" = "true" ]; then
    echo "Reset requested: removing containers and volumes..."
    docker compose down -v
else
    echo "Skipping 'docker compose down -v' to preserve DB volume. Use './setup.sh reset' to reset the DB."
fi

docker compose up -d

# 4. Finished
echo "✅ Setup complete!"
echo "Database is running on port $DB_PORT."
echo "Use DB_USER=${DB_USER}, DB_PASS=${DB_PASS}, DB_NAME=${DB_NAME} in your Spring Boot application.yml"
