# Defect Log - CareerBridge Java Backend (Review 1 code audit)

Found by manual/API testing with curl against the running server (JDK 21). All fixes re-tested.

| ID | Defect | Severity | Priority | Steps to reproduce | Fix applied | Status |
|----|--------|----------|----------|--------------------|-------------|--------|
| D-01 | Password-hash file publicly downloadable | Critical | High | Start server, open `/data/users.tsv` -> HTTP 200 with all hashes | Static server now blocks `data/`, `backend/`, `docs/`; only allow-listed file types served | Fixed |
| D-02 | Java source code downloadable | High | High | Open `/backend/src/main/java/.../AuthService.java` -> 200 | Same allow-list + blocked folders | Fixed |
| D-03 | Passwords hashed with plain unsalted SHA-256 | High | High | Register two users with same password -> identical hash | Salted PBKDF2-HMAC-SHA256 (120,000 iterations); old hashes still accepted | Fixed |
| D-04 | Any visitor could list all users' applications | High | High | `GET /api/applications` with no login -> everyone's records | Endpoint now requires `email` and returns only that user's records | Fixed (note: no session/JWT yet) |
| D-05 | Server crash (HTTP 500) on blank/corrupt line in users file | Medium | Medium | Append blank line + `badline` to `data/users.tsv`, then register -> 500 | Rows are parsed defensively; bad lines skipped | Fixed |
| D-06 | Wrong HTTP status codes | Low | Medium | Wrong password -> 400; duplicate email -> 400 | 401 for bad credentials, 409 for duplicate | Fixed |
| D-07 | No request-size limit | Medium | Medium | POST 70 KB body to `/api/auth/login` | Body capped at 64 KB, returns 400 | Fixed |
| D-08 | Email case/whitespace not normalised; tab/newline in name could corrupt the TSV | Low | Low | Register `Riya@Test.com` then `riya@test.com` | Email trimmed + lower-cased; control characters stripped | Fixed |
| D-09 | Port, data folder and frontend folder hard-wired (blocks Docker/cloud) | Medium | High | Try running in a container | `PORT`, `DATA_DIR`, `STATIC_DIR` environment variables | Fixed |
| D-10 | Pending application writes could be lost on shutdown | Low | Low | Stop server right after POST | Shutdown hook drains the thread pool | Fixed |
| D-11 | Signup/login showed "failed" when the page was not opened from the Java server (Live Server / double-click) | High | High | Open `signup.html` from a file or port 5500 and submit | Frontend picks the right backend address; shows "Cannot reach the server. Start run-windows.bat..." | Fixed |
| D-12 | "Continue with Google" was a fake button that opened the dashboard as a default demo user (wrong account) | High | High | Click Google on the login page | Real Google sign-in (ID token verified on the server) or a clear "not set up" message | Fixed (real Google login needs a client ID; not tested) |
| D-13 | Dashboard and inner pages opened without any login, showing the default demo user | Medium | High | Open `dashboard.html` directly | Session check redirects to `login.html`; Sign Out clears the session | Fixed |
| D-14 | Data kept only in text files, not a database | Medium | High | Inspect `data/` folder | Users and applications in an H2 SQL database (viewable in H2 Console); files kept for the activity log | Fixed |
| D-15 | JSON helper did not escape tab/control characters and decoded `\\n`, `\\t`, `\\uXXXX` wrongly (could corrupt names/emails sent as JSON) | Medium | Medium | Unit tests `quoteEscapesTabAndControlCharacters`, `getDecodesNewlineTabAndUnicodeEscapes` | `Json.quote` escapes control characters; `Json.get` decodes standard escapes | Fixed |

## Known open issues (honest status)
- No sessions/JWT: login does not issue a token, so API calls are not truly authenticated.
- User data is in TSV files (prototype). Cloud phase will add a managed database / object storage.
- CORS allows any origin; HTTPS is provided by the cloud platform, not by this server.
- Not yet verified: Windows machine run, concurrent-load test, full browser UI flow.
