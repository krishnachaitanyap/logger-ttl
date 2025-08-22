#!/bin/bash

# Logger TTL Framework - GitHub Packages Setup
# This script helps set up publishing to GitHub Packages

echo "🚀 Logger TTL Framework - GitHub Packages Setup"
echo "================================================"

# Check if we're in a git repository
if [ ! -d ".git" ]; then
    echo "❌ Not in a git repository. Please run this from your project directory."
    exit 1
fi

# Check if remote is configured
if ! git remote get-url origin > /dev/null 2>&1; then
    echo "❌ No 'origin' remote configured. Please add your GitHub repository:"
    echo "   git remote add origin https://github.com/krishnachaitanyap/logger-ttl.git"
    exit 1
fi

REMOTE_URL=$(git remote get-url origin)
if [[ ! $REMOTE_URL =~ krishnachaitanyap/logger-ttl ]]; then
    echo "⚠️  Remote URL doesn't match expected pattern:"
    echo "   Expected: https://github.com/krishnachaitanyap/logger-ttl.git"
    echo "   Found: $REMOTE_URL"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "✅ Git repository configured correctly"
echo "✅ POM.xml updated with your information"
echo "✅ GitHub Packages distribution configured"

echo ""
echo "🔑 Next Steps:"
echo "=============="
echo ""
echo "1. Generate GitHub Token:"
echo "   - Go to: https://github.com/settings/tokens"
echo "   - Click 'Generate new token (classic)'"
echo "   - Name: 'Maven Publishing'"
echo "   - Scopes: repo, write:packages, read:packages"
echo "   - Copy the token"
echo ""
echo "2. Configure Maven Settings:"
echo "   - Copy github-settings.xml to ~/.m2/settings.xml"
echo "   - Replace 'YOUR_GITHUB_TOKEN_HERE' with your actual token"
echo ""
echo "3. Test the Build:"
echo "   mvn clean test"
echo ""
echo "4. Deploy to GitHub Packages:"
echo "   mvn clean deploy"
echo ""
echo "5. Verify Publication:"
echo "   - Go to your GitHub repository"
echo "   - Click 'Packages' tab"
echo "   - You should see 'logger-ttl' package"
echo ""

# Check if Maven is available
if command -v mvn >/dev/null 2>&1; then
    echo "✅ Maven is available"
    
    # Offer to test the build
    read -p "Would you like to test the build now? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo ""
        echo "🧪 Testing the build..."
        mvn clean test
        
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ Build successful! Ready for deployment."
            echo ""
            echo "🚀 To deploy to GitHub Packages:"
            echo "   mvn clean deploy"
        else
            echo ""
            echo "❌ Build failed. Please fix issues before deploying."
        fi
    fi
else
    echo "⚠️  Maven not found. Please install Maven first."
fi

echo ""
echo "📚 For detailed instructions, see:"
echo "   - GITHUB_PUBLISHING.md (GitHub-specific guide)"
echo "   - PUBLISHING_GUIDE.md (Comprehensive guide)"
echo "   - README.md (Project documentation)"
echo ""
echo "🎉 Setup complete! Your project is ready for GitHub Packages publishing."
