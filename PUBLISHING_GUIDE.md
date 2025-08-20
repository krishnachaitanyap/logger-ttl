# Maven Repository Publishing Guide

This guide explains how to publish the Logger TTL Framework to various Maven repositories.

## 📋 Prerequisites

Before publishing, ensure you have:
- ✅ All tests passing (`mvn test`)
- ✅ Clean build (`mvn clean package`)
- ✅ Proper versioning (semantic versioning recommended)
- ✅ Complete documentation (README.md)

## 🌟 Option 1: Maven Central (Recommended for Open Source)

Maven Central is the primary repository for open-source Java libraries.

### Step 1: Prerequisites for Maven Central

1. **Create a Sonatype JIRA account**: https://issues.sonatype.org/
2. **Create a new project ticket** requesting a new namespace
3. **Set up GPG signing** for artifact security
4. **Configure Maven settings**

### Step 2: Set up GPG Signing

```bash
# Generate a GPG key pair
gpg --gen-key

# List your keys
gpg --list-keys

# Export your public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export your public key to another keyserver for redundancy
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

### Step 3: Configure Maven Settings

Create or update `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>ossrh</id>
            <username>your-sonatype-username</username>
            <password>your-sonatype-password</password>
        </server>
    </servers>
    
    <profiles>
        <profile>
            <id>release</id>
            <properties>
                <gpg.executable>gpg</gpg.executable>
                <gpg.keyname>YOUR_KEY_ID</gpg.keyname>
                <gpg.passphrase>your-gpg-passphrase</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
</settings>
```

### Step 4: Update POM Configuration

The `pom.xml` has been updated with the necessary configuration. You need to:

1. **Replace placeholders**:
   - `yourusername` → your GitHub username
   - `Your Name` → your actual name
   - `your.email@example.com` → your email

2. **Update the groupId**: Use `io.github.yourusername` format

### Step 5: Deploy to Maven Central

```bash
# Deploy snapshot version (if version ends with -SNAPSHOT)
mvn clean deploy

# Deploy release version
mvn clean deploy -P release

# Or use the nexus staging plugin
mvn clean deploy -P release
mvn nexus-staging:release
```

### Step 6: Verify Publication

After successful deployment, your artifact will be available at:
- **Staging**: https://s01.oss.sonatype.org/
- **Maven Central**: https://search.maven.org/

## 🏢 Option 2: GitHub Packages

GitHub Packages is integrated with GitHub repositories and is free for public repositories.

### Step 1: Update POM for GitHub Packages

Add this to your `pom.xml`:

```xml
<distributionManagement>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/yourusername/logger-ttl</url>
    </repository>
</distributionManagement>
```

### Step 2: Configure Authentication

Add to `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>your-github-username</username>
        <password>your-github-token</password>
    </server>
</servers>
```

### Step 3: Deploy to GitHub Packages

```bash
mvn clean deploy
```

### Step 4: Usage

Users can consume your package by adding this to their `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/yourusername/logger-ttl</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.yourusername</groupId>
    <artifactId>logger-ttl</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🏗️ Option 3: JitPack (Easiest for GitHub Projects)

JitPack automatically builds and publishes your GitHub repository as a Maven dependency.

### Step 1: Ensure Your Project is on GitHub

Push your project to a public GitHub repository.

### Step 2: Create a Git Tag

```bash
git tag v1.0.0
git push origin v1.0.0
```

### Step 3: Build on JitPack

Visit https://jitpack.io and enter your repository URL. JitPack will automatically build your project.

### Step 4: Usage

Users can consume your package immediately:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.yourusername</groupId>
    <artifactId>logger-ttl</artifactId>
    <version>v1.0.0</version>
</dependency>
```

## 🏢 Option 4: Private/Corporate Repository

For private or corporate use, you can deploy to services like:

### Nexus Repository Manager

```xml
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <url>http://your-nexus-server/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <url>http://your-nexus-server/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### Artifactory

```xml
<distributionManagement>
    <repository>
        <id>artifactory-releases</id>
        <url>http://your-artifactory-server/artifactory/libs-release</url>
    </repository>
    <snapshotRepository>
        <id>artifactory-snapshots</id>
        <url>http://your-artifactory-server/artifactory/libs-snapshot</url>
    </snapshotRepository>
</distributionManagement>
```

## 🚀 Quick Start Commands

### For Maven Central:
```bash
# 1. Update pom.xml with your details
# 2. Set up GPG and Maven settings
# 3. Deploy
mvn clean deploy -P release
```

### For GitHub Packages:
```bash
# 1. Update pom.xml with GitHub repository
# 2. Configure GitHub token in settings.xml
# 3. Deploy
mvn clean deploy
```

### For JitPack:
```bash
# 1. Push to GitHub
git push origin main
# 2. Create and push tag
git tag v1.0.0
git push origin v1.0.0
# 3. Visit jitpack.io and build
```

## 📝 Best Practices

### Versioning
- Use semantic versioning (e.g., 1.0.0, 1.0.1, 1.1.0)
- Use SNAPSHOT for development versions (e.g., 1.1.0-SNAPSHOT)
- Create Git tags for releases

### Documentation
- Maintain comprehensive README.md
- Include usage examples
- Document breaking changes in changelog

### Quality Gates
- Ensure all tests pass before publishing
- Run static analysis tools
- Include source and javadoc JARs

### Security
- Sign your artifacts with GPG
- Use secure authentication tokens
- Regularly rotate credentials

## 🔍 Verification

After publishing, verify your artifact:

### Maven Central
```bash
# Search for your artifact
curl "https://search.maven.org/solrsearch/select?q=g:io.github.yourusername+AND+a:logger-ttl"
```

### GitHub Packages
Check your repository's "Packages" tab on GitHub.

### JitPack
Visit `https://jitpack.io/com/github/yourusername/logger-ttl/v1.0.0/` to see build status.

## ❗ Troubleshooting

### Common Issues

1. **GPG signing fails**: Ensure GPG is installed and key is available
2. **Authentication errors**: Verify credentials in settings.xml
3. **Build failures**: Check that all tests pass locally
4. **Missing metadata**: Ensure pom.xml has all required fields for Maven Central

### Getting Help

- **Maven Central**: https://central.sonatype.org/
- **GitHub Packages**: https://docs.github.com/en/packages
- **JitPack**: https://jitpack.io/docs/
- **Stack Overflow**: Search for Maven deployment issues

## 📚 Additional Resources

- [Maven Central Guide](https://central.sonatype.org/publish/publish-guide/)
- [GitHub Packages Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
- [JitPack Documentation](https://jitpack.io/docs/)
- [Maven Deploy Plugin](https://maven.apache.org/plugins/maven-deploy-plugin/)
