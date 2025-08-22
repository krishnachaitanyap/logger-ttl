# 🚀 Publishing to GitHub Packages - Quick Guide

## 📋 Prerequisites

1. ✅ **GitHub Repository**: Your project should be on GitHub at `https://github.com/krishnachaitanyap/logger-ttl`
2. ✅ **GitHub Token**: Personal access token with `packages:write` scope
3. ✅ **Maven**: Installed and configured

## 🔑 Step 1: Generate GitHub Token

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Give it a name like "Maven Publishing"
4. Select scopes:
   - ✅ `repo` (Full control of private repositories)
   - ✅ `write:packages` (Upload packages to GitHub Package Registry)
   - ✅ `read:packages` (Download packages from GitHub Package Registry)
5. Click "Generate token"
6. **Copy the token** (you won't see it again!)

## ⚙️ Step 2: Configure Maven Settings

### Option A: Use the provided settings file
1. Copy `github-settings.xml` to `~/.m2/settings.xml`
2. Replace `YOUR_GITHUB_TOKEN_HERE` with your actual GitHub token

### Option B: Update existing settings
Add this to your `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>krishnachaitanyap</username>
        <password>YOUR_ACTUAL_TOKEN_HERE</password>
    </server>
</servers>
```

## 🧪 Step 3: Test the Build

```bash
# Clean and test
mvn clean test

# Build the project
mvn clean package
```

## 📦 Step 4: Deploy to GitHub Packages

```bash
# Deploy the current version
mvn clean deploy
```

## 🔍 Step 5: Verify Publication

1. Go to your GitHub repository
2. Click on the "Packages" tab
3. You should see your `logger-ttl` package
4. Click on it to see version details

## 📚 Step 6: Usage Instructions

### For Other Developers

Add this to their `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/krishnachaitanyap/logger-ttl</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.krishnachaitanyap</groupId>
    <artifactId>logger-ttl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### For Gradle Users

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/krishnachaitanyap/logger-ttl")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.krishnachaitanyap:logger-ttl:1.0.0")
}
```

## 🚀 Automated Publishing with GitHub Actions

The project includes GitHub Actions workflows that will automatically publish to GitHub Packages when you:

1. **Create a release** on GitHub
2. **Push a tag** (e.g., `v1.0.0`)

### Manual Release Process

```bash
# 1. Update version if needed
./release.sh version 1.0.1

# 2. Create and push tag
git tag v1.0.1
git push origin v1.0.1

# 3. Create GitHub release
# Go to GitHub → Releases → Create new release
# Tag: v1.0.1
# Title: Release 1.0.1
# Description: [Your release notes]
```

## 🔧 Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Check your GitHub token has correct scopes
   - Verify username is correct in settings.xml
   - Ensure token hasn't expired

2. **Package Not Found**
   - Wait a few minutes after deployment
   - Check the "Packages" tab in your repository
   - Verify the package name matches your artifactId

3. **Build Failures**
   - Run `mvn clean test` locally first
   - Check all tests are passing
   - Ensure Java 11+ is being used

### Debug Commands

```bash
# Verbose Maven output
mvn clean deploy -X

# Check Maven settings
mvn help:effective-settings

# Verify project configuration
mvn help:effective-pom
```

## 📈 Next Steps

After successful publication to GitHub Packages:

1. **Update README.md** with installation instructions
2. **Create release notes** for each version
3. **Consider Maven Central** for wider adoption
4. **Monitor package usage** in GitHub Insights

## 🎉 Success!

Once published, your Logger TTL Framework will be available at:
`https://maven.pkg.github.com/krishnachaitanyap/logger-ttl`

Users can easily include it in their projects using the dependency coordinates:
- **Group ID**: `io.github.krishnachaitanyap`
- **Artifact ID**: `logger-ttl`
- **Version**: `1.0.0`

## 📞 Need Help?

- **GitHub Packages Docs**: https://docs.github.com/en/packages
- **Maven Documentation**: https://maven.apache.org/
- **Project Issues**: Create an issue in your GitHub repository
