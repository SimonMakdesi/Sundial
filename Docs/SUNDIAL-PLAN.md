# SUNDIAL — Build Plan & Product Specification

**A home screen that follows the light.**
Android launcher · Kotlin + Jetpack Compose · 100% local, no accounts, no cloud.

This document is the complete brief for building Sundial v1. It is designed to be
read by Claude Code alongside its companion file:

> **`sundial-demo.html` — the design contract.**
> The HTML demo is the visual and behavioral source of truth. Every palette hex,
> font pairing, spacing rhythm, animation timing, and line of interface copy in it
> is to be reproduced faithfully in Compose unless this document explicitly says
> otherwise. Where the demo and intuition disagree, the demo wins. Where the demo
> is silent (real system behavior), this document wins.

---

## 1. Product summary

Sundial replaces the Android home screen with a text-only list of apps that
changes with the time of day. Three **modes** — Morning, Day, Evening — each with
their own palette, app list, and a user-written intention line. Nothing is ever
blocked: apps outside the current mode are "asleep" and reachable through search.
Selected apps ask for one breath before opening (the **ritual**).

The entire interaction model:
- **Tap** — open an app
- **Swipe up** — search everything
- **Long-press** — settings ("Your day")

Positioning: calm, not discipline. The product never scolds, counts screen time,
or guilt-trips. Its opinions are stated once, gently, and never enforced.

Privacy stance (marketing-relevant, engineering-binding): no accounts, no
analytics, no tracking, no data leaves the device — with one optional,
off-by-default exception: fetching the temperature for a manually chosen city
(see 3.8). With weather off, the app makes zero network calls.

---

## 2. Design contract highlights

(Full values live in the HTML; these are the load-bearing rules.)

### 2.1 The three theme rules
1. **Two typefaces only.** A warm serif (Fraunces, weight 300, italic for
   intentions) for time, intentions, and ceremonial moments; a plain grotesk
   (Manrope) for everything functional. No third font, ever.
2. **Color lives in the horizon.** The UI is ink-on-paper monochrome except the
   horizon band (top edge gradient) and its wash. The horizon is the only place
   Sundial is allowed to be loudly beautiful.
3. **Moments speak serif.** Ritual lines, "Welcome back.", "Good call." — any
   time the app speaks in its own voice, it is set in the serif.

### 2.2 Palettes (from the demo, authoritative)
| Mode | bg | ink | character |
|---|---|---|---|
| Morning (Dawn) | `#F3EEE6` | `#2A2620` | warm dawn paper; peach horizon wash bleeds ~46% down |
| Day (Noon) | `#F4F5F3` | `#1E2124` | cool crisp neutral; wash tightens to ~16% |
| Evening (Dusk) | `#131216` | `#D9D3C8` | dark ember; wash ~30% |

Faint text = ink at ~42% alpha. Hairlines = ink at ~12% alpha. Exact gradient
stops for horizon and wash: copy from `PALETTES` in the demo source.

### 2.3 Motion
- Palette crossfades: ~1.1s ease (bg/ink), ~1.4s (horizon/wash).
- Layer transitions (home ↔ settings): fade + slight scale, ~450ms.
- Breath circle: 3.6s ease-in-out loop, scale 1 → 1.32.
- Ritual choices appear after ~3.4s.
- Respect the system reduced-motion setting: replace breath animation with a
  static circle at mid-opacity; shorten all transitions to near-instant fades.

### 2.4 Things the demo fakes that must be real
- Clock/date/temperature: demo shows static strings. Real app: live clock and
  date from system locale (24h/12h, date format, translated day names — all
  inherited, never hardcoded). The `· 18°` temperature suffix appears only when
  the optional weather whisper is enabled (3.8).
- App list: demo has a hardcoded registry. Real app: installed launchable apps
  via PackageManager, localized labels from the system.
- The demo's left-side caption/time-of-day switch is demo scaffolding — not part
  of the product.

