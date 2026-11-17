# Deploying CareerBridge (GitHub + Render, free, no credit card)

1. Put this folder in a GitHub repository (Add file -> Upload files; commit to `main`). Do not upload passwords or API keys: none are needed.
2. Sign up at render.com with your GitHub account.
3. New -> Web Service -> pick the repository.
   - Language / Runtime: **Docker** (uses the `Dockerfile` in this folder)
   - Instance type: **Free**
   - Health Check Path: `/api/health`
4. Create Web Service. The first build takes a few minutes. The public HTTPS address looks like `https://careerbridge-xxxx.onrender.com`.
5. Open the address, sign up, then open `/api/health` (it must show `"status":"UP"` and `"database":"UP"`).

Settings (environment variables, all optional): `DB_URL`, `DB_USER`, `DB_PASSWORD`, `GOOGLE_CLIENT_ID`. Render supplies `PORT` automatically.

Notes
- Free services go to sleep after about 15 minutes without visitors; the first request afterwards takes up to a minute.
- With the default embedded H2 database the data is stored inside the container and is lost when Render restarts or redeploys it. Use an external database (`DB_URL`) for permanent data.
- Updates: push to GitHub and Render redeploys automatically.
