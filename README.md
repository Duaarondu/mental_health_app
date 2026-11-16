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
│   ├── controller/                           # Auth, appointments, therapists, crisis, journal endpoints
│   ├── service/                              # Business rules (booking, accounts, crisis alerts, journal encryption)
│   ├── model/                                # Entities mapped to database/schema.sql
│   ├── repository/                           # Database queries (Spring Data JPA)
│   ├── security/                             # Spring Security: roles, BCrypt, sessions
│   └── config/DemoDataSeeder.java            # Demo accounts for presentations
├── frontend/
│   ├── src/api.js                            # All calls to the backend
│   ├── src/App.jsx                           # Main component, shared state
│   └── src/components/                       # Login, sign-up, patient home, mood check-in, journal, therapist dashboard, crisis screen
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

## Accounts and login

There are three kinds of users:

| Account | How they sign in | What they can do |
|---|---|---|
| **Patient** | Username + password | Daily mood check-in, private encrypted journal, book and cancel sessions, press "Let my therapist know" on the crisis screen |
| **Therapist** | Username + password (Therapist toggle) | See their schedule, see crisis alerts from their own patients. New therapists must be verified (`USER.isVerified = TRUE`) before patients can book them |
| **Anonymous** | "Continue anonymously", with an optional nickname | Same as a patient, but therapists only see the nickname. They get a one-time username and recovery code to come back later |

Passwords are stored as BCrypt hashes. Logins use a session cookie (HttpOnly, SameSite=Strict) stored in the database, so users stay signed in across restarts.

**Demo accounts** (created on startup when `SEED_DEMO_DATA=true`, the default), all with password `Demo1234!` unless `DEMO_PASSWORD` is set:

| Username | Role |
|---|---|
| `dr.rivera`, `dr.okafor`, `dr.chen` | Verified therapists |
| `jordan` | Patient |

On a public website, set `SEED_DEMO_DATA=false` or choose a private `DEMO_PASSWORD`.

On Cloud Run, the app refuses to start if `DEMO_PASSWORD` (while demo data is on) or `JOURNAL_KEY` is missing, so the public defaults are never used on the live site. Changing `DEMO_PASSWORD` later does not change the password of demo accounts that already exist.

**Abuse limits:** 5 wrong passwords lock that username for 15 minutes; each network address can create 10 accounts an hour (300 in total); each patient can have 3 upcoming sessions. Limits are counted per server, so keep Cloud Run at a few instances: `gcloud run services update mindcare-api --region us-east4 --max-instances=2`.

---

## API

| Method | Endpoint | Who | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Anyone | `{username, password, firstName, lastName, accountType: "patient" or "therapist", specializations}` |
| POST | `/api/auth/login` | Anyone | `{username, password, accountType}` |
| POST | `/api/auth/anonymous` | Anyone | `{nickname}`. Returns a one-time `recoveryCode` |
| GET | `/api/auth/me` | Anyone | The signed-in user, or 401 |
| POST | `/api/auth/logout` | Signed in | Ends the session |
| GET | `/api/home`, `/api/resources` | Anyone | Welcome message, list of resources |
| GET | `/api/therapists` | Anyone | Verified therapists |
| GET | `/api/appointments` | Signed in | Patients: their sessions. Therapists: sessions booked with them |
| POST | `/api/appointments` | Patients | `{therapistId, scheduledTime: "2026-11-20T15:00", duration: 30 or 50}`. Rejects past times, unverified therapists and double bookings |
| POST | `/api/appointments/{id}/cancel` | That patient or therapist | Cancel a scheduled session |
| GET | `/api/journal` | Patients | Their own check-ins and journal entries, newest first |
| POST | `/api/journal` | Patients | `{moodScore: 1-5, content}`. Empty `content` = a quick mood check-in. Text is encrypted before it's saved |
| DELETE | `/api/journal/{id}` | That patient | Delete one of their entries |
| POST | `/api/crisis/alert` | Patients | "Let my therapist know": sets `CLIENT_PROFILE.isCrisisActive` |
| GET | `/api/therapist/alerts` | Therapists | Their patients who asked for help |
| POST | `/api/therapist/alerts/{clientId}/resolve` | Therapists | Mark an alert as followed up |

---

## Database

The backend connects to MySQL using these settings (in `src/main/resources/application.properties`):

| Variable | Default (local) |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/mental_health_db?...` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |
| `SEED_DEMO_DATA` | `true` (create demo accounts) |
| `DEMO_PASSWORD` | `Demo1234!` locally. Required on Cloud Run |
| `COOKIE_SECURE` | `false` (set `true` when deployed over HTTPS) |
| `APP_TIME_ZONE` | `America/New_York` |
| `JOURNAL_KEY` | *(none: a development key is used and a warning is logged; on Cloud Run the app won't start without it)*. Make one with `openssl rand -base64 32` and keep it in Secret Manager. Losing it makes old journal entries unreadable |

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