---

## 3. Feature specification (v1)

### 3.1 Home screen
- Live clock (serif, large) + meridiem per locale; dateline below in faint.
- Intention line for the current mode, if non-empty, between hairlines.
- App list: the current mode's apps in the user's chosen order (v1: order of
  addition; drag-to-reorder is a Later item). Each row: name (lowercase as given
  by system label), optional notification count in faint, ritual dot if flagged.
- List scrolls invisibly (no scrollbar) with soft fade masks top and bottom.
- Footer: `{n} apps asleep · {NextMode} begins at {time}` — computed live.
- Bottom handle hints the swipe-up gesture.
- Alignment setting flips the entire stack (see 3.7).

### 3.2 Modes & the clock engine
- Fixed spans, device local time: **Morning 06:00–09:00 · Day 09:00–18:00 ·
  Evening 18:00–06:00.** Not user-adjustable in v1 (Later item), but implement
  spans as data, not constants, so the door stays open.
- Mode boundaries are scheduled exact-ish alarms (AlarmManager, inexact is fine
  — a minute of drift is acceptable and battery-kind). On fire: recompute mode,
  animate the palette crossfade if the launcher is visible.
- Recompute mode on: boot, ACTION_TIME_CHANGED, ACTION_TIMEZONE_CHANGED, app
  resume. DST is automatically handled by using local wall-clock time.

### 3.3 Gestures
- **Tap** app row → launch (or ritual first, see 3.5).
- **Swipe up** anywhere on home → search sheet.
- **Long-press** (~550ms) empty home area → settings. During the hold the home
  content recedes slightly (scale .97, opacity .75) as tactile feedback.
