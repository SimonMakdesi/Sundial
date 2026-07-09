# Sundial — Google Play release kit

Everything the Play Console will ask for, drafted and ready to paste.
Product copy is in Sundial's voice: calm, no discipline-speak, no guilt.

---

## App details

- **App name:** Sundial
- **Package / applicationId:** `com.makdesi.sundial`
  *(final call before first upload — it can never change afterwards)*
- **Category:** Personalization
- **Tags:** launcher, home screen, minimal, focus
- **Contains ads:** No · **In-app purchases:** No
- **Price:** Free

## Short description (max 80 chars)

> A home screen that follows the light. Three moments a day, only your apps.

*(78 chars)*

## Full description

> Sundial replaces your home screen with a quiet, text-only list of apps
> that changes with the time of day.
>
> **Three moments.** Morning, day, and evening each hold only the apps you
> choose for that time, under a horizon that shifts from dawn paper to noon
> light to dusk ember.
>
> **Nothing is ever blocked.** Apps outside the current moment are simply
> asleep — one swipe up finds anything, any hour. Sundial states its
> opinions once, gently, and never enforces them.
>
> **One breath.** Mark an app with ☉ and it asks for a single breath before
> opening. Open it anyway, or decide it can wait. Your call, always.
>
> **An intention, not a to-do list.** Write one line for each moment of the
> day. It sits under the clock, between two hairlines.
>
> **Whispers, not badges.** An optional quiet number next to an app's name
> when something waits. No banners, no red.
>
> **Private by design.** No account, no analytics, no tracking. Nothing
> leaves your phone — with one optional exception: if you turn on the
> weather whisper, Sundial fetches the temperature for a city you choose
> (Open-Meteo, no account, no identifiers). Weather off means zero network
> use. That's the whole list.
>
> The entire interaction model: tap to open, swipe up to search, hold to
> tend your day.

## Data Safety form (answers)

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | N/A (nothing collected) |
| Do you provide a way for users to request that their data is deleted? | N/A (nothing collected) |

Notes for reviewers / rationale:
- All settings (app lists, intentions, flags) are stored on-device in
  DataStore, enrolled in Android Auto Backup (device-to-device, user's own
  Google backup — not developer-accessible).
- The optional weather feature sends only coordinates/city of a
  user-chosen location to Open-Meteo, with no user identifiers, no
  account, and no persistence server-side that the app controls. Location
  permission is coarse, optional, requested only when the user enables
  weather, and never used otherwise. This does not constitute collection
  of user data under Play's definitions (ephemeral, service-essential,
  not linked to identity), and weather is off by default.

## Permission declarations

### QUERY_ALL_PACKAGES (sensitive — requires justification text)

> Sundial is a launcher (home screen replacement; category
> Personalization, holds the HOME intent). Its core, user-facing function
> is to list and open every launchable app on the device: the home screen
> shows the apps the user assigned to the current time of day, and the
> universal search lists all installed apps. This is impossible without
> broad app visibility. Sundial does not transmit the app list off the
> device; it has no analytics and no backend.

*(Play policy explicitly allows QUERY_ALL_PACKAGES for launchers.)*

### Other permissions (no form, but be ready to explain)

- `INTERNET` + `ACCESS_COARSE_LOCATION`: solely the optional weather
  whisper; off by default; zero use when off.
- `RECEIVE_BOOT_COMPLETED`: reschedule the time-of-day alarms after boot.
- Notification listener (`BIND_NOTIFICATION_LISTENER_SERVICE`): the
  optional notification counts; never required; the app is fully
  functional without it. Contents of notifications are never read beyond
  counting, never stored, never transmitted.

## Release configuration

- **versionCode 1 · versionName 1.0.0**
- **Artifact:** `app/build/outputs/bundle/release/app-release.aab`
  (build with `./gradlew bundleRelease`)
- **Signing:** upload key `sundial-upload.jks` (alias `sundial-upload`),
  credentials in `key.properties` — both gitignored, **back both up
  outside this machine**. Enroll in **Play App Signing** at first upload
  (Google holds the app signing key; the upload key can then be reset if
  ever lost).
- Target audience: 13+ (no child-directed content).
- Content rating questionnaire: no violence/sex/profanity/gambling/drugs;
  no user-generated content; no data sharing → expect "Everyone".

## Store assets still needed (human/design tasks)

- 512×512 hi-res icon PNG (export from the adaptive icon art).
- Feature graphic 1024×500 (suggestion: the three horizon gradients as
  bands on ink, wordmark in Fraunces).
- At least 2 phone screenshots per Play requirements (suggestion: Dawn
  home with intention, Dusk home, search sheet, ritual overlay, Your day).
- Privacy policy URL (required even when collecting nothing — a one-page
  static site stating the above; GitHub Pages works).

## Launch sequence — organization account (decided 2026-07-09)

Using the existing registered Swedish company. Organization accounts are
exempt from the personal-account gate (12 testers / 14 consecutive days).

1. **D-U-N-S number**: look up the company at dnb.com with the
   organisationsnummer — Swedish companies often already have one
   auto-generated from public registries. If absent, request free
   (days–2 weeks).
2. **Play Console: create an Organization account** ($25 one-time).
   Legal name must match the organisationsnummer registration; a
   company-domain email smooths verification. The company name appears
   publicly as the developer.
3. Create app → upload `app-release.aab`. Fill listing + Data Safety +
   content rating + QUERY_ALL_PACKAGES declaration (all texts above).
4. Optional but recommended: a short closed-testing round with a few
   people (by choice — no mandated window), then promote to production.
