#!/usr/bin/env bash

# Event Mate AI - Automated Deployment Script
# This script helps you deploy to Vercel

echo "==================================="
echo "Event Mate AI - Vercel Deployment"
echo "==================================="
echo ""

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "❌ Git is not installed. Please install Git first."
    exit 1
fi

# Step 1: Initialize Git Repository
if [ ! -d ".git" ]; then
    echo "📚 Initializing Git repository..."
    git init
    git add .
    git commit -m "Initial commit: Event Mate AI application"
    echo "✅ Git repository initialized"
else
    echo "✅ Git repository already exists"
fi

# Step 2: Check if remote is configured
if git remote get-url origin > /dev/null 2>&1; then
    echo "✅ Remote 'origin' already configured"
else
    echo ""
    echo "📌 To proceed, you need a GitHub repository."
    echo "Visit: https://github.com/new"
    echo ""
    echo "After creating your repository, run:"
    echo "  git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git"
    echo "  git branch -M main"
    echo "  git push -u origin main"
    echo ""
    exit 1
fi

# Step 3: Push to GitHub
echo ""
echo "📤 Pushing code to GitHub..."
git branch -M main
git push -u origin main
echo "✅ Code pushed to GitHub"

echo ""
echo "==================================="
echo "✅ Ready for Vercel Deployment!"
echo "==================================="
echo ""
echo "Next steps:"
echo "1. Visit: https://vercel.com/new"
echo "2. Sign in with GitHub"
echo "3. Import your repository"
echo "4. It will auto-detect the Vite project"
echo "5. Click 'Deploy'"
echo ""
echo "After deployment, get your live URL from Vercel dashboard!"
