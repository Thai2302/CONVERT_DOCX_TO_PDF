# DOCX to PDF Converter - Dự án JSP Servlet

## 📋 Mô tả dự án

Đây là ứng dụng web Java sử dụng JSP Servlet để chuyển đổi file DOCX sang PDF. Ứng dụng được xây dựng theo mô hình MVC với các tính năng:

- ✅ Xác thực người dùng (Login/Register)
- ✅ Upload nhiều file DOCX cùng lúc (tối đa 50MB/file)
- ✅ Xử lý conversion bất đồng bộ với hàng đợi (Queue)
- ✅ Theo dõi trạng thái conversion jobs
- ✅ Download file PDF đã convert
- ✅ Giao diện hiện đại, responsive

## 🏗️ Thiết kế MVC

### 1. Model Layer
```
model/
├── bean/                    # BEAN - Các entity classes
│   ├── UserBean.java
│   └── ConversionJobBean.java
├── dao/                     # DAO - Data Access Object
│   ├── UserDAO.java
│   └── ConversionJobDAO.java
└── bo/                      # BO - Business Object
    ├── UserBO.java
    └── ConversionJobBO.java
```

**BEAN**: Đại diện cho các entity/model trong database
- `UserBean`: Thông tin user (userId, username, password, email, fullName)
- `ConversionJobBean`: Thông tin conversion job (jobId, userId, filename, status, paths)

**DAO**: Xử lý các thao tác với database
- `UserDAO`: CRUD operations cho users (insert, select, update, delete)
- `ConversionJobDAO`: CRUD operations cho conversion jobs

**BO**: Xử lý business logic
- `UserBO`: Logic đăng ký, đăng nhập, hash password với BCrypt
- `ConversionJobBO`: Logic tạo job, quản lý queue, validate file

### 2. Controller Layer
```
controller/
├── LoginController.java         # Xử lý login
├── RegisterController.java      # Xử lý đăng ký
├── LogoutController.java        # Xử lý logout
├── UploadController.java        # Xử lý upload files
├── JobStatusController.java     # Hiển thị danh sách jobs
├── DownloadController.java      # Download file PDF
└── DeleteJobController.java     # Xóa job
```

Mỗi Controller kế thừa `HttpServlet` và xử lý:
- Nhận request từ client (doGet/doPost)
- Gọi Business Object để xử lý logic
- Forward/Redirect đến View tương ứng

### 3. View Layer
```
webapp/view/
├── login_view.jsp          # Trang đăng nhập
├── register_view.jsp       # Trang đăng ký
├── dashboard_view.jsp      # Dashboard hiển thị jobs
├── upload_view.jsp         # Trang upload files
└── error_view.jsp          # Trang lỗi
```

### 4. Utils Layer
```
utils/
├── DatabaseUtil.java           # Quản lý kết nối database
├── FileUtil.java              # Utility xử lý file
├── ConversionUtil.java        # Logic convert DOCX to PDF
├── QueueManager.java          # Quản lý hàng đợi jobs
└── QueueManagerListener.java  # Khởi động queue khi app start
```

## 📊 Sơ đồ kiến trúc

