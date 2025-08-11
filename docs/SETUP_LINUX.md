# Linux Setup (Ubuntu/Debian)

## Install Java 17 (JDK)
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
java -version   # should print 17.x
```

## Install Docker + Docker Compose v2
```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release; echo $VERSION_CODENAME) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# run docker without sudo (log out/in or new shell afterwards)
sudo usermod -aG docker $USER
newgrp docker

# verify
docker --version
docker compose version
```

## Gradle
This project uses the Gradle Wrapper (`./gradlew`), so no system Gradle install is required.

## Run the project
```bash
# this will run clean build test bootJar
./gradlew all
docker-compose up --build
```
Then open `http://localhost:8080/ping`. 


## Troubleshooting:

ensure docker-compose is present:

```
shell> docker-compose up --build
Command 'docker-compose' not found, but can be installed with:..

# yet docker compose is present:
shell> docker compose version
Docker Compose version v2.18.1
# you are hitting old vs new compose comand, create an alias:
shel> alias docker-compose='docker compose'
```