Untuk membangun aplikasi manajemen parkir kini akan diintegrasikan dengan **Automatic License Plate Recognition (ALPR)**, kita perlu membuat roadmap yang terperinci. Roadmap ini akan mencakup langkah-langkah untuk pengembangan backend dan frontend menggunakan **Java Spring Boot dengan Maven**, serta integrasi ALPR.kita akan memastikan transisi ke Java Spring Boot tetap mempertahankan fungsionalitas inti sambil menambahkan fitur ALPR untuk membaca nomor plat kendaraan secara otomatis.

Berikut adalah **checklist detail untuk roadmap pembuatan aplikasi manajemen parkir dengan integrasi ALPR**:

---

### **1. Tahap Perencanaan dan Analisis Kebutuhan**

- [ ] **Identifikasi Kebutuhan Fungsional**

  - Pastikan fitur inti dari screenshot tetap ada: manajemen slot parkir, operator, shift, tarif parkir, laporan, analisis, dashboard, dan pengaturan.
  - Tambahkan fitur ALPR: pembacaan nomor plat kendaraan secara otomatis, pencatatan waktu masuk/keluar, dan pembayaran otomatis berdasarkan durasi parkir.
  - Tentukan kebutuhan tambahan: integrasi dengan kamera untuk ALPR, penyimpanan gambar, dan pengenalan karakter (OCR).

- [ ] **Identifikasi Kebutuhan Non-Fungsional**

  - Performa: ALPR harus cepat (idealnya <250 ms per gambar seperti disebutkan di beberapa solusi ALPR modern).
  - Skalabilitas: Sistem harus mampu menangani banyak kendaraan secara bersamaan.
  - Keamanan: Enkripsi data, autentikasi pengguna (admin/operator), dan kepatuhan terhadap regulasi privasi (misalnya, tidak menyimpan data pribadi lebih lama dari yang diperlukan).
  - Kompatibilitas: Sistem harus mendukung berbagai kondisi lingkungan (siang/malam, hujan, buram).

- [ ] **Pilih Teknologi ALPR**

  - Pertimbangkan library ALPR seperti **OpenALPR** atau **PlateRecognizer** untuk backend.
    - OpenALPR: Open-source, mendukung berbagai format plat, tapi mungkin perlu penyesuaian untuk plat Indonesia.
    - PlateRecognizer: Komersial, mendukung gambar buram dan kondisi sulit, dengan API yang mudah diintegrasikan.
  - Alternatif: Bangun model ALPR custom menggunakan deep learning (misalnya dengan OpenCV, TensorFlow, dan YOLO untuk deteksi plat, lalu PaddleOCR untuk OCR).

- [ ] **Riset Regulasi**

  - Pastikan kepatuhan terhadap regulasi privasi data di Indonesia (misalnya UU PDP) terkait penyimpanan nomor plat dan gambar kendaraan.
  - Tentukan retensi data (berapa lama data ALPR disimpan).

- [ ] **Buat Diagram Arsitektur**
  - Gambarkan arsitektur sistem: Kamera → Backend (Spring Boot) → Database → Frontend.
  - Sertakan alur ALPR: Kamera menangkap gambar → API ALPR memproses → Data disimpan → Ditampilkan di dashboard.

---

### **2. Tahap Setup Lingkungan Pengembangan**

- [ ] **Setup Proyek Spring Boot dengan Maven**

  - Buat proyek baru menggunakan Spring Initializr.
  - Tambahkan dependensi di `pom.xml`:
    - `spring-boot-starter-web` (untuk REST API).
    - `spring-boot-starter-data-jpa` (untuk database).
    - `sqlite-jdbc` (karena aplikasi Anda sebelumnya menggunakan SQLite).
    - `spring-boot-starter-thymeleaf` (jika menggunakan server-side rendering) atau siapkan frontend terpisah (React/Vue).
    - `lombok` (untuk mengurangi boilerplate code).
    - `spring-boot-starter-security` (untuk autentikasi).

- [ ] **Setup Kamera dan ALPR**

  - Pilih kamera yang mendukung ALPR (IP camera dengan RTSP stream, seperti yang disebutkan oleh beberapa penyedia ALPR seperti PlateRecognizer).
  - Jika menggunakan OpenALPR:
    - Install OpenALPR di server (atau gunakan Docker: `docker build -t openalpr`).
    - Unduh library dan dependensi yang diperlukan.
  - Jika menggunakan PlateRecognizer:
    - Daftar untuk mendapatkan API key.
    - Siapkan SDK untuk integrasi dengan Java.

