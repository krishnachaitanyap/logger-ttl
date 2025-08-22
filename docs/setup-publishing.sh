#!/bin/bash

# Logger TTL Framework - Publishing Setup Script
# This script helps set up the necessary configuration for publishing to Maven repositories

echo "🚀 Logger TTL Framework - Publishing Setup"
echo "=========================================="

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command_exists mvn; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

if ! command_exists git; then
    echo "❌ Git is not installed. Please install Git first."
    exit 1
fi

if ! command_exists gpg; then
    echo "⚠️  GPG is not installed. You'll need it for Maven Central publishing."
    echo "   Install with: brew install gnupg (macOS) or apt-get install gnupg (Ubuntu)"
else
    echo "✅ GPG is available"
fi

echo "✅ Maven is available"
echo "✅ Git is available"

# Check if we're in a git repository
if [ ! -d ".git" ]; then
    echo "⚠️  Not in a git repository. Initializing..."
    git init
    git add .
    git commit -m "Initial commit: Logger TTL Framework"
fi

# Prompt for user information
echo ""
echo "📝 Please provide your information for publishing:"

read -p "GitHub username: " GITHUB_USERNAME
read -p "Your full name: " FULL_NAME
read -p "Your email address: " EMAIL_ADDRESS
read -p "Project description (optional): " PROJECT_DESC

if [ -z "$PROJECT_DESC" ]; then
    PROJECT_DESC="A TTL logging framework built on top of SLF4J/Log4j"
fi

# Update pom.xml with user information
echo ""
echo "🔧 Updating pom.xml with your information..."

# Use sed to replace placeholders in pom.xml
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/yourusername/$GITHUB_USERNAME/g" pom.xml
    sed -i '' "s/Your Name/$FULL_NAME/g" pom.xml
    sed -i '' "s/your.email@example.com/$EMAIL_ADDRESS/g" pom.xml
    sed -i '' "s/A TTL logging framework built on top of SLF4J\/Log4j/$PROJECT_DESC/g" pom.xml
else
    # Linux
    sed -i "s/yourusername/$GITHUB_USERNAME/g" pom.xml
    sed -i "s/Your Name/$FULL_NAME/g" pom.xml
    sed -i "s/your.email@example.com/$EMAIL_ADDRESS/g" pom.xml
    sed -i "s/A TTL logging framework built on top of SLF4J\/Log4j/$PROJECT_DESC/g" pom.xml
fi

echo "✅ Updated pom.xml"

# Create Maven settings template
MAVEN_SETTINGS="$HOME/.m2/settings.xml"
echo ""
echo "🔧 Checking Maven settings..."

if [ ! -f "$MAVEN_SETTINGS" ]; then
    echo "Creating Maven settings template at $MAVEN_SETTINGS"
    mkdir -p "$HOME/.m2"
    cat > "$MAVEN_SETTINGS" << EOF
<settings>
    <servers>
        <!-- For Maven Central (OSSRH) -->
        <server>
            <id>ossrh</id>
            <username><!-- Your Sonatype username --></username>
            <password><!-- Your Sonatype password --></password>
        </server>
        
        <!-- For GitHub Packages -->
        <server>
            <id>github</id>
            <username>$GITHUB_USERNAME</username>
            <password><!-- Your GitHub token --></password>
        </server>
    </servers>
    
    <profiles>
        <profile>
            <id>release</id>
            <properties>
                <gpg.executable>gpg</gpg.executable>
                <gpg.keyname><!-- Your GPG key ID --></gpg.keyname>
                <gpg.passphrase><!-- Your GPG passphrase --></gpg.passphrase>
            </properties>
        </profile>
    </profiles>
</settings>
EOF
    echo "✅ Created Maven settings template"
    echo "⚠️  Please edit $MAVEN_SETTINGS and fill in your credentials"
else
    echo "✅ Maven settings file already exists"
fi

# Run tests to ensure everything works
echo ""
echo "🧪 Running tests to ensure everything works..."
mvn clean test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Tests failed. Please fix issues before publishing."
    exit 1
fi

# Build the project
echo ""
echo "📦 Building the project..."
mvn clean package

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed. Please fix issues before publishing."
    exit 1
fi

# Provide next steps
echo ""
echo "🎉 Setup complete! Next steps:"
echo ""
echo "For Maven Central:"
echo "1. Create account at https://issues.sonatype.org/"
echo "2. Request namespace for io.github.$GITHUB_USERNAME"
echo "3. Set up GPG key: gpg --gen-key"
echo "4. Update $MAVEN_SETTINGS with your credentials"
echo "5. Deploy: mvn clean deploy -P release"
echo ""
echo "For GitHub Packages:"
echo "1. Create GitHub token with packages:write scope"
echo "2. Update $MAVEN_SETTINGS with your token"
echo "3. Deploy: mvn clean deploy"
echo ""
echo "For JitPack (easiest):"
echo "1. Push to GitHub: git push origin main"
echo "2. Create tag: git tag v1.0.0 && git push origin v1.0.0"
echo "3. Visit https://jitpack.io and build your project"
echo ""
echo "📚 See PUBLISHING_GUIDE.md for detailed instructions"
