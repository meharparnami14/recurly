# Recurly - Subscription Management Platform

A full-stack subscription management platform built with Spring Boot and vanilla HTML/CSS/JavaScript.

## Tech Stack

- **Backend**: Spring Boot 3.2.1 (Java 17)
- **Database**: H2 (file-based for persistence)
- **Frontend**: HTML, CSS, JavaScript
- **Build**: Maven
- **Deployment**: Docker on Render

## Project Structure

```
recurly/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/recurly/
│   │   │   │   ├── BackendApplication.java
│   │   │   │   ├── Subscription.java
│   │   │   │   ├── SubscriptionController.java
│   │   │   │   ├── Payment.java
│   │   │   │   └── PaymentController.java
│   │   │   └── resources/
│   │   │       ├── static/          # Frontend files
│   │   │       └── application.properties
│   ├── Dockerfile
│   └── pom.xml
├── .gitignore
└── render.yaml
```

## API Endpoints

### Subscriptions
- `GET /subscriptions` - Get all subscriptions
- `POST /subscriptions` - Add a new subscription

### Payments
- `GET /payments` - Get all payments
- `POST /payments` - Record a new payment
- `GET /payments/spending` - Get spending grouped by subscription

## Local Development

If you have Maven and Java 17 installed:

```bash
cd backend
mvn spring-boot:run
```

Visit `http://localhost:8080` to view the application.

## Deploy to Render

### Option 1: Using Blueprint (Recommended)

1. Push your code to GitHub:
   ```bash
   git add .
   git commit -m "Configure for Render deployment"
   git push
   ```

2. Go to [Render Dashboard](https://dashboard.render.com/)

3. Click **New** → **Blueprint**

4. Connect your GitHub repository

5. Render will detect `render.yaml` automatically

6. Click **Apply** to deploy

### Option 2: Manual Docker Deploy

1. Go to [Render Dashboard](https://dashboard.render.com/)

2. Click **New** → **Web Service**

3. Connect your GitHub repository

4. Configure:
   - **Name**: recurly-subscription-platform
   - **Runtime**: Docker
   - **Dockerfile Path**: ./backend/Dockerfile
   - **Docker Context**: ./backend

5. Add environment variable:
   - `PORT`: 10000

6. Click **Create Web Service**

## After Deployment

- Your app will be available at: `https://recurly-subscription-platform.onrender.com`
- The landing page is at the root path: `/`
- Dashboard: `/dashboard.html`

## Features

- Track all your subscriptions in one place
- Record and view payment history
- Visualize spending by subscription
- Add new subscriptions
- Monitor upcoming payments

## Notes

- The database is file-based H2, so data persists between restarts
- CORS is enabled for all origins (adjust in production if needed)
- Free tier on Render may spin down after inactivity
