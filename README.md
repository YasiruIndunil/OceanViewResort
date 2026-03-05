# 🌊 Ocean View Resort – Room Reservation System

A full-stack Java web application for managing hotel reservations at Ocean View Resort, Galle, Sri Lanka.

**Module:** CIS6003 Advanced Programming | Cardiff Metropolitan University

---

## 🏗 Architecture

```
3-Tier Web Architecture
├── Presentation Layer   → HTML + CSS + JavaScript (AJAX)
├── Business Layer       → Java Servlets + DAO Pattern
└── Data Layer          → MySQL + Stored Procedures
```

**Design Patterns Used:**
- Singleton (DatabaseConnection)
- DAO (Data Access Object)
- Front Controller (Servlets)
- MVC (Model-View-Controller)

---

## ⚙ Tech Stack

| Layer        | Technology           |
|--------------|----------------------|
| Language     | Java 11              |
| Web          | Jakarta Servlets 5   |
| Database     | MySQL 8              |
| Build Tool   | Maven 3.x            |
| Testing      | JUnit 5 + Mockito    |
| CI/CD        | GitHub Actions       |
| Server       | Apache Tomcat 10     |

---

## 🚀 Setup Instructions

### Prerequisites
- Java JDK 11+
- Maven 3.8+
- MySQL 8+
- Apache Tomcat 10.x (or VS Code with Tomcat extension)

### 1. Database Setup
```sql
-- Run the SQL script
mysql -u root -p < sql/schema.sql
```

### 2. Configure Database
Edit `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/ocean_view_resort?useSSL=false&serverTimezone=UTC
db.username=your_username
db.password=your_password
```

### 3. Build the Project
```bash
mvn clean package
```

### 4. Deploy to Tomcat
Copy `target/OceanViewResort.war` to Tomcat's `webapps/` folder and start Tomcat.

### 5. Access the Application
Open: `http://localhost:8080/OceanViewResort`

**Default Login:**
- Username: `admin` | Password: `Admin@123`
- Username: `staff1` | Password: `Staff@123`

---

## 🧪 Running Tests
```bash
mvn test
mvn jacoco:report  # generates coverage report at target/site/jacoco/index.html
```

---

## 📁 Project Structure
```
OceanViewResort/
├── .github/workflows/    # GitHub Actions CI/CD
├── sql/                  # Database schema & stored procedures
├── src/
│   ├── main/
│   │   ├── java/com/oceanview/
│   │   │   ├── dao/      # Data Access Objects
│   │   │   ├── model/    # Domain models
│   │   │   ├── servlet/  # Web servlets
│   │   │   └── util/     # Utilities (DB, Validator)
│   │   ├── resources/    # db.properties
│   │   └── webapp/       # HTML, CSS, JS, WEB-INF
│   └── test/             # JUnit 5 test classes
└── pom.xml
```

---

## 📋 Features
- ✅ User Authentication (SHA-256 hashed passwords)
- ✅ Add New Reservations (with validation)
- ✅ View Reservation Details
- ✅ Display All Reservations (filterable)
- ✅ Generate & Print Bills (10% tax)
- ✅ Cancel Reservations
- ✅ Room Availability Management
- ✅ Help Guide for Staff
- ✅ REST API Endpoints
- ✅ Input Validation
- ✅ CI/CD with GitHub Actions
