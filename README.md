# Fit & Fat: Food Rush 🍔🥗

โปรเจกต์เกมมือถือ 2D Fast-Reflex สำหรับรายวิชา **CS364 การพัฒนาโปรแกรมประยุกต์สำหรับอุปกรณ์พกพา (ปีการศึกษา 2569)** พัฒนาด้วย **Android Kotlin + Jetpack Compose**

แยกป้อนอาหารให้ **คนผอม (พลังงานสูง)** และ **คนอ้วน (อาหารคลีน)** พร้อมปัดทิ้ง **ขยะ/ของอันตราย** ที่เร่งความเร็วขึ้นเรื่อยๆ จนถึงเพดานคงที่ (Max Speed Cap) พลาด 1 ครั้งแพ้ทันที พร้อมระบบสั่น **Haptic Feedback**

---

## 📚 เอกสารโครงงาน

| ไฟล์ | หน้าที่ |
| :--- | :--- |
| [AGENTS.md](./AGENTS.md) | **คัมภีร์หลักสำหรับคนในทีมและ AI (Auto-Loaded):** สเปกเกม กฎเหล็ก CS364 โครงสร้างโค้ด Kotlin และ PR Template |
| [docs/REPORT_AND_AI_LOG.md](./docs/REPORT_AND_AI_LOG.md) | เช็กลิสต์ตรวจคะแนน 20 ข้อ, บันทึกปัญหาเทคนิค Before/After และตารางจดการใช้ AI/สื่อ (เกณฑ์ข้อ 5.2) |

---

## 👥 สมาชิกทีม 5 คน & การแบ่งหน้าที่ (เลือกจับจองได้เลย)

| Role | ชื่อ-รหัสสมาชิก | ไฟล์ / หน้าจอที่รับผิดชอบ | เกณฑ์ CS364 ที่รับผิดชอบในรายงาน |
| :--- | :--- | :--- | :--- |
| **Role 1: Game Engine & Speed Mechanics** | `<ชื่อ + รหัส>` | `game/GameEngine.kt`, `SpeedController.kt`, `FoodItem.kt` (ระบบคิวจาน, สูตร Speed Cap, Delta Time) | ข้อ 1.4, 2.1–2.4, 5.3 |
| **Role 2: Gameplay UI & Lifecycle Guard** | `<ชื่อ + รหัส>` | `ui/screens/GameplayScreen.kt`, `PauseExitDialog.kt`, `CharacterView.kt` (กระดานเล่น, ปุ่ม 3 ทาง, Pause/Resume) | ข้อ 4.2, 4.4 |
| **Role 3: Menu, Navigation, Haptics & Audio** | `<ชื่อ + รหัส>` | `ui/screens/MainMenuScreen.kt`, `AppNavGraph.kt`, `HapticAudioManager.kt` (หน้าแรก, เส้นทางหน้าจอ, สั่น & เสียง) | ข้อ 1.3, 4.1, 5.1 |
| **Role 4: Game Over, Score DB & Delete Guard** | `<ชื่อ + รหัส>` | `ui/screens/GameOverScreen.kt`, `ScoreHistoryScreen.kt`, `ConfirmDeleteDialog.kt`, `ScoreRepository.kt` | ข้อ 1.1, 4.3 |
| **Role 5: Settings, Day/Night, TH/EN & QA** | `<ชื่อ + รหัส>` | `ui/screens/SettingsScreen.kt`, `ui/theme/*`, `SettingsPreferences.kt`, `res/values*/strings.xml` + รูป/เสียงหลัก | ข้อ 1.2, 3.1–3.4, 5.2 |
