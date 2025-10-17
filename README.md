# 🚗 RouteLink (RideShare Connect)
**_Drive • Share • Save_**

[![Spring Boot](https://img.shields.io/badge/Backend-SpringBoot-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/Frontend-React-blue?logo=react)](https://react.dev/)
[![TailwindCSS](https://img.shields.io/badge/UI-TailwindCSS-38b2ac?logo=tailwind-css)](https://tailwindcss.com/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169e1?logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🌟 Overview

**RouteLink** is a carpooling and ride-sharing platform that connects drivers and riders traveling along similar routes.  
It helps reduce travel costs, optimize trips, and promote eco-friendly commuting.

### Key Features
- 🚘 **Drivers:** Post trips with available seats, route, and pricing.  
- 🧳 **Riders:** Search and book rides along or near the same route.  
- 🔔 Real-time trip updates, confirmations, and cancellations.  
- 🔐 Secure **JWT-based authentication** and verified accounts.  
- 📧 Automated **email notifications** for trip updates.  
- ⭐ Two-way **rating system** for riders and drivers.

---

## 🏗️ Architecture

| Layer | Technology |
|-------|-------------|
| **Backend** | Spring Boot (Java 17) |
| **Database** | PostgreSQL |
| **Frontend** | React + Tailwind CSS |
| **APIs** | RESTful Services (JWT Secured) |
| **Integration** | Google Maps API for route mapping and location search |

---

## ⚙️ Modules

| Module | Description |
|---------|--------------|
| **User Module** | Registration, login, profile, verification, and rating |
| **Trip Module** | Create, update, delete, and search trips (with along-the-way detection) |
| **Booking Module** | Seat booking, status updates, and notifications |
| **Rating Module** | Driver and rider feedback system |
| **Auth Module** | JWT token creation, validation, and secure access control |

---

## 📂 Project Structure
```bash
routelink/
├── backend/                  # Spring Boot backend
│   ├── src/main/java/com/routelink/
│   └── pom.xml
│
├── routelink-frontend/       # React + Tailwind frontend
│   ├── src/
│   └── package.json
│
└── README.md


---

## 🚀 Getting Started

### 1️⃣ Run the Backend
```bash
cd backend
mvn spring-boot:run

cd routelink-frontend
npm install
npm start

Future Enhancements

🤝 Pair-up matching when multiple riders book the same trip.

🧭 Dynamic route pricing based on distance and waypoints.

🚦 Driver path visualization with live map updates.

💬 In-app chat between riders and drivers.

📱 Native mobile app using React Native.

