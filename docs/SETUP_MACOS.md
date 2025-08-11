# macOS Setup

## Install Java 17 (JDK)
Using Homebrew:
```bash
brew install openjdk@17
/usr/libexec/java_home -V
java -version   # should print 17.x
```
Alternative downloads:
- Temurin 17 (Adoptium): `https://adoptium.net`
- Oracle JDK: `https://www.oracle.com/java/technologies/downloads/`

## Install Docker Desktop
Download and install:
- `https://www.docker.com/products/docker-desktop/`

Verify:
```bash
docker --version
docker compose version
```

## Gradle
This project uses the Gradle Wrapper (`./gradlew`), so no system Gradle install is required.

## Run the project
```bash
./gradlew bootJar
docker-compose up --build
```
Then open `http://localhost:8080`. 