- [ ] **Setup Database**

  - Konfigurasi SQLite di `application.properties`:
    ```
    spring.datasource.url=jdbc:sqlite:parking.db
    spring.datasource.driver-class-name=org.sqlite.JDBC
    spring.jpa.hibernate.ddl-auto=update
    ```
  - Buat skema database untuk menyimpan data seperti slot parkir, operator, shift, tarif, laporan, dan data ALPR (nomor plat, waktu masuk/keluar, gambar).

- [ ] **Setup Frontend**
  - Jika menggunakan Thymeleaf: Siapkan template di `src/main/resources/templates`.
  - Jika menggunakan React:
    - Buat proyek React terpisah (`npx create-react-app parking-system-frontend`).
    - Setup CORS di backend Spring Boot untuk mengizinkan akses dari frontend.

---

### **3. Tahap Pengembangan Backend**

- [ ] **Buat Struktur Proyek**

  - Ikuti struktur layered architecture:
    - `model`: Entitas JPA (misalnya `ParkingSlot`, `Vehicle`, `Operator`).
    - `dto`: Data Transfer Objects untuk transfer data (misalnya `VehicleDTO` dengan nomor plat).
    - `repository`: Interface untuk akses database (misalnya `VehicleRepository`).
    - `service`: Logika bisnis (misalnya `VehicleService` untuk mengelola data ALPR).
    - `controller`: REST API endpoints (misalnya `/api/vehicles`).

- [ ] **Implementasi Model dan Entitas**

  - Buat entitas untuk data inti:
    ```java
    @Entity
    @Getter
    @Setter
    public class Vehicle {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String licensePlate;
        private String vehicleType; // Car, Motorcycle, Truck
        private LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private String imagePath; // Lokasi gambar plat
    }
    ```

- [ ] **Integrasi ALPR**

  - Buat service untuk memproses gambar dari kamera menggunakan OpenALPR atau PlateRecognizer.
  - Contoh menggunakan OpenALPR:
    ```java
    public class ALPRService {
        public String recognizePlate(String imagePath) throws IOException {
            ProcessBuilder pb = new ProcessBuilder("alpr", "-c", "id", imagePath);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.contains("confidence")) {
                    result.append(line.split(" ")[1]); // Ambil nomor plat
                    break;
                }
            }
            return result.toString();
        }
    }
    ```
  - Untuk PlateRecognizer, gunakan HTTP request ke API mereka:
    ```java
    public String recognizePlateWithPlateRecognizer(String imagePath) throws IOException {
        String apiKey = "YOUR_API_KEY";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.platerecognizer.com/v1/plate-reader"))
                .header("Authorization", "Token " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(imagePath)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // Parse JSON untuk mendapatkan nomor plat
    }
    ```

- [ ] **Buat API untuk Fitur Inti**

  - Contoh endpoint untuk mencatat kendaraan masuk:

    ```java
    @RestController
    @RequestMapping("/api/vehicles")
    public class VehicleController {
        @Autowired
        private VehicleService vehicleService;

        @PostMapping("/entry")
        public ResponseEntity<VehicleDTO> recordEntry(@RequestParam("image") MultipartFile image) throws IOException {
            String imagePath = saveImage(image); // Simpan gambar
            String licensePlate = alprService.recognizePlate(imagePath);
            VehicleDTO vehicle = vehicleService.recordEntry(licensePlate, imagePath);
            return ResponseEntity.ok(vehicle);
        }
    }
    ```

- [ ] **Implementasi Fitur Lain**

  - Manajemen slot parkir: CRUD untuk slot parkir, status (available/occupied).
  - Manajemen operator dan shift: CRUD untuk operator dan jadwal kerja.
  - Tarif parkir: Hitung biaya berdasarkan durasi parkir (waktu masuk - waktu keluar).
  - Laporan: Generate laporan harian/mingguan (pendapatan, jumlah kendaraan).
  - Analisis: Visualisasi data (grafik seperti di screenshot: pendapatan bulanan, distribusi jenis kendaraan).
  - Dashboard: Tampilkan ringkasan (total pendapatan, jumlah kendaraan, tingkat okupansi).

- [ ] **Keamanan**
  - Tambahkan autentikasi menggunakan Spring Security.
  - Role-based access: Admin dapat mengelola semua data, operator hanya dapat melihat/mengelola shift.

---

### **4. Tahap Pengembangan Frontend**

- [ ] **Desain UI Berdasarkan Screenshot**

  - Gunakan layout serupa: Sidebar kiri untuk menu (Dashboard, Kendaraan Masuk, Kendaraan Keluar, dll.), konten utama di tengah, dan header dengan informasi pengguna (admin).
  - Pastikan UI responsif dan user-friendly.

