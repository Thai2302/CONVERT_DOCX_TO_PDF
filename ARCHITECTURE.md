# Sơ Đồ Thiết Kế Hệ Thống - DOCX to PDF Converter

## Kiến Trúc MVC Pattern

```mermaid
graph TB
    User([👤 User])
    
    subgraph VIEW["VIEW"]
        direction TB
        V1["Login.jsp<br/>Register.jsp<br/>Upload.jsp<br/>Dashboard.jsp"]
        style V1 fill:#90EE90,stroke:#333,stroke-width:2px
    end
    
    subgraph CONTROLLER["CONTROLLER"]
        direction TB
        C1["LoginController<br/>RegisterController<br/>UploadController<br/>JobStatusController<br/>DownloadController<br/>DeleteJobController"]
        style C1 fill:#FFD700,stroke:#333,stroke-width:2px
    end
    
    subgraph MODEL["MODEL"]
        direction TB
        
        BO["<b>BO - Xử lý nghiệp vụ</b><br/><br/>UserBO<br/>ConversionJobBO"]
        style BO fill:#87CEEB,stroke:#333,stroke-width:2px
        
        DAO["<b>DAO - Truy xuất DB</b><br/><br/>UserDAO<br/>ConversionJobDAO"]
        style DAO fill:#87CEEB,stroke:#333,stroke-width:2px
        
        Bean["<b>Bean - Entity</b><br/><br/>UserBean<br/>ConversionJobBean"]
        style Bean fill:#87CEEB,stroke:#333,stroke-width:2px
        
        Utils["<b>Utils - Tiện ích</b><br/><br/>DatabaseUtil<br/>FileUtil<br/>ConversionUtil<br/>QueueManager"]
        style Utils fill:#DDA0DD,stroke:#333,stroke-width:2px
    end
    
    DB[("🗄️ DATABASE<br/><br/>docx_to_pdf_db<br/><br/>users<br/>conversion_jobs")]
    style DB fill:#FFB6C1,stroke:#333,stroke-width:3px
    
    User -->|"1️⃣ Request"| V1
    V1 -->|"2️⃣ Shows"| User
    
    V1 -.->|"3️⃣ Submit"| C1
    C1 -.->|"4️⃣ Response"| V1
    
    C1 -->|"Gọi BO"| BO
    BO -->|"Gọi DAO"| DAO
    
    C1 -->|"Sử dụng"| Utils
    BO -->|"Sử dụng"| Utils
    
    DAO -->|"Truy vấn"| DB
    DAO -->|"Sử dụng"| Bean
    Utils -->|"Kết nối"| DB
    
    classDef viewStyle fill:#90EE90,stroke:#333,stroke-width:3px,color:#000
    classDef controllerStyle fill:#FFD700,stroke:#333,stroke-width:3px,color:#000
    classDef modelStyle fill:#87CEEB,stroke:#333,stroke-width:3px,color:#000
    classDef dbStyle fill:#FFB6C1,stroke:#333,stroke-width:3px,color:#000
```

---

## Giải Thích Kiến Trúc

### 📊 Các Tầng Chính

| Tầng | Mô tả | Thành phần |
|------|-------|------------|
| **VIEW** | Giao diện người dùng | JSP pages (Login, Register, Upload, Dashboard) |
| **CONTROLLER** | Xử lý request/response | Servlet Controllers (7 controllers) |
| **MODEL** | Xử lý logic và dữ liệu | BO (Business Object), DAO (Data Access), Bean (Entity), Utils |
| **DATABASE** | Lưu trữ dữ liệu | MySQL (users, conversion_jobs) |

### 🔄 Luồng Xử Lý

1. **User → View**: User truy cập giao diện web
2. **View → User**: Hiển thị form/nội dung
3. **View → Controller**: Submit request (login, upload, etc.)
4. **Controller → Model**: Gọi BO để xử lý nghiệp vụ
5. **Model → Database**: DAO truy vấn/cập nhật database
6. **Controller → View**: Trả về kết quả
7. **View → User**: Hiển thị kết quả

### 🛠️ Công Nghệ Sử Dụng

