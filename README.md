# Docker API

A small containerized student and department API. The project combines a Spring Boot REST service, PostgreSQL, and an Apache HTTP server in a Docker Compose stack.

## Architecture

| Service | Purpose | Host port |
| --- | --- | --- |
| `httpd` | Apache serves the bundled static page and proxies HTTP requests to the backend | `80` |
| `backend` | Spring Boot API for greetings, students, and departments | `8080` |
| `database` | PostgreSQL database, initialized with the project schema and sample rows | Not published |

Apache forwards requests to `backend:8080` over the private `app-network`. The backend connects to PostgreSQL over the separate `db-network`. The database has a readiness check; Compose waits for it to become healthy before starting the backend. A named `postgres-data` volume keeps database contents across container restarts.

## Requirements

- Docker Engine
- Docker Compose v2 (`docker compose`)

## Run the stack

From the repository root:

```sh
docker compose up --build
```

Open [http://localhost](http://localhost) for the Apache-served page. The Spring API is also published directly on [http://localhost:8080](http://localhost:8080). Stop the containers with `Ctrl+C`, or run `docker compose down` in another terminal.

Compose reads database settings from the shell environment or a `.env` file next to `docker-compose.yaml`. The current database image and initialization scripts use `db`, `usr`, and `pwd` as defaults. Set matching values when starting the stack, for example:

```dotenv
POSTGRES_DB=db
POSTGRES_USER=usr
POSTGRES_PASSWORD=pwd
DB_HOST=database
DB_PORT=5432
```

The backend JDBC URL is assembled from `DB_HOST` and `DB_PORT`; use `database` and `5432` for the Compose database service. Keep credentials appropriate to your environment in a local `.env` file and do not commit secrets.

To stop and remove the containers and networks while retaining database data, use `docker compose down`. To also delete the database volume and its contents, use `docker compose down -v`.

## REST API

Requests can be sent through Apache at port `80` or directly to the backend at port `8080`. Responses are JSON unless the route serves the static Apache page.

### Greetings

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/` | Returns a greeting. Optional query parameter `name` defaults to `World`. |

Example: `GET /?name=Aymane` returns a greeting object containing an incrementing `id` and `content` such as `Hello, Aymane!`.

### Students

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/students` | List students. |
| `GET` | `/students/{id}` | Get one student; returns `404` if not found. |
| `POST` | `/students` | Create a student; returns `201` and a `Location` header. |
| `PUT` | `/students/{id}` | Update an existing student; returns `404` if not found. |
| `DELETE` | `/students/{id}` | Delete an existing student; returns `404` if not found. |

Student JSON uses `firstname`, `lastname`, and a nested `department` object. For example:

```json
{
  "firstname": "Ada",
  "lastname": "Lovelace",
  "department": { "id": 1 }
}
```

### Departments

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/departments` | List departments. |
| `GET` | `/departments/{departmentName}` | Get a department by name. |
| `GET` | `/departments/{departmentName}/students` | List students in a department. |
| `GET` | `/departments/{departmentName}/count` | Count students in a department. |

The database initialization scripts create departments `IRC`, `ETI`, and `CGP` and insert four sample students. PostgreSQL runs files in `database/init` when it initializes an empty data directory. Existing named-volume data is not reinitialized when those scripts change.

### Actuator

The Spring configuration exposes the Actuator web endpoints `health`, `info`, `env`, `metrics`, `beans`, and `configprops`. For example, the health endpoint is available at `/actuator/health`.

## Project layout

```text
.
├── docker-compose.yaml       # Services, networks, port mappings, volume, health check
├── ay-app/                   # Spring Boot REST API and Maven build
│   ├── Dockerfile            # Multi-stage Java 21 image build
│   └── src/
├── database/                 # PostgreSQL image and startup SQL scripts
│   ├── Dockerfile
│   └── init/                 # Schema and sample data
├── httpd/                    # Apache configuration and static landing page
│   ├── Dockerfile
│   ├── httpd.conf
│   └── index.html
└── Hello-world/              # Standalone Java hello-world container example
```

The API source is organized into controllers, services, DAOs, and JPA entities under `ay-app/src/main/java/fr/takima/training/simpleapi`. The Maven project targets Java 21 and Spring Boot 3.4.2. Tests are under `ay-app/src/test` and use Spring's test support and Testcontainers dependencies.

`Hello-world` is a separate minimal Java container example and is not part of the Compose application.

## Build the API locally

The wrapper is included in `ay-app`:

```sh
cd ay-app
./mvnw package
```

The Docker build uses Maven in a Java 21 build stage and a Java 21 runtime stage. It packages the app without running tests (`mvn package -DskipTests`).
