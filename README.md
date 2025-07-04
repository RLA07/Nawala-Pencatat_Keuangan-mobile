<div align="center">

<img src="https://raw.githubusercontent.com/RLA07/Nawala-Pencatat_Keuangan-mobile/main/art/Logo-Fit.png" width="180" alt="Logo Nawala">

Aplikasi pencatat keuangan pribadi untuk Android yang simpel, privat, dan bekerja penuh secara offline. Dibangun secara native menggunakan Java dengan praktik terbaik untuk membantu Anda memahami alur kas dan mengelola finansial dengan mudah. Proyek ini adalah studi kasus dalam membangun aplikasi Android native yang rapi, modern, dan mengikuti kaidah pengembangan yang baik.

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-brightgreen.svg" alt="Platform">
  <img src="https://img.shields.io/badge/API-21%2B-brightgreen.svg" alt="API Level 21+">
  <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License">
</p>

</div>

---

## ✨ Fitur Utama

- [x] **Privasi Terjamin & 100% Offline:** Tidak ada data yang dikirim ke server. Semua informasi keuangan Anda disimpan secara aman di dalam perangkat menggunakan `Room Database`.
- [x] **Dashboard Interaktif:** Ringkasan visual saldo, total pemasukan, dan total pengeluaran bulan ini yang dihitung secara _real-time_.
- [x] **Manajemen Transaksi CRUD Penuh:**
  - `[✓]` **Create:** Menambah data transaksi baru (pemasukan/pengeluaran).
  - `[✓]` **Read:** Menampilkan riwayat transaksi yang dikelompokkan per tanggal.
  - `[✓]` **Update:** Mengubah data transaksi yang sudah ada.
  - `[✓]` **Delete:** Menghapus data transaksi dengan mudah.
- [x] **Desain Modern & Intuitif:** Antarmuka yang bersih dan mudah digunakan berdasarkan prinsip `Material Design`, nyaman diakses di berbagai ukuran layar.
- [x] **Pengalaman Pengguna (UX) Ditingkatkan:** Fitur seperti pemisahan sumber dana (Bank/Dompet), tombol dengan kode warna, dan input tanggal yang interaktif.

---

## 🚀 Teknologi Yang Dipakai

<div align="center">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
    <img src="https://img.shields.io/badge/Room_DB-8A644D?style=for-the-badge&logo=android-studio&logoColor=white" alt="Room DB">
    <img src="https://img.shields.io/badge/Material_Design-757575?style=for-the-badge&logo=material-design&logoColor=white" alt="Material Design">
</div>

---

## 🛠️ Panduan Instalasi & Setup

Ikuti langkah-langkah berikut untuk menjalankan proyek ini di lingkungan lokal Anda.

### **1. Prasyarat**

Pastikan Anda sudah menginstal perangkat lunak berikut:

- **Android Studio** (Versi Bumblebee | 2021.1.1 atau lebih baru)
- **JDK 11** atau lebih tinggi
- **Android 5.0 (Lollipop) / API Level 21** atau lebih tinggi (untuk perangkat target)

### **2. Langkah-langkah Setup**

1.  **Clone Repositori**
    Buka terminal dan jalankan perintah berikut:

    ```bash
    git clone https://github.com/RLA07/Nawala-Pencatat_Keuangan-mobile.git
    ```

2.  **Buka Proyek**

    - Jalankan Android Studio.
    - Pilih **"Open an Existing Project"**.
    - Navigasikan ke folder tempat Anda melakukan clone proyek, lalu klik **"OK"**.

3.  **Sinkronisasi Gradle**

    - Biarkan Android Studio melakukan sinkronisasi Gradle secara otomatis. Proses ini akan mengunduh semua dependensi yang diperlukan oleh proyek.

4.  **Jalankan Aplikasi**
    - Pilih target (Emulator atau perangkat fisik Android).
    - Klik tombol **"Run 'app'"** (ikon ▶️).

---

## 🖼️ Tampilan Aplikasi

