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

- [x] **3.1** ทดสอบ ≥2 ขนาด/ความละเอียดหน้าจอ (เช่น จอเล็ก 5" กับจอใหญ่ 6.7"/Tablet) ข้อความ/ปุ่มไม่ตัดและไม่ซ้อน
- [x] **3.2** ทดสอบแนวตั้งและแนวนอน (หรือระบุการตรึงแนวหน้าจอเดียวใน Manifest/Config พร้อมเหตุผล)
- [x] **3.3** รองรับทั้ง **ไทย (TH)** และ **อังกฤษ (EN)** เปลี่ยนครบทั้งเมนู ปุ่ม และ Dialog แจ้งเตือน
- [x] **3.4** รองรับทั้ง **Day Theme** และ **Night Theme** อ่านข้อความและแยกปุ่มจากพื้นหลังชัดเจน

### ข้อ 4: Back Navigation, Delete Confirmation & Activity Lifecycle

- [ ] **4.1** กด Back จากทุกหน้าเนื้อหาที่เข้าจากเมนูแล้วกลับหน้าก่อนหน้าตามผัง ไม่กระโดดข้ามหน้า
- [x] **4.2** กด Back ขณะอยู่ในหน้า Gameplay/หน้าที่จะออกจากแอป ต้องไม่หลุดทันที (มีหน้าต่างยืนยัน/กดซ้ำ และกดยกเลิกเพื่ออยู่ต่อได้)
- [ ] **4.3** ทุกคำสั่งลบใน Score History มี Dialog ยืนยันก่อนลบ (ทดสอบทั้ง *ยกเลิก* → ข้อมูลอยู่ และ *ยืนยัน* → ข้อมูลหาย)
- [ ] **4.4** สลับไปแอปอื่น (`onPause`) เกมและเสียงหยุดทันที กลับมา (`onResume`) เล่นต่อจากคะแนน/ความเร็ว/สถานะเดิมได้

**Role 2 scope:** Gameplay Back protection and game-state preservation passed device tests. Item 4.4 remains open for app-wide completion until Role 3 connects and verifies actual audio. Item 4.2 is verified for Gameplay; Main Menu exit protection belongs to Role 3.

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
| **Role 2: KO Back guard and incomplete sprite limbs (2026-11-24)** | BackHandler was disabled during the delayed KO transition; old KO/crying images had cropped or disconnected limbs | Keep BackHandler active (triggerPause ignores KO); pause audio on background even during KO and on disposal; replace four sprites with complete transparent full-body artwork | Before images: docs/ui/before_role2_sprite_repair/. After: drawable-nodpi/char_*_dead.png and char_*_sad.png. Final validation: 8 JVM tests and 2 Android device tests passed; two-size screenshots linked in Section 4 |
| **Role 2: Happy eating hands appeared cut off (2026-11-24)** | Both yum sprites had incomplete forearms/hands | Replaced both with complete smiling belly-holding poses, preserving transparent backgrounds and original clothing | Originals: docs/ui/before_role2_sprite_repair/char_*_yum.png; replacements: drawable-nodpi/char_*_yum.png. Both sprites inspected in the Android QA fixture at two sizes; assembleDebug passed (Section 4) |
| **Role 2: Pose scale jumps and Activity recreation resets the game (2026-11-24)** | Transparent padding differed between sprites; the engine lived only in composition; KO delay could run in background | Draw using measured opaque bounds and cached painters; reserve side lanes and scale KO by standing height; navigation-scoped GameplaySessionViewModel retains engine; frame loop requires RESUMED; KO navigation waits for RESUMED and feedback runs once | 8 JVM tests and 2 Android emulator tests passed. Screenshots: docs/ui/role2_verified/ (360x640 TH Day and 412x915 EN Night, happy/KO/pause). Recreation test asserts identical engine and paused state |
| `<ความท้าทายเพิ่มเติมของเพื่อนในทีม>` | `<ระบุฟังก์ชันและสาเหตุ>` | `<วิธีแก้และเหตุผล>` | `<ลิงก์รูปหรือผลเทส>` |

---

## 3. บันทึกแหล่งที่มาของโค้ด สื่อ และ AI (Attribution & AI Log — Rubric 5.2)

### 3.1 การใช้ AI (AI Assistance)

