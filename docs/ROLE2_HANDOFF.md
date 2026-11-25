# Role 2 — Gameplay UI & Lifecycle handoff

## Summary

Implements the gameplay board, drag/swipe/tap targets, corrected happy/crying/KO sprites, pause/exit dialog and lifecycle protection. Sprite content bounds keep pose sizes stable; cached painters avoid repeatedly decoding images. GameplaySessionViewModel preserves the engine across Activity recreation.

**Type:** `fix` / `ui`

## CS364 Quality Gate

- [x] Debug build and Android QA activity run successfully.
- [x] Role 2 TH/EN labels and pause dialog verified.
- [x] Role 2 Day/Night visuals verified.
- [x] Actual emulator screenshots at 360×640 and 412×915 dp reviewed.
- [x] Gameplay Back, background pause/resume and Activity recreation tested.
- [x] AI/media attribution and technical challenge log updated.
- N/A: Score deletion and Main Menu exit guard belong to other Roles.
- Pending integration: production navigation, real audio/haptics and app-wide settings.

## Build and test (Windows)

Run from the repository root with Android SDK/JDK configured:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:installDebug
adb shell am start -n com.cs364.foodrush/.ui.screens.GameplayQaActivity
```

The connected tests require a running emulator/device. Recorded result: 8 JVM tests and 2 Android tests passed on Android 15 / Pixel_6a.

The debug activity runs GameplayScreen; navigation callbacks are counted for tests rather than opening other screens. It is not part of release builds. For visual fixtures, add `--es pose yum` (or `skinny_ko`, `chubby_ko`, `sad`, `pause`), `--es lang th`/`en`, and optionally `--ez night true`.

## Integration contract

- Role 3: call GameplayScreen inside the gameplay navigation entry. Connect `onNavigateBackToMenu`, `onNavigateToGameOver`, feedback and audio callbacks. Each new game should use a fresh navigation entry/engine. The default ViewModel uses the nearest ViewModelStoreOwner; outside Navigation it uses the Activity owner.
- If injecting `gameEngine`, its owner controls session retention/reset and its match/level callbacks. The default screen-created session wires those callbacks automatically.
- Role 4: Game Over callback supplies score plus failure-template and food-name resource IDs.
- Role 5: Role 2 strings are in `values/strings_role2.xml` and `values-th/strings_role2.xml`; UI uses MaterialTheme colors. App-wide locale/theme switching still needs integration.
- State survives Activity configuration recreation within its owner, not process death or force-stop.

## Evidence

- [Verification and AI log](REPORT_AND_AI_LOG.md)
- [Final screenshots](ui/role2_verified/)
- [Original sprites for comparison](ui/before_role2_sprite_repair/)

Screenshots cover TH/Day at 360×640 dp and EN/Night at 412×915 dp. They are static rendering fixtures; the separate Android tests exercise the live screen lifecycle. Actual audio output and destination screens are not verified by the debug harness.
