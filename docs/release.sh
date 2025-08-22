#!/bin/bash

# Logger TTL Framework - Release Script
# This script helps with versioning and releasing the framework

set -e

echo "🚀 Logger TTL Framework - Release Manager"
echo "========================================"

# Function to get current version from pom.xml
get_current_version() {
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout
}

# Function to update version in pom.xml
update_version() {
    local new_version=$1
    mvn versions:set -DnewVersion="$new_version" -DgenerateBackupPoms=false
}

# Function to validate version format
validate_version() {
    local version=$1
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$ ]]; then
        echo "❌ Invalid version format. Use semantic versioning (e.g., 1.0.0 or 1.0.0-SNAPSHOT)"
        exit 1
    fi
}

# Get current version
CURRENT_VERSION=$(get_current_version)
echo "📋 Current version: $CURRENT_VERSION"

# Parse command line arguments
ACTION=${1:-"help"}

case $ACTION in
    "snapshot")
        echo "🔄 Creating snapshot release..."
        
        # Ensure version ends with -SNAPSHOT
        if [[ $CURRENT_VERSION != *-SNAPSHOT ]]; then
            NEW_VERSION="$CURRENT_VERSION-SNAPSHOT"
            update_version "$NEW_VERSION"
            echo "✅ Updated version to $NEW_VERSION"
        fi
        
        # Run tests
        echo "🧪 Running tests..."
        mvn clean test
        
        # Deploy snapshot
        echo "📦 Deploying snapshot..."
        mvn clean deploy
        
        echo "✅ Snapshot deployed successfully!"
        ;;
        
    "release")
        NEW_VERSION=${2}
        
        if [ -z "$NEW_VERSION" ]; then
            echo "❌ Please specify a version number"
            echo "Usage: $0 release <version>"
            echo "Example: $0 release 1.0.1"
            exit 1
        fi
        
        validate_version "$NEW_VERSION"
        
        if [[ $NEW_VERSION == *-SNAPSHOT ]]; then
            echo "❌ Release version should not contain -SNAPSHOT"
            exit 1
        fi
        
        echo "🎯 Creating release version $NEW_VERSION..."
        
        # Update version
        update_version "$NEW_VERSION"
        
        # Run tests
        echo "🧪 Running tests..."
        mvn clean test
        
        # Build and package
        echo "📦 Building release..."
        mvn clean package
        
        # Git operations
        echo "📝 Committing version update..."
        git add pom.xml
        git commit -m "Release version $NEW_VERSION"
        git tag "v$NEW_VERSION"
        
        # Deploy release
        echo "🚀 Deploying release..."
        mvn clean deploy -P release
        
        # Prepare next development version
        IFS='.' read -ra VERSION_PARTS <<< "$NEW_VERSION"
        NEXT_PATCH=$((VERSION_PARTS[2] + 1))
        NEXT_DEV_VERSION="${VERSION_PARTS[0]}.${VERSION_PARTS[1]}.$NEXT_PATCH-SNAPSHOT"
        
        echo "🔄 Preparing next development version: $NEXT_DEV_VERSION"
        update_version "$NEXT_DEV_VERSION"
        git add pom.xml
        git commit -m "Prepare next development version $NEXT_DEV_VERSION"
        
        echo "✅ Release $NEW_VERSION completed successfully!"
        echo "📌 Don't forget to push: git push origin main --tags"
        ;;
        
    "version")
        NEW_VERSION=${2}
        
        if [ -z "$NEW_VERSION" ]; then
            echo "❌ Please specify a version number"
            echo "Usage: $0 version <version>"
            echo "Example: $0 version 1.1.0-SNAPSHOT"
            exit 1
        fi
        
        validate_version "$NEW_VERSION"
        
        echo "🔄 Updating version to $NEW_VERSION..."
        update_version "$NEW_VERSION"
        echo "✅ Version updated successfully!"
        ;;
        
    "build")
        echo "📦 Building project..."
        mvn clean package
        echo "✅ Build completed!"
        ;;
        
    "test")
        echo "🧪 Running tests..."
        mvn clean test
        echo "✅ Tests completed!"
        ;;
        
    "help"|*)
        echo "Usage: $0 <command> [options]"
        echo ""
        echo "Commands:"
        echo "  snapshot              - Deploy snapshot version"
        echo "  release <version>     - Create and deploy release version"
        echo "  version <version>     - Update version number"
        echo "  build                 - Build the project"
        echo "  test                  - Run tests"
        echo "  help                  - Show this help"
        echo ""
        echo "Examples:"
        echo "  $0 snapshot                    # Deploy current version as snapshot"
        echo "  $0 release 1.0.1              # Release version 1.0.1"
        echo "  $0 version 1.1.0-SNAPSHOT     # Update to version 1.1.0-SNAPSHOT"
        echo ""
        echo "Current version: $CURRENT_VERSION"
        ;;
esac
