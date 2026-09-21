# MediStock: Render & Vercel Deployment Guide

This guide walks you step-by-step through deploying **MediStock** to production:
- **Backend API & PostgreSQL**: Deployed on [Render](https://render.com)
- **Frontend SPA**: Deployed on [Vercel](https://vercel.com)

---

## Architecture Overview

```
 [Client Browser]
        │
        ├───► [Vercel Global CDN] (React Frontend SPA)
        │            │
        │            ▼  (REST API requests with JWT)
        └───► [Render Web Service] (Spring Boot 3 API)
                     │
                     ▼
              [Render Managed PostgreSQL]
```

---

## Phase 1: Push Project to GitHub

Make sure your latest code with these changes is committed and pushed to your GitHub repository:
```bash
git add .
git commit -m "Configure MediStock for Render and Vercel deployment"
git push origin main
```

---

## Phase 2: Deploy Database & Backend on Render

### Step 2.1: Create PostgreSQL Database on Render
1. Log into [Render Dashboard](https://dashboard.render.com).
2. Click **New +** → **PostgreSQL**.
3. Fill in the details:
   - **Name**: `medistock-db`
   - **Database**: `medistock_db`
   - **User**: `postgres` (or default)
   - **Region**: Choose the region closest to you (e.g., Singapore, Frankfurt, Oregon)
   - **Plan**: Free / Starter
4. Click **Create Database**.
5. Once created, keep this page open and locate the connection strings:
   - If deploying backend on Render: copy **Internal Database URL** (faster and free internal bandwidth).
   - If external: copy **External Database URL**.

> [!TIP]
> **JDBC URL Format**:
> Render URLs look like:
> `postgres://user:password@dpg-xxxx.render.com/medistock_db`
> For Spring Boot's `DB_URL`, prefix it with `jdbc:`:
> `jdbc:postgresql://dpg-xxxx.render.com:5432/medistock_db`
> *(Render also provides a dedicated "PSQL Command" or "Host, Port, User, Password" breakdown on the dashboard).*

---

### Step 2.2: Deploy Spring Boot Backend on Render
1. In Render Dashboard, click **New +** → **Web Service**.
2. Connect your GitHub repository (`MediStock`).
3. Configure the service:
   - **Name**: `medistock-api`
   - **Region**: Same region as your database!
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Runtime**: **Docker**
   - **Plan**: Free / Starter
4. Under **Environment Variables**, add the following:

| Key | Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates `application-prod.properties` |
| `DB_URL` | `jdbc:postgresql://<db-host>:5432/<db-name>` | From Step 2.1 |
| `DB_USER` | `<your-db-username>` | From Step 2.1 |
| `DB_PASSWORD` | `<your-db-password>` | From Step 2.1 |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` | Or any 256-bit base64 secret |
| `CORS_ALLOWED_ORIGINS` | `https://*.vercel.app,http://localhost:5173` | Allows Vercel frontend |
| `EMAIL_ALERTS_ENABLED` | `false` | Set to `true` if SMTP configured |

5. Click **Deploy Web Service**.
6. Render will automatically run the multi-stage Docker build, start your Spring Boot application, and execute `DataLoader` to seed default categories, suppliers, and users.
7. Once live, copy your Render Web Service URL (e.g., `https://medistock-api.onrender.com`).
   - Test it by opening `https://medistock-api.onrender.com/api/medicines` in browser — it should return `401 Unauthorized` (indicating Spring Security is functioning properly).

---

## Phase 3: Deploy Frontend on Vercel

### Step 3.1: Import Project into Vercel
1. Log into [Vercel Dashboard](https://vercel.com).
2. Click **Add New…** → **Project**.
3. Import your `MediStock` GitHub repository.
4. Configure Project Settings:
   - **Framework Preset**: `Vite` (automatically detected)
   - **Root Directory**: Click **Edit** and select `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
5. Expand **Environment Variables** and add:

| Key | Value |
| :--- | :--- |
| `VITE_API_BASE` | `https://medistock-api.onrender.com/api` *(replace with your Render backend URL + `/api`)* |

6. Click **Deploy**.
7. Vercel will build and deploy your React app in ~30 seconds, providing a public domain (e.g., `https://medistock-frontend.vercel.app`).

---

## Phase 4: Verification & Login

1. Open your Vercel URL in your browser.
2. Sign in with the seeded accounts:
   - **Admin**: Username `admin_01` / Password `123456`
   - **Pharmacist**: Username `pharmacist_01` / Password `123456`
   - **Staff**: Username `staff_01` / Password `123456`
3. Verify that:
   - Dashboard stats, low stock alerts, and expiry notifications load correctly.
   - You can create, edit, or adjust inventory.
   - Page refresh on deep links (e.g., `/inventory`, `/suppliers`) works smoothly without 404s (handled by `frontend/vercel.json`).

> [!CAUTION]
> **Production Security Tip**: Immediately after your first login, change the default passwords for `admin_01`, `pharmacist_01`, and `staff_01` via the user profile or admin user management page.
