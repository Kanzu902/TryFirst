# TryFirst Backend API

Backend server untuk aplikasi Android TryFirst — dibangun dengan Ktor (Kotlin).

## 📁 Struktur Project

```
tryfirst-backend/
├── src/main/kotlin/com/tryfirst/api/
│   ├── Application.kt              ← Entry point (seperti MainActivity di Android)
│   ├── models/
│   │   └── Models.kt               ← Data class (seperti PracticeEntity di Android)
│   ├── services/
│   │   ├── GeminiService.kt        ← Sama persis dengan GeminiService di Android
│   │   └── PracticeRepository.kt   ← Sama persis dengan PracticeRepository di Android
│   ├── routes/
│   │   └── Routes.kt               ← Semua endpoint API
│   └── plugins/
│       └── Plugins.kt              ← Konfigurasi JSON, CORS, Database, Error handling
└── src/main/resources/
    └── application.yaml            ← Konfigurasi server
```

## 🚀 Cara Menjalankan (Development)

### 1. Clone dan masuk ke folder
```bash
git clone <repo-url>
cd tryfirst-backend
```

### 2. Setup environment variable
```bash
cp .env.example .env
# Edit .env, isi GEMINI_API_KEY dengan key kamu
```

### 3. Jalankan server
```bash
./gradlew run
```

Server berjalan di `http://localhost:8080`

---

## 📡 Endpoint API

### Health Check
```
GET /
Response: { "status": "TryFirst API is running! 🚀" }
```

### Feedback Writing
```
POST /feedback/writing
Body: { "userInput": "I go to school yesterday" }
Body: { "userInput": "...", "question": "soalnya" }  ← jika ada soal

Response:
{
  "success": true,
  "data": {
    "feedback": "1. ✅ Yang sudah benar: ...",
    "type": "writing"
  }
}
```

### Feedback Speaking
```
POST /feedback/speaking
Body: { "userInput": "I am go to school" }

Response:
{
  "success": true,
  "data": {
    "feedback": "1. ✅ Kalimat yang terdeteksi: ...",
    "type": "speaking"
  }
}
```

### History
```
GET    /history          ← Semua riwayat latihan
GET    /history/stats    ← Statistik (total, writing, speaking)
POST   /history          ← Simpan manual
DELETE /history/{id}     ← Hapus satu item
DELETE /history          ← Hapus semua
```

---

## ☁️ Deploy ke Railway (Gratis)

1. Push project ke GitHub
2. Buka [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Tambah environment variable: `GEMINI_API_KEY=...`
4. Railway otomatis build dan deploy!

URL backend kamu akan jadi: `https://tryfirst-backend-xxxx.railway.app`

---

## 📱 Integrasi ke Android (TryFirst)

Setelah deploy, update base URL di Android:

```kotlin
// Di Android kamu, ganti URL Gemini langsung
// menjadi panggil server ini via Retrofit/Ktor Client

const val BASE_URL = "https://tryfirst-backend-xxxx.railway.app"
```