```
┌─────────────────────────────────────────────────────────────┐
│                         Client/Browser                       │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP Request
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  ┌─────────────┐ ┌──────────────┐ ┌─────────────────────┐  │
│  │LoginController│ │UploadController│ │JobStatusController│  │
│  └──────┬──────┘ └───────┬──────┘ └──────────┬──────────┘  │
└─────────┼─────────────────┼───────────────────┼──────────────┘
          │                 │                   │
          ▼                 ▼                   ▼
┌─────────────────────────────────────────────────────────────┐
│                   Business Object Layer                      │
│     ┌────────────┐              ┌──────────────────┐        │
│     │  UserBO    │              │ ConversionJobBO  │        │
│     └──────┬─────┘              └────────┬─────────┘        │
└────────────┼─────────────────────────────┼──────────────────┘
             │                             │
             ▼                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   Data Access Layer                          │
│     ┌────────────┐              ┌──────────────────┐        │
│     │  UserDAO   │              │ ConversionJobDAO │        │
│     └──────┬─────┘              └────────┬─────────┘        │
└────────────┼─────────────────────────────┼──────────────────┘
             │                             │
             ▼                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       MySQL Database                         │
│         ┌──────────┐              ┌────────────────┐        │
│         │  users   │              │ conversion_jobs│        │
│         └──────────┘              └────────────────┘        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   Background Processing                      │
│                    ┌──────────────┐                          │
│                    │ QueueManager │                          │
│                    │  (3 Workers) │                          │
│                    └──────┬───────┘                          │
│                           │                                  │
│                           ▼                                  │
│                  ┌─────────────────┐                         │
│                  │ ConversionUtil  │                         │
│                  │ (Docx4j Library)│                         │
│                  └─────────────────┘                         │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 Công nghệ sử dụng

- **Backend**: Java 11, JSP, Servlet
- **Server**: Apache Tomcat 10
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Libraries**:
  - Docx4j: Convert DOCX to PDF (giữ nguyên format)
  - BCrypt: Hash password
  - MySQL Connector: Kết nối database
  - Jakarta Servlet API: Servlet 5.0

## 📦 Cài đặt và Chạy

### 1. Cài đặt MySQL

1. Download và cài MySQL 8.0 từ: https://dev.mysql.com/downloads/mysql/
2. Trong quá trình cài đặt:
   - Chọn password cho root user (hoặc để trống)
   - Port: 3306 (mặc định)

### 2. Tạo Database

1. Mở MySQL Command Line hoặc MySQL Workbench
2. Chạy file `database_schema.sql`:

```sql
mysql -u root -p < database_schema.sql
```

Hoặc copy nội dung file và chạy trong MySQL Workbench.

3. File này sẽ tạo:
   - Database: `docx_to_pdf_db`
   - Bảng: `users`, `conversion_jobs`
   - 2 user demo: admin/test123, testuser/test123

### 3. Cấu hình Database Connection

Mở file `src/main/resources/database.properties` và điều chỉnh:

```properties
db.url=jdbc:mysql://localhost:3306/docx_to_pdf_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### 4. Cài đặt Tomcat 10

Bạn đã cài sẵn Tomcat 10 tại: `C:\Tomcat10\tomcat`

Kiểm tra:
- Thư mục `webapps` tồn tại
- Port 8080 chưa bị sử dụng

### 5. Build Project với Maven

```powershell
# Di chuyển vào thư mục project
cd C:\Users\dinht\Convert_docx_to_pdf

# Build project
mvn clean package
```

Lệnh này sẽ:
- Download tất cả dependencies
- Compile code
- Tạo file WAR trong thư mục `target/docx-to-pdf.war`

### 6. Deploy vào Tomcat

**Cách 1: Manual Deploy**
```powershell
# Copy file WAR vào Tomcat
copy target\docx-to-pdf.war C:\Tomcat10\tomcat\webapps\
```

**Cách 2: Eclipse/IntelliJ**
- Add Tomcat server trong IDE
- Right-click project → Run on Server

### 7. Khởi động Tomcat

```powershell
# Di chuyển vào thư mục Tomcat
cd C:\Tomcat10\tomcat\bin

# Khởi động
startup.bat
```

### 8. Truy cập ứng dụng

Mở trình duyệt và truy cập:
```
http://localhost:8080/docx-to-pdf/
```

## 👤 Tài khoản Demo

- **Username**: `admin` | **Password**: `test123`
- **Username**: `testuser` | **Password**: `test123`

## 🎯 Hướng dẫn sử dụng

### 1. Đăng nhập/Đăng ký
- Truy cập trang chủ → Đăng nhập hoặc Đăng ký tài khoản mới
- Username tối thiểu 3 ký tự, Password tối thiểu 6 ký tự

### 2. Upload File DOCX
- Click tab "Upload File"
- Click vào khu vực upload hoặc kéo thả file
- Chọn 1 hoặc nhiều file DOCX (mỗi file ≤ 50MB)
- Click "Upload và Convert"
- File sẽ được thêm vào hàng đợi

### 3. Theo dõi tiến trình
- Tab "Danh sách Jobs" hiển thị tất cả jobs
- Trạng thái:
  - **PENDING**: Đang chờ xử lý
  - **PROCESSING**: Đang convert
  - **COMPLETED**: Hoàn thành
  - **FAILED**: Thất bại
- Trang tự động refresh mỗi 5 giây

### 4. Download file PDF
- Khi job COMPLETED, click nút "📥 Download"
- File PDF sẽ được tải về máy

### 5. Xóa Job
- Click nút "🗑️ Xóa" để xóa job
- File gốc và file đã convert sẽ bị xóa

## 🔄 Luồng xử lý Conversion

```
1. User upload file DOCX
   ↓
2. UploadController nhận file
   ↓
3. Lưu file vào thư mục uploads/
   ↓
4. ConversionJobBO tạo job trong database (status: PENDING)
   ↓
5. Job được thêm vào BlockingQueue
   ↓
6. Worker thread lấy job từ queue
   ↓
7. Cập nhật status → PROCESSING
   ↓
8. ConversionUtil convert DOCX → PDF (sử dụng Docx4j)
   ↓
9. Lưu file PDF vào thư mục converted/
   ↓
10. Cập nhật status → COMPLETED (hoặc FAILED nếu lỗi)
    ↓
11. User download file PDF
```

