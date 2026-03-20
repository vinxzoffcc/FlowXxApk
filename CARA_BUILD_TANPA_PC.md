# Cara Build FlowX APK Tanpa PC — Pakai GitHub Actions (GRATIS)

GitHub Actions build APK di server cloud mereka. Kamu tinggal upload file, klik Run, tunggu ~5 menit, APK siap didownload langsung dari HP.

---

## Langkah-langkah (semua dari HP Chrome)

### 1. Buat akun GitHub
Buka **github.com** → Sign up (gratis, pakai email)

---

### 2. Buat repository baru
- Klik tombol **+** di pojok kanan atas → **New repository**
- Nama: `FlowX`
- Pilih **Public**
- Klik **Create repository**

---

### 3. Upload semua file project
- Di halaman repo, klik **uploading an existing file**
- Extract ZIP `FlowX_APK_Project.zip` dulu, lalu upload semua file/folder
- **Atau cara lebih gampang:** pakai github.dev (lihat step 4)

---

### 4. Cara upload via github.dev (lebih gampang dari HP)
- Setelah buat repo kosong, ganti URL dari:
  `github.com/username/FlowX`
  jadi:
  `github.dev/username/FlowX`
- Muncul editor VSCode di browser
- Drag & drop semua file dari ZIP ke sini
- Klik ikon Save/Commit (tanda centang)

---

### 5. Jalankan build
- Buka tab **Actions** di repo
- Klik workflow **Build FlowX APK**
- Klik **Run workflow** → **Run workflow** (hijau)
- Tunggu ~3-5 menit

---

### 6. Download APK
- Setelah build selesai (✅ hijau), klik nama workflow-nya
- Scroll bawah → bagian **Artifacts**
- Klik **FlowX-debug** → ZIP terdownload
- Extract → ada `app-debug.apk`
- Install di HP!

---

## Saat install APK:
1. HP minta izin "Install unknown apps" → Allow
2. Install normal
3. Buka FlowX → izinkan **Draw Over Other Apps** waktu diminta
4. Tab HUD → Aktifkan HUD → widget muncul di layar
5. Buka game — widget tetap di atas ✅

---

## Kalau mau update app nanti:
Tinggal edit file di github.dev → commit → Actions otomatis build ulang → download APK baru
