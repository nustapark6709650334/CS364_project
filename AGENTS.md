# Fit & Fat: Food Rush — Project Rules & Spec (CS364 2569)

## 0. AI File-Reading & Token-Saving Policy (CRITICAL)
- **This file (`AGENTS.md`) is auto-loaded.** It contains 100% of the game spec, CS364 rules, Git workflow, and PR template.
- **Do NOT read `README.md`** — it is only a short GitHub cover page for humans.
- **Do NOT read `docs/REPORT_AND_AI_LOG.md` while coding or debugging.** Only open/edit it **once at the end of a completed feature/fix** to tick `[x]` on completed rubric items and log AI/asset/bug rows, or when asked to work on the report.

---

## 1. Tech Stack & Package Structure (Locked)
- **Language / UI:** Android **Kotlin** + **Jetpack Compose** (Material 3)
- **Localization:** `res/values/strings.xml` (EN) + `res/values-th/strings.xml` (TH) + per-app locale switching
- **Theme:** Material 3 `lightColorScheme` (Day) / `darkColorScheme` (Night) toggleable in Settings
- **Haptics & Audio:** `VibratorManager` (API 31+) / `Vibrator` fallback; `SoundPool` / `MediaPlayer`
- **Storage:** DataStore Preferences (settings, theme, lang, high score) + Room or JSON/SQLite (score history)
- **Conflict-Free 5-Role File Ownership (`com.cs364.foodrush`):**
  - **Role 1 (Game Engine & Speed Mechanics):** `game/GameEngine.kt`, `game/SpeedController.kt`, `game/FoodItem.kt` — Rubric 1.4, 2.1–2.4, 5.3
  - **Role 2 (Gameplay UI & Lifecycle Guard):** `ui/screens/GameplayScreen.kt`, `ui/components/PauseExitDialog.kt`, `ui/components/CharacterView.kt` — Rubric 4.2, 4.4
  - **Role 3 (Main Menu, Navigation, Haptics & Audio):** `ui/screens/MainMenuScreen.kt`, `ui/navigation/AppNavGraph.kt`, `game/HapticAudioManager.kt` — Rubric 1.3, 4.1, 5.1
  - **Role 4 (Game Over, Score DB & Delete Confirmation):** `ui/screens/GameOverScreen.kt`, `ui/screens/ScoreHistoryScreen.kt`, `ui/components/ConfirmDeleteDialog.kt`, `data/ScoreRepository.kt` — Rubric 1.1, 4.3
  - **Role 5 (Settings/How-To, Day/Night Theme, TH/EN i18n & Multi-Screen QA):** `ui/screens/SettingsScreen.kt`, `ui/theme/*`, `data/SettingsPreferences.kt`, `res/values*/strings.xml` — Rubric 1.2, 3.1–3.4, 5.2

---

## 2. Core Game Spec

### Characters & Items (3 Types)
| Target | Concept | Accepted Items (+1 Score) | Wrong Input |
| :--- | :--- | :--- | :--- |
| **Skinny (คนผอม)** | เพิ่มน้ำหนัก/พลังงาน | High-calorie: Burger, Pizza, Fried Chicken, Steak | Clean food or Hazard → **Game Over** |
| **Chubby (คนอ้วน)** | คุมน้ำหนัก/รักสุขภาพ | Clean food: Salad, Broccoli, Apple, Boiled Chicken | Fast food or Hazard → **Game Over** |
| **Trash (ถังขยะ)** | กำจัดสิ่งอันตราย | Hazards: Plastic straw, Fishbone, Spoiled food, Bomb | Any real food → **Game Over** |

### Controls, Speed Cap & Haptics
- **Controls:** Central lane/conveyor + 3 action buttons at bottom: **Left (Skinny)**, **Center (Trash)**, **Right (Chubby)** (optional: swipe gestures).
- **Speed Scaling & Cap:**
  $$v(s) = \min\left(v_{\max},\; v_0 + k \cdot \left\lfloor \frac{s}{5} \right\rfloor\right)$$
  - Starts at $v_0$, increases every 5 points until reaching $v_{\max}$ (then speed stays capped so touch input stays fair).
  - After reaching $v_{\max}$, increase challenge via tighter spawn interval & higher hazard ratio (10% → 25%).
  - **Use frame Delta Time (`dt`)** so item speed is identical across 60Hz/90Hz/120Hz devices.
