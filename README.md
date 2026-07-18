# 🌾 AI Farmer Assistant Platform

> A production-ready, full-stack AI-powered farming companion for Indian farmers, built with **Java Spring Boot**, **React.js**, **MySQL**, **Google Gemini AI**, **OpenWeatherMap**, and **HuggingFace**.

---

## 🚀 Features

| Feature | Description |
|---------|-------------|
| 🔐 **JWT Authentication** | Signup, Login, OTP Password Reset, Email Verify, Role-based access |
| 🌾 **AI Crop Recommendation** | NPK/climate analysis → best crop + Gemini AI explanation |
| ☁️ **Weather & Forecasts** | GPS + manual city search, 5-day forecast, AI farming advice |
| 🦠 **Disease Detection** | Image upload → HuggingFace plant model + Gemini treatment advice |
| 📈 **Market Prices** | Live mandi data from data.gov.in + 30-day trend charts + AI sell advice |
| 📋 **Government Schemes** | Browse, search, filter, and bookmark PM agricultural schemes |
| 🤖 **KisanAI Chatbot** | Multilingual (EN/HI/MR) conversational AI with image analysis |
| 🔔 **Notifications** | User & broadcast notifications with read/unread tracking |
| 👤 **User Profile** | Edit profile, upload avatar (S3), farmer detail management |
| 🛡️ **Admin Panel** | Scheme management, broadcast notifications, platform overview |

---

## 🏗️ Architecture

```
AI-Farmer-Assistant/
├── frontend/                   # React 18 + Vite + Tailwind CSS
│   ├── src/
│   │   ├── pages/              # Auth, Dashboard, Crop, Weather, Disease, Market, Schemes, Chat, Profile, Admin
│   │   ├── components/         # Layout, Spinner, shared UI components
│   │   ├── services/           # Axios API modules for all backend endpoints
│   │   └── store/              # Zustand state: auth, theme, notifications
│   ├── Dockerfile
│   └── nginx.conf
│
├── backend/                    # Spring Boot 3.2 + Java 21
│   ├── controller/             # REST endpoints for all features
│   ├── service/                # Business logic + AI integrations
│   ├── repository/             # JPA repositories
│   ├── model/                  # JPA entities (11 tables)
│   ├── dto/                    # Request/Response DTOs
│   ├── security/               # JWT + Spring Security
│   ├── config/                 # S3, Email, Cache, OpenAPI, CORS
│   ├── exception/              # Global exception handling
│   └── Dockerfile
│
├── database/
│   ├── schema.sql              # 11 tables with indexes and constraints
│   └── seed.sql                # Sample data (schemes, users, prices)
│
├── docker-compose.yml          # Full stack: MySQL + Backend + Frontend
├── .env.example                # API key template
└── README.md
```

---

## 🛠️ Tech Stack

### Backend
- **Java 21** + **Spring Boot 3.2**
- **Spring Security** + **JWT** (stateless auth)
- **Spring Data JPA** + **MySQL 8.0**
- **Spring Mail** (Gmail SMTP / OTP)
- **AWS SDK v2** (S3 image upload)
- **Redis** (optional caching)
- **Springdoc OpenAPI** (Swagger UI at `/swagger-ui.html`)

### Frontend
- **React 18** + **Vite**
- **Tailwind CSS 3** (custom green agriculture theme)
- **Zustand** (state management)
- **Recharts** (charts — radar, area, line, bar)
- **Axios** (API client with JWT interceptor + auto-refresh)
- **React Router 6** (lazy-loaded routes)
- **React Dropzone** (image upload)
- **Lucide React** (icons)
- **React Hot Toast** (notifications)

### AI Integrations
- **Google Gemini Flash** — crop advice, weather analysis, disease explanation, market advice, chatbot
- **HuggingFace** — `linkanjarad/plant-disease-47-classes` plant disease model
- **OpenWeatherMap** — current weather + 5-day forecast

### Data Sources
- **data.gov.in** — live mandi/market prices
- **Government scheme database** — seeded with real Indian agricultural schemes

---

## 🔧 Setup & Running

### Prerequisites
- Java 21+
- Node.js 20+
- MySQL 8.0+
- Docker & Docker Compose (recommended)

---

### 🐳 Option 1: Docker Compose (Recommended)

```bash
# 1. Clone and enter directory
git clone <repo-url>
cd AI-Farmer-Assistant

# 2. Create environment file
cp .env.example .env
# Edit .env with your API keys

# 3. Start all services
docker-compose up --build

# Access:
#   Frontend:  http://localhost
#   Backend:   http://localhost:8080
#   Swagger:   http://localhost:8080/swagger-ui.html
```

---

### 💻 Option 2: Local Development

#### Backend

```bash
cd backend

# Create MySQL database
mysql -u root -p -e "CREATE DATABASE farmer_assistant;"

# Configure application.yml with your settings
# (or set environment variables from .env.example)

# Run Spring Boot
./mvnw spring-boot:run
```

#### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start development server (proxies /api to localhost:8080)
npm run dev
# Open http://localhost:5173
```

---

## 🔑 Required API Keys

| Key | Where to Get |
|-----|-------------|
| `GEMINI_API_KEY` | [Google AI Studio](https://aistudio.google.com/app/apikey) |
| `OPENWEATHER_API_KEY` | [OpenWeatherMap](https://home.openweathermap.org/api_keys) |
| `HUGGINGFACE_API_KEY` | [HuggingFace Settings](https://huggingface.co/settings/tokens) |
| `AWS_ACCESS_KEY` + `AWS_SECRET_KEY` | [AWS IAM Console](https://console.aws.amazon.com/iam/) |
| `DATA_GOV_IN_API_KEY` | [data.gov.in](https://data.gov.in/user/register) |
| Gmail App Password | [Google Account → Security → App Passwords](https://myaccount.google.com/apppasswords) |

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT tokens |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/forgot-password` | Send OTP to email |
| POST | `/api/auth/reset-password` | Reset password with OTP |
| GET | `/api/auth/me` | Get current user |
| POST | `/api/crops/recommend` | AI crop recommendation |
| GET | `/api/crops/history` | Recommendation history |
| GET | `/api/weather` | Weather by coordinates |
| GET | `/api/weather/city` | Weather by city name |
| POST | `/api/disease/detect` | Upload image for disease detection |
| GET | `/api/market/prices` | Live mandi prices |
| GET | `/api/market/trend` | 30-day price trend |
| GET | `/api/market/advice` | AI sell timing advice |
| GET | `/api/schemes` | All government schemes |
| GET | `/api/schemes/search` | Search schemes |
| POST | `/api/schemes/{id}/bookmark` | Toggle bookmark |
| POST | `/api/chat/message` | Send message to KisanAI |
| GET | `/api/chat/sessions` | Get chat sessions |
| GET | `/api/notifications` | Get user notifications |
| PATCH | `/api/notifications/read-all` | Mark all as read |
| GET | `/api/profile` | Get user profile |
| PUT | `/api/profile` | Update profile + image |
| GET | `/api/profile/dashboard` | Dashboard summary |

> Full interactive API docs at: `http://localhost:8080/swagger-ui.html`

---

## 🌐 AWS Deployment Guide

### Infrastructure

```
AWS Architecture:
  Route 53 → ALB → EC2 (Spring Boot) → RDS (MySQL)
                 → S3 (Static React Build + Image Uploads)
                 → ElastiCache (Redis for caching)
```

### Steps

1. **RDS** — Create MySQL 8.0 instance, note endpoint
2. **S3** — Create bucket for images + CloudFront distribution
3. **EC2** — Launch t3.small/medium, install Java 21, deploy backend JAR
4. **ALB** — Create Application Load Balancer with target group
5. **Route 53** — Point domain to ALB
6. **SSL** — Use ACM for HTTPS certificate

```bash
# Build backend for deployment
cd backend && mvn clean package -DskipTests
# Upload farmer-assistant-*.jar to EC2

# Build frontend for S3
cd frontend && npm run build
# Upload dist/ contents to S3 bucket, enable static website hosting
```

---

## 🎨 UI Design System

The frontend uses a **custom green agriculture theme** built on Tailwind CSS:

- **Primary colors** — Green palette (`primary-50` to `primary-950`)
- **Earth tones** — Amber/earth palette for market & schemes
- **Glassmorphism** — Frosted glass cards with backdrop blur
- **Dark mode** — Full dark theme support
- **Animations** — `fade-in`, `slide-up`, `float`, `shimmer`, `bounce`
- **Typography** — Inter (body) + Poppins (headings)

---

## 🔐 Security Features

- **BCrypt** password hashing (strength 12)
- **JWT** access tokens (24h) + refresh tokens (7d)
- **CORS** configured for production origins
- **Role-based** access control (`FARMER`, `OFFICER`, `ADMIN`)
- **Rate limiting** ready (add Spring Boot actuator + Bucket4j)
- **Input validation** on all DTOs
- **SQL injection** protection via JPA parameterized queries
- **XSS** protection via Spring Security headers

---

## 👥 User Roles

| Role | Capabilities |
|------|-------------|
| `FARMER` | All features, chat, crop advice, disease detection, schemes, market |
| `OFFICER` | Same as farmer + additional access for official duties |
| `ADMIN` | Everything + manage schemes, broadcast notifications |

---

## 📦 Seeded Demo Data

After running `seed.sql`, the following accounts are available:

| Email | Password | Role |
|-------|----------|------|
| `admin@farmerassist.in` | `Admin@123` | ADMIN |
| `officer@farmerassist.in` | `Officer@123` | OFFICER |
| `farmer@farmerassist.in` | `Farmer@123` | FARMER |

---

## 📄 License

This project is built for educational/portfolio purposes as part of the IBM Skills Build program.

---

*Built with ❤️ for Indian Farmers — Jai Kisan! 🌾*