## 🗂️ Cấu trúc thư mục đầy đủ

```
Convert_docx_to_pdf/
├── src/
│   ├── main/
│   │   ├── java/com/docxtopdf/
│   │   │   ├── model/
│   │   │   │   ├── bean/
│   │   │   │   │   ├── UserBean.java
│   │   │   │   │   └── ConversionJobBean.java
│   │   │   │   ├── dao/
│   │   │   │   │   ├── UserDAO.java
│   │   │   │   │   └── ConversionJobDAO.java
│   │   │   │   └── bo/
│   │   │   │       ├── UserBO.java
│   │   │   │       └── ConversionJobBO.java
│   │   │   ├── controller/
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   ├── LogoutController.java
│   │   │   │   ├── UploadController.java
│   │   │   │   ├── JobStatusController.java
│   │   │   │   ├── DownloadController.java
│   │   │   │   └── DeleteJobController.java
│   │   │   └── utils/
│   │   │       ├── DatabaseUtil.java
│   │   │       ├── FileUtil.java
│   │   │       ├── ConversionUtil.java
│   │   │       ├── QueueManager.java
│   │   │       └── QueueManagerListener.java
│   │   ├── resources/
│   │   │   └── database.properties
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       ├── view/
│   │       │   ├── login_view.jsp
│   │       │   ├── register_view.jsp
│   │       │   ├── dashboard_view.jsp
│   │       │   ├── upload_view.jsp
│   │       │   └── error_view.jsp
│   │       └── css/
│   │           ├── style.css
│   │           └── dashboard.css
├── uploads/              # Thư mục lưu file DOCX upload
├── converted/            # Thư mục lưu file PDF đã convert
├── database_schema.sql   # Script tạo database
├── pom.xml              # Maven configuration
└── README.md            # File này
```

## 🐛 Troubleshooting

### Lỗi kết nối database
```
Kiểm tra:
1. MySQL service đang chạy
2. Username/password trong database.properties đúng
3. Database đã được tạo
4. Port 3306 không bị block bởi firewall
```

### Lỗi compile
```
# Clean và rebuild
mvn clean compile
```

### Lỗi deploy
```
Kiểm tra:
1. Tomcat đang chạy
2. Không có lỗi trong logs: C:\Tomcat10\tomcat\logs\catalina.out
3. Port 8080 không bị chiếm
```

### File conversion thất bại
```
Kiểm tra:
1. File DOCX không bị corrupt
2. File size < 50MB
3. Logs trong Tomcat để xem chi tiết lỗi
```

## 📝 Yêu cầu đồ án

✅ **Thực hiện theo mô hình MVC**: Tách biệt Model-View-Controller rõ ràng

✅ **Có kết nối cơ sở dữ liệu**: MySQL với 2 bảng (users, conversion_jobs)

✅ **Có tính toán lớn chạy ngầm**: 
- Conversion DOCX to PDF (xử lý file lớn)
- Sử dụng BlockingQueue với 3 worker threads
- Client upload → Server thêm vào queue → Xử lý bất đồng bộ

✅ **Client xem kết quả qua account**: 
- Mỗi user chỉ xem được jobs của mình
- Dashboard hiển thị thống kê và trạng thái real-time

✅ **Có thiết kế mô hình MVC**: Xem phần "Thiết kế MVC" và sơ đồ kiến trúc

✅ **Có hướng dẫn cài đặt**: File README.md này

## 🎓 Ghi chú cho giảng viên

- Project tuân thủ đầy đủ mô hình MVC với 4 layers rõ ràng
- Naming convention: tất cả file có đuôi tên thư mục (UserBean, UserDAO, UserBO, LoginController, login_view.jsp)
- Xử lý conversion bất đồng bộ với Queue để không block main thread
- Sử dụng Docx4j library để đảm bảo chất lượng conversion cao
- Security: Password được hash bằng BCrypt, validation đầy đủ
- Database: Normalized design, foreign key constraints
- UI/UX: Modern, responsive, user-friendly

## 👨‍💻 Tác giả

- **Sinh viên**: [Tên của bạn]
- **Lớp**: [Lớp của bạn]
- **Môn học**: JSP Servlet - Lập trình Web

## 📄 License

Dự án này được tạo cho mục đích học tập.
