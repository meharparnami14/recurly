# Learning Document: Restructuring for Render Deployment

## Overview
This document explains all the changes made to restructure the Recurly subscription management platform for deployment on Render. The original project had separate frontend and backend folders, which needed to be consolidated into a single deployable unit.

---

## Original Project Structure

```
recurly/
├── frontend/
│   ├── index.html
│   ├── dashboard.html
│   ├── add-subscription.html
│   ├── confirm-payment.html
│   ├── history.html
│   ├── tracking.html
│   ├── success.html
│   ├── style.css
│   ├── dashboard.css
│   └── assets/
│       ├── lp1.jpg
│       ├── lp2.png
│       └── lp3.jpg
└── backend/
    ├── src/
    │   ├── main/
    │   │   ├── java/com/recurly/
    │   │   └── resources/
    │   │       └── application.properties
    ├── Dockerfile
    └── pom.xml
```

---

## New Project Structure

```
recurly/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/recurly/
│   │   │   │   ├── BackendApplication.java
│   │   │   │   ├── Subscription.java
│   │   │   │   ├── SubscriptionController.java
│   │   │   │   ├── SubscriptionRepository.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   └── PaymentRepository.java
│   │   │   └── resources/
│   │   │       ├── static/          # ← FRONTEND MOVED HERE
│   │   │       │   ├── index.html
│   │   │       │   ├── dashboard.html
│   │   │       │   ├── add-subscription.html
│   │   │       │   ├── confirm-payment.html
│   │   │       │   ├── history.html
│   │   │       │   ├── tracking.html
│   │   │       │   ├── success.html
│   │   │       │   ├── style.css
│   │   │       │   ├── dashboard.css
│   │   │       │   └── assets/
│   │   │       │       ├── lp1.jpg
│   │   │       │       ├── lp2.png
│   │   │       │       └── lp3.jpg
│   │   │       └── application.properties
│   ├── Dockerfile
│   └── pom.xml
├── frontend/          # ← OLD FOLDER (kept for reference)
├── .gitignore         # ← NEW FILE
├── render.yaml        # ← NEW FILE
└── README.md          # ← NEW FILE
```

---

## Changes Made and Why

### 1. **Moved Frontend to Backend Static Folder**

**What Changed:**
- Copied all files from `frontend/` to `backend/src/main/resources/static/`

**Why:**
- **Spring Boot Convention**: Spring Boot automatically serves any files in the `src/main/resources/static/` folder as static content
- **Single Deployable Unit**: Instead of deploying frontend and backend separately, we now have one unified application
- **Simplified Deployment**: Render only needs to build and deploy one Docker container
- **No CORS Issues**: Since frontend and backend are served from the same origin, we avoid cross-origin complications
- **Cost Effective**: Running one service instead of two on Render (important for free tier)

**How It Works:**
```
When user visits: https://your-app.onrender.com/
Spring Boot serves: src/main/resources/static/index.html

When user visits: https://your-app.onrender.com/dashboard.html
Spring Boot serves: src/main/resources/static/dashboard.html

When JavaScript makes API call to: /subscriptions
Spring Boot routes to: SubscriptionController
```

---

### 2. **Updated Application Properties**

**File:** `backend/src/main/resources/application.properties`

#### Change 2.1: Dynamic Port Configuration
**Before:**
```properties
server.port=8080
```

**After:**
```properties
server.port=${PORT:8080}
```

**Why:**
- **Render Requirement**: Render assigns a random port via the `PORT` environment variable
- **Flexibility**: `${PORT:8080}` means "use PORT env variable if available, otherwise default to 8080"
- **Local Development**: Still works on port 8080 when running locally (no PORT variable set)
- **Production**: Automatically uses Render's assigned port in production

#### Change 2.2: Database Configuration
**Before:**
```properties
spring.datasource.url=jdbc:h2:mem:recurlydb
```

**After:**
```properties
spring.datasource.url=jdbc:h2:file:./data/recurlydb;DB_CLOSE_ON_EXIT=FALSE
```

