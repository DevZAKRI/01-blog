# Copy and edit environment variables
cp .env.example .env

# Build and start all containers
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop
docker-compose down