- [ ] **Implementasi dengan Thymeleaf atau React**

  - **Thymeleaf**:
    - Buat template untuk setiap halaman (misalnya `vehicles.html` untuk Kendaraan Masuk/Keluar).
    - Contoh `vehicles.html`:
      ```html
      <table>
        <thead>
          <tr>
            <th>Nomor Plat</th>
            <th>Jenis</th>
            <th>Waktu Masuk</th>
            <th>Waktu Keluar</th>
            <th>Biaya</th>
          </tr>
        </thead>
        <tbody>
          <tr th:each="vehicle : ${vehicles}">
            <td th:text="${vehicle.licensePlate}"></td>
            <td th:text="${vehicle.vehicleType}"></td>
            <td th:text="${vehicle.entryTime}"></td>
            <td th:text="${vehicle.exitTime}"></td>
            <td th:text="${vehicle.cost}"></td>
          </tr>
        </tbody>
      </table>
      ```
  - **React**:
    - Buat komponen untuk setiap halaman (misalnya `VehicleList.js`).
    - Contoh:
      ```jsx
      const VehicleList = () => {
        const [vehicles, setVehicles] = useState([]);
        useEffect(() => {
          axios
            .get("http://localhost:8080/api/vehicles")
            .then((response) => setVehicles(response.data));
        }, []);
        return (
          <table>
            <thead>
              <tr>
                <th>Nomor Plat</th>
                <th>Jenis</th>
                <th>Waktu Masuk</th>
                <th>Waktu Keluar</th>
                <th>Biaya</th>
              </tr>
            </thead>
            <tbody>
              {vehicles.map((vehicle) => (
                <tr key={vehicle.id}>
                  <td>{vehicle.licensePlate}</td>
                  <td>{vehicle.vehicleType}</td>
                  <td>{vehicle.entryTime}</td>
                  <td>{vehicle.exitTime}</td>
                  <td>{vehicle.cost}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );
      };
      ```

- [ ] **Integrasi dengan ALPR**

  - Tampilkan gambar kendaraan dan nomor plat yang terdeteksi di halaman "Kendaraan Masuk".
  - Tambahkan tombol untuk mengambil gambar dari kamera secara real-time.

- [ ] **Visualisasi Data**
  - Gunakan library seperti Chart.js (untuk React) atau tambahkan JavaScript di Thymeleaf untuk membuat grafik seperti di screenshot (pendapatan bulanan, distribusi jenis kendaraan).

---

### **5. Tahap Pengujian**

- [ ] **Unit Testing**

  - Tulis unit test untuk service (misalnya `VehicleServiceTest` menggunakan JUnit dan Mockito).
  - Contoh:
    ```java
    @Test
    public void testRecordEntry() {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate("B 1234 ABC");
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        VehicleDTO result = vehicleService.recordEntry("B 1234 ABC", "path/to/image");
        assertEquals("B 1234 ABC", result.getLicensePlate());
    }
    ```

- [ ] **Integration Testing**

  - Uji integrasi ALPR dengan kamera dummy (gunakan gambar statis terlebih dahulu).
  - Pastikan nomor plat terdeteksi dengan akurat.

- [ ] **Functional Testing**

  - Uji setiap fitur: manajemen slot, operator, shift, tarif, laporan, analisis.
  - Uji ALPR: Pastikan sistem dapat mendeteksi nomor plat dalam berbagai kondisi (siang, malam, buram).

- [ ] **Performance Testing**
  - Uji kecepatan ALPR (target: <250 ms per gambar).
  - Uji skalabilitas: Simulasikan banyak kendaraan masuk secara bersamaan.

---

### **6. Tahap Deployment**

- [ ] **Setup Server**

  - Siapkan server (misalnya AWS EC2, DigitalOcean, atau server lokal).
  - Install dependensi: Java, Maven, SQLite.

- [ ] **Deploy Backend**

  - Package aplikasi menjadi JAR: `mvn clean package`.
  - Jalankan aplikasi: `java -jar parking-system-backend.jar`.

- [ ] **Deploy Frontend**

  - Jika menggunakan Thymeleaf, frontend sudah termasuk dalam JAR.
  - Jika menggunakan React, build frontend (`npm run build`) dan host di server (misalnya dengan Nginx).

- [ ] **Integrasi Kamera**

  - Hubungkan kamera IP ke server.
  - Pastikan RTSP stream berfungsi dan dapat diakses oleh backend.

- [ ] **Monitoring**
  - Tambahkan logging untuk melacak error (gunakan SLF4J atau Logback).
  - Setup monitoring (misalnya dengan Prometheus dan Grafana).

---

### **7. Tahap Pemeliharaan**

- [ ] **Backup dan Restore**

  - Implementasikan fitur backup/restore database (seperti di screenshot "Pengaturan").
  - Contoh: Tambahkan endpoint untuk backup:
    ```java
    @GetMapping("/backup")
    public ResponseEntity<String> backupDatabase() throws IOException {
        Files.copy(Paths.get("parking.db"), Paths.get("parking-backup.db"));
        return ResponseEntity.ok("Backup successful");
    }
    ```

