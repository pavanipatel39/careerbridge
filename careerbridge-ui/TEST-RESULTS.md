# Verification log

Environment: Linux, JDK 21, H2 2.2.220, JUnit Platform 1.9.1 (Jupiter), JaCoCo 0.8.11.

## Automated JUnit run (`run-tests.sh`)
| Item | Result |
|---|---|
| Tests found / passed / failed | 74 / 74 / 0 |
| Line coverage (JaCoCo) | 84% |
| Branch coverage | 69% |
| Method coverage | 88% |
| Not covered by tests | `Main` (program entry), `TokenInfoVerifier` (calls Google over the internet) |

Test classes: JsonTest, ModelTest, AuthServiceTest (incl. parameterized boundary tests and a 20-thread race test), BackgroundProcessorTest, FileRepositoryTest (100 concurrent appends), JdbcRepositoryTest (incl. SQL-injection text), DatabaseImportTest, ApiServerTest (real HTTP server, 22 tests incl. 30 simultaneous signups and blocked download paths).

## Manual / scripted checks
| Check | Result |
|---|---|
| Register -> row in `users` table (H2 Shell) | PASS |
| Application POST -> row in `applications` table | PASS |
| Frontend signup, duplicate signup, wrong password, login (jsdom against live server) | PASS |
| Dashboard without login redirects to login | PASS |
| Google button without client ID shows "not set up" | PASS |
| Container-style run with PORT / DATA_DIR / STATIC_DIR env vars | PASS (simulated, no Docker here) |
| Google sign-in with a real client ID | NOT TESTED (needs a Client ID) |
| Run in real Chrome on Windows | Confirmed by the student for signup/login/database view |
| `docker build`, GitHub Actions run, cloud deployment | NOT RUN YET |
