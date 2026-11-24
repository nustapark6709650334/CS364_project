# CS364 Rubric Checklist, Technical Challenge & AI/Media Log

ไฟล์นี้ใช้สำหรับ **เตรียมทำรายงานและตรวจคะแนน 20/20** ตามเกณฑ์ CS364 ปีการศึกษา 2569 (AI ไม่จำเป็นต้องอ่านไฟล์นี้ระหว่างเขียนโค้ดปกติ ให้อ่านแค่ตอนอัปเดตประวัติหรือช่วยทำรายงาน)

---

## 1. CS364 Rubric Checklist (เป้าหมาย 4 คะแนนเต็มทั้ง 5 หมวด)

### ข้อ 1: ความสมบูรณ์ของงานและหลักฐานในรายงาน
- [ ] **1.1** ตารางฟังก์ชันครบตามขอบเขต พร้อมระบุหน้าจอและผลลัพธ์ของแต่ละฟังก์ชัน
- [ ] **1.2** ตารางผลทดสอบทุกฟังก์ชัน (วิธีทดสอบ / ผลที่คาดหวัง / ผลที่ได้จริง)
- [ ] **1.3** เชื่อมโยงเนื้อหาวิชา CS364 (Layout, Activity Lifecycle, Back Navigation, Localization TH/EN, Day/Night Theme, Storage, Hardware Vibrator API)
- [x] **1.4** เชื่อมโยงรายวิชาอื่น: คณิตศาสตร์/ฟิสิกส์ (ฟังก์ชัน $v(s) = \min(v_{\max}, v_0 + k \cdot \lfloor s/5 \rfloor)$ และ Delta Time `dt`) + HCI/UX (Haptic Feedback & Error Prevention)

### ข้อ 2: ความสามารถของแอปพลิเคชันและความท้าทายเชิงเทคนิค
- [x] **2.1** อธิบายกลไกฟังก์ชันหลักอย่างน้อย 1 ฟังก์ชันตั้งแต่รับ Input จนได้ผลลัพธ์ (เช่น Game Loop + Speed Controller + Matching Validation พร้อมโค้ด/Flowchart)
- [x] **2.2** ระบุความท้าทายทางเทคนิค ≥1 เรื่อง (พบปัญหาอะไร ในฟังก์ชันใด และเกิดจากอะไร)
- [x] **2.3** อธิบายวิธีแก้ปัญหา (แก้อะไร เพราะเหตุใด และแก้ที่โค้ดส่วนใด)
- [x] **2.4** มีภาพหรือผลทดสอบ **Before vs After** หลังแก้ปัญหาจริง

### ข้อ 3: รองรับอุปกรณ์พกพาที่หลากหลาย
- [ ] **3.1** ทดสอบ ≥2 ขนาด/ความละเอียดหน้าจอ (เช่น จอเล็ก 5" กับจอใหญ่ 6.7"/Tablet) ข้อความ/ปุ่มไม่ตัดและไม่ซ้อน
- [x] **3.2** ทดสอบแนวตั้งและแนวนอน (หรือระบุการตรึงแนวหน้าจอเดียวใน Manifest/Config พร้อมเหตุผล)
- [ ] **3.3** รองรับทั้ง **ไทย (TH)** และ **อังกฤษ (EN)** เปลี่ยนครบทั้งเมนู ปุ่ม และ Dialog แจ้งเตือน
- [ ] **3.4** รองรับทั้ง **Day Theme** และ **Night Theme** อ่านข้อความและแยกปุ่มจากพื้นหลังชัดเจน