| วันที่ | ผู้ใช้ | เครื่องมือ AI | ไฟล์ / ฟีเจอร์ | ส่วนที่นำมาจาก AI | ส่วนที่ทีมพัฒนา / ปรับแก้เอง |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `2026-11-21` | ทีม | Antigravity | เอกสารวางแผนโครงงาน (`README.md`, `docs/*.md`) | ช่วยจัดโครงสร้างเอกสาร กฎทีม และแมปเกณฑ์ CS364 | ทีมออกแบบไอเดียเกมต้นแบบ (2 ตัวละคร, Speed Cap, Haptics) และกำหนดกติกา |
| `2026-11-24` | ทีม | Antigravity Image Gen | ออกแบบ UI Mockup (`docs/ui/*.png`) และ 16-Bit Pixel Art Sprites (`res/drawable-nodpi/*.png`) | สร้างภาพตัวอย่าง UI หลายสไตล์และเจน Sprite ตัวละคร/อาหารตามบรีฟ | ทีมคัดเลือกโครงหน้าจอ Option 1 (ลากอาหารไปป้อนตัวละคร ซ้าย/ขวา/ถังขยะล่าง) และเลือกลายเส้น Style 3 (16-Bit Pixel Art) พร้อมตัดพื้นหลังโปร่งใส |
| `2026-11-24` | Role 1 | Antigravity | `game/FoodItem.kt`, `game/SpeedController.kt`, `game/GameEngine.kt` (Issues #2–#3) | ช่วยขึ้นโครงคลาส `SpeedController` และ `StateFlow` ใน `GameEngine` | กำหนดไอเทม 3 ชนิด (เบอร์เกอร์, สลัดผัก, เศษอาหาร), พารามิเตอร์สูตร $v(s) = \min(v_{\max}, v_0 + k\lfloor s/5\rfloor)$, สัดส่วนขยะ 10%→25%, การล็อก `MAX_FRAME_DT_SECONDS` และชุดทดสอบ `SpeedAndEngineTest.kt` |
| `2026-11-24` | Role 2 (เมเจอร์) | Antigravity | `ui/components/CharacterView.kt`, `ui/screens/GameplayScreen.kt`, `ui/components/PauseExitDialog.kt`, `res/values*/strings_role2.xml`, `res/drawable-nodpi/char_*_{yum,dead,sad}.png` (Issues #4–#5) | ช่วยขึ้นโครง Jetpack Compose Canvas (`PixelArcadeBox`, สายพาน 3D, ฉากพื้นตารางหมากรุก), แอนิเมชัน 3 ชั้น, และสคริปต์ดัดแปลงพิกเซล 6 ท่า | ปรับสัดส่วนตัวละครเต็มตัวยืนบนพื้นให้ตรงภาพ `ui_pixel_gameplay.png`, ออกแบบแอคชั่นสมมาตร (กินถูก=ลูบพุงอร่อย `YUMMY`, กินผิด=ล้มนอนตาย `DEAD` + อีกฝั่งร้องไห้ `SAD` (รุ่นแรกนั่งคุกเข่า; รุ่นปัจจุบันยืนเช็ดน้ำตา)), ใช้ `MaterialTheme.colorScheme` ล้วน, เชื่อม `BackHandler` + `LifecycleEventObserver`, และเขียน `GameplayGestureAndLifecycleTest.kt` |
| 2026-11-24 | Role 2 | OpenAI Codex + built-in ImageGen | GameplayScreen.kt and four dead/sad character PNGs | Back/lifecycle guard patch and AI image repairs | User identified broken hands/legs; original identities, clothing, pixel-art style and left/right placement retained; sprites visually inspected on Android at two sizes (Section 4) |
| 2026-11-24 | Role 2 | OpenAI Codex + built-in ImageGen | char_skinny_yum.png, char_chubby_yum.png | Generated repaired happy belly-holding sprites from existing identity references | User requested happy expressions and complete hands on belly for both characters; existing correct-match reaction logic retained |
| 2026-11-24 | Role 2 | OpenAI Codex | GameplayScreen.kt, CharacterView.kt, PauseExitDialog.kt, GameplaySessionViewModel.kt, SpriteContentBounds.kt, debug QA harness and tests | Implemented layout normalization, painter cache, ViewModel retention, lifecycle gates, adaptive dialog and regression tests | User requested reasonable proportions and remaining Role 2 fixes; validated on Android emulator with original gameplay rules and Role 3 integration boundary preserved |
| 2026-11-24 | Role 2 | OpenAI Codex | `CharacterView.kt` | Reduced idle motion, hover and non-uniform scaling | Final sprites and layout inspected on Android at two sizes; see Section 4 |
| `2026-11-25` | Role 2 | Antigravity | `GameplayScreen.kt`, `GameplaySessionViewModel.kt`, `CharacterView.kt`, `res/values*/strings_role2.xml`, `GameplayQaActivity.kt`, Role 2 unit & device tests | Implemented compact `GameplayKoRetryBanner`, `koRetryBannerPlacement` helper, TH/EN Role 2 fail-reason mappings, `handleRetry` session reset flow, `koRetryButtonResetsSessionAndAllowsSecondRound` device test, and KO floor-impact pixel dust puff in `CharacterView.kt` | User requested an in-screen Retry button after defeat and a small KO floor-impact dust/smoke puff; placed banner above characters to keep KO/crying artwork unobstructed, kept `deadDropOffsetDp` animating when disabled, and verified build + 11 JVM unit tests |
| `2026-11-25` | Role 5 (`sumetha06`) | AI Assistant | `data/settings/SettingsPreferences.kt`, `data/settings/SettingsScreen.kt`, `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `res/values*/strings.xml`, `SettingsAndLocalizationTest.kt` (PR #15 / Issues #10–#11) | ช่วยขึ้นโครง DataStore Preferences, ชุดสี Material 3 Day/Night และหน้าตั้งค่า | ปรับแต่ง Per-App Language (`AppCompatDelegate.setApplicationLocales`), คำแปล TH/EN ครบถ้วน และเขียน `SettingsAndLocalizationTest.kt` |
| `<YYYY-MM-DD>` | `<ชื่อ>` | `<AI>` | `<ไฟล์>` | `<ส่วนที่ AI ช่วย>` | `<ส่วนที่แก้และเขียนต่อเอง>` |

*ส่วนที่พัฒนาเอง 100% โดยไม่ใช้ AI:* `<ระบุชื่อไฟล์/โมดูล และผู้พัฒนา>`

### 3.2 สื่อ รูปภาพ ไอคอน เสียง และฟอนต์ (Media & Assets)

| ประเภท | ชื่อไฟล์ | แหล่งที่มา / URL | License | การปรับแต่งโดยทีม |
| :--- | :--- | :--- | :--- | :--- |
| ตัวละคร 16-Bit Pixel Art | `char_skinny.png`, `char_chubby.png`, `char_trash.png` | สร้างด้วย AI Image Generator ตามบรีฟของทีม (`app/src/main/res/drawable-nodpi/`) | ใช้งานในโปรเจกต์การศึกษา CS364 | คุมสไตล์ 16-Bit Pixel Art และตัดพื้นหลังขาวให้โปร่งใส (Transparent PNG) |
| สไปรต์รีแอคชันรุ่นแรก (ประวัติ; ถูกแทนที่แล้ว) | `char_skinny_yum.png`, `char_skinny_dead.png`, `char_skinny_sad.png`, `char_chubby_yum.png`, `char_chubby_dead.png`, `char_chubby_sad.png` | ดัดแปลงพิกเซลต่อยอดจาก `char_skinny.png` / `char_chubby.png` ด้วยสคริปต์ Python PIL (`Role 2`) | ใช้งานในโปรเจกต์การศึกษา CS364 | วาดท่าลูบพุงอร่อย (`^_^`), ท่านอนสลบ/ตาย (`X_X` + ดาวมึน), และท่านั่งคุกเข่าร้องไห้ (`T_T` + น้ำตาพิกเซล) พร้อมตัดพื้นหลังโปร่งใส |
| ไอเทมอาหาร & ขยะ 16-Bit Pixel Art | `food_burger.png`, `food_salad.png`, `food_hazard.png` | สร้างด้วย AI Image Generator ตามบรีฟของทีม (`app/src/main/res/drawable-nodpi/`) | ใช้งานในโปรเจกต์การศึกษา CS364 | แยกชิ้นส่วนจานอาหาร 3 ประเภทและตัดพื้นหลังให้โปร่งใส |
| ภาพต้นแบบ UI 5 หน้าจอ | `ui_pixel_mainmenu.png`, `ui_pixel_gameplay.png`, `ui_pixel_subscreens.png` | สร้างด้วย AI Image Generator (`docs/ui/`) | ใช้งานในโปรเจกต์การศึกษา CS364 | ทีมเลือกเลย์เอาต์ Option 1 + ลายเส้น Style 3 (16-Bit Pixel Art) |
| Repaired KO and crying sprites (2026-11-24; supersedes prior dead/sad versions) | char_skinny_dead.png, char_chubby_dead.png, char_skinny_sad.png, char_chubby_sad.png | Built-in OpenAI ImageGen edits using existing project sprites as references | AI-generated edits; original reference provenance above | Complete attached arms/hands/legs/shoes; transparent background; crying poses now standing; originals preserved in docs/ui/before_role2_sprite_repair/ |
| Repaired happy eating sprites (2026-11-24; supersedes prior yum images) | char_skinny_yum.png, char_chubby_yum.png | Built-in OpenAI ImageGen; existing character references | AI-generated edits; reference provenance above | Full connected arms/wrists/hands; one hand flat on belly, happy eyes and smile; original clothing and alpha transparency retained |
| เสียง / ฟอนต์ | `<เช่น sfx_eat.wav>` | `<Freesound / Google Fonts>` | `<CC0 / OFL>` | `<เช่น ตัดเสียงสั้นลง>` |

### 3.3 ไลบรารีและโค้ดอ้างอิงภายนอก (Libraries & References)

| ชื่อไลบรารี / เอกสารอ้างอิง | URL / เวอร์ชัน | ใช้ในส่วนใด | ส่วนที่ทีมเขียนต่อยอดเอง |
| :--- | :--- | :--- | :--- |
| `<เช่น VibratorManager / Room / SharedPrefs>` | `<URL>` | `<ระบบสั่น / เก็บสถิติ>` | `<สร้าง Pattern 3 ระดับ / Schema & Confirm Delete>` |

### 3.4 ImageGen prompts (used for the six replacement sprites)

- KO: complete horizontal full-body character (skinny head left, chubby head right); two connected arms and legs with visible shoes. Preserve identity, clothes, chunky 16-bit outlines/palette, transparent background; no cropped limbs, text or extra objects.
- Crying: standing full-body character wiping one cheek, with connected shoulders, elbows, wrists and hands; preserve identity, clothes and pixel style; transparent background.
- Happy: standing character (skinny facing right, chubby facing left), satisfied closed eyes and smile, one complete hand flat on lower belly with visible fingers and a connected bent arm; other arm relaxed; two complete legs and shoes. No detached limbs, symbols or text. Existing correct-match handlers show YUMMY for 850 ms; this timing was inspected in source.

## 4. Role 2 implementation / QA — 2026-11-24

Results apply to Role 2 and its debug harness, not the unfinished app-wide integration.

| Check | Result / scope |
| :--- | :--- |
| Build | `:app:assembleDebug` and `:app:installDebug` passed; launched debug-only QA activity on Android 15 / Pixel_6a emulator |
| JVM tests | 11 passed: engine, gesture dead zone, pause preservation, post-KO reset, sprite alpha bounds, layout & KO banner placement constraints, Role 2 fail-reason string mappings |
| Device tests | 3 passed: pause/background/recreation retains engine and exact paused score/items/speed; Back cancels/resumes and pauses again; KO consumes Back and delays navigation until resumed; post-KO Retry banner resets score/belt/audio and allows repeated rounds |
| TH/EN and Day/Night | Role 2 board, pause dialog, and KO Retry banner visually inspected; no clipped action labels in captured sizes |
| Responsive | Actual Android screenshots at 360x640 dp and 412x915 dp; 5 poses/dialog/KO-retry states per size. Final shadow alignment and banner clearance also verified |
| Integration boundary | MainActivity, production navigation, real audio and other Roles were not implemented here. Feedback/audio callbacks remain ready for Role 3 |
| Persistence limit | ViewModel survives configuration/Activity recreation within its owner. Process-death or force-stop restoration is not implemented |
| Attribution | Existing sprite edits and their prompts are recorded above; no additional image generation for layout adjustments |

Evidence: [verified screenshots](ui/role2_verified/) and [original sprites](ui/before_role2_sprite_repair/).

| State | 360×640 dp / TH / Day | 412×915 dp / EN / Night |
| :--- | :--- | :--- |
| Happy | [View](ui/role2_verified/360_day_th_yum.png) | [View](ui/role2_verified/412_night_en_yum.png) |
| Skinny KO | [View](ui/role2_verified/360_day_th_skinny_ko.png) | [View](ui/role2_verified/412_night_en_skinny_ko.png) |
| Chubby KO | [View](ui/role2_verified/360_day_th_chubby_ko.png) | [View](ui/role2_verified/412_night_en_chubby_ko.png) |
| Pause | [View](ui/role2_verified/360_day_th_pause.png) | [View](ui/role2_verified/412_night_en_pause.png) |
| KO Retry Banner | [View](ui/role2_verified/360_day_th_skinny_ko_retry.png) | [View](ui/role2_verified/412_night_en_chubby_ko_retry.png) |

Debug-only manual QA entry point (excluded from release):
`adb shell am start -n com.cs364.foodrush/.ui.screens.GameplayQaActivity`

For a static visual fixture, add `--es pose yum` (or `skinny_ko`, `chubby_ko`, `sad`, `pause`, `skinny_ko_retry`, `chubby_ko_retry`), `--es lang th` / `en`, and optionally `--ez night true`. Static fixtures are for layout inspection; the no-extra activity runs the real GameplayScreen. Navigation callbacks are counted by the harness for tests rather than connected to other roles' screens.

Quality gate for this Role 2 patch: build/run, scoped TH/EN, scoped Day/Night, two screen sizes, Back/lifecycle tests and AI log completed. App-wide checklist items for other screens remain unchanged; actual audio integration and score deletion belong to their respective roles.
