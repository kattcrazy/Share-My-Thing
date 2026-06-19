# Privacy Policy for Share My Thing

This policy describes how the Share My Thing apps handle information. It applies to both the watch and phone builds.

## Summary

Share My Thing stores the content you enter (item names, text, URLs, and related settings) on your devices. There is no developer-run server, no user account, and no analytics or advertising in the app.

## Information you provide

When you use the app, you may enter data such as:

- Item titles and body text  
- URLs or other strings encoded as QR codes  
- Assignments of items to watch tile or complication slots  

This data is saved locally using Android storage on each device where the app is installed.

## How data is used

Your data is used only to:

- Display items in the app and on watch tiles/complications  
- Generate QR codes from your content  
- Sync items and slot assignments between your paired phone and watch  

The developer does not receive, access, or sell your item content.

## Sync between phone and watch

When phone and watch are paired and both apps are installed, data syncs directly between your devices using Google’s Wear OS / Play services data layer (Bluetooth/Wi‑Fi as managed by the system). Sync payloads are JSON messages between the two apps.

Google’s handling of Play services is governed by [Google’s privacy policy](https://policies.google.com/privacy).

## Device backup

Your app data may be included in your Google/Android device backup if you use that feature. Backup is controlled by your device and Google account settings, not by the developer.

## Links opened in a browser

The phone app may open external links in your browser, including:

- A support me product page on kattcrazy.nz
- The Share My Thing GitHub repository  

The app does not pass your item content to those pages.

## Data retention and deletion

Data remains on your device until you delete items in the app or uninstall the app. Uninstalling the app on one device does not remove your items on a paired phone or watch. Only uninstalling on both devices removes all app data (aside from any device backup).

## Changes to this policy

This policy may be updated from time to time. The current version will always be in this repository at `docs/PRIVACY.md`.

## Open source

Source code is available at [github.com/kattcrazy/Share-My-Thing](https://github.com/kattcrazy/Share-My-Thing) under the [GNU GPL v3.0](../LICENSE).