### ข้อ 4: Back Navigation, Delete Confirmation & Activity Lifecycle
- [ ] **4.1** กด Back จากทุกหน้าเนื้อหาที่เข้าจากเมนูแล้วกลับหน้าก่อนหน้าตามผัง ไม่กระโดดข้ามหน้า
- [x] **4.2** กด Back ขณะอยู่ในหน้า Gameplay/หน้าที่จะออกจากแอป ต้องไม่หลุดทันที (มีหน้าต่างยืนยัน/กดซ้ำ และกดยกเลิกเพื่ออยู่ต่อได้)
- [ ] **4.3** ทุกคำสั่งลบใน Score History มี Dialog ยืนยันก่อนลบ (ทดสอบทั้ง *ยกเลิก* → ข้อมูลอยู่ และ *ยืนยัน* → ข้อมูลหาย)
- [x] **4.4** สลับไปแอปอื่น (`onPause`) เกมและเสียงหยุดทันที กลับมา (`onResume`) เล่นต่อจากคะแนน/ความเร็ว/สถานะเดิมได้

### ข้อ 5: รายงานและการนำเสนอ
- [ ] **5.1** ภาพหน้าแอปเรียงตามการใช้งานหลักตั้งแต่เริ่มจนจบ พร้อมระบุปุ่มที่กดและการกระทำระหว่างหน้าจอ
- [ ] **5.2** ระบุแหล่งที่มาของโค้ด สื่อ หรือ AI ที่ใช้ครบทุกส่วน (จดลงตารางหัวข้อ 3 ด้านล่าง)
- [x] **5.3** นำเสนอความท้าทายจากข้อ 2 เชื่อม ปัญหา → วิธีแก้ → ผลลัพธ์ พร้อมหลักฐานประกอบ
- [ ] **5.4** เรียบเรียงรายงานและสไลด์นำเสนอไหลลื่น ไม่วน

---

## 2. บันทึกความท้าทายทางเทคนิค (Technical Challenge Log — Rubric 2.2–2.4 & 5.3)

*เจอบั๊กหรือปัญหาเทคนิคระหว่างทำ ให้จด + แคปภาพ "ก่อนแก้" ทันที:*

| หัวข้อปัญหา (Challenge) | ฟังก์ชัน / ไฟล์ที่พบ & สาเหตุ (2.2) | วิธีแก้ในโค้ด & เหตุผล (2.3) | หลักฐาน Before / After (2.4) |
| :--- | :--- | :--- | :--- |
| **Role 1: จานอาหารกระโดดข้ามสายพานเมื่อค่า Delta Time (`dt`) พุ่งสูงหลังหยุดเกมชั่วคราว** | `GameEngine.kt` (`update`): เมื่อคำนวณระยะเลื่อน $\Delta y = v(s) \cdot dt$ หากเกมหยุดชั่วคราวหรือเฟรมสะดุด ค่า `rawDtSeconds` ของเฟรมถัดมาอาจพุ่งสูงหลายวินาที ทำให้ `yProgress` พุ่งเกิน `1.0f` จนแพ้ทันที | จำกัดเพดาน `val dt = rawDtSeconds.coerceIn(0f, MAX_FRAME_DT_SECONDS)` (`0.05f` วินาที) และบล็อกการอัปเดตขณะ `isPaused == true` ใน `GameEngine.update()` | **Before:** ส่ง `dt = 2.0f` หลัง Resume แล้ว `yProgress` พุ่งเกิน `1.0f` (Game Over ทันที)<br>**After:** ผลรัน Unit Test `gameEngine_pausePreservesStateAndClampsPostResumeDtSpike` ยืนยันว่าตำแหน่งขยับไม่เกิน `0.01f` และไม่เกิด Game Over |
| **Role 2 (เมเจอร์): แตะจานพลาดเล็กน้อยหรือพับแอป (`ON_PAUSE`) แล้วเกิด Sudden Death ไม่ยุติธรรม** | `CharacterView.kt` (`resolveDragTarget`) & `GameplayScreen.kt` (`withFrameNanos` / `LifecycleEventObserver`): การแตะคลาดเคลื่อนเล็กน้อยอาจส่งอาหารผิดฝั่ง หรือเมื่อพับแอป (`ON_PAUSE`/`ON_STOP`) ลูป `withFrameNanos` ยังเดินต่อและเกิดช่วง `dt` กระโดดตอนกลับมา | เพิ่มระยะ dead-zone `thresholdPx` (`42.dp`) ใน `resolveDragTarget`, ดัก `Lifecycle.Event.ON_PAUSE`/`ON_STOP` ด้วย `DisposableEffect` + `LifecycleEventObserver` เพื่อสั่ง `pauseGame()` พร้อมเปิด `PauseExitDialog` และรีเซ็ต `lastFrameNanos = 0L` ทุกครั้งที่ `LaunchedEffect` กลับมาทำงาน | **Before:** แตะสั้นๆ บนสายพานหรือพับแอปชั่วคราวทำให้อาหารพุ่งหลุดขอบ/ป้อนผิดฝั่งจน Game Over<br>**After:** Unit Test `GameplayGestureAndLifecycleTest` ยืนยันว่า micro-drag คืน `null` และสถานะคะแนน/ความเร็ว/ตำแหน่งบนสายพานถูกเก็บรักษาครบถ้วนหลัง Pause |
| `<ความท้าทายเพิ่มเติมของเพื่อนในทีม>` | `<ระบุฟังก์ชันและสาเหตุ>` | `<วิธีแก้และเหตุผล>` | `<ลิงก์รูปหรือผลเทส>` |

