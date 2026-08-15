# 🎬 Netflix-like Movie Streaming Management System

[![Java Version](https://img.shields.io/badge/Java-8-orange.svg?style=flat-square&logo=openjdk)](https://www.oracle.com/java/technologies/java8.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg?style=flat-square)]()
[![Code Style](https://img.shields.io/badge/Code%20Style-Google%20Java-blue.svg?style=flat-square)](checkstyle.xml)

An enterprise-grade, console-based **Movie Streaming Management System** built with **Pure Java 8**, strictly adhering to **Layered MVC Architecture**, **File-based I/O persistence (No Database)**, **GoF Design Patterns**, and **Zero External Runtime Frameworks**.

---

## 📑 Table of Contents
- [Key Architectural Highlights](#-key-architectural-highlights)
- [System Architecture Diagram](#-system-architecture-diagram)
- [Feature Matrix](#-feature-matrix)
- [Project Directory Structure](#-project-directory-structure)
- [Quickstart & Build Instructions](#-quickstart--build-instructions)
- [Docker Execution](#-docker-execution)
- [Testing & Code Quality](#-testing--code-quality)
- [Contributing & Commit Conventions](#-contributing--commit-conventions)

---

## 🌟 Key Architectural Highlights

- **Pure Java 8 Core**: No Spring, No Hibernate, No external runtime dependencies.
- **Layered MVC Architecture**: Complete decoupling across `model`, `repository`, `service`, `controller`, and `view`.
- **Atomic File Persistence**: File corruption prevention using OS-level atomic temporary file replacement (`.tmp` $\to$ atomic move).
- **In-Memory Inverted Index**: Instant $O(1) / O(K)$ search across Title, Actors, Director, and Genres.
- **Command Pattern**: Undo/Redo mechanism for Watchlist modifications with bounded dual-stack history (`BoundedDeque`).
- **Specification Pattern**: Dynamic multi-condition movie filtering powered by Java 8 `Predicate<Movie>` chaining.
- **Movie Ranking Engine**: Weighted multi-factor normalization formula ($w_r \times Rating + w_v \times \log(Views) + w_f \times Favs$) with Max-Heap Top-K extraction.
- **3-Tier Validation Framework**: Console input loop protection, Domain business rule validators, and resilient fail-safe CSV ingestion.

---

## 🏛️ System Architecture Diagram

```
+-------------------------------------------------------------------------------+
|                                  VIEW (CLI)                                   |
|   - ANSI Color Netflix Dark Theme          - Interactive Menus & Pagination   |
|   - Streaming Simulation Player (mm:ss)    - InputHelper (Strict Validation)  |
+-------------------------------------------------------------------------------+
                                       │
                                       ▼
+-------------------------------------------------------------------------------+
|                               CONTROLLER LAYER                                |
|   - MovieController      - CategoryController    - AuthController             |
|   - WatchlistController  - StreamingController   - AnalyticsController        |
+-------------------------------------------------------------------------------+
                                       │
                                       ▼
+-------------------------------------------------------------------------------+
|                                SERVICE LAYER                                  |
|   - MovieService         - CategoryService       - UserService / Session      |
|   - IndexingService      - SortingService        - RankingEngine (Max-Heap)   |
|   - FilterService        - WatchlistCommandMgr   - StatisticsReportService    |
|     (Specification/Predicates) (Undo/Redo Command Pattern)                   |
+-------------------------------------------------------------------------------+
                                       │
                                       ▼
+-------------------------------------------------------------------------------+
|                       DATA ACCESS LAYER (REPOSITORY/DAO)                      |
|   - GenericFileRepository<T, ID>                 - CsvSerializer<T>           |
|   - AtomicFileWriter (.tmp -> atomic replace)    - ResilientCsvParser         |
+-------------------------------------------------------------------------------+
                                       │
                                       ▼
+-------------------------------------------------------------------------------+
|                               FILE STORAGE (CSV)                              |
|   - data/movies.csv      - data/categories.csv   - data/users.csv             |
|   - data/history.csv     - data/watchlists.csv   - data/favorites.csv         |
+-------------------------------------------------------------------------------+
```

---

## 🎯 Feature Matrix

### 🟢 Basic (B)
- [x] **Movie CRUD**: Add, edit, delete, and list movies with unique title & category validation.
- [x] **Category CRUD**: Manage movie genres with foreign key integrity checks.
- [x] **Search Movies**: Instant search by Title, Actor, Director, or Genre via Inverted Index.
- [x] **Sort Movies**: Multi-attribute sorting (Title A-Z/Z-A, Rating, Release Year, Popularity/Views).
- [x] **Watchlist Management**: Per-user movie watchlist addition and removal.
- [x] **Favorite Management**: Bookmark favorite movies.
- [x] **View Movie Details**: Full metadata inspection with ASCII table formatting.

### 🟡 Medium (M)
- [x] **Watching History**: Chronological log of watched movies with timestamps and durations.
- [x] **Continue Watching**: Real-time playback checkpointing (resume from exact `mm:ss`).
- [x] **Browse by Category**: Category-specific movie catalog with 10-item pagination.
- [x] **Viewing Statistics**: Aggregate watch hours, completed movies count, and completion rate.
- [x] **Recently Watched Movies**: Quick access to recent viewing history.
- [x] **Trending Categories**: Most watched categories within sliding time windows (7 / 30 days).

### 🔴 Hard (H)
- [x] **Undo/Redo Watchlist**: Command Pattern engine allowing undo/redo for add/remove/clear operations.
- [x] **Automatic Movie Ranking**: Weighted normalization ranking engine using `PriorityQueue` (Max-Heap).
- [x] **Advanced Multi-Condition Filter**: Specification Pattern chaining dynamic `Predicate<Movie>` filters.
- [x] **Viewing Reports Generator**: Export comprehensive user viewing analytics to `.txt` and `.csv`.

---

## 🚀 Quickstart & Build Instructions

### Prerequisites
- **JDK 8** (OpenJDK / Eclipse Temurin 8 recommended)
- **Apache Maven 3.6+** (or use the included `./mvnw` wrapper)

### Build & Test
```bash
# Clone the repository
git clone https://github.com/your-username/fcode-be-crew-movie-streaming-management-system.git
cd fcode-be-crew-movie-streaming-management-system

# Run tests and verify code standards
./mvnw clean verify

# Package the standalone JAR
./mvnw clean package
```

### Run Application
```bash
# Run via Maven
./mvnw compile exec:java -Dexec.mainClass="com.moviestreaming.App"

# Or run the packaged executable JAR directly
java -jar target/movie-streaming-management-system-1.0.0-SNAPSHOT.jar
```

---

## 🐳 Docker Execution

Build and run the interactive CLI within an isolated container:

```bash
# Build and start via Docker Compose
docker compose run --rm app

# Or build and run directly via Docker CLI
docker build -t movie-streaming-app .
docker run -it --rm movie-streaming-app
```

---

## 🧪 Testing & Code Quality

```bash
# Run unit and integration tests
./mvnw test

# Run Checkstyle audit (Google Java Style)
./mvnw checkstyle:check
```

---

## 📜 Contributing & License

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for branch management and commit guidelines.  
Distributed under the **MIT License**. See [LICENSE](LICENSE) for details.
