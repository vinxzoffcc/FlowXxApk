# FlowX APK — Panduan Build

## Apa ini?
FlowX versi **APK native Android** — bukan PWA. Fitur utama:
- ✅ **Overlay HUD beneran** di atas game (persis Meter Info ADV)
- ✅ **Foreground Service** — jalan terus di background
- ✅ **Drag to move** — seret widget ke posisi manapun
- ✅ **Data real** — CPU freq dari `/sys`, RAM dari ActivityManager, suhu dari BatteryManager
- ✅ **Shizuku** — jalankan settings put tanpa root
- ✅ **FlowX AI** — chat Gemini 2.0 Flash
- ✅ **6 Modul boost** — FPS, Thermal, Network, RAM, Graphics, Game

---

## Cara Build di Android Studio

### Syarat:
- Android Studio Hedgehog / Iguana ke atas
- JDK 17
- Android SDK 34

### Langkah:

1. **Buka Android Studio** → File → Open → pilih folder `FlowX-APK`

2. **Tunggu Gradle sync** selesai (perlu internet untuk download Shizuku dependency)

3. **Build APK:**
   - Menu: Build → Build Bundle(s)/APK(s) → Build APK(s)
   - Atau tekan `Ctrl+Shift+A` → cari "Build APK"

4. **APK ada di:**
   ```
   FlowX-APK/app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Install ke HP:**
   - Enable "Install from Unknown Sources" di HP
   - Transfer APK via USB / Telegram ke HP sendiri
   - Tap install

---

## Permissions yang diminta saat pertama buka:

| Permission | Kenapa |
|---|---|
| Draw Over Other Apps | Wajib untuk HUD overlay di atas game |
| Post Notifications | Notifikasi service aktif di status bar |
| Shizuku API | Jalankan adb shell commands |

---

## Cara aktifkan HUD overlay:

1. Buka FlowX → tab **HUD**
2. Tap **Aktifkan HUD**
3. Widget muncul di layar — seret ke posisi nyaman
4. Buka game — widget tetap tampil di atas game ✅
5. Tap area widget untuk seret pindah posisi

---

## Struktur Project:

```
FlowX-APK/
├── app/src/main/
│   ├── AndroidManifest.xml          ← permissions
│   ├── java/com/vinz/flowx/
│   │   ├── MainActivity.kt          ← main screen + nav
│   │   ├── service/
│   │   │   └── OverlayService.kt    ← inti HUD overlay ⭐
│   │   ├── ui/
│   │   │   ├── BoostFragment.kt     ← tab Boost
│   │   │   ├── HudFragment.kt       ← tab HUD
│   │   │   ├── AiFragment.kt        ← tab AI
│   │   │   └── SettingsFragment.kt  ← tab Setelan
│   │   └── utils/
│   │       ├── SystemStats.kt       ← baca CPU/RAM/suhu real
│   │       └── ShizukuHelper.kt     ← jalankan adb commands
│   └── res/
│       ├── layout/overlay_hud.xml   ← tampilan widget HUD
│       └── drawable/bg_overlay_hud.xml
├── app/build.gradle                 ← dependencies + Shizuku
└── settings.gradle
```

---

## Notes:

- **FPS counter**: Android tidak expose FPS ke app non-system. Yang ditampilkan adalah estimasi dari CPU load. Untuk FPS akurat perlu Game Mode API Samsung / Xiaomi atau root.
- **CPU Frequency**: dibaca langsung dari `/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq` — real dan akurat.
- **Suhu**: dari BatteryManager (suhu baterai), bukan CPU. Untuk suhu CPU perlu akses `/sys/class/thermal/` yang butuh root di beberapa device.
- **Shizuku**: pastikan Shizuku sudah aktif via Wireless Debugging sebelum pakai fitur boost.