- **Jakarta EE Servlet 5.0**: Web framework
- **JSP + JSTL**: View rendering
- **MySQL 9.5**: Database
- **Docx4j**: DOCX → PDF conversion
- **jBCrypt**: Password hashing
- **BlockingQueue**: Background job processing (3 worker threads)
- **Apache Tomcat 10**: Application server

---

## Module Requests

| Module | Requests |
|--------|----------|
| **Auth** | Login, Logout, Register |
| **File** | Upload, Download, Delete |
| **Job** | View Status, Track Progress |

## Contacts Model

**BO (Business Object)**
- UserBO
- ConversionJobBO

**DAO (Data Access Object)**
- UserDAO - Truy xuất users table
- ConversionJobDAO - Truy xuất conversion_jobs table

**Controller gọi BO**
- Controller không gọi trực tiếp DAO
- Luôn đi qua BO để xử lý logic nghiệp vụ

---

## 2. Luồng Xử Lý Upload và Convert File (Chi Tiết)

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant UploadCtrl as UploadController
    participant FileUtil
    participant ConversionJobBO
    participant ConversionJobDAO
    participant QueueManager
    participant Worker as ConversionWorker
    participant ConversionUtil
    participant Database as MySQL DB
    
    User->>Browser: Chọn file DOCX
    Browser->>UploadCtrl: POST /upload (multipart)
    
    UploadCtrl->>FileUtil: Validate file (.docx, <50MB)
    FileUtil-->>UploadCtrl: Valid ✓
    
    UploadCtrl->>FileUtil: Generate unique filename
    FileUtil-->>UploadCtrl: timestamp_uuid.docx
    
    UploadCtrl->>UploadCtrl: Save file to uploads/
    
    UploadCtrl->>ConversionJobBO: createAndSubmitJob()
    ConversionJobBO->>ConversionJobDAO: insertJob(PENDING)
    ConversionJobDAO->>Database: INSERT INTO conversion_jobs
    Database-->>ConversionJobDAO: jobId
    
    ConversionJobBO->>QueueManager: submitJob(job)
    QueueManager->>QueueManager: Add to BlockingQueue
    
    Note over QueueManager,Worker: Async Processing (3 Worker Threads)
    
    Worker->>QueueManager: Poll queue
    QueueManager-->>Worker: Get job
    
    Worker->>ConversionJobDAO: updateJobToProcessing()
    ConversionJobDAO->>Database: UPDATE status=PROCESSING
    
    Worker->>ConversionUtil: convertDocxToPdf()
    ConversionUtil->>ConversionUtil: Use Docx4j library
    ConversionUtil-->>Worker: PDF file created
    
    Worker->>ConversionJobDAO: updateJobToCompleted()
    ConversionJobDAO->>Database: UPDATE status=COMPLETED
    
    Worker-->>Browser: (Background complete)
    
    User->>Browser: Refresh dashboard
    Browser->>JobStatusCtrl: GET /jobs
    JobStatusCtrl->>ConversionJobDAO: getJobsByUserId()
    ConversionJobDAO->>Database: SELECT * FROM conversion_jobs
    Database-->>Browser: Show job list with status
