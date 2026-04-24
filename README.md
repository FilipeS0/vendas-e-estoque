# Modern Retail ERP System

A robust, full-stack Enterprise Resource Planning (ERP) system designed specifically for retail operations and Point of Sale (POS) integrations. This project is built using Domain-Driven Design (DDD) principles and leverages a modern technology stack to deliver a secure, performant, and reactive user experience.

## 🚀 Technology Stack

### Backend
- **Java 21**
- **Spring Boot 4.0+**
- **Spring Security** (Stateless JWT Authentication)
- **Spring Data JPA** (Hibernate)
- **PostgreSQL 15**
- **Flyway** (Database Migrations)
- **Lombok**

### Frontend
- **Angular 21+** (Standalone Components, modern `@if` / `@for` control flow)
- **Angular Material** (Premium, reactive UI components)
- **RxJS** (State management, debouncing)

### Infrastructure
- **Docker & Docker Compose** (Multi-stage builds, isolated environments)

---

## 🌟 Key Features

- **Robust Security**: Stateless JWT-based authentication with Spring Security. Protected API endpoints and Angular Route Guards.
- **Product Management**: 
  - Complete CRUD lifecycle for Products.
  - Strict validations (EAN/Barcode uniqueness, NCM formatting, positive pricing).
  - Native Backend Pagination using Spring's `Pageable`.
  - Frontend filtering with RxJS Debouncing for high-performance searches.
  - **Soft Deletes**: Products are inactivated instead of dropped from the database to preserve historical sales records.
- **Automated Provisioning**: Built-in `DataSeeder` automatically injects default categories, suppliers, and an Admin user on startup.
- **Open Source Ready**: Sensitive credentials are automatically excluded via `.gitignore` and `.env` configuration. Dummy fallback keys are used to prevent accidental credential leaks.

---

## 🛠️ Getting Started

Follow these steps to run the application locally for development or testing.

### 1. Prerequisites
- [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/)
- Node.js (v18+) & npm
- JDK 21+ (Optional, for running outside of Docker)

### 2. Environment Configuration
Create a `.env` file in the root directory based on the following template.

### 3. Run the Backend & Database (via Docker)
Start the PostgreSQL database and the Spring Boot API using Docker Compose:

```bash
docker compose up --build
```
The backend API will be available at `http://localhost:8080`.
*Note: Flyway migrations and the Data Seeder will run automatically on startup.*

### 4. Run the Frontend
Open a new terminal, navigate to the `app` directory, and start the Angular development server:

```bash
cd app
npm install
npm run start
```
The frontend application will be available at `http://localhost:4200`.

---

## 🔐 Default Credentials

Because the application uses an automated `DataSeeder`, you can log into the system immediately using the following Admin credentials:

- **Email:** `admin@erp.com`
- **Password:** `admin123`

---

## 🤝 Contributing

This project is open-source! Feel free to submit Pull Requests to add new features, improve the UI, or fix bugs. Ensure that you do not commit any sensitive `.env` files or hardcode real passwords into the codebase.

## 📝 License

This project is licensed under the MIT License.
