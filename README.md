# ⚡ SmartBin — Ephemeral File Sharing Platform

> **Transfer files between devices without logging into anything.**
> Upload on your phone, receive a 6-character code, and download on any workstation or lab computer. All files automatically expire and are purged after 30 minutes.

---

## 🚀 Live Demo

**Try SmartBin:** [https://smartbin.sahalmuzammil.me](https://smartbin.sahalmuzammil.me)



## 📖 Overview

**SmartBin** is a secure, lightweight, and temporary file-transfer web platform designed for situations where you need to quickly beam a document, photo, or archive between devices (e.g., phone to desktop, or student workstation to lab machine) without logging into Gmail, WhatsApp, Telegram, Google Drive, or carrying a flash drive.

### User Workflow
1. **Upload**: Open SmartBin on your phone or computer and select or drop a file.
2. **Transfer Code**: SmartBin stores the file temporarily and generates an unambiguous, 6-character code (e.g., `7K4P92`).
3. **Retrieve**: Go to SmartBin on any other computer, type the 6-character code, and immediately stream the download.
4. **Auto-Purge**: Once downloaded (or after 30 minutes), the physical file and metadata are permanently removed from both storage and PostgreSQL.

---

## 🏗️ Architecture

SmartBin is structured following clean enterprise separation of concerns:

```
                      ┌──────────────────────────────────────┐
                      │    Client (Mobile / Lab Desktop)     │
                      │  React + Vite + Modern Responsive CSS│
                      └──────────────────┬───────────────────┘
                                         │ REST (JSON / Multipart / Streams)
                                         ▼
                      ┌──────────────────────────────────────┐
                      │    Spring Boot REST Controllers      │
                      │       (FileTransferController)       │
                      └──────────────────┬───────────────────┘
                                         │
                      ┌──────────────────▼───────────────────┐
                      │            Service Layer             │
                      │  - TransferCodeService (SecureRandom)│
                      │  - RateLimitingService (Sliding IP)  │
                      │  - StorageService (Modular Interface)│
                      │  - FileCleanupScheduler (@Scheduled) │
                      └──────────┬───────────────────┬───────┘
                                 │                   │
                 Physical Stream │                   │ Metadata (JPA / Hibernate)
                                 ▼                   ▼
                      ┌────────────────────┐   ┌───────────────────────────┐
                      │  Local File System │   │   PostgreSQL Database     │
                      │ (S3-ready Adapter) │   │ (SmartBin Table & Indexes)│
                      └────────────────────┘   └───────────────────────────┘
```

---

## 🛠️ Technologies Used

### Backend
- **Java 21 / 26**
- **Spring Boot 3.3.4**
  - `spring-boot-starter-web` (REST controllers, multipart handling, streaming downloads)
  - `spring-boot-starter-data-jpa` (Hibernate ORM, persistence, repositories)
  - `spring-boot-starter-validation` (Jakarta Validation)
- **PostgreSQL 18** (Relational metadata storage)
- **Maven 3.9** (Build & dependency management)
- **JUnit 5 & MockMvc** (Automated integration & endpoint testing)

### Frontend
- **React 18**
- **Vite 6** (Blazing-fast build tool and dev server)
- **Axios** (Multipart uploads with progress tracking and blob streaming)
- **Lucide React** (Clean, minimalist iconography)
- **Responsive CSS** (Mobile-first, touch-friendly, dark/light balanced surfaces)

---

## 📁 Project Structure

```
untitled/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/smartbin/
│       │   │   ├── SmartBinApplication.java
│       │   │   ├── config/
│       │   │   │   ├── AppProperties.java
│       │   │   │   └── WebMvcConfig.java
│       │   │   ├── controller/
│       │   │   │   └── FileTransferController.java
│       │   │   ├── dto/
│       │   │   │   ├── ErrorResponse.java
│       │   │   │   ├── FileMetadataResponse.java
│       │   │   │   ├── FileUploadResponse.java
│       │   │   │   └── RetrieveRequest.java
│       │   │   ├── entity/
│       │   │   │   └── FileTransfer.java
│       │   │   ├── exception/
│       │   │   │   ├── FileNotFoundException.java
│       │   │   │   ├── FileStorageException.java
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── RateLimitExceededException.java
│       │   │   │   └── ResourceNotFoundException.java
│       │   │   ├── repository/
│       │   │   │   └── FileTransferRepository.java
│       │   │   ├── scheduler/
│       │   │   │   └── FileCleanupScheduler.java
│       │   │   └── service/
│       │   │       ├── FileTransferService.java
│       │   │       ├── RateLimitingService.java
│       │   │       ├── StorageService.java
│       │   │       ├── TransferCodeService.java
│       │   │       └── impl/
│       │   │           └── LocalStorageService.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/smartbin/
│               └── SmartBinApplicationTests.java
│
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       ├── index.css
│       ├── services/
│       │   └── api.js
│       ├── components/
│       │   ├── ErrorMessage.jsx
│       │   ├── FileDetails.jsx
│       │   ├── FileUploader.jsx
│       │   ├── LoadingSpinner.jsx
│       │   ├── Navbar.jsx
│       │   ├── RetrieveForm.jsx
│       │   └── TransferCode.jsx
│       └── pages/
│           ├── Home.jsx
│           ├── Retrieve.jsx
│           └── Upload.jsx
│
└── README.md
```

---

## 🗄️ Database Schema

### Table: `file_transfers`

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Internal surrogate key (never exposed) |
| `transfer_code` | `VARCHAR(8)` | `NOT NULL, UNIQUE` | 6-character user transfer code |
| `original_filename` | `VARCHAR(255)`| `NOT NULL` | Sanitized original filename |
| `stored_filename` | `VARCHAR(255)`| `NOT NULL, UNIQUE` | UUID filename on physical disk |
| `file_path` | `VARCHAR(512)`| `NOT NULL` | Path to file storage |
| `file_size` | `BIGINT` | `NOT NULL` | Size in bytes |
| `content_type` | `VARCHAR(128)`| `NOT NULL` | MIME type |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Upload timestamp |
| `expires_at` | `TIMESTAMPTZ` | `NOT NULL` | Expiration timestamp |

**Indexes**:
- `idx_transfer_code` on `transfer_code` (`UNIQUE`)
- `idx_expires_at` on `expires_at` (Optimized periodic cleanup query)

---

## 🚀 How to Run the Application

### Prerequisites
- Java 21+
- Maven 3.8+ (or IntelliJ Maven)
- PostgreSQL 14+ running on port `5432`
- Node.js 18+ and npm

### 1. Database Setup
Ensure PostgreSQL is running and create the `smartbin` database:
```sql
CREATE DATABASE smartbin;
```

### 2. Backend Setup & Run
Configure `backend/src/main/resources/application.properties` with your PostgreSQL credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smartbin
spring.datasource.username=postgres
spring.datasource.password=your_password
```

Run the backend:
```bash
cd backend
mvn spring-boot:run
```
The backend starts at `http://localhost:8080`.

### 3. Frontend Setup & Run
```bash
cd frontend
npm install
npm run dev
```
Open your browser at `http://localhost:5173`.

---

## 📡 REST API Reference

| Method | Endpoint | Description | Request / Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/files/upload` | Upload a file and get a code | `multipart/form-data` with `file` |
| `POST` | `/api/files/text` | Share text message or note | JSON `{ "text": "...", "title": "..." }` |
| `POST` | `/api/files/retrieve` | Look up file/text metadata | JSON `{ "transferCode": "7K4P92" }` |
| `GET` | `/api/files/{code}` | Retrieve file/text metadata | Path variable `{code}` |
| `GET` | `/api/files/{code}/download` | Stream download physical file or note | Path variable `{code}` |
| `DELETE` | `/api/files/{code}` | Manually delete file/text & code | Path variable `{code}` |

### Error Responses
All errors return a consistent, secure JSON payload:
```json
{
  "timestamp": "2026-10-04T17:36:27.995Z",
  "status": 404,
  "error": "Not Found",
  "message": "Invalid or expired transfer code.",
  "path": "/api/files/7K4P92"
}
```

---

## 🔒 Security Best Practices Implemented

1. **Unpredictable Transfer Codes**: Generated using `java.security.SecureRandom` over an unambiguous 32-character base charset (`23456789ABCDEFGHJKLMNPQRSTUVWXYZ`), eliminating easily confused characters (`0`, `O`, `1`, `I`). Yields over 1 billion combinations with zero database collision.
2. **Brute-Force Protection**: IP-based sliding window rate limiter protects `/api/files/retrieve` and code queries. After 10 rapid attempts, returns `HTTP 429 Too Many Requests`.
3. **No Internal Information Leakage**: Database IDs and server physical file paths are never returned to clients.
4. **Unified 404 Responses**: Invalid codes return identical responses whether the code never existed or expired, preventing enumeration attacks.
5. **Path Traversal Prevention**: Filenames are sanitized via `StringUtils.cleanPath` and stripped of relative directory structures (`..`, `/`, `\`). Storage paths verify boundary confinement.
6. **Memory-Safe Streaming**: Files are uploaded and downloaded using NIO streams (`UrlResource` / `InputStreamResource`) rather than loading entire multi-megabyte buffers into JVM heap.
7. **Scheduled Physical Auto-Purge**: Background `@Scheduled` cleanup sweeps every minute to purge both disk files and database records.

---

## 🔮 Future Improvements

- **End-to-End Client Encryption**: Client-side AES-GCM encryption in WebCrypto before upload, with the decryption key encoded in a URL hash.
- **S3 / Cloud Object Storage Adapter**: Plug an AWS S3 / Cloudflare R2 implementation into the existing `StorageService` interface.
- **QR Code Scanning**: Render a QR code on the desktop screen for smartphone cameras to automatically fill the transfer code.
