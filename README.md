# Mental Health App

A mental health support app where users can browse resources and book appointments.

- **Backend:** Java 21, Spring Boot (REST API on port 8080)
- **Database:** MySQL 8 (Spring Data JPA)
- **Frontend:** React + Vite (dev server on port 5173)
- **Docker:** runs both with one command
- **CI/CD:** GitHub Actions builds and tests every pull request, and publishes Docker images when code is merged to `main`

> Want to contribute? Read **[CONTRIBUTING.md](CONTRIBUTING.md)** for the fork and pull request workflow.

---

## Project structure

```
mental_health_app/
├── src/main/java/com/mentalhealth/mentalhealth_backend/
│   ├── MentalhealthBackendApplication.java   # Starts the Spring Boot server
│   ├── Home_Controller.java                  # GET  /api/home
│   ├── Resource_Controller.java              # GET  /api/resources
│   ├── Appointment_Controller.java           # GET/POST /api/appointments
│   └── Appointment.java                      # Appointment model (type, date, time)
├── frontend/
│   ├── src/api.js                            # All calls to the backend
│   ├── src/App.jsx                           # Main component, shared state
│   └── src/components/                       # Header, ResourceList, BookingForm, AppointmentList, Footer
├── database/schema.sql                        # MySQL tables (source of truth)
├── Dockerfile                                # Backend image
├── frontend/Dockerfile                       # Frontend image (nginx)
├── docker-compose.yml                        # Runs both together
└── .github/workflows/ci.yml                  # CI/CD pipeline
```

---

## Running the app

### Option 1: Docker (easiest, no Java or Node needed)

Install [Docker Desktop](https://www.docker.com/products/docker-desktop/), then from the project folder (this starts MySQL, the backend and the frontend):

```
docker compose up --build
```

- App: http://localhost:3000
- API: http://localhost:8080/api/resources

Stop it with `Ctrl + C`.

### Option 2: Run locally (for development)

You need **JDK 21 or newer**, **Node.js 20.19 or newer**, and a **MySQL** database.

**Start MySQL** (easiest with Docker):
```
docker compose up -d mysql
```
This also creates all the tables from `database/schema.sql`.

No Docker? Install MySQL Server locally with user `root` / password `root`, run `database/schema.sql` once (for example in MySQL Workbench), or point the backend at another database with the `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables.

**Terminal 1: backend**
```
.\mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run            # Mac / Linux
```

**Terminal 2: frontend**
```
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Vite forwards `/api` requests to the backend, so both must be running.

> **Windows tip:** if you see `release version 21 not supported` or `UnsupportedClassVersionError`, your `JAVA_HOME` points to an old JDK. Point it at JDK 21+:
> ```
> setx JAVA_HOME "C:\path\to\your\jdk-21"
> ```
> then open a new terminal.

---

## API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/home` | Welcome message |
| GET | `/api/resources` | List of resources |
| GET | `/api/appointments` | All booked appointments |
| POST | `/api/appointments` | Book an appointment. Body: `{"type": "Counseling", "date": "2026-11-20", "time": "10:00"}` |

## Database

The backend connects to MySQL using these settings (in `src/main/resources/application.properties`):

| Variable | Default (local) |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/mental_health_db?...` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |

For the hosted database, set these as environment variables on the hosting platform. **Never commit real passwords.**

**Schema:** the tables are defined in [`database/schema.sql`](database/schema.sql). Hibernate does not create or change tables (`ddl-auto=validate`); it only checks that the Java `@Entity` classes match them. To change the database, edit `schema.sql` in a pull request. With Docker, run `docker compose down -v` and then `docker compose up -d mysql` to rebuild the tables (this deletes local data).

Tests and CI use an in-memory H2 database (`src/test/resources/application-test.properties`), so they don't need MySQL.

---

## CI/CD

The pipeline in `.github/workflows/ci.yml` runs automatically:

| When | What happens |
|---|---|
| A pull request is opened or updated | Backend tests run, the frontend is built, and both Docker images are built. The PR shows ✅ or ❌. |
| Code is merged into `main` | All of the above, then the Docker images are published to GitHub Container Registry (see **Packages** on the repo page). |

---

If you are in crisis, call or text **988** (Suicide & Crisis Lifeline, US) or call **911**.
