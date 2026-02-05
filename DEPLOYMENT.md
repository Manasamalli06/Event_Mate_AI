# Event Mate AI - Deployment Guide

## Frontend Deployment to Vercel

### Prerequisites
1. Vercel account (free or paid) - [Sign up here](https://vercel.com/signup)
2. Git repository (GitHub, GitLab, or Bitbucket)
3. Project pushed to your Git repository

### Step-by-Step Deployment

#### 1. Push Your Code to GitHub
```bash
# In your project root directory
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/Event_Mate_AI.git
git branch -M main
git push -u origin main
```

#### 2. Deploy to Vercel
1. Go to [vercel.com](https://vercel.com)
2. Click "New Project"
3. Import your GitHub repository
4. Select the repository: `Event_Mate_AI`
5. Configure project settings:
   - **Framework Preset**: Vite
   - **Root Directory**: `./frontend/eventmateai`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
6. Add environment variables if needed (see Backend API Configuration below)
7. Click "Deploy"

#### 3. Domain Configuration
- Your frontend will be deployed to a `*.vercel.app` domain
- You can add a custom domain in Vercel settings

---

## Backend Deployment (Java Spring Boot)

### IMPORTANT: Vercel does NOT support Java natively

You have several options:

#### Option 1: Deploy Backend to Railway (Recommended)
1. Go to [railway.app](https://railway.app)
2. Sign up with GitHub
3. Create a new project
4. Connect to your GitHub repository
5. Select the `backend/eventmateai` directory
6. Railway will detect Maven and build automatically
7. Configure environment variables for database

#### Option 2: Deploy Backend to Render
1. Go to [render.com](https://render.com)
2. Connect your GitHub account
3. Create new Web Service
4. Configure with:
   - Build Command: `mvn clean package`
   - Start Command: `java -jar target/eventmateai-0.0.1-SNAPSHOT.jar`
5. Add environment variables

#### Option 3: Deploy Backend to Heroku
1. Go to [heroku.com](https://heroku.com)
2. Create new app
3. Connect to GitHub repository
4. Configure buildpacks for Java
5. Deploy

---

## Frontend-Backend API Integration

### Update Frontend API Base URL

After deploying the backend, update the frontend to use the correct API URL.

1. Find where API calls are made in your frontend (likely in service files)
2. Update the base URL to your backend deployment URL:

```javascript
// Example: Update API endpoints
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Or using environment variables
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
```

3. In Vercel project settings, add environment variable:
   - **Name**: `VITE_API_URL`
   - **Value**: `https://your-backend-url.railway.app/api` (or your deployed backend URL)

4. Redeploy frontend to apply changes

---

## Environment Variables

### Frontend (.env.local or in Vercel)
```
VITE_API_URL=https://your-backend.railway.app/api
VITE_APP_NAME=Event Mate AI
```

### Backend (application.properties or environment variables)
```
spring.datasource.url=jdbc:mysql://your-database:3306/eventmateai
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

---

## Database Setup

If using Railway or Render:
1. Add MySQL database service in your deployment platform
2. Configure connection string
3. Run database migrations if needed

---

## Monitoring & Logs

### Vercel
- View logs: Vercel Dashboard → Deployments → Select build → Logs
- Monitor performance: Analytics tab

### Railway/Render
- View logs: Dashboard → Project → Logs tab
- Monitor resource usage and health

---

## Troubleshooting

### Build Fails on Vercel
1. Check build logs in Vercel dashboard
2. Ensure `package.json` is in `frontend/eventmateai`
3. Verify Node.js version (should be 18+)

### API Calls Fail After Deployment
1. Check CORS configuration in backend
2. Verify API URL in frontend matches deployed backend
3. Check backend logs for errors

### Database Connection Issues
1. Verify database credentials in environment variables
2. Check network access/firewall settings
3. Ensure database service is running

---

## CI/CD Pipeline

Vercel automatically redeploys when you push to your main branch. To customize:

1. Go to Vercel Project Settings
2. Navigate to "Git"
3. Configure branch deployments and preview deployments

---

## Quick Summary

1. ✅ **frontend/eventmateai** → Deploy to Vercel
2. ✅ **backend/eventmateai** → Deploy to Railway/Render/Heroku
3. ✅ Update frontend API URLs with deployed backend address
4. ✅ Configure environment variables in both platforms
5. ✅ Test integration between frontend and backend
