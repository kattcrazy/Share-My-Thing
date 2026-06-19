# Sync behaviour (2026-06)

## What triggers sync now

Any local data change in `ItemsRepository` invokes `onLocalDataChanged`, wired in both apps to `SyncRepository.syncWithWatch()`:

- Create / edit item (`upsert`)
- Delete item
- Tile/complication slot assign or clear (`setSlotItemId`)
- Reorder items (`reorderItems`)

Both **phone and watch** push to the paired device. Incoming `/sync/request` is still handled by `MobileSyncListenerService` / `WearSyncListenerService`.

On app start, a one-shot sync runs after ~1.5s (to catch drift after reconnect).

## If sync still feels broken

1. Confirm both apps are installed and show as connected in Wear OS / adb devices.
2. Phone app should stay running in background (listener service) for watch-initiated sync.
3. Sync waits up to 30s for a `/sync/response`; timeouts fail silently today.
4. `surfacesPlacedOnWatch` (whether a tile is pinned on the watch face) is **not** synced — only slot → item UUID assignments are.

## Follow-up ideas

- Surface sync errors in UI (optional toast/snackbar).
- Sync on `MainActivity.onResume` when a peer is connected.
- Include `placedOnWatch` in `SyncPayload` if cross-device tile placement state matters.