**Why:**
- **In-Memory Problem**: `jdbc:h2:mem` stores data in RAM - lost when server restarts
- **File-Based Solution**: `jdbc:h2:file` stores data in a file on disk
- **Data Persistence**: Subscriptions and payments survive server restarts
- **DB_CLOSE_ON_EXIT=FALSE**: Prevents database corruption during shutdowns
- **Path**: `./data/recurlydb` creates a `data` folder (ignored by git via .gitignore)

#### Change 2.3: H2 Console
**Before:**
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2
```

**After:**
```properties
spring.h2.console.enabled=false
```

**Why:**
- **Security**: H2 console is a database admin interface - shouldn't be exposed in production
- **Unnecessary**: Only needed during development to view database tables
- **Best Practice**: Disable debug/admin tools in production environments

---

### 3. **Added .gitignore File**

**File Created:** `.gitignore` (in root directory)

**Contents:**
```gitignore
# Compiled class files
*.class

# Package files
*.jar
*.war
*.ear

# Maven
backend/target/
target/

# IDE
.idea/
*.iml
.vscode/
*.swp

# OS
.DS_Store
Thumbs.db

# H2 Database files
*.db
data/

# Logs
*.log

# Misc
nul
```

**Why:**
- **Reduce Repo Size**: Build artifacts (`target/`) can be huge and are regenerated anyway
- **Security**: Database files shouldn't be committed (may contain sensitive data)
- **Clean History**: Prevents accidental commits of IDE settings, compiled files, OS files
- **Team Consistency**: Each developer can use their own IDE without conflicts
- **Best Practice**: Standard practice for Java/Maven projects

**What Gets Ignored:**
- `backend/target/` - Maven build output (*.class files, JARs)
- `data/` - H2 database files
- `*.class` - Compiled Java bytecode
- IDE files - IntelliJ (.idea), VS Code (.vscode), etc.

---

### 4. **Created render.yaml**

**File Created:** `render.yaml` (in root directory)

**Contents:**
```yaml
services:
  - type: web
    name: recurly-subscription-platform
    runtime: docker
    dockerfilePath: ./backend/Dockerfile
    dockerContext: ./backend
    envVars:
      - key: PORT
        value: 10000
      - key: JAVA_OPTS
        value: "-Xmx512m -Xms256m"
    healthCheckPath: /subscriptions
    plan: free
```

**Why Each Setting:**

| Setting | Value | Why |
|---------|-------|-----|
| `type: web` | web service | This is a web application (not a cron job or background worker) |
| `name` | recurly-subscription-platform | The name that appears in Render dashboard |
| `runtime: docker` | Use Docker | We have a Dockerfile, so use it to build the app |
| `dockerfilePath` | ./backend/Dockerfile | Where to find the Dockerfile |
| `dockerContext` | ./backend | Build context (where `pom.xml` and `src/` are located) |
| `PORT` | 10000 | Default port (Render may override this) |
| `JAVA_OPTS` | -Xmx512m -Xms256m | Memory limits: max 512MB, start 256MB (fits free tier) |
| `healthCheckPath` | /subscriptions | Render pings this endpoint to check if app is healthy |
| `plan: free` | Free tier | Use Render's free tier |

**Benefits of render.yaml:**
- **Blueprint Deployment**: One-click deploy from GitHub
- **Infrastructure as Code**: Deployment config is version controlled
- **Reproducible**: Anyone can deploy the same way
- **Documentation**: Config file documents deployment requirements

---

## Understanding the Dockerfile

**File:** `backend/Dockerfile`

```dockerfile
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

**Why Multi-Stage Build:**
1. **Stage 1 (Build)**: Uses full Maven + JDK (large image ~800MB)
   - Downloads dependencies
   - Compiles Java code
   - Packages into JAR file

2. **Stage 2 (Run)**: Uses only JRE (small image ~200MB)
   - Copies only the built JAR from stage 1
   - Doesn't include Maven, source code, or build tools
   - Final image is much smaller

