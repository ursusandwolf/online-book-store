# Online Book Store

Spring Boot REST application for managing books, shopping carts, and orders.

## Environment variables

The project uses environment variables for database and JWT configuration.

1. Copy `.env.sample` to `.env`.
2. Fill in the values in `.env`.

Recommended values:

```env
MYSQLDB_DATABASE=online_book_store
MYSQLDB_USER=bookstore_user
MYSQLDB_PASSWORD=bookstore_password
MYSQLDB_ROOT_PASSWORD=root_password
JWT_SECRET=hellomateshellomateshellomateshellomates
JWT_EXPIRATION=300000
MYSQLDB_LOCAL_PORT=3307
MYSQLDB_DOCKER_PORT=3306
SPRING_LOCAL_PORT=8080
SPRING_DOCKER_PORT=8080
DEBUG_PORT=5005
```

`.env` is ignored by git and must not be committed.

## Run with Docker Compose

Build and start the application with MySQL:

```bash
docker compose up --build
```

The API will be available at `http://localhost:${SPRING_LOCAL_PORT}` after startup.

## Run without Docker

The application can also be started locally with Maven. By default it connects to:

- MySQL on `localhost:3306`
- database `online_book_store`
- user `root`

Override these defaults with environment variables when needed.