- System Back / Home from settings or sheet returns to home. Back on home does
  nothing (it's the home screen).

### 3.4 Search sheet
- Slides up over ~80% of the screen, matching current palette; focused text
  field on open.
- Lists all launchable apps alphabetically, filtered as you type.
- Apps not in the current mode show a faint `asleep` tag but launch normally.
- Ritual-flagged apps show the dot and trigger the ritual on launch.
- Empty result: serif italic "Nothing by that name."
- Dismiss: drag down, tap outside, Back.

### 3.5 The breath ritual
- Per-app boolean flag, set in the day editor (☉ toggle). Applies everywhere
  (home, search), at all hours.
- Flow: full-screen overlay in the current palette's translucent tone → breathing
  circle → "Take one breath." → after ~3.4s the choice appears: **Open for
  10 min** / **Not now**.
- "Open for 10 min": launches the app and records a timestamp; for the next 10
  minutes, launches of that app skip the ritual. After that, the ritual returns.
  (No enforcement, no alarm when time is up — the window simply expires.)
- "Not now": dismiss with toast "Good call." in serif.
- Defaults: no apps flagged. (The demo flags instagram/youtube for show;
  real users choose their own.)

### 3.6 Settings — "Your day"
Entered by long-press; exits with "Done". Sections, in order:

1. **Your modes** — three cards (name, span, horizon band strip, app names,
   intention preview). Tap → mode editor:
   - Intention text field (60 chars, live-updates home).
   - Legend line explaining inclusion + ☉ (copy from demo).
   - Searchable list of all installed apps: circle check = in this mode,
     ☉ toggle = ritual flag (global, per-app).
   - Soft cap: above 8 apps, show serif italic
     "`{n}` apps — a screen this full stops being quiet." Never blocks.
2. **Appearance**
   - **Theme:** Follow the sun (default) / Dawn / Noon / Dusk. Fixed themes lock
     the palette; rhythm (apps, intention, footer) always keeps following time.
   - **Alignment:** Left / Right. Flips home stack and search rows; counts and
     dots move to the opposite side of the name. For accessibility/one-handed
     reach.
3. **System**
   - Default launcher row: shows status; if not default, tapping opens the
     system default-home settings screen.
   - **Notification whispers** row: status + tap → system notification-access
     screen (see 3.7).
   - **Weather** row: off by default; enabling reveals a city search field
     (see 3.8). Shows the chosen city when on.
   - **Pause Sundial**: shows a plain, ordinary grid of all app icons inside
     Sundial (the "this is your phone without Sundial" screen from the demo)
     with a Resume button and a small link to change default launcher for a
     full exit. Toast on resume: "Welcome back."

Persistence: all settings in Jetpack DataStore (Preferences), enrolled in
Android Auto Backup so a new phone restores the day.

### 3.7 Notification counts (user decision: IN for v1)
- Implementation: `NotificationListenerService`. Count active notifications per
  package; display the number in faint next to the app name. No red, no badge,
  no icons — a whispered number, exactly as the demo shows.
- Permission UX: never demanded. Onboarding mentions it once with a "later is
  fine" option; the Settings row shows "off" state and deep-links to the system
  grant screen. Everything works without it — counts simply don't appear.
- Update counts on listener events; debounce repaints.

### 3.8 Weather whisper (optional, off by default)
- One number in the dateline: `Wednesday, July 8 · 18°`. Nothing else — no
  icons, no forecast, no weather screen. Unit (°C/°F) follows locale.
- **Location ladder** (triggered only when the user enables Weather; never at
  onboarding, never re-nagged):
  1. Ask for **coarse, while-in-use** location (`ACCESS_COARSE_LOCATION`).
     Granted → resolve to nearest city for the fetch.
  2. Declined (or location off) → pre-fill the city from the device
     **timezone** (e.g. `Europe/Stockholm` → Stockholm), no permission needed.
  3. Either way, the resolved city is always **displayed** in the Weather
     settings row and always editable via a city search field (Open-Meteo
     geocoding) — the assumption is visible, so it is self-correcting.
- Source: Open-Meteo (free, keyless, no account). Fetch current temperature
  at most ~6×/day (on unlock, throttled) and on city change; cache the last
  value; show nothing when stale > 6h or offline. Fail silently — a missing
  temperature is never an error state.
- Requires `INTERNET` and `ACCESS_COARSE_LOCATION` in the manifest; the app
  must remain fully functional with weather off and must make zero network
  calls and zero location reads in that state (verifiable in tests).

### 3.9 First-run onboarding (minimal, in-product-voice)
1. Welcome screen (serif): one sentence of what Sundial is.
2. Pick your apps: a single simplified picker seeding all three modes with
   sensible suggestions (messages, camera, phone pre-checked if present);
   user adjusts per mode later in settings.
3. Offer notification whispers (skippable).
4. Prompt to set Sundial as default home (system dialog).
Total: under a minute. No account, no tour, no tooltips.

---

## 4. Architecture

- **Language/UI:** Kotlin, Jetpack Compose, Material 3 as a base but heavily
  custom-themed (our palettes/typography override everything visible).
- **Structure:** single Gradle module, MVVM-lite:
  - `MainActivity` — the HOME activity (`intent-filter`: MAIN + HOME + DEFAULT).
  - `ui/` — composables: `HomeScreen`, `SearchSheet`, `RitualOverlay`,
    `SettingsScreen`, `ModeEditor`, `PausedScreen`, `Onboarding`.
  - `theme/` — palette definitions, typography, motion specs. One source of
    truth mirroring the demo's `PALETTES`.
  - `domain/` — `ModeEngine` (current mode, next boundary, recompute),
    `RitualGate` (flag store + 10-min windows).
  - `data/` — `AppRepository` (PackageManager queries, label cache, launch
    intents, package add/remove receiver), `SettingsRepository` (DataStore),
    `NotificationCounter` (listener service + flow of counts).
  - `system/` — `BootReceiver`, `TimeChangeReceiver`, alarm scheduling.
- **State:** a single `SundialState` (mode, theme, align, per-mode app lists,
  intentions, ritual flags, counts) exposed as a `StateFlow`; Compose renders
  from it. No global mutable singletons.
- **Permissions manifest:** `QUERY_ALL_PACKAGES` (justified: launcher),
  `RECEIVE_BOOT_COMPLETED`, notification listener service declaration, and —
  solely for the optional weather whisper (3.8) — `INTERNET` and
  `ACCESS_COARSE_LOCATION` (while-in-use, requested only when enabling
  weather). Enforce in code and tests that zero network calls and zero
  location reads occur while weather is off. Nothing else.
- **minSdk 26 (Android 8.0), targetSdk latest stable.**

### Platform conduct (binding rules)
- Inherit, never override: locale time/date formats, font scale, system insets,
  reduced-motion, RTL layout direction.
- No fixed pixel positions; column layout with `widthIn(max = ~480.dp)` centered
  — this single rule handles foldables, tablets, landscape, split-screen.
- Touch targets ≥ 48dp; full TalkBack labels on every interactive element;
  the ritual overlay announces itself politely.
- Non-Latin app labels render via font fallback chain (Fraunces/Manrope →
  system sans); never block or garble a script we don't cover.
- Battery: zero polling. Clock ticks only while visible; mode changes are
  alarms; notification counts are event-driven.
- Cold start budget: interactive < 400ms on mid-range hardware. The launcher is
  seen 100+ times a day; startup is a feature.

---

## 5. Tooling — what to install, in order

1. **Android Studio** (latest stable) — installed for the SDK, platform tools,
   and the emulator, not as the editor. During setup: install SDK for the
   latest stable Android + an emulator system image; create one Pixel-class
   AVD and, later, one foldable AVD for layout checks.
2. **VS Code + Claude Code extension** — the actual workbench.
3. **Git** — init the repo before the first line of code.
4. Ensure `JAVA_HOME`/JDK: use the JDK bundled with Android Studio (point
   Gradle at it) — no separate install needed.
5. Physical device (optional but recommended from Milestone 2 on): enable
   Developer Options + USB debugging; `adb install` takes seconds.

Claude Code drives everything from the terminal: `./gradlew assembleDebug`,
`./gradlew installDebug`, `adb shell` for poking, emulator time-fiddling to test
mode boundaries (`adb shell su? no — use emulator console or change AVD time`
— simplest: temporarily shrink mode spans in a debug build to watch transitions).

---

## 5b. Accounts & registrations (human tasks, not code)

Sundial deliberately needs almost nothing — no analytics accounts, no API keys
(Open-Meteo is keyless), no backend, no Apple anything. The complete list:

1. **Google account** — ideally a fresh one dedicated to the developer
   identity, not a personal inbox.
2. **Google Play Console developer account** — $25 one-time, government ID
   verification (allow ~2 business days). **Decision point:**
   - *Personal account:* fastest to open, but new personal accounts must pass
     closed testing (12 opted-in testers for 14 consecutive days) before
     production access.
   - *Organization account:* exempt from the 12-tester gate, but requires a
     registered business + D-U-N-S number (obtainable for a Swedish enskild
     firma via its organisationsnummer).
3. **Google Payments profile** (only when charging money): bank account for
   payouts + tax info. Google is merchant of record on Play and handles
   consumer VAT; Google's 15% service fee applies under $1M/year.
4. **Skatteverket** (Sweden, only when charging money): declare app income;
   for regular profit-oriented activity, register enskild firma + F-skatt.
   Not legal advice — confirm specifics with Skatteverket/an accountant.
5. **Git hosting** (optional but wise): a private GitHub/GitLab repo for
   backup and history.
6. **Testers** (only if personal Play account): 12+ friendly humans for the
   14-day closed test — recruit early, it's the schedule's long pole.

Timeline reality: from paying the $25 to a public listing, budget 2–4 weeks
(verification + closed testing + review), independent of code being finished.
Start the account process in parallel with ~M7, not after M10.

---

## 6. Build sequence — milestones for Claude Code

Work strictly in order; each milestone ends runnable on the emulator with its
acceptance checks passing before the next begins.

**M0 — Skeleton.** Compose project scaffold, HOME intent filter, builds and
installs; pressing Home offers Sundial. *Accept: Sundial appears in the default
launcher chooser and shows a blank themed screen.*

**M1 — App list & launch.** AppRepository, hardcoded single mode, tap to
launch, package add/remove updates. *Accept: every launchable app opens; labels
localized; uninstalls vanish from the list live.*

**M2 — Theme & modes.** Palette system from the demo, ModeEngine with fixed
spans, live clock/dateline, footer, alarms + receivers, animated crossfade.
*Accept: changing device time flips modes with the correct palettes; DST/timezone
change recomputes; survives reboot.*

**M3 — Gestures & search.** Long-press feedback, swipe-up sheet with filter,
asleep tags. *Accept: all three gestures work; keyboard opens focused; Back
behaves.*

**M4 — Ritual.** Flag store, overlay flow, 10-min window, toasts. *Accept:
flagged app shows ritual from both home and search; window skips correctly;
reduced-motion honored.*

**M5 — Settings & persistence.** Your day, mode editor, DataStore, soft cap,
intentions live-update. *Accept: kill the process; every setting survives.*

**M6 — Appearance.** Theme lock (Dawn/Noon/Dusk), alignment flip everywhere.
*Accept: fixed theme holds while rhythm continues; right-align mirrors counts
and dots.*

**M7 — Whispers.** Notification listener service, counts, permission row and
graceful degradation; weather whisper (location ladder → coarse location or
timezone prefill, editable city, Open-Meteo fetch, cache, dateline suffix).
*Accept: counts appear/disappear with real notifications; zero crashes when
permission absent; granting location resolves the right city; declining
pre-fills from timezone with no re-ask; temp vanishes cleanly when offline or
disabled; airplane-mode run with weather off logs zero network activity.*

**M8 — Onboarding & Pause.** First-run flow, default-launcher prompt, Pause
grid + resume. *Accept: fresh install to living home screen in under a minute.*

**M9 — Hardening.** TalkBack pass, foldable AVD pass, landscape, RTL smoke
test, cold-start profiling, battery sanity (no wakelocks), edge cases (empty
mode list → show only footer + handle, very long app names ellipsize).

**M10 — Release prep.** App icon (☉ mark, monochrome-capable for themed icons),
Play listing copy, privacy declaration (no data collected — makes the Data
Safety form trivially clean), closed-testing track, `QUERY_ALL_PACKAGES`
justification text, versioning + signed AAB.

---

## 7. Later list (explicitly out of v1 scope)

- Adjustable mode hours (tappable spans in the day editor — UI slot exists).
- Morning intention prompt (first unlock of the day asks "What's the one thing
  today?"; replaces static intention for that day).
- **Sundial: Instrument** — the Swiss/grotesk edition (see
  `sundial-two-faces.html`); first entry of a curated, one-at-a-time
  editions system (possible paid unlocks — the monetization path).
- Drag-to-reorder apps within a mode.
- System-wide grayscale helper (deep-link route; ADB `WRITE_SECURE_SETTINGS`
  power-user note).
- Additional languages beyond English (strings are externalized from day one).
- Widgets/complications (a single "asleep until" lockscreen widget, maybe).

Monetization for v1: free, no IAP, build trust and reviews. Revisit with
editions.

---

## 8. Working notes for Claude Code

- Read `sundial-demo.html` before M2 and keep it open as reference; port
  `PALETTES`, copy strings verbatim (they are product copy, not placeholders),
  and match motion timings.
- All user-facing strings in `strings.xml` from the first commit.
- Prefer boring, well-trodden APIs; this app's ambition is craft, not
  cleverness.
- After each milestone: commit, brief changelog, install on emulator, and stop
  for human review before proceeding.
