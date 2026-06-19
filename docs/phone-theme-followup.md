# Phone theme — follow-up checklist

You confirmed the **watch** colours look good after the shared `:ui-theme` seed (`#6495ED`, M3 Tonal Spot). The phone was not tested yet.

## What to verify on mobile

1. Install `dist/ShareMyThing-mobile-debug.apk`.
2. Toggle system **light** and **dark** mode.
3. Check these screens in both modes:
   - Item list (primary cards, “Watch” section, tiles row)
   - Edit item
   - Detail (text + QR)
   - About (?)

## Expected behaviour

- **Light:** primary actions use brand blue (`ShareMyThingColorSchemes.light`, seed `#6495ED`).
- **Dark:** should match watch tokens (`ShareMyThingColorSchemes.dark` → same as `watchDark` source).
- Role hierarchy should mirror Material 3 (primary cards, `secondaryContainer` for secondary rows), not the old hand-tuned palette in deleted `mobile/.../theme/Color.kt`.

## If something looks wrong

| Issue | Likely fix |
|-------|------------|
| Primary too weak/strong in light mode | Regenerate Tonal Spot tokens in [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/) from `#6495ED` and update [`ui-theme/.../ShareMyThingColorSchemes.kt`](../ui-theme/src/main/java/com/sharemyththing/theme/ShareMyThingColorSchemes.kt) |
| Dark mode doesn’t match watch | Ensure mobile `Theme.kt` uses `ShareMyThingColorSchemes.dark`, not a local override |
| Status bar contrast | Adjust `SideEffect` in [`mobile/.../theme/Theme.kt`](../mobile/src/main/java/com/sharemyththing/presentation/theme/Theme.kt) |

## Source of truth

- Shared schemes: [`ui-theme/src/main/java/com/sharemyththing/theme/`](../ui-theme/src/main/java/com/sharemyththing/theme/)
- Watch fixed palette: `ShareMyThingColorSchemes.watchDark` (always dark)