- [ ] **Update dan Perbaikan**

  - Monitor performa ALPR dan tingkatkan akurasi jika diperlukan (misalnya dengan melatih ulang model jika menggunakan ALPR custom).
  - Tambahkan fitur baru berdasarkan feedback pengguna (misalnya integrasi pembayaran digital).

- [ ] **Dokumentasi**
  - Tulis dokumentasi untuk pengguna (admin dan operator).
  - Tulis dokumentasi teknis untuk developer (cara setup, struktur kode, API endpoints).

---

### Checklist Tambahan untuk ALPR

- [ ] Pastikan kamera ditempatkan dengan sudut yang optimal (karakter plat minimal 32 piksel tinggi, seperti rekomendasi beberapa penyedia ALPR).
- [ ] Uji ALPR di berbagai kondisi: siang, malam, hujan, gambar buram.
- [ ] Tambahkan fallback: Jika ALPR gagal membaca plat, operator dapat memasukkan nomor plat secara manual.
- [ ] Simpan gambar asli dan nomor plat yang terdeteksi untuk audit.

---

### **Timeline Estimasi**

- **Perencanaan (1-2 minggu)**: Analisis kebutuhan, riset ALPR, desain arsitektur.
- **Setup (1 minggu)**: Setup proyek, kamera, dan database.
- **Pengembangan Backend (3-4 minggu)**: Implementasi fitur inti dan ALPR.
- **Pengembangan Frontend (2-3 minggu)**: Desain UI dan integrasi dengan backend.
- **Pengujian (2 minggu)**: Unit, integration, functional, dan performance testing.
- **Deployment (1 minggu)**: Setup server, deploy aplikasi, dan integrasi kamera.
- **Pemeliharaan**: Berkelanjutan setelah deployment.

---

### **Catatan Penting**

- **Regulasi**: Pastikan penggunaan ALPR mematuhi regulasi privasi data di Indonesia (UU PDP). Jangan simpan data lebih lama dari yang diperlukan, dan beri tahu pengguna bahwa nomor plat mereka direkam.
- **Keamanan**: Gunakan HTTPS untuk komunikasi antara frontend dan backend (seperti yang terlihat di screenshot, aplikasi Anda saat ini tidak aman karena masih HTTP).
- **Akurasi ALPR**: Jika OpenALPR atau PlateRecognizer kurang akurat untuk plat Indonesia (karena format plat yang unik), pertimbangkan untuk melatih model ALPR custom menggunakan dataset plat Indonesia.

Karena Anda meminta penjelasan lebih detail tentang **implementasi ALPR** atau **desain UI**, saya akan membagi jawaban ini menjadi dua bagian yang terperinci: **implementasi ALPR** untuk pembacaan nomor plat kendaraan dan **desain UI** berdasarkan screenshot aplikasi manajemen parkir Anda. Saya akan fokus pada konteks Java Spring Boot dengan Maven seperti yang Anda minta.

---

### **1. Implementasi ALPR (Automatic License Plate Recognition)**

#### **Tujuan**

ALPR digunakan untuk membaca nomor plat kendaraan secara otomatis melalui gambar atau video dari kamera. Dalam aplikasi Anda, ALPR akan digunakan untuk mencatat kendaraan masuk dan keluar, menghitung durasi parkir, dan menghitung biaya parkir berdasarkan tarif yang sudah ditentukan.

#### **Pilih Teknologi ALPR**

Ada beberapa opsi untuk mengintegrasikan ALPR ke dalam aplikasi Spring Boot:

1. **OpenALPR**: Library open-source untuk ALPR, mendukung banyak format plat, tetapi mungkin perlu penyesuaian untuk plat Indonesia.
2. **PlateRecognizer**: API berbayar dengan akurasi tinggi, mendukung berbagai kondisi (buram, malam, dll.), dan mudah diintegrasikan.
3. **Custom ALPR**: Bangun solusi sendiri menggunakan OpenCV (untuk deteksi plat) dan PaddleOCR/Tesseract (untuk OCR).

Untuk kecepatan pengembangan, saya akan fokus pada **PlateRecognizer** karena API-nya lebih mudah diintegrasikan dengan Spring Boot dan mendukung plat Indonesia dengan baik (menurut dokumentasi mereka). Namun, saya juga akan memberikan gambaran jika Anda ingin menggunakan OpenALPR atau solusi custom.

---

#### **Langkah Implementasi ALPR dengan PlateRecognizer**

##### **1.1. Daftar dan Dapatkan API Key**