---

## 3. บันทึกแหล่งที่มาของโค้ด สื่อ และ AI (Attribution & AI Log — Rubric 5.2)

### 3.1 การใช้ AI (AI Assistance)
| วันที่ | ผู้ใช้ | เครื่องมือ AI | ไฟล์ / ฟีเจอร์ | ส่วนที่นำมาจาก AI | ส่วนที่ทีมพัฒนา / ปรับแก้เอง |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `2026-11-21` | ทีม | Antigravity | เอกสารวางแผนโครงงาน (`README.md`, `docs/*.md`) | ช่วยจัดโครงสร้างเอกสาร กฎทีม และแมปเกณฑ์ CS364 | ทีมออกแบบไอเดียเกมต้นแบบ (2 ตัวละคร, Speed Cap, Haptics) และกำหนดกติกา |
| `2026-11-24` | ทีม | Antigravity Image Gen | ออกแบบ UI Mockup (`docs/ui/*.png`) และ 16-Bit Pixel Art Sprites (`res/drawable-nodpi/*.png`) | สร้างภาพตัวอย่าง UI หลายสไตล์และเจน Sprite ตัวละคร/อาหารตามบรีฟ | ทีมคัดเลือกโครงหน้าจอ Option 1 (ลากอาหารไปป้อนตัวละคร ซ้าย/ขวา/ถังขยะล่าง) และเลือกลายเส้น Style 3 (16-Bit Pixel Art) พร้อมตัดพื้นหลังโปร่งใส |
| `2026-11-24` | Role 1 | Antigravity | `game/FoodItem.kt`, `game/SpeedController.kt`, `game/GameEngine.kt` (Issues #2–#3) | ช่วยขึ้นโครงคลาส `SpeedController` และ `StateFlow` ใน `GameEngine` | กำหนดไอเทม 3 ชนิด (เบอร์เกอร์, สลัดผัก, เศษอาหาร), พารามิเตอร์สูตร $v(s) = \min(v_{\max}, v_0 + k\lfloor s/5\rfloor)$, สัดส่วนขยะ 10%→25%, การล็อก `MAX_FRAME_DT_SECONDS` และชุดทดสอบ `SpeedAndEngineTest.kt` |
| `2026-11-24` | Role 2 (เมเจอร์) | Antigravity | `ui/components/CharacterView.kt`, `ui/screens/GameplayScreen.kt`, `ui/components/PauseExitDialog.kt`, `res/values*/strings_role2.xml`, `res/drawable-nodpi/char_*_{yum,dead,sad}.png` (Issues #4–#5) | ช่วยขึ้นโครง Jetpack Compose Canvas (`PixelArcadeBox`, สายพาน 3D, ฉากพื้นตารางหมากรุก), แอนิเมชัน 3 ชั้น, และสคริปต์ดัดแปลงพิกเซล 6 ท่า | ปรับสัดส่วนตัวละครเต็มตัวยืนบนพื้นให้ตรงภาพ `ui_pixel_gameplay.png`, ออกแบบแอคชั่นสมมาตร (กินถูก=ลูบพุงอร่อย `YUMMY`, กินผิด=ล้มนอนตาย `DEAD` + อีกฝั่งนั่งคุกเข่าร้องไห้ `SAD`), ใช้ `MaterialTheme.colorScheme` ล้วน, เชื่อม `BackHandler` + `LifecycleEventObserver`, และเขียน `GameplayGestureAndLifecycleTest.kt` |
| `<YYYY-MM-DD>` | `<ชื่อ>` | `<AI>` | `<ไฟล์>` | `<ส่วนที่ AI ช่วย>` | `<ส่วนที่แก้และเขียนต่อเอง>` |

*ส่วนที่พัฒนาเอง 100% โดยไม่ใช้ AI:* `<ระบุชื่อไฟล์/โมดูล และผู้พัฒนา>`

### 3.2 สื่อ รูปภาพ ไอคอน เสียง และฟอนต์ (Media & Assets)
| ประเภท | ชื่อไฟล์ | แหล่งที่มา / URL | License | การปรับแต่งโดยทีม |
| :--- | :--- | :--- | :--- | :--- |
| ตัวละคร 16-Bit Pixel Art | `char_skinny.png`, `char_chubby.png`, `char_trash.png` | สร้างด้วย AI Image Generator ตามบรีฟของทีม (`app/src/main/res/drawable-nodpi/`) | ใช้งานในโปรเจกต์การศึกษา CS364 | คุมสไตล์ 16-Bit Pixel Art และตัดพื้นหลังขาวให้โปร่งใส (Transparent PNG) |
| สไปรต์ท่าทางรีแอคชันตัวละคร (6 ท่า) | `char_skinny_yum.png`, `char_skinny_dead.png`, `char_skinny_sad.png`, `char_chubby_yum.png`, `char_chubby_dead.png`, `char_chubby_sad.png` | ดัดแปลงพิกเซลต่อยอดจาก `char_skinny.png` / `char_chubby.png` ด้วยสคริปต์ Python PIL (`Role 2`) | ใช้งานในโปรเจกต์การศึกษา CS364 | วาดท่าลูบพุงอร่อย (`^_^`), ท่านอนสลบ/ตาย (`X_X` + ดาวมึน), และท่านั่งคุกเข่าร้องไห้ (`T_T` + น้ำตาพิกเซล) พร้อมตัดพื้นหลังโปร่งใส |
| ไอเทมอาหาร & ขยะ 16-Bit Pixel Art | `food_burger.png`, `food_salad.png`, `food_hazard.png` | สร้างด้วย AI Image Generator ตามบรีฟของทีม (`app/src/main/res/drawable-nodpi/`) | ใช้งานในโปรเจกต์การศึกษา CS364 | แยกชิ้นส่วนจานอาหาร 3 ประเภทและตัดพื้นหลังให้โปร่งใส |
| ภาพต้นแบบ UI 5 หน้าจอ | `ui_pixel_mainmenu.png`, `ui_pixel_gameplay.png`, `ui_pixel_subscreens.png` | สร้างด้วย AI Image Generator (`docs/ui/`) | ใช้งานในโปรเจกต์การศึกษา CS364 | ทีมเลือกเลย์เอาต์ Option 1 + ลายเส้น Style 3 (16-Bit Pixel Art) |
| เสียง / ฟอนต์ | `<เช่น sfx_eat.wav>` | `<Freesound / Google Fonts>` | `<CC0 / OFL>` | `<เช่น ตัดเสียงสั้นลง>` |

### 3.3 ไลบรารีและโค้ดอ้างอิงภายนอก (Libraries & References)
| ชื่อไลบรารี / เอกสารอ้างอิง | URL / เวอร์ชัน | ใช้ในส่วนใด | ส่วนที่ทีมเขียนต่อยอดเอง |
| :--- | :--- | :--- | :--- |
| `<เช่น VibratorManager / Room / SharedPrefs>` | `<URL>` | `<ระบบสั่น / เก็บสถิติ>` | `<สร้าง Pattern 3 ระดับ / Schema & Confirm Delete>` |