<details>
<summary>Klik untuk melihat screenshot</summary>
<br>
<table>
  <tr>
    <td><center>Layar Utama (Dashboard)</center></td>
  </tr>
  <tr>
    <td><center><img src="https://raw.githubusercontent.com/RLA07/Nawala-Pencatat_Keuangan-mobile/main/art/dashboard.jpg" width="60%" alt="Layar Utama"></center></td>
  </tr>
  <tr>
    <td><center>Layar Detail Saldo</center></td>
  </tr>
  <tr>
    <td><center><img src="https://raw.githubusercontent.com/RLA07/Nawala-Pencatat_Keuangan-mobile/main/art/detail_saldo.jpg" width="60%" alt="Layar Detail Saldo"></center></td>
  </tr>
  <tr>
    <td><center>Tambah Transaksi</center></td>
  </tr>
  <tr>
    <td><center><img src="https://raw.githubusercontent.com/RLA07/Nawala-Pencatat_Keuangan-mobile/main/art/tambah_transaksi.jpg" width="60%" alt="Formulir Transaksi"></center></td>
  </tr>
  <tr>
    <td><center>Edit Transaksi</center></td>
  </tr>
  <tr>
    <td><center><img src="https://raw.githubusercontent.com/RLA07/Nawala-Pencatat_Keuangan-mobile/main/art/edit_transaksi.jpg" width="60%" alt="Formulir Transaksi"></center></td>
  </tr>
</table>
</details>

---

## 📁 Struktur Proyek

Struktur folder ini dirancang untuk kerapian dan kemudahan pengelolaan, dengan fokus pada direktori `app/src/main` tempat semua kode sumber utama berada. File-file yang dibuat otomatis oleh Gradle (seperti `build/` dan `.gradle/`) sengaja tidak ditampilkan untuk kejelasan.

```
.
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/nawala/keuangan/        # Logika Inti & Kode Java
│   │   │   │   ├── AppDatabase.java             # Konfigurasi Room Database
│   │   │   │   ├── DetailSaldoActivity.java     # Layar Rincian Saldo
│   │   │   │   ├── EditTransaksiActivity.java   # Layar Edit Transaksi
│   │   │   │   ├── MainActivity.java            # Layar Utama (Dashboard)
│   │   │   │   ├── TambahTransaksiActivity.java # Layar Tambah Transaksi
│   │   │   │   ├── Transaction.java             # Model/Entity untuk tabel transaksi
│   │   │   │   ├── TransactionAdapter.java      # Adapter untuk RecyclerView
│   │   │   │   └── TransactionDao.java          # Interface untuk operasi database
│   │   │   │
│   │   │   ├── res/                        # Semua resource aplikasi
│   │   │   │   ├── drawable/               # Aset gambar & bentuk (shape) kustom
│   │   │   │   ├── layout/                 # File XML untuk desain antarmuka
│   │   │   │   ├── mipmap-hdpi/            # Ikon aplikasi untuk berbagai resolusi
│   │   │   │   └── values/                 # File resource (string, warna, tema)
│   │   │   │
│   │   │   └── AndroidManifest.xml         # Konfigurasi utama aplikasi
│   │
│   └── build.gradle                      # Konfigurasi dependensi & build modul app
│
└── build.gradle                          # Konfigurasi build level proyek
```

---

## 📝 Rencana Pengembangan

Beberapa fitur dan perbaikan yang direncanakan untuk pengembangan selanjutnya:

- [ ] Implementasi Unit Testing & UI Testing.
- [ ] Fitur ekspor data ke format CSV atau PDF.
- [ ] Fitur penganggaran (budgeting) bulanan per kategori.
- [ ] Fitur pencarian dan filter transaksi.

---

## ⚖️ Lisensi

Didistribusikan di bawah Lisensi MIT. Lihat [`LICENSE`](https://github.com/RLA07/Nawala-Pencatat_Keuangan-mobile/blob/main/LICENSE) untuk informasi lebih lanjut.

---

<div align="center">
Dibuat dengan begadang dan secangkir semangat ☕
</div>
