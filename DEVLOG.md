# Sundial — Dev Log

Running log of development sessions. Newest first.

---

## Entry 2 — 2026-07-09 (evening)

**Summary:** v1 finished (M8–M10: onboarding, pause, hardening, release prep — signed AAB builds), then the editions system arrived ahead of schedule: the Instrument face from `sundial-two-faces.html`, full app-wide re-voicing, the face gallery with live previews, sun-theming for both faces, and a lock screen that follows the light.

### What Was Worked On

- **M8 — Onboarding & Pause:** four-beat first-run (welcome → picker → whispers offer → make-it-home), under a minute verified from `pm clear`. Onboarding replaced the silent auto-seed and only fills empty modes. Pause shows the plain icon grid, Resume toasts "Welcome back.", paused state persists.
- **M9 — Hardening:** fixed the 480dp column cap (fillMaxSize defeated widthIn — visible in landscape), long-name ellipsis, a11y roles/descriptions (toggleable checks, ☉ switches, dots), RTL verified end-to-end, cold start 415/325ms (release, emulator), no wakelocks.
- **M10 — Release prep:** adaptive ☉ icon (dawn-paper bg, ink mark, monochrome layer), v1.0.0, upload keystore + gitignored `key.properties` (release falls back to debug signing when absent), signed AAB, `Docs/PLAY-LISTING.md` (listing copy, Data Safety, QUERY_ALL_PACKAGES text, org-account launch path — user has a registered Swedish company).
- **Copy decision:** ritual button "Open for 10 min" → **"Open"** (no lockout implication; the 10-min quiet-skip window is unchanged, now unadvertised).
- **Editions (faces):** `Face` enum persisted; **Instrument** face implemented per two-faces demo — Space Grotesk + IBM Plex Mono, instrument scale with live day-fraction marker, numbered ruled rows, split mono footer, single #E8501E accent for live info only.
- **FaceVoice:** composition-local typography (ceremonial/functional/meta + italic posture + headline weight) re-voices settings, editor, search, ritual, toasts per face. Signature unchanged; onboarding and Paused stay face-less by design.
- **Face gallery:** the Face row opens a horizontal pager of live home miniatures (real apps/intention/time, current sun-theme) — faces are the themes users browse; the sun-theme control stayed as the inline segmented row (corrected after a misunderstanding: gallery previews faces, not sun-themes).
- **Instrument follows the sun:** InstrumentDawn/Noon/Dusk palettes; theme row applies to both faces.
- **Lock screen follows the light:** `WallpaperSync` renders face+theme as the lock wallpaper (FLAG_LOCK, normal SET_WALLPAPER permission); re-syncs on face/theme changes and at mode boundaries via existing alarms; change-guard signature; fails silently. Iterated: bolder motifs (lock screen zoom-crops edges + downsamples), glow as quadratic fade (a flat block read as a header), band full-bleed from the top edge (corner-radius clip seam).

### Findings & Learnings

- **Lock screens are hostile to wallpaper precision:** SystemUI zoom-crops the edges, downsamples (thin lines vanish), clips display corner radii, and dims everything with a keyguard scrim (no opt-out API; decided to accept, judge on real hardware before compensating).
- The emulator AVD had **keyguard disabled** — waking went straight to the app; several "lock screen" test taps were actually landing on the keyguard once enabled (`locksettings set-disabled false`, `wm dismiss-keyguard` for scripted flows; screen must be awake before input).
- `am start -W` reports TotalTime 0 for HOME activities — use the logcat `Displayed` line for cold-start numbers.
- Release vs debug signature swaps require uninstall (INSTALL_FAILED_UPDATE_INCOMPATIBLE) — wipes local config; plan test order accordingly.
- Product correction recorded: **confirm with the user before implementing** — the theme-gallery was built for sun-themes first when the intent was faces.

### Next Steps

- Human-side launch: D-U-N-S lookup, org Play Console account, store assets (512 icon, feature graphic, screenshots, privacy policy page).
- TalkBack listening pass + a week of living with it on a real phone; check lock wallpaper crop/dim on real hardware.
- Future editions ride the established shape: palettes × FaceVoice × home composable (+ wallpaper motif).

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
