# Versioning

## Terms

| Term | Meaning | Example |
|------|---------|---------|
| **Visible version** | What users and store listings show. **You choose this only.** Format: `key.features.fixes` (semver-style). | `2.1.0` |
| **versionName** | Same as visible version; set in Gradle on **both** modules. | `"2.1.0"` |
| **Git tag** | Source snapshot on GitHub. Always `v` + versionName. | `v2.1.0` |
| **versionCode (phone)** | Internal Play integer for the **mobile AAB**. Increments by 1 every phone upload. | `2`, `3`, `4`… |
| **versionCode (wear)** | Internal Play integer for the **wear APK**. **Must differ from phone** and every other upload for this package. | `2000`, `2010`, `2013`… |

The About screen does **not** show a version string — only Gradle / Play / GitHub need updating.

---

## Play Store versionCode rules

Same package (`kattcrazy.sharemything`), separate tracks:

| Track | Artifact | versionCode |
|-------|----------|-------------|
| General (phone/tablet) | `ShareMyThing-mobile-release.aab` | Phone sequence: `1`, `2`, `3`… |
| Wear OS | `ShareMyThing-wear-release.apk` | Wear scheme: `major×1000 + minor×10 + patch` |

Examples:

| Visible version | Phone versionCode | Wear versionCode |
|-----------------|-------------------|------------------|
| `2.0.0` | `2` | `2000` |
| `2.1.0` | `3` | `2010` |
| `2.1.3` | `4` | `2013` |

Phone `versionCode` always **+1** from the last phone upload (not derived from versionName).

Wear `versionCode` is derived from versionName. If that value was already used, use the next free integer ≥ that value.

Both modules always share the same **versionName**.

References: [Wear packaging](https://developer.android.com/training/wearables/packaging), [Play form-factor tracks](https://support.google.com/googleplay/android-developer/answer/13295490).

---

## Bumping for a new Play release

**You** pick the visible version (e.g. `2.1.0`). Then run:

```powershell
cd "Other Programs\Share My Thing"
.\scripts\bump-version.ps1 -VersionName 2.1.0
```

The script updates `mobile/build.gradle.kts` and `wear/build.gradle.kts` automatically.

Then build, commit, push, and tag — see [Release checklist](#release-checklist) below.

---

## Release checklist

Use this whenever a build is **ready for Play Store** (closed testing or production):

1. **You** decide the visible version (`key.features.fixes`).
2. Run `.\scripts\bump-version.ps1 -VersionName X.Y.Z` (updates Play versionCodes + versionName).
3. Move done items out of [Roadmap](#roadmap) / note changes in commit message.
4. Run `.\scripts\release.ps1 -VersionTag vX.Y.Z`.
5. **Commit and push to GitHub** (`main`).
6. Create git tag: `git tag vX.Y.Z && git push origin vX.Y.Z`.
7. **Reminder:** create a [GitHub Release](https://github.com/kattcrazy/Share-My-Thing/releases/new) — attach APKs + `SHA256SUMS.txt` from `dist/release/vX.Y.Z/`. (Or use `-GitHub` if `gh` CLI is installed.)
8. Upload to Play Console (general AAB + Wear APK on their respective tracks).

### Agent / Cursor instructions

When preparing a Play-ready release:

- **Always** run `bump-version.ps1` with the user-provided visible version (never guess the version number).
- **Always** bump phone and wear versionCodes via the script — do not leave them matching each other.
- **Always** push commits to GitHub and **remind the user to create a GitHub Release** (tag + artifacts).
- Do **not** show version strings in the app UI unless the user asks.
- Visible version format is **`key.features.fixes`** — only the user decides those numbers.

---

## Roadmap

Planned for the **next visible version** (number chosen by you when ready):

- [ ] **Delayed save/delete on mobile** — saving or deleting an item takes 5–30 seconds; fix responsiveness.
- [ ] **Watch complication icons** — some complication formats need icons; use the item type icon (text / QR / both).
- [ ] **Mobile app shortcuts** — shortcuts to chosen items (like widgets). New section under Widgets, up to 5 shortcuts.
- [ ] **Watch privacy policy styling** — privacy policy text should use small body/heading style, not an item bubble.
- [ ] **Watch tiles + complications** — combine Tiles and Complications setup onto one scrollable page.
- [ ] **Mobile surfaces split** — split Tiles and Complications into two separate sections (currently combined or unclear).

---

## File locations

| What | Where |
|------|--------|
| Phone versionName / versionCode | `mobile/build.gradle.kts` |
| Wear versionName / versionCode | `wear/build.gradle.kts` |
| Bump script | `scripts/bump-version.ps1` |
| Release build | `scripts/release.ps1` |
| Built artifacts | `dist/release/v{versionName}/` |

See also [PUBLISHING.md](PUBLISHING.md).