- **Sudden Death & Haptics (toggleable in Settings):**
  - Correct match: `Light Tick (~20ms)` + `+1` popup + SFX.
  - Speed level up: `Double pulse [0, 40, 60, 40] ms` + `SPEED UP!` banner.
  - Game Over (wrong input / missed item): `Heavy (~400ms)` + screen shake + SFX.

---

## 3. Screens & Navigation Flow (5 Screens)

```text
[Main Menu] ──Back──► "Press again / Confirm exit app"
   ├──► [Gameplay] ──Back──► [Pause & Confirm Exit Dialog] (Cancel -> resume)
   │        └──Fail──► [Game Over & Save Score] ──► Play Again / Save -> [Score History]
   ├──► [Score History] ──Delete──► [Confirm Delete Dialog] (Cancel keeps data)
   └──► [Settings & How-To] (Language: TH/EN | Theme: Day/Night | SFX & Vibration)
```

---

## 4. CS364 Mandatory Kotlin Rules (Rubric 3.1–4.4 & 5.2)

1. **Responsive & Orientation (3.1–3.2):** Use `Modifier.weight`, `BoxWithConstraints`, `dp`/`sp` (never hardcoded `px`). Test on ≥2 screen sizes. Support orientation or explicitly lock `portrait` in `AndroidManifest.xml` with UX reason.
2. **No Hardcoded Strings — TH/EN (3.3):** Every UI label, food name, dialog, and error must use `stringResource(...)` in both `values/strings.xml` and `values-th/strings.xml`.
3. **No Hardcoded Colors — Day/Night (3.4):** Always use `MaterialTheme.colorScheme.*` (never `Color(0xFF...)` inside screens). Verify contrast in Day & Night themes.
4. **Back Navigation Guard (4.1–4.2):** Sub-screens pop back to `MainMenu`. In `GameplayScreen`, `BackHandler` pauses the game and shows a confirm-exit dialog (Cancel resumes). In `MainMenu`, `BackHandler` requires double-press or exit dialog.
5. **Confirm Every Delete (4.3):** Deleting 1 score or clearing history in `ScoreHistoryScreen` must open a `ConfirmDeleteDialog` first (test both Cancel & Confirm).
6. **Activity Lifecycle Safety (4.4):** Use `DisposableEffect` + `LifecycleEventObserver` in `GameplayScreen`: on `ON_PAUSE`/`ON_STOP`, immediately pause the game loop & audio while preserving score, speed, and items; allow clean resume on `ON_RESUME`.
7. **Auto-Tick & Log Immediately (5.2):** When finishing any feature or bug fix, open `docs/REPORT_AND_AI_LOG.md` to tick `- [x]` on verified rubric items and add AI/asset/bug (Before/After) rows before finishing the PR.

---

## 5. Git & PR Workflow (AI Must Follow)

- **Branches:** `main` (release) ← `develop` (integration) ← `feature/<name>`, `fix/<name>`, `docs/<name>`. Never push directly to `main`.
- **Commits:** `feat:`, `fix:`, `ui:`, `i18n:`, `theme:`, `docs:`
- **PR Rules:** 1 PR = 1 small feature/fix; `git pull` `develop` before starting; ≥1 teammate must Approve.
- **AI End-of-Task Rule:** Before finishing a feature/fix/PR, you MUST:
  1. Self-check every Quality Gate item below and verify what is actually tested.
  2. Open `docs/REPORT_AND_AI_LOG.md` and (a) tick `- [x]` on Section 1 rubric items that are now implemented & verified, (b) add a Section 2 row if a technical bug/challenge was fixed, and (c) add a Section 3.1 row documenting what AI generated vs what was customized.
  3. Output the completed PR summary using this template (with `[x]` checked):

```markdown
## Summary
- <What this PR changes & files touched>
**Type:** `feat` | `fix` | `ui` | `i18n` | `theme` | `docs`

## CS364 Quality Gate (Definition of Done)
- [ ] Build & Run clean on Android (no crash)
- [ ] TH/EN (3.3): All strings in `values/` & `values-th/`
- [ ] Day/Night (3.4): Uses `MaterialTheme.colorScheme`, readable in both themes
- [ ] Responsive (3.1–3.2): Verified on ≥2 screen sizes, no overlap/clipping
- [ ] Back / Lifecycle / Delete (4.1–4.4): `BackHandler` works, `ON_PAUSE` pauses & preserves state, delete has Confirm Dialog
- [ ] Log (5.2): Updated `docs/REPORT_AND_AI_LOG.md`

## Screenshots / Evidence
- <Attach Before/After, Day/Night, or multi-screen screenshots if UI changed>
```
