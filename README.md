# Heterogeneous Database Social Network Application

A full-stack social networking application demonstrating the integration and orchestration of multiple heterogeneous database technologies to handle different aspects of social media functionality.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Features](#features)
- [Development](#development)
- [License](#license)

## 🎯 Overview

This project demonstrates a modern approach to building scalable social network applications by leveraging the strengths of different database technologies. Each database type is chosen based on its optimal use case:

- **MySQL** for structured user authentication data
- **Neo4j** for graph-based social relationships
- **MongoDB** for flexible document-based posts and comments
- **HBase** for high-throughput timeline caching

The application consists of two main components:
1. **Frontend**: Node.js/Express server with an AngularJS single-page application
2. **Backend**: Java-based REST API using Undertow web server

## 🏗️ Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (SN2)                          │
│    Node.js + Express + AngularJS + Bootstrap                │
│                 (Port: Default)                             │
└─────────────────┬───────────────────────────────────────────┘
                  │ HTTP Requests
                  │
┌─────────────────▼───────────────────────────────────────────┐
│              Backend (social_network_backend)               │
│         Java Servlets + Undertow Server                     │
│                 (Port: 80)                                  │
└─────┬─────────┬──────────┬──────────┬─────────────────────┘
      │         │          │          │
      ▼         ▼          ▼          ▼
  ┌────────┐┌────────┐┌─────────┐┌──────────┐
  │ MySQL  ││ Neo4j  ││ MongoDB ││  HBase   │
  │ (Auth) ││(Social)││ (Posts) ││ (Cache)  │
  └────────┘└────────┘└─────────┘└──────────┘
```

### Component Responsibilities

#### Frontend (SN2)
- **Technology**: Node.js, Express 4.x, AngularJS
- **Purpose**: Serves the web interface and proxies requests to the Java backend
- **Key Features**:
  - User authentication interface
  - Profile viewing
  - Follower management
  - Timeline and post viewing
  - Comment display

#### Backend (social_network_backend)
- **Technology**: Java 8, Undertow 2.x, Maven
- **Purpose**: Handles business logic and database operations
- **Servlets**:
  - `ProfileServlet` (Task 1): User authentication and profile retrieval
  - `FollowerServlet` (Task 2): Social graph queries for followers
  - `HomepageServlet` (Task 3): User's authored comments
  - `TimelineServlet` (Task 4): Aggregated timeline from multiple databases
  - `TimelineWithCacheServlet` (Task 5): Cached timeline with HBase

## 🛠️ Technologies Used

### Frontend Stack
- **Node.js**: JavaScript runtime
- **Express 4.13.x**: Web application framework
- **AngularJS**: Frontend MVC framework
- **Bootstrap 3.x**: UI framework
- **Jade**: Template engine
- **Bower**: Frontend package management

### Backend Stack
- **Java 8**: Programming language
- **Maven**: Build and dependency management
- **Undertow 2.0**: Lightweight web server
- **JDBC**: Database connectivity for MySQL
- **Gson**: JSON serialization/deserialization

### Database Technologies

#### 1. MySQL
- **Use Case**: User authentication and profile storage
- **Version**: 8.0.18
- **Driver**: MySQL Connector/J
- **Schema**: `reddit_db` database with user credentials and profiles

#### 2. Neo4j
- **Use Case**: Social graph relationships (followers/following)
- **Version**: Compatible with Neo4j Java Driver 1.6.3
- **Type**: Graph database for relationship queries
- **Query Language**: Cypher

#### 3. MongoDB
- **Use Case**: Posts and comments storage
- **Version**: Compatible with Mongo Java Driver 3.6.3
- **Type**: Document database for flexible content storage
- **Database**: `reddit_db` with `posts` collection

#### 4. Apache HBase
- **Use Case**: Timeline caching for high-throughput reads
- **Version**: 1.4.1
- **Type**: Wide-column store for distributed caching
- **Integration**: Google Cloud Bigtable compatibility

### Additional Technologies
- **Apache Hadoop**: 3.0.0 (for HBase compatibility)
- **Jackson**: JSON processing
- **Mockito**: Unit testing framework
- **JUnit**: Testing framework

## 💾 Database Schema

### MySQL (reddit_db)
Stores user authentication and profile information:
- User credentials (username, password)
- Profile metadata (name, profile image URL)

### Neo4j Graph
Stores social relationships:
- Nodes: Users
- Relationships: FOLLOWS edges between users
- Properties: User names and profile images

### MongoDB (reddit_db.posts)
Document structure for posts and comments:
```json
{
  "author": "username",
  "ups": 100,
  "timestamp": "YYYY-MM-DD HH:MM:SS",
  "content": "Post or comment content",
  "parent_id": "parent_post_id",
  "profile": "profile_image_url"
}
```

### HBase
Caches user timelines for improved read performance:
- Row Key: User ID
- Column Family: Timeline data
- Cells: Cached timeline JSON

## 📁 Project Structure

```
het-db/
├── SN2/                          # Frontend Node.js application
│   ├── app.js                    # Express application configuration
│   ├── package.json              # Node.js dependencies
│   ├── bower.json                # Frontend dependencies
│   ├── routes/                   # Express route handlers
│   │   ├── index.js              # Main routes (tasks 1-5)
│   │   ├── proxy.js              # Backend API proxy
│   │   └── backend.js            # Backend communication
│   ├── public/                   # Static assets
│   │   ├── index.html            # Main SPA page
│   │   ├── js/                   # AngularJS application
│   │   │   ├── app.js            # Angular app config
│   │   │   ├── controllers.js   # Angular controllers
│   │   │   ├── services.js      # Angular services
│   │   │   └── ...
│   │   ├── css/                  # Stylesheets
│   │   └── partials/             # Angular templates
│   └── views/                    # Jade templates
│
└── social_network_backend/       # Backend Java application
    ├── pom.xml                   # Maven configuration
    └── src/
        ├── main/
        │   ├── java/edu/cmu/cc/minisite/
        │   │   ├── MiniSite.java            # Main server entry point
        │   │   ├── ProfileServlet.java      # Task 1: Authentication (MySQL)
        │   │   ├── FollowerServlet.java     # Task 2: Followers (Neo4j)
        │   │   ├── HomepageServlet.java     # Task 3: Comments (MongoDB)
        │   │   ├── TimelineServlet.java     # Task 4: Timeline (Multi-DB)
        │   │   ├── TimelineWithCacheServlet.java  # Task 5: Cached (HBase)
        │   │   └── Cache.java               # Caching utilities
        │   └── resources/
        └── test/                 # Test files
```

## ✅ Prerequisites

Before you begin, ensure you have the following installed:

### Required Software
- **Java Development Kit (JDK) 8 or higher**
  ```bash
  java -version  # Should show 1.8 or higher
  ```

- **Apache Maven 3.x**
  ```bash
  mvn -version
  ```

- **Node.js 4.x or higher and npm**
  ```bash
  node --version
  npm --version
  ```

- **Bower**
  ```bash
  npm install -g bower
  ```

### Required Database Services
You need access to the following database instances:

1. **MySQL Server 5.7+**
   - Running on default port 3306 or custom port
   - Database: `reddit_db`

2. **Neo4j Server 3.x+**
   - Running on default port 7687
   - Authentication enabled

3. **MongoDB Server 3.x+**
   - Running on default port 27017
   - Database: `reddit_db`

4. **Apache HBase 1.4+** or **Google Cloud Bigtable**
   - Properly configured and accessible

## 📥 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/vismithaN/het-db.git
cd het-db
```

### 2. Install Frontend Dependencies

```bash
cd SN2
npm install
bower install
cd ..
```

### 3. Install Backend Dependencies

```bash
cd social_network_backend
mvn clean install
cd ..
```

## ⚙️ Configuration

### Backend Configuration

The backend uses environment variables for database credentials. Set the following before running:

```bash
# MySQL Configuration
export MYSQL_HOST="your-mysql-host:3306"
export MYSQL_NAME="your-mysql-username"
export MYSQL_PWD="your-mysql-password"

# Neo4j Configuration
export NEO4J_HOST="bolt://your-neo4j-host:7687"
export NEO4J_NAME="neo4j"
export NEO4J_PWD="your-neo4j-password"

# MongoDB Configuration
export MONGO_HOST="your-mongodb-host"

# HBase Configuration (if applicable)
# Configure HBase connection settings in your HBase configuration files
```

### Frontend Configuration

Update the backend URL in `SN2/routes/proxy.js` or `SN2/routes/backend.js` if your backend is not running on localhost:

```javascript
var backend_url = 'http://localhost/MiniSite';
```

## 🚀 Running the Application

### Start the Backend Server

1. Navigate to the backend directory:
   ```bash
   cd social_network_backend
   ```

2. Set environment variables (see Configuration section)

3. Run the server:
   ```bash
   mvn clean package exec:java
   ```

   The backend server will start on port 80 at path `/MiniSite`

### Start the Frontend Server

1. In a new terminal, navigate to the frontend directory:
   ```bash
   cd SN2
   ```

2. Start the Node.js server:
   ```bash
   npm start
   ```

   The frontend server will start on the default port (usually 3000)

3. Access the application:
   ```
   http://localhost:3000
   ```

## 🔌 API Endpoints

The backend exposes the following REST endpoints under `/MiniSite`:

### Task 1: User Authentication (MySQL)
```
GET /MiniSite/task1?id={userId}&password={password}
```
**Response:**
```json
{
  "name": "John Doe",
  "profile": "http://example.com/profile.jpg"
}
```

### Task 2: Get Followers (Neo4j)
```
GET /MiniSite/task2?id={userId}
```
**Response:**
```json
{
  "name": "John Doe",
  "profile": "http://example.com/profile.jpg",
  "followers": [
    {
      "name": "Jane Smith",
      "profile": "http://example.com/jane.jpg"
    }
  ]
}
```

### Task 3: Get User Comments (MongoDB)
```
GET /MiniSite/task3?id={userId}
```
**Response:**
```json
{
  "name": "John Doe",
  "profile": "http://example.com/profile.jpg",
  "posts": [
    {
      "pid": "post123",
      "uid": "user456",
      "name": "John Doe",
      "profile": "http://example.com/profile.jpg",
      "timestamp": "2024-01-01 12:00:00",
      "content": "This is a comment",
      "comments": []
    }
  ]
}
```

### Task 4: Get User Timeline (Multi-Database)
```
GET /MiniSite/task4?id={userId}
```
**Response:** Aggregated data from MySQL, Neo4j, and MongoDB
```json
{
  "name": "John Doe",
  "profile": "http://example.com/profile.jpg",
  "followers": [...],
  "posts": [...]
}
```

### Task 5: Get Cached Timeline (HBase)
```
GET /MiniSite/task5?id={userId}
```
**Response:** Similar to Task 4 but served from HBase cache for improved performance

### Health Check
```
GET /heartbeat
```
**Response:**
```json
{
  "url": "default_profile_url"
}
```

## ✨ Features

### Implemented Features

1. **User Authentication**
   - MySQL-based credential validation
   - Profile information retrieval

2. **Social Graph Management**
   - Neo4j-powered follower relationships
   - Alphabetically sorted follower lists

3. **Content Management**
   - MongoDB storage for posts and comments
   - Hierarchical comment threading
   - Sorting by popularity (ups) and timestamp

4. **Timeline Aggregation**
   - Fan-out queries across multiple databases
   - Retrieval of followees' popular content
   - Real-time data aggregation

5. **Performance Optimization**
   - HBase-based timeline caching
   - Reduced database query load
   - Improved read performance

6. **Responsive UI**
   - Bootstrap-based responsive design
   - AngularJS single-page application
   - Real-time updates

## 🔧 Development

### Running Tests

#### Backend Tests
```bash
cd social_network_backend
mvn test
```

#### Frontend Tests
```bash
cd SN2
npm test
```

### Building for Production

#### Backend
```bash
cd social_network_backend
mvn clean package
```

The compiled JAR will be in the `target/` directory.

#### Frontend
```bash
cd SN2
npm run build  # If build script is configured
```

### Code Style

- **Java**: Follows standard Java conventions
- **JavaScript**: Uses ES5 syntax for broad compatibility
- **Indentation**: Spaces (2 spaces for JavaScript, 4 for Java)

## 📝 Notes

- This application is designed for educational purposes to demonstrate heterogeneous database integration
- The project structure reflects a typical course project layout (Tasks 1-5)
- Security credentials should never be hardcoded; always use environment variables
- For production deployment, additional security measures (HTTPS, authentication tokens, etc.) should be implemented
- Database schemas should be initialized with appropriate sample data before running the application

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is part of an educational assignment. Please check with the original course or institution for licensing information.

## 🙏 Acknowledgments

- Carnegie Mellon University (based on package names and structure)
- Cloud Computing course materials
- Open source database communities (MySQL, MongoDB, Neo4j, HBase)

---

**Project Repository**: [https://github.com/vismithaN/het-db](https://github.com/vismithaN/het-db)

For questions or issues, please open an issue on the GitHub repository.