**Benefits:**
- **Smaller Image**: Only ships what's needed to run (not build tools)
- **Faster Deploys**: Less to upload/download
- **Security**: Fewer tools = smaller attack surface
- **Cost**: Smaller images = less bandwidth/storage

---

## How It All Works Together

### Deployment Flow:

```
1. Push code to GitHub
   ↓
2. Render detects render.yaml
   ↓
3. Render reads Dockerfile location
   ↓
4. Render builds Docker image:
   - Downloads dependencies (mvn dependency:go-offline)
   - Compiles Java code
   - Packages JAR with static files included
   ↓
5. Render starts container:
   - Sets PORT environment variable
   - Runs: java -jar app.jar
   ↓
6. Spring Boot starts:
   - Reads application.properties
   - Uses PORT from environment
   - Initializes H2 database (./data/recurlydb)
   - Serves static files from classpath:/static/
   - Exposes REST API endpoints
   ↓
7. Application is live!
   - Frontend: https://your-app.onrender.com/
   - API: https://your-app.onrender.com/subscriptions
```

### Request Flow:

```
User Browser
    ↓
    ↓ (visits /)
    ↓
Spring Boot Server
    ↓
    ├─→ /index.html ────→ Serve from static/
    ├─→ /dashboard.html ─→ Serve from static/
    ├─→ /assets/lp1.jpg ─→ Serve from static/assets/
    ├─→ /subscriptions ──→ SubscriptionController (API)
    └─→ /payments ───────→ PaymentController (API)
```

---

## Key Concepts Learned

### 1. **Static Resources in Spring Boot**
- Files in `src/main/resources/static/` are served automatically
- No controller needed for static files
- Files are packaged into the JAR during build

### 2. **Environment Variables**
- `${VAR_NAME:default}` syntax in properties files
- Allows different configs for dev/prod without code changes
- Render injects `PORT` automatically

### 3. **H2 Database Modes**
- `mem:` - In-memory (fast, but lost on restart)
- `file:` - File-based (persistent, slower)
- Production apps usually use PostgreSQL/MySQL, but H2 works for demos

### 4. **Docker Multi-Stage Builds**
- Separate build and runtime stages
- Reduces final image size dramatically
- Best practice for compiled languages (Java, Go, C++)

### 5. **Monolithic Deployment**
- Single application serves both frontend and backend
- Simpler than microservices for small projects
- Easier to deploy and maintain

---

## What You Could Do Next

### For Production Use:
1. **Database**: Migrate from H2 to PostgreSQL (Render offers free tier)
2. **Security**: Add authentication/authorization (Spring Security)
3. **CORS**: Restrict `@CrossOrigin` to specific domains
4. **Logging**: Add proper logging framework (Logback)
5. **Monitoring**: Add health checks and metrics (Spring Actuator)

### For Learning:
1. **API Testing**: Add Postman collection or REST tests
2. **Frontend Framework**: Migrate to React/Vue/Angular
3. **CI/CD**: Add GitHub Actions for automated testing
4. **Containerization**: Learn Kubernetes for orchestration

---

## Troubleshooting Common Issues

### Issue: Port already in use
**Solution:** Change `server.port` in application.properties

### Issue: Static files not loading
**Solution:** Ensure files are in `src/main/resources/static/` (not `resources/public/`)

### Issue: Database resets on deploy
**Solution:** Use persistent disk on Render (paid feature) or migrate to PostgreSQL

### Issue: Build fails on Render
**Solution:** Check Dockerfile paths and ensure `dockerContext: ./backend` is correct

---

## Summary

The restructuring transformed a development setup into a production-ready application by:
1. Consolidating frontend and backend into a single deployable unit
2. Configuring dynamic port binding for cloud deployment
3. Enabling data persistence with file-based database
4. Adding proper version control practices (.gitignore)
5. Documenting infrastructure as code (render.yaml)

This setup is now ready for deployment on Render's free tier and follows industry best practices for Spring Boot applications.