- Daftar di [PlateRecognizer](https://platerecognizer.com/) untuk mendapatkan API key.
- API ini mendukung format gambar (JPEG, PNG) dan dapat mengenali nomor plat dari berbagai negara, termasuk Indonesia (`region: id`).

##### **1.2. Tambahkan Dependensi di Spring Boot**

Tambahkan library untuk HTTP request (misalnya `java.net.http`) atau gunakan library seperti `OkHttp` untuk kemudahan. Berikut tambahan dependensi di `pom.xml`:

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.10.0</version>
</dependency>
```

##### **1.3. Buat Service untuk ALPR**

Buat service `ALPRService` untuk mengintegrasikan PlateRecognizer API:

```java
package com.parkingsystem.service;

import okhttp3.*;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

@Service
public class ALPRService {
    private static final String PLATE_RECOGNIZER_URL = "https://api.platerecognizer.com/v1/plate-reader";
    private static final String API_KEY = "YOUR_PLATE_RECOGNIZER_API_KEY"; // Ganti dengan API key Anda

    public String recognizePlate(String imagePath) throws IOException {
        // Setup client HTTP
        OkHttpClient client = new OkHttpClient();

        // Buat request multipart untuk mengirim gambar
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload", imagePath, RequestBody.create(new File(imagePath), MediaType.parse("image/jpeg")))
                .addFormDataPart("regions", "id") // Set region ke Indonesia
                .build();

        // Buat HTTP request
        Request request = new Request.Builder()
                .url(PLATE_RECOGNIZER_URL)
                .header("Authorization", "Token " + API_KEY)
                .post(requestBody)
                .build();

        // Kirim request dan dapatkan response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Parse response JSON
            String responseBody = response.body().string();
            // Contoh response: {"results": [{"plate": "B1234ABC", "confidence": 0.98, ...}]}
            // Anda perlu parse JSON untuk mendapatkan nomor plat
            // Untuk simpel, kita asumsikan menggunakan library JSON (misalnya Jackson)
            return parsePlateFromResponse(responseBody);
        }
    }

    private String parsePlateFromResponse(String responseBody) {
        // Gunakan library JSON seperti Jackson untuk parse response
        // Untuk demo, kita gunakan string parsing sederhana (tidak disarankan untuk produksi)
        int plateIndex = responseBody.indexOf("\"plate\":") + 9;
        int endIndex = responseBody.indexOf("\"", plateIndex);
        return responseBody.substring(plateIndex, endIndex);
    }
}
```

Catatan: Untuk parsing JSON yang lebih baik, tambahkan dependensi `Jackson` di `pom.xml`:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

Lalu ubah `parsePlateFromResponse` menjadi:

```java
private String parsePlateFromResponse(String responseBody) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(responseBody);
    JsonNode results = root.path("results");
    if (results.isArray() && results.size() > 0) {
        return results.get(0).path("plate").asText();
    }
    return null; // Atau lempar exception jika tidak ditemukan
}
```

##### **1.4. Simpan Gambar dari Kamera**

Tambahkan logika untuk menyimpan gambar dari kamera ke server. Dalam aplikasi Anda, ada halaman "Kendaraan Masuk" yang menampilkan gambar kamera.

```java
@Service
public class ImageService {
    private static final String IMAGE_STORAGE_PATH = "uploads/";

    public String saveImage(MultipartFile image) throws IOException {
        File directory = new File(IMAGE_STORAGE_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        String filePath = IMAGE_STORAGE_PATH + fileName;
        image.transferTo(new File(filePath));
        return filePath;
    }
}
```

##### **1.5. Buat Controller untuk Mencatat Kendaraan Masuk**

Tambahkan endpoint untuk mencatat kendaraan masuk, termasuk pembacaan nomor plat via ALPR.

```java
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    private ALPRService alprService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/entry")
    public ResponseEntity<VehicleDTO> recordEntry(@RequestParam("image") MultipartFile image) throws IOException {
        // Simpan gambar
        String imagePath = imageService.saveImage(image);

        // Gunakan ALPR untuk membaca nomor plat
        String licensePlate = alprService.recognizePlate(imagePath);
        if (licensePlate == null) {
            throw new RuntimeException("Gagal membaca nomor plat");
        }

        // Simpan data kendaraan
        VehicleDTO vehicle = vehicleService.recordEntry(licensePlate, imagePath);
        return ResponseEntity.ok(vehicle);
    }
}
```

##### **1.6. Tambahkan Fallback untuk Input Manual**

Jika ALPR gagal membaca nomor plat (misalnya gambar buram), operator harus bisa memasukkan nomor plat secara manual.

```java
@PostMapping("/entry/manual")
public ResponseEntity<VehicleDTO> recordEntryManual(
        @RequestParam("licensePlate") String licensePlate,
        @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
    String imagePath = image != null ? imageService.saveImage(image) : null;
    VehicleDTO vehicle = vehicleService.recordEntry(licensePlate, imagePath);
    return ResponseEntity.ok(vehicle);
}
```

##### **1.7. Integrasi dengan Kamera**

Untuk mendapatkan gambar dari kamera secara real-time, gunakan RTSP stream (misalnya dengan library `OpenCV` di Java). Tambahkan dependensi OpenCV:

```xml
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.8</version>
</dependency>
```

Contoh kode untuk menangkap gambar dari kamera:

```java
@Service
public class CameraService {
    public String captureImageFromCamera(String rtspUrl) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        grabber.start();

