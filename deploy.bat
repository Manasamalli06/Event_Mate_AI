@echo off
REM Event Mate AI - Automated Deployment Script for Windows
REM This script helps you deploy to Vercel

echo ===================================
echo Event Mate AI - Vercel Deployment
echo ===================================
echo.

REM Check if git is installed
git --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Git is not installed. Please install Git from: https://git-scm.com
    pause
    exit /b 1
)

REM Step 1: Initialize Git Repository
if not exist ".git" (
    echo 📚 Initializing Git repository...
    call git init
    call git add .
    call git commit -m "Initial commit: Event Mate AI application"
    echo ✅ Git repository initialized
) else (
    echo ✅ Git repository already exists
)

REM Step 2: Check if remote is configured
for /f %%i in ('git remote get-url origin 2^>nul') do set ORIGIN=%%i
if not "%ORIGIN%"=="" (
    echo ✅ Remote 'origin' already configured: %ORIGIN%
) else (
    echo.
    echo 📌 To proceed, you need a GitHub repository.
    echo Visit: https://github.com/new
    echo.
    echo After creating your repository, run:
    echo   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
    echo   git branch -M main
    echo   git push -u origin main
    echo.
    pause
    exit /b 1
)

REM Step 3: Push to GitHub
echo.
echo 📤 Pushing code to GitHub...
call git branch -M main
call git push -u origin main
echo ✅ Code pushed to GitHub

echo.
echo ===================================
echo ✅ Ready for Vercel Deployment!
echo ===================================
echo.
echo Next steps:
echo 1. Visit: https://vercel.com/new
echo 2. Sign in with GitHub
echo 3. Import your repository
echo 4. It will auto-detect the Vite project
echo 5. Click 'Deploy'
echo.
echo After deployment, get your live URL from Vercel dashboard!
echo.
pause