```

## 3. Cấu Trúc Database Schema

```mermaid
erDiagram
    users ||--o{ conversion_jobs : "has many"
    
    users {
        int user_id PK
        varchar username UK "Unique"
        varchar password "BCrypt hashed"
        varchar email UK "Unique"
        varchar full_name
        timestamp created_at
        timestamp updated_at
    }
    
    conversion_jobs {
        int job_id PK
        int user_id FK
        varchar original_filename "File gốc"
        varchar stored_filename "File lưu trữ"
        varchar converted_filename "File PDF"
        varchar original_path "Đường dẫn DOCX"
        varchar converted_path "Đường dẫn PDF"
        bigint file_size "Kích thước byte"
        enum status "PENDING, PROCESSING, COMPLETED, FAILED"
        timestamp created_at
        timestamp started_at
        timestamp completed_at
    }
```

## 4. Kiến Trúc Background Job Processing

```mermaid
graph LR
    subgraph "Main Thread"
        UploadCtrl[UploadController]
    end
    
    subgraph "QueueManager - Singleton"
        Queue[BlockingQueue]
        Executor[ExecutorService<br/>Fixed Thread Pool]
        
        Worker1[Worker Thread 1]
        Worker2[Worker Thread 2]
        Worker3[Worker Thread 3]
    end
    
    subgraph "Conversion Process"
        Convert[ConversionUtil]
        Docx4j[Docx4j Library]
    end
    
    UploadCtrl -->|Submit Job| Queue
    
    Queue --> Worker1
    Queue --> Worker2
    Queue --> Worker3
    
    Worker1 --> Convert
    Worker2 --> Convert
    Worker3 --> Convert
    
    Convert --> Docx4j
    
    style Queue fill:#fff4e1
    style Executor fill:#e1f5ff
    style Convert fill:#e1ffe1
```

## 5. Flow Chart - Quy Trình Xác Thực User

```mermaid
flowchart TD
    Start([User truy cập trang]) --> CheckSession{Session<br/>tồn tại?}
    
    CheckSession -->|Không| ShowLogin[Hiển thị Login Page]
    CheckSession -->|Có| CheckUser{currentUser<br/>trong session?}
    
    CheckUser -->|Không| ShowLogin
    CheckUser -->|Có| AllowAccess[Cho phép truy cập]
    
    ShowLogin --> UserInput[User nhập username/password]
    UserInput --> SubmitLogin[Submit form]
    
    SubmitLogin --> LoginCtrl[LoginController]
    LoginCtrl --> UserBO[UserBO.loginUser]
    
    UserBO --> UserDAO[UserDAO.getUserByUsername]
    UserDAO --> CheckDB{User<br/>tồn tại?}
    
    CheckDB -->|Không| LoginFail[Thông báo lỗi]
    CheckDB -->|Có| CheckPassword{BCrypt<br/>verify password?}
    
    CheckPassword -->|Sai| LoginFail
    CheckPassword -->|Đúng| CreateSession[Tạo session<br/>Lưu currentUser]
    
    LoginFail --> ShowLogin
    CreateSession --> Redirect[Redirect to Dashboard]
    Redirect --> AllowAccess
    
    AllowAccess --> End([End])
    
    style CheckSession fill:#ffe1e1
    style CheckPassword fill:#ffe1e1
    style AllowAccess fill:#e1ffe1
    style LoginFail fill:#ffcccc
```

## 6. Component Diagram - Các Thành Phần Chính

```mermaid
graph TB
    subgraph "Presentation Tier"
        JSP[JSP Views<br/>HTML + CSS + JavaScript]
    end
    
    subgraph "Application Tier"
        subgraph "Controller"
            Servlets[7 Servlet Controllers]
        end
        
        subgraph "Business Logic"
            BO[Business Objects<br/>UserBO, ConversionJobBO]
        end
        
        subgraph "Background Processing"
            Queue[Queue Manager<br/>3 Worker Threads]
        end
    end
    
    subgraph "Data Tier"
        subgraph "Data Access"
            DAO[Data Access Objects<br/>UserDAO, ConversionJobDAO]
        end
        
        subgraph "Storage"
            DB[(MySQL Database)]
            Files[File System<br/>uploads/ folder]
        end
    end
    
    subgraph "External Libraries"
        Docx4j[Docx4j<br/>PDF Conversion]
        BCrypt[jBCrypt<br/>Password Hashing]
        JDBC[MySQL Connector<br/>JDBC Driver]
    end
    
    JSP --> Servlets
    Servlets --> BO
    Servlets --> Queue
    BO --> DAO
    Queue --> DAO
    DAO --> DB
    DAO --> JDBC
    Servlets --> Files
    Queue --> Docx4j
    BO --> BCrypt
    
    style JSP fill:#e1f5ff
    style DB fill:#ffe1e1
    style Queue fill:#fff4e1
    style Docx4j fill:#e1ffe1
```

## 7. Deployment Architecture

```mermaid
graph TB
    subgraph "Client Side"
        Browser[Web Browser<br/>Chrome, Firefox, Edge]
    end
    
    subgraph "Server Side - Apache Tomcat 10"
        subgraph "Web Container"
            WAR[docx-to-pdf.war]
            
            subgraph "Application"
                Servlets[Servlet Controllers]
                JSP[JSP Pages]
                Queue[Background Queue<br/>3 Threads]
            end
        end
        
        subgraph "File System"
            Uploads[uploads/<br/>DOCX & PDF files]
        end
    end
    
    subgraph "Database Server"
        MySQL[(MySQL 9.5<br/>docx_to_pdf_db)]
    end
    
    Browser <-->|HTTP/HTTPS<br/>Port 8080| WAR
    Servlets <-->|JDBC| MySQL
    Servlets <-->|Read/Write| Uploads
    Queue <-->|Read/Write| Uploads
    
    style Browser fill:#e1f5ff
    style MySQL fill:#ffe1e1
    style Uploads fill:#fff4e1
```

## 8. Class Diagram - Model Layer

```mermaid
classDiagram
    class UserBean {
        -int userId
        -String username
        -String password
        -String email
        -String fullName
        -Timestamp createdAt
        -Timestamp updatedAt
        +UserBean()
        +getters()
        +setters()
    }
    
    class ConversionJobBean {
        -int jobId
        -int userId
        -String originalFilename
        -String storedFilename
        -String convertedFilename
        -String originalPath
        -String convertedPath
        -long fileSize
        -JobStatus status
        -Timestamp createdAt
        -Timestamp startedAt
        -Timestamp completedAt
        +getFormattedFileSize()
        +getProcessingTime()
    }
    
    class JobStatus {
        <<enumeration>>
        PENDING
        PROCESSING
        COMPLETED
        FAILED
    }
    
    class UserDAO {
        +insertUser(UserBean) int
        +getUserByUsername(String) UserBean
        +getUserByEmail(String) UserBean
        +getUserById(int) UserBean
        +updateUser(UserBean) boolean
        +updatePassword(int, String) boolean
        +deleteUser(int) boolean
        +isUsernameExists(String) boolean
        +isEmailExists(String) boolean
    }
    
    class ConversionJobDAO {
        +insertJob(ConversionJobBean) int
        +getJobById(int) ConversionJobBean
        +getJobsByUserId(int) List
        +getPendingJobs() List
        +updateJobToProcessing(int) boolean
        +updateJobToCompleted(int, String, String) boolean
        +updateJobToFailed(int, String) boolean
        +deleteJob(int) boolean
    }
    
    class UserBO {
        -UserDAO userDAO
        +registerUser(String, String, String, String) UserBean
        +loginUser(String, String) UserBean
        +changePassword(int, String, String) boolean
    }
    
    class ConversionJobBO {
        -ConversionJobDAO jobDAO
        -QueueManager queueManager
        +createAndSubmitJob(...) ConversionJobBean
        +getJobById(int) ConversionJobBean
        +getJobsByUserId(int) List
        +deleteJob(int) boolean
        +getJobStatistics(int) Map
    }
    
    ConversionJobBean --> JobStatus
    UserDAO --> UserBean
    ConversionJobDAO --> ConversionJobBean
    UserBO --> UserDAO
    ConversionJobBO --> ConversionJobDAO
```

## Tổng Kết Kiến Trúc

### Các Tầng Chính:

1. **View Layer (JSP)**: Giao diện người dùng
2. **Controller Layer (Servlets)**: Xử lý HTTP requests/responses
3. **Business Logic Layer (BO)**: Logic nghiệp vụ
4. **Data Access Layer (DAO)**: Truy cập database
5. **Entity Layer (Bean)**: Đối tượng dữ liệu
6. **Utils Layer**: Tiện ích hỗ trợ
7. **Background Processing**: Xử lý bất đồng bộ với Queue

### Công Nghệ Sử Dụng:

- **Jakarta EE Servlet 5.0**: Framework web
- **MySQL 9.5**: Database
- **Docx4j**: DOCX to PDF conversion
- **jBCrypt**: Mã hóa mật khẩu
- **BlockingQueue + ExecutorService**: Xử lý background jobs
- **Apache Tomcat 10**: Application server