        Frame frame = grabber.grabImage();
        if (frame == null) {
            throw new RuntimeException("Gagal menangkap gambar dari kamera");
        }

        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage image = converter.convert(frame);

        String imagePath = "uploads/" + System.currentTimeMillis() + ".jpg";
        ImageIO.write(image, "jpg", new File(imagePath));

        grabber.stop();
        return imagePath;
    }
}
```

Panggil service ini dari controller:

```java
@PostMapping("/capture")
public ResponseEntity<VehicleDTO> captureAndRecordEntry() throws Exception {
    String rtspUrl = "rtsp://your-camera-url"; // Ganti dengan URL kamera Anda
    String imagePath = cameraService.captureImageFromCamera(rtspUrl);
    return recordEntry(new MockMultipartFile("image", new FileInputStream(imagePath)));
}
```

##### **1.8. Penanganan Error dan Optimasi**

- Tambahkan retry mechanism jika ALPR gagal (misalnya gambar buram).
- Optimasi performa: Pastikan waktu pemrosesan ALPR <250 ms (PlateRecognizer biasanya cukup cepat).
- Tambahkan logging untuk debugging:

```java
@Slf4j
@Service
public class ALPRService {
    public String recognizePlate(String imagePath) throws IOException {
        log.info("Memproses gambar: {}", imagePath);
        // ... (kode seperti di atas)
        log.info("Nomor plat dikenali: {}", licensePlate);
        return licensePlate;
    }
}
```

---

#### **Alternatif: Implementasi dengan OpenALPR**

Jika Anda memilih OpenALPR:

1. Install OpenALPR di server:
   ```
   sudo apt-get install openalpr openalpr-daemon openalpr-utils libopenalpr-dev
   ```
2. Konfigurasi untuk plat Indonesia (edit `openalpr.conf` untuk mendukung pattern plat Indonesia).
3. Panggil OpenALPR dari Java:

```java
public String recognizePlateWithOpenALPR(String imagePath) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("alpr", "-c", "id", imagePath);
    Process process = pb.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = reader.readLine()) != null) {
        if (line.contains("confidence")) {
            return line.split(" ")[1]; // Ambil nomor plat
        }
    }
    return null;
}
```

---

### **2. Desain UI**

Berdasarkan screenshot, aplikasi Anda memiliki UI yang terdiri dari sidebar kiri untuk menu, header dengan informasi pengguna, dan konten utama di tengah. Saya akan mendesain UI menggunakan **React** (karena lebih fleksibel untuk aplikasi dinamis) dan memastikan desainnya konsisten dengan screenshot.

#### **2.1. Struktur Komponen React**

Struktur direktori React:

```
parking-system-frontend/
├── src/
│   ├── components/
│   │   ├── Sidebar.js         # Sidebar untuk menu
│   │   ├── Header.js          # Header dengan info pengguna
│   │   ├── Dashboard.js       # Halaman dashboard
│   │   ├── VehicleEntry.js    # Halaman Kendaraan Masuk
│   │   ├── VehicleExit.js     # Halaman Kendaraan Keluar
│   │   ├── ParkingSlots.js    # Manajemen slot parkir
│   │   ├── Operators.js       # Manajemen operator
│   │   ├── Shifts.js          # Manajemen shift
│   │   ├── Rates.js           # Manajemen tarif
│   │   ├── Reports.js         # Laporan
│   │   ├── Analytics.js       # Analisis
│   │   └── Settings.js        # Pengaturan
│   ├── App.js
│   ├── App.css
│   └── index.js
├── package.json
└── README.md
```

#### **2.2. Desain Sidebar**

Buat komponen `Sidebar.js` yang mencerminkan menu di screenshot:

```jsx
import React from "react";
import { NavLink } from "react-router-dom";
import "./Sidebar.css";

