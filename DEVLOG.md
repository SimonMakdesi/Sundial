# Sundial — Dev Log

Running log of development sessions. Newest first.

---

## Entry 1 — 2026-07-09

**Summary:** From an empty machine to a working launcher: environment setup and milestones M0–M7 built, verified on emulator, and pushed. Sundial is now a daily-usable home screen — themed modes, gestures, search, the breath ritual, full settings with persistence, appearance options, and both whispers (notification counts + weather).

### What Was Worked On

- **Environment (no Android tooling existed):** standalone Android SDK at `%LOCALAPPDATA%\Android\Sdk` (platform 35, build-tools, emulator, system image), AEHD hypervisor driver (AMD, no Hyper-V), Pixel 7 AVD `SundialPixel`, Gradle 8.10.2 wrapper building with Unity's bundled JDK 17. Android Studio installed later and reuses the same SDK. Private GitHub repo: https://github.com/SimonMakdesi/Sundial
- **M0 — Skeleton:** Compose scaffold, HOME intent filter, blank dawn-paper screen; appears in the default-launcher chooser.
- **M1 — App list & launch:** AppRepository via PackageManager, localized labels, tap to launch, live package add/remove updates.
- **M2 — Theme & modes:** verbatim `PALETTES` port (Dawn/Noon/Dusk, horizon gradients, washes), Fraunces + Manrope variable fonts, ModeEngine with spans-as-data, live clock/dateline (locale 12/24h), boundary alarms + boot/time/timezone receivers, 1.1s/1.4s crossfades, reduced-motion support.
- **M3 — Gestures & search:** long-press (receding-content feedback) → settings; swipe-up / list-overscroll → search sheet with focused field, live filter, serif "Nothing by that name."; Back behavior.
- **M4 — Breath ritual:** DataStore-persisted per-app flags, overlay (breathing circle, "Take one breath." → choices after 3.4s), 10-minute windows, "Good call." in-app toast, ritual dots.
- **M5 — Settings & persistence:** "Your day" mode cards with horizon strips, mode editor (intention 60ch live, circle checks, ☉ toggles, >8-app soft cap), per-mode lists + intentions in DataStore, first-run seeding, real asleep count + search asleep tags. Survives process kill.
- **M6 — Appearance:** theme lock (Follow the sun / Dawn / Noon / Dusk — palette locks, rhythm keeps following time) and Left/Right alignment mirroring home + search.
- **M7 — Whispers:** NotificationListenerService counts (verified with a real emulator SMS), settings status rows, weather whisper: location ladder (coarse → timezone prefill, editable city via Open-Meteo geocoding), throttled cached temperature in the dateline, locale unit, zero network when off.

### Findings & Learnings

- **AVDs created via `avdmanager` default `hw.keyboard=no`** — host keyboard typing silently doesn't work until enabled in `config.ini`.
- **Android's stretch overscroll consumes leftover scroll deltas** before they reach a parent `NestedScrollConnection` — had to disable `LocalOverscrollConfiguration` on the home list for the swipe-past-end-opens-search gesture.
- **`clickable` rows + parent long-press detection double-fire** (settings opened *and* app launched on release); `combinedClickable` cancels the click when long-press fires.
- **Emulator timezone reports Europe/Berlin** on this host despite Stockholm expectations — validated the weather ladder's self-correcting city design.
- Verification-by-screenshot via `adb exec-out screencap` at every milestone caught real bugs early; `adb emu sms send` produces genuine notifications for listener testing.
- `cmd notification allow_listener` grants notification access non-interactively for testing.

### Next Steps

- **M8 — Onboarding & Pause:** first-run flow (welcome, app picker seeding, whispers offer, default-home prompt) and the Pause screen (plain icon grid + Resume + "Welcome back." toast).
- **M9 — Hardening:** TalkBack pass, foldable AVD, landscape, RTL smoke test, cold-start profiling, empty-mode edge case, long-name ellipsis.
- **M10 — Release prep:** real ☉ app icon, Play listing, Data Safety (nothing collected), signed AAB. Play Console account setup should start in parallel (~2–4 weeks lead time).
- Open decisions: applicationId rename before first Play upload (currently `com.makdesi.sundial`); theme currently locked to Dawn on the test emulator (user preference).
