# CareerBridge

HTML/CSS/JavaScript frontend + Core Java backend (HTTP server, H2 SQL database, file logging, thread pool).

## Requirements
JDK 17 or newer (Windows 10/11). Maven is NOT needed.

## Run (Windows)
1. Extract the zip and double-click `run-windows.bat`.
2. Open **http://localhost:8080** in Chrome (always use this address, not by double-clicking the .html files).
3. Health check: http://localhost:8080/api/health (shows `database: UP`).

## Where is the data saved? (Database)
- Users and internship applications are stored in an **H2 SQL database**: `data/careerbridge.mv.db` (created automatically).
- Tables: `users`, `applications`.
- Activity log (file handling): `data/activity.log` (REGISTER / LOGIN / APPLICATION lines).
- Old `users.tsv` / `applications.tsv` files, if present, are imported into the database once.

### How to view the database
1. Double-click `database-console-windows.bat` (the website may keep running).
2. A browser opens the H2 Console. Enter the JDBC URL printed by the .bat (`jdbc:h2:file:<this folder>/data/careerbridge;AUTO_SERVER=TRUE`), User `sa`, empty password.
3. Run `SELECT * FROM users;` and `SELECT * FROM applications;`.

Cloud / other database: set environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD` (any JDBC database).

## Login and sign up
- Email + password: `signup.html` / `login.html` -> saved in the `users` table; passwords are stored as salted PBKDF2 hashes.
- Dashboard and other inner pages need a login; without one you are sent to the login page.
- "Demo account" button signs in as the sample student (demo mode, nothing saved).

### Google login (optional, needs your own client ID)
1. Google Cloud Console -> APIs & Services -> Credentials -> Create OAuth client ID -> "Web application".
2. Authorized JavaScript origins: `http://localhost:8080` (add your cloud URL later).
3. Copy the Client ID, open `run-windows.bat`, remove `rem` from the `GOOGLE_CLIENT_ID` line and paste it. Restart.
4. Without a client ID the Google button only shows a "not set up" message (no fake login).

## Automated tests (JUnit 5 + JaCoCo coverage)
- Windows: double-click `run-tests-windows.bat`. Linux/macOS/CI: `sh run-tests.sh`.
- 74 tests: unit tests (AuthService, Json, models, thread pool, file storage), database tests (H2 in memory) and integration tests that start the real HTTP server on a random port.
- Results: `test-reports/test-output.txt`, XML in `test-reports/junit/`. Coverage: open `test-reports/coverage/html/index.html` (summary in `test-reports/coverage/coverage-summary.txt`).
- Test libraries are included in `lib-test/` (JUnit Platform Console 1.9.1, JaCoCo 0.8.11, ASM) so no Maven or internet is needed.

## Docker (cloud phase)
`docker build -t careerbridge .` then `docker run -p 8080:8080 -v careerbridge-data:/data careerbridge`. The container reads `PORT`, `DATA_DIR`, `STATIC_DIR`, `DB_URL`, `GOOGLE_CLIENT_ID`. CI pipeline: `.github/workflows/ci.yml` (tests, then image build).

## Configuration (environment variables)
`PORT` (default 8080), `DATA_DIR` (default ./data), `STATIC_DIR` (default .), `DB_URL`, `DB_USER`, `DB_PASSWORD`, `GOOGLE_CLIENT_ID`.

## API
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | Server + database status |
| GET | `/api/config` | Public config (Google client id) |
| POST | `/api/auth/register` | Register (name, email, password) -> 201 / 400 / 409 |
| POST | `/api/auth/login` | Login (email, password) -> 200 / 401 |
| POST | `/api/auth/google` | Login with Google ID token |
| POST | `/api/applications` | Queue an internship application (email, internshipId) -> 202 |
| GET | `/api/applications?email=` | That user's applications |

## Java concepts demonstrated
- OOP: abstract `User`, `Student`, `Employer`, `InternshipApplication`.
- Inheritance and polymorphism: `getDashboardType()` is overridden.
- Interfaces: `StorageRepository`, `UserRepository`, `ApplicationRepository`, `GoogleTokenVerifier` (file, JDBC and HTTP implementations).
- Custom exceptions: `InvalidCredentialsException`, `DuplicateUserException`, `InvalidInputException`.
- Multithreading: `BackgroundProcessor` (thread pool) saves applications and writes the activity log; the HTTP server uses an 8-thread executor.
- File management: `FileRepository` writes `data/activity.log`.
- Libraries: H2 Database 2.2.220 (MPL 2.0 / EPL 1.0), Google Identity Services (browser script).

## Honest limitations
Prototype: no JWT/session tokens on the API (the browser keeps a simple local session), most career/task/progress data is still in browser LocalStorage, Docker/CI files are written but the image build and cloud deployment have not been run yet. Google login needs your own client ID and could not be tested without one.

See `docs/DEFECT-LOG.md`, `TEST-RESULTS.md`, `docs/uml-class-diagram.md`.