const Sidebar = () => {
  return (
    <div className="sidebar">
      <h2>MENU UTAMA</h2>
      <ul>
        <li>
          <NavLink to="/dashboard" activeClassName="active">
            Dashboard
          </NavLink>
        </li>
        <li>
          <NavLink to="/vehicle-entry" activeClassName="active">
            Kendaraan Masuk
          </NavLink>
        </li>
        <li>
          <NavLink to="/vehicle-exit" activeClassName="active">
            Kendaraan Keluar
          </NavLink>
        </li>
        <li>
          <NavLink to="/reports" activeClassName="active">
            Riwayat
          </NavLink>
        </li>
        <li>
          <NavLink to="/reports-daily" activeClassName="active">
            Laporan
          </NavLink>
        </li>
        <li>
          <NavLink to="/analytics" activeClassName="active">
            Analisis
          </NavLink>
        </li>
        <li>
          <NavLink to="/slots" activeClassName="active">
            Kelola Slot
          </NavLink>
        </li>
        <li>
          <NavLink to="/operators" activeClassName="active">
            Kelola Operator
          </NavLink>
        </li>
        <li>
          <NavLink to="/shifts" activeClassName="active">
            Kelola Shift
          </NavLink>
        </li>
        <li>
          <NavLink to="/rates" activeClassName="active">
            Tarif Parkir
          </NavLink>
        </li>
        <li>
          <NavLink to="/settings" activeClassName="active">
            Pengaturan
          </NavLink>
        </li>
        <li>
          <NavLink to="/logout" activeClassName="active">
            Keluar
          </NavLink>
        </li>
      </ul>
    </div>
  );
};

export default Sidebar;
```

CSS (`Sidebar.css`):

```css
.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: white;
  height: 100vh;
  position: fixed;
  padding: 20px;
}

.sidebar h2 {
  font-size: 20px;
  margin-bottom: 20px;
}

.sidebar ul {
  list-style: none;
  padding: 0;
}

.sidebar li {
  margin: 10px 0;
}

.sidebar a {
  color: white;
  text-decoration: none;
  display: flex;
  align-items: center;
}

.sidebar a.active {
  background-color: #3498db;
  padding: 5px;
  border-radius: 5px;
}
```

#### **2.3. Desain Header**

Buat komponen `Header.js` untuk header seperti di screenshot:

```jsx
import React from "react";
import "./Header.css";

const Header = () => {
  return (
    <div className="header">
      <h1>Sistem Manajemen Parkir</h1>
      <div className="user-info">
        <span>admin</span>
        <button>Logout</button>
      </div>
    </div>
  );
};

