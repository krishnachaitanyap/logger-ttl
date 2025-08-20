# 🚀 Quick Start - Publish to GitHub Packages

## ✅ What's Already Done

Your Logger TTL Framework is **fully configured** for GitHub Packages publishing:

- ✅ **POM.xml updated** with your information (krishnachaitanyap)
- ✅ **GitHub Packages distribution** configured
- ✅ **All tests passing** (41 test cases)
- ✅ **Build successful** with all required artifacts

## 🚀 Ready to Publish in 3 Steps

### Step 1: Generate GitHub Token
1. Go to: https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Name: "Maven Publishing"
4. Select scopes:
   - ✅ `repo` (Full control of private repositories)
   - ✅ `write:packages` (Upload packages)
   - ✅ `read:packages` (Download packages)
5. Click "Generate token"
6. **Copy the token** (save it somewhere safe!)

### Step 2: Configure Maven
```bash
# Copy the provided settings file
cp github-settings.xml ~/.m2/settings.xml

# Edit the file and replace YOUR_GITHUB_TOKEN_HERE
nano ~/.m2/settings.xml
# or
code ~/.m2/settings.xml
```

**Replace this line:**
```xml
<password>YOUR_GITHUB_TOKEN_HERE</password>
```

**With your actual token:**
```xml
<password>ghp_your_actual_token_here</password>
```

### Step 3: Deploy to GitHub Packages
```bash
# Deploy the current version
mvn clean deploy
```

## 🔍 Verify Publication

1. Go to: https://github.com/krishnachaitanyap/logger-ttl
2. Click the "Packages" tab
3. You should see your `logger-ttl` package
4. Click on it to see version details

## 📚 Usage for Other Developers

Once published, other developers can use your framework:

### Maven Users
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

### Gradle Users
```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/krishnachaitanyap/logger-ttl")
        credentials {
            username = "krishnachaitanyap"
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.krishnachaitanyap:logger-ttl:1.0.0")
}
```

## 🛠️ Helper Scripts

### Quick Setup
```bash
./setup-github.sh
```

### Release Management
```bash
# Create new version
./release.sh version 1.0.1

# Deploy snapshot
./release.sh snapshot

# Create release
./release.sh release 1.0.1
```

## 🔧 Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Check your GitHub token has correct scopes
   - Verify username is `krishnachaitanyap` in settings.xml
   - Ensure token hasn't expired

2. **Package Not Found**
   - Wait a few minutes after deployment
   - Check the "Packages" tab in your repository
   - Verify the package name matches `logger-ttl`

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

## 📈 Next Steps After Publishing

1. **Update README.md** with installation instructions
2. **Create GitHub release** with release notes
3. **Share your package** with the community
4. **Monitor usage** in GitHub Insights

## 🎉 Success!

Once published, your Logger TTL Framework will be available at:
**https://maven.pkg.github.com/krishnachaitanyap/logger-ttl**

### Package Coordinates
- **Group ID**: `io.github.krishnachaitanyap`
- **Artifact ID**: `logger-ttl`
- **Version**: `1.0.0`

## 📞 Need Help?

- **Run the setup script**: `./setup-github.sh`
- **Check detailed guide**: `GITHUB_PUBLISHING.md`
- **View comprehensive guide**: `PUBLISHING_GUIDE.md`
- **GitHub Packages Docs**: https://docs.github.com/en/packages

## 🚀 You're Ready!

Your project is configured and ready for GitHub Packages publishing. Just follow the 3 steps above and you'll have your TTL logging framework available to the world!

**Good luck with your publishing! 🎉**
