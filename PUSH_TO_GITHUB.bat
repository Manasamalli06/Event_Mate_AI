@echo off
REM Copy this script output and paste into PowerShell

echo.
echo ========================================
echo Step 1: Set Git Config (one time)
echo ========================================
git config --global user.email "your_email@example.com"
git config --global user.name "Your Name"

echo.
echo ========================================
echo Step 2: Add Remote and Push to GitHub
echo ========================================
echo REPLACE "YOUR_USERNAME" in the URL below:
echo.
git remote add origin https://github.com/YOUR_USERNAME/Event-Mate-AI.git
git branch -M main
git push -u origin main

echo.
echo ========================================
echo ✅ Done! Your code is on GitHub
echo ========================================
echo.
echo Next: Go to https://vercel.com/new
echo Select your repository and click Deploy!
echo.
pause