export default Header;
```

CSS (`Header.css`):

```css
.header {
  background-color: #ecf0f1;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: fixed;
  top: 0;
  left: 250px;
  right: 0;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-info span {
  margin-right: 10px;
}
```

#### **2.4. Desain Halaman Kendaraan Masuk (VehicleEntry)**

Halaman ini akan menampilkan gambar dari kamera, nomor plat yang terdeteksi, dan form untuk input manual jika ALPR gagal.

```jsx
import React, { useState } from "react";
import axios from "axios";
import "./VehicleEntry.css";

const VehicleEntry = () => {
  const [image, setImage] = useState(null);
  const [licensePlate, setLicensePlate] = useState("");
  const [vehicleType, setVehicleType] = useState("Car");

  const captureImage = async () => {
    try {
      const response = await axios.post(
        "http://localhost:8080/api/vehicles/capture"
      );
      setImage(response.data.imagePath);
      setLicensePlate(response.data.licensePlate);
    } catch (error) {
      console.error("Gagal menangkap gambar:", error);
    }
  };

  const handleManualEntry = async () => {
    try {
      await axios.post("http://localhost:8080/api/vehicles/entry/manual", {
        licensePlate,
        vehicleType,
      });
      alert("Kendaraan berhasil dicatat");
    } catch (error) {
      console.error("Gagal mencatat kendaraan:", error);
    }
  };

  return (
    <div className="vehicle-entry">
      <h2>Kendaraan Masuk</h2>
      <div className="camera-section">
        <button onClick={captureImage}>Ambil Foto</button>
        {image && (
          <img src={`http://localhost:8080/${image}`} alt="Kendaraan" />
        )}
      </div>
      <div className="details-section">
        <h3>Detail Kendaraan</h3>
        <label>Nomor Plat:</label>
        <input
          type="text"
          value={licensePlate}
          onChange={(e) => setLicensePlate(e.target.value)}
        />
        <label>Jenis Kendaraan:</label>
        <select
          value={vehicleType}
          onChange={(e) => setVehicleType(e.target.value)}
        >
          <option value="Car">Mobil</option>
          <option value="Motorcycle">Motor</option>
          <option value="Truck">Truk</option>
        </select>
        <button onClick={handleManualEntry}>Catat Kendaraan Masuk</button>
      </div>
    </div>
  );
};

export default VehicleEntry;
```

CSS (`VehicleEntry.css`):

```css
.vehicle-entry {
  margin-left: 250px;
  padding: 80px 20px 20px 20px;
}

.camera-section {
  margin-bottom: 20px;
}

.camera-section img {
  max-width: 500px;
  margin-top: 10px;
}

.details-section label {
  display: block;
  margin: 10px 0 5px;
}

.details-section input,
.details-section select {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
}
```

#### **2.5. Desain Halaman Dashboard**

Halaman dashboard menampilkan ringkasan seperti total pendapatan, jumlah kendaraan, dan grafik (seperti di screenshot pertama).

```jsx
import React, { useEffect, useState } from "react";
import axios from "axios";
import { Chart } from "react-chartjs-2";
import "chart.js/auto";

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalRevenue: 0,
    totalVehicles: 0,
    occupancyRate: 0,
  });
  const [monthlyRevenue, setMonthlyRevenue] = useState([]);
  const [vehicleTypes, setVehicleTypes] = useState({
    cars: 0,
    motorcycles: 0,
    trucks: 0,
  });

  useEffect(() => {
    // Ambil data statistik
    axios
      .get("http://localhost:8080/api/dashboard/stats")
      .then((response) => setStats(response.data));

    // Ambil data pendapatan bulanan
    axios
      .get("http://localhost:8080/api/dashboard/monthly-revenue")
      .then((response) => setMonthlyRevenue(response.data));

    // Ambil distribusi jenis kendaraan
    axios
      .get("http://localhost:8080/api/dashboard/vehicle-types")
      .then((response) => setVehicleTypes(response.data));
  }, []);

  const revenueChartData = {
    labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun"],
    datasets: [
      {
        label: "Pendapatan Bulanan",
        data: monthlyRevenue,
        borderColor: "#3498db",
        fill: true,
      },
    ],
  };

  const vehicleTypeChartData = {
    labels: ["Mobil", "Motor", "Truk"],
    datasets: [
      {
        data: [
          vehicleTypes.cars,
          vehicleTypes.motorcycles,
          vehicleTypes.trucks,
        ],
        backgroundColor: ["#3498db", "#2ecc71", "#e74c3c"],
      },
    ],
  };

  return (
    <div className="dashboard">
      <h2>Dashboard</h2>
      <div className="stats">
        <div className="stat-card">
          <h3>Total Pendapatan</h3>
          <p>Rp {stats.totalRevenue.toLocaleString()}</p>
        </div>
        <div className="stat-card">
          <h3>Total Kendaraan</h3>
          <p>{stats.totalVehicles}</p>
        </div>
        <div className="stat-card">
          <h3>Tingkat Okupansi</h3>
          <p>{stats.occupancyRate}%</p>
        </div>
      </div>
      <div className="charts">
        <div className="chart">
          <h3>Pendapatan Bulanan 2025</h3>
          <Chart type="line" data={revenueChartData} />
        </div>
        <div className="chart">
          <h3>Distribusi Jenis Kendaraan</h3>
          <Chart type="doughnut" data={vehicleTypeChartData} />
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
```

CSS (`Dashboard.css`):

```css
.dashboard {
  margin-left: 250px;
  padding: 80px 20px 20px 20px;
}

.stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background-color: #ecf0f1;
  padding: 20px;
  border-radius: 5px;
  flex: 1;
  text-align: center;
}

.charts {
  display: flex;
  gap: 20px;
}

.chart {
  flex: 1;
  background-color: #ecf0f1;
  padding: 20px;
  border-radius: 5px;
}
```

#### **2.6. Routing di React**

Gunakan `react-router-dom` untuk navigasi antar halaman:

```jsx
import React from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import Dashboard from "./components/Dashboard";
import VehicleEntry from "./components/VehicleEntry";
// Tambahkan import untuk komponen lain

const App = () => {
  return (
    <Router>
      <Sidebar />
      <Header />
      <Switch>
        <Route path="/dashboard" component={Dashboard} />
        <Route path="/vehicle-entry" component={VehicleEntry} />
        {/* Tambahkan route untuk halaman lain */}
      </Switch>
    </Router>
  );
};

export default App;
```

---

### **Kesimpulan**

- **Implementasi ALPR**:
  - Menggunakan PlateRecognizer adalah cara tercepat karena API-nya mudah diintegrasikan dengan Spring Boot.
  - Tambahkan logika untuk menangkap gambar dari kamera (via RTSP) dan simpan ke server.
  - Berikan opsi input manual sebagai fallback jika ALPR gagal.
- **Desain UI**:
  - Gunakan React untuk membangun UI yang dinamis.
  - Sidebar mencerminkan menu di screenshot, header menunjukkan informasi pengguna, dan konten utama menampilkan halaman seperti Kendaraan Masuk dan Dashboard.
  - Dashboard menampilkan statistik dan grafik seperti di screenshot pertama (pendapatan bulanan, distribusi jenis kendaraan).
