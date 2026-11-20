# Publishing Beer Tracker on Google Play

Everything in this folder is ready to paste or upload. The steps below are in order; the
ones marked **(you)** have to be done by the account holder.

| Item | Value |
|---|---|
| Package name (permanent) | `com.ambertally.beertracker` |
| Developer name to show publicly | **Amber Tally** |
| App name on the store | **Beer Tracker: Weekly Units** |
| Category | Health & Fitness |
| Price | Free, no ads, no in-app purchases |
| Current version | 1.5 (versionCode 6), targets Android 16 (API 36) |

Files:
- `icon-512.png`: store icon (512×512)
- `feature-graphic.png`: feature graphic (1024×500)
- `screenshots/*.png`: 5 phone screenshots (1080×1920)
- `../app/src/main/assets/privacy.html`: privacy policy. Also shown inside the app; needs a contact email, then a public web address.

---

## 1. Create the developer account (you)

1. Go to <https://play.google.com/console/signup>, choose **Yourself** (personal account).
2. Pay the one-time $25 fee and complete identity verification with your ID.
3. **Developer name:** enter `Amber Tally`. This is what appears on the store under the app.
   Your legal name is used for verification. Read the Console's notes on the sign-up screen
   about which details are shown publicly.
4. **Contact email:** this is shown publicly on the store listing. To keep your personal
   address private, create a separate address just for the app (for example a new Gmail
   account) and use that.

## 2. Sign builds with the upload key (you, about 5 minutes)

You were given `GITHUB-SECRETS.txt` and `upload.jks`. **Save both somewhere safe** (a password
manager is ideal). They are not in the repository and cannot be recovered from this chat later.

1. On GitHub, open the repository → **Settings → Secrets and variables → Actions → New repository secret**.
2. Add the four secrets exactly as listed in `GITHUB-SECRETS.txt`:
   `BT_KEY_ALIAS`, `BT_KEYSTORE_PASSWORD`, `BT_KEY_PASSWORD`, `BT_KEYSTORE_BASE64`.
3. Open **Actions → Beer Tracker Android → Run workflow** (pick the branch with the app).
4. When it finishes (about 1 minute), open the run and download the **beer-tracker-release**
   artifact. Inside the zip, `bundle/release/app-release.aab` is the file you upload to Play.
   The "Check signature" step should say `jar verified`.

If the upload key is ever lost, Play support can reset it. Google holds the real app signing
key (see step 5), so a lost upload key doesn't lose the app.

## 3. Create the app in Play Console (you)

**Home → Create app:**
- App name: `Beer Tracker: Weekly Units`
- Default language: English (United Kingdom) – en-GB
- App or game: **App**. Free or paid: **Free**
- Tick the declarations (Developer Program Policies, US export laws).

## 4. App content (Policy → App content)

Answer each section like this:

| Section | Answer |
|---|---|
| **Privacy policy** | The public URL where you published `privacy.html` (see "Privacy policy hosting" below) |
| **App access** | All functionality is available without special access (no login) |
| **Ads** | No, my app does not contain ads |
| **Content rating** | Start the questionnaire. Email: your app contact email. Category: **All Other App Types** (it's a utility, not a game, social or news app). Answer **No** to violence, sexual content, profanity, gambling, user-to-user interaction, sharing location, and digital purchases. For **alcohol, tobacco or drugs**, answer **Yes: references to alcohol** (it logs drinks). It doesn't sell alcohol or show anyone drinking. Expect a teen-or-older rating. |
| **Target audience** | **18 and over** only. Answer **No** to "could the app unintentionally appeal to children". |
| **Data safety** | Does your app collect or share any of the required user data types? **No**. (Everything stays on the phone. Data that never leaves the device doesn't count as "collected".) Then save. |
| **Government apps** | No |
| **Financial features** | My app doesn't provide any financial features |
| **Health apps** (if shown) | Answer truthfully. It lets people log their own alcohol consumption and shows totals against the UK 14-units guideline. It gives no medical advice or diagnosis and has no sensors or health-service connection. Pick the category closest to personal consumption/habit tracking. |
| **News apps** | No |

## 5. Store listing (Grow users → Store presence → Main store listing)

**App name** (30 max)
```
Beer Tracker: Weekly Units
```

**Short description** (80 max)
```
Log beers in one tap and keep an eye on weekly units, dry days and history.
```

**Full description** (4000 max)
```
Beer Tracker is a simple, private way to keep count of what you drink each week.

Tap Pint, Half, Can or Bottle and it's logged. Beer Tracker works out the alcohol units from the size and strength, and shows how your week compares with the UK low-risk guideline of 14 units a week.

WHAT YOU GET
• One-tap logging for pints, halves, cans and bottles, with your usual strength (ABV)
• Add details when you want them: beer name, any size, how many, and the time
• Forgot one? Log drinks on any earlier day
• This week at a glance: drinks, UK units, pints by volume and dry days
• A day-by-day chart and the last 8 weeks of history
• Home-screen widget: see this week's total and add a pint without opening the app
• Undo for anything you add by mistake
• Light and dark mode

PRIVATE BY DESIGN
• No account, no ads and no tracking
• No internet access. Your log never leaves your phone
• Export a backup file whenever you like, and import it on a new phone

Beer Tracker is a personal log and does not give medical advice. If you're worried about your drinking, talk to your doctor or a support service in your country.

For adults of legal drinking age.
```

**Graphics:** upload `icon-512.png`, `feature-graphic.png`, and all five files in `screenshots/`
(Phone screenshots).

**Category:** Health & Fitness. **Tags:** pick the closest offered (e.g. habit tracking, health tracking).

**Contact details:** the app contact email from step 1. Website is optional.

## 6. Closed test: 12 testers for 14 days (you)

New personal accounts must run this before going public.

1. **Testing → Closed testing → Create track** (or use the default "Alpha").
2. **Countries:** pick where your testers are.
3. **Testers:** create an email list with at least **12** Google accounts that use Android phones
   (yourself and your friend count). Put in a few extra in case someone drops out.
4. **Create new release:**
   - When asked about app signing, keep **"Use Google-generated key"** (Play App Signing).
   - Upload `app-release.aab` from step 2.
   - Release name: `1.5`. Release notes:
     ```
     First release of Beer Tracker: one-tap logging, weekly units, history, widget and backup.
     ```
   - **Review release → Start rollout to Closed testing.**
5. After Google reviews it (often 1–3 days), copy the **opt-in link** from the track's Testers tab
   and send it to every tester. Each one must open it, tap **Become a tester**, then install the
   app from the Play Store link on that page.
6. Testers must **stay opted in for 14 days in a row**. Ask them to open the app a few times. Google
   asks about how testing went when you apply.

## 7. Go live (you)

After 14 days, Dashboard → **Apply for production access**. Answer the questions about the test,
for example: how you recruited testers, what feedback you got, and what you changed. Once approved:
**Production → Create new release → add the same (or a newer) bundle → Start rollout.**
Production review usually takes a few days.

## Moving your friend to the Play version

Play signs the app with Google's key, and the store app has a new package name, so it installs as
a separate app next to the sideloaded one. To move the data:
1. Old app: **Backup → Export** and save the file.
2. Install Beer Tracker from Play, open it, **Backup → Import** that file.
3. Check the drinks are there, then uninstall the old sideloaded app.

## Shipping an update later

1. Bump `versionCode` (must increase every upload) and `versionName` in `app/build.gradle`.
2. Push. GitHub Actions builds a signed bundle automatically. Download it from the run.
3. Play Console → the track → **Create new release** → upload → roll out.

## Privacy policy hosting

Google needs the privacy policy at a public web address, and inside the app (done: the app's "Privacy" link
opens a bundled copy). `app/src/main/assets/privacy.html` is ready apart
from the contact email (contact email filled in). Any
static host works. You just need the final link to paste into step 4.
