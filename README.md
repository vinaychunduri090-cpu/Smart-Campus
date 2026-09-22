# Academic Campus Event & Registration Portal

## STARTUP INSTRUCTIONS (Local)

1. Start XAMPP, enable Apache + MySQL
2. Open phpMyAdmin, create database `campus_events` and run `database/schema.sql`
3. Copy `.env.example` to `.env` and fill values
4. `npm install`
5. `npm run dev`
6. Open http://localhost:3000
   - Admin Login: `admin@campus.edu` / `Admin@123`

---

## DEPLOYMENT GUIDE (Backend)

The frontend is live at: https://charan-25022005.github.io/campusPortal/public/index.html

### Step 1: Deploy MySQL Database
You need a hosting provider that supports MySQL. I recommend:
- **Aiven.io** (Free tier MySQL)
- **Railway.app** (Paid after credits, but very easy)

Once you get the connection details, update your **Deployment Environment Variables**:
- `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`

### Step 2: Deploy the Express Server (Node.js)
1. Go to [Render.com](https://render.com/) and sign in with GitHub.
2. Click **New +** > **Web Service**.
3. Select your repository: `charan-25022005/campusPortal`.
4. Use these settings:
   - **Environment**: Node
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
5. **Environment Variables**: Add everything from your `.env` file (Database credentials, `SESSION_SECRET`, etc.).
6. Once deployed, Render will give you a URL (e.g., `https://campus-portal-v1.onrender.com`).

### Step 3: Link Frontend to Backend
1. Update `public/js/utils.js` (line 3) with your new Render URL.
2. Push the change to GitHub:
   ```bash
   git add .
   git commit -m "Update backend URL"
   git push origin main
   git checkout gh-pages
   git add public -f
   git commit -m "Update live assets"
   git push origin gh-pages --force
   git checkout main
   ```
