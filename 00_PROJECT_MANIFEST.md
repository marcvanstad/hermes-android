# 00_PROJECT_MANIFEST — Project 2: HERMES-ANDROID BRIDGE

## DNA (purpose in one line)
Give Zee hands: remote Android device control for hermes-agent — read screens, tap, type, swipe, open apps, SMS/calls, notifications — via a companion bridge app + 38 `android_*` tools.

## Status
- Phase: 4 — **LIVE (v0.4.0)**: bridge app installed + paired (Telegram route), relay :8766 supervised, 38 android_* tools active; PII-safe; re-pair on demand.
- Last updated: 2026-08-14
- Owner: Marco / Zee ⚡
- Upstream: https://github.com/raulvidis/hermes-android (v0.3.0 plugin / v0.2.0 APK)

## Structure
- **Brain (logic):** Python toolset (`~/.hermes/plugins/hermes-android/` — android_tool.py + android_relay.py, aiohttp relay on port 8766)
- **Muscle (execution):** Kotlin bridge app (APK on the phone — AccessibilityService + WebSocket client)
- **Bridge (comms):** WebSocket, phone connects OUT to server relay (works behind NAT). Local dev: `http://localhost:8766` (Ktor port 8765 for USB/LAN).

## Architecture
```
Phone (this Fold Z 8) ──WebSocket──> Hermes relay (port 8766) <──tools── Zee
   ▲ Hermes Bridge app                    ▲ runs on this same device (Termux)
   └── AccessibilityService = screen reads + taps/types/swipes
```

## Restore Steps (after reboot / reinstall)
1. Plugin: `git clone --depth 1 https://github.com/raulvidis/hermes-android` → copy `hermes-android-plugin/` to `~/.hermes/plugins/hermes-android` (or `curl -sSL https://raw.githubusercontent.com/raulvidis/hermes-android/main/install.sh | bash`)
2. APK: download `hermes-android-<version>.apk` from the `latest-build` release → install on phone → grant permissions
3. Verify: `/plugins` shows `✓ hermes-android (38 tools)`; `android_ping()` answers

## Config & Secrets
- Secrets → `~/.hermes/.env`: `ANDROID_BRIDGE_URL`, `ANDROID_BRIDGE_TOKEN`. NEVER in this manifest, never echoed.

## Pending (activation checklist)
- [x] APK installed (`com.hermesandroid.bridge` v0.2.0) — via Telegram-delivered APK after Auto Blocker + Play Protect toggles
- [x] Permissions granted: Accessibility service ON (verified via /ping)
- [x] Paired/connected: relay on localhost:8766, code DSB7DR, phone authenticated (auto-reconnects)
- [x] Plugin enabled (`hermes plugins enable hermes-android`) — 38 tools as deferred tools, gated on relay health
- [x] End-to-end verified: `android_ping` → `{"status":"ok","bridge":{...authenticated:true}}` (one-shot agent)
- [x] Relay watchdog cron: every 5 min, restarts relay if down (script `android_relay_watchdog.py`)
- [x] Tools appear in NEW sessions (deferred tools load per-session; plugin enabled)
- [x] Notification access CONFIRMED LIVE (2026-08-13) — relay /notifications returns real notifications; `notification_guard.py` + `agentic_trigger.py` unblocked
- [x] Agentic actions (2026-08-14): `agentic_trigger.py` per-rule `actions` array (`open` / `screenshot` / `speak`) — see daily log 2026-08-14
- [x] Bridge health in `health_check.py` (2026-08-14): relay + auth + a11y snapshot; anomaly alert only when down
- [ ] TLS for the relay — CLOSED as not-needed (2026-08-13, Marco): loopback-only + token auth; reopen only if relay goes LAN/remote

## Runtime facts
- **Bridge v0.4.0 INSTALLED** (2026-08-08) — pairing code rotated to 2SFWWC (in ~/.hermes/.env: ANDROID_BRIDGE_TOKEN). APK upgrade requires cert check first (v0.2.0 vs v0.4.0 used different debug keys — signature mismatch blocks updates; uninstall+fresh install needed).
- Relay runs as runit service `android-relay` (reads token from .env each start; pre-kill removed — port conflict handled by runit ownership). Env: `ANDROID_BRIDGE_URL=http://localhost:8766`, `ANDROID_BRIDGE_TOKEN=<code>` in `~/.hermes/.env`.
- adb pairing with Android 17 fails on adb 35 ("protocol fault") — same-device setup makes adb unnecessary anyway.

## Postponed / Deferred (resume point — full detail in daily log 2026-08-14)
- **Bridge upgrade v0.4.0 → v0.4.1** — STAY + WATCH (Marco, 2026-08-14): v0.4.1 fixes relay reconnect loop, but stable-as-is won; flag again if follow-ups or v0.5 (call features) drop.
- **Voice hands** ("Zee, open Uber" — one-listen → whisper → local LLM → open_app → speak ack) — DEFERRED, all pieces proven.
- **UI macro recipes bank** (`bridge-macro-recipes` skill + `macro_recipes.json`) — DEFERRED with its paired skill; build both together.
- **Banking auto-guard daemon** — PARKED (Marco): revival = `sv up banking-guard`; manual toggle + `banking_guard.py --status` stay.
- **Known limits** — keep workarounds: `/type` dodge (clipboard+paste), Voice Recorder tap no-op, never force-stop bridge app (a11y reset).

## Safety Rules (adopted from repo AGENTS.md / skill.md)
- Destructive on-device actions (purchases, sends, calls, deletions) → CONFIRM with Marco first.
- Review-first workflows (e.g. SMS draft via intent) preferred over direct send.
- Max 5-7 tool calls per request; no screenshot loops; 2-attempt cap then report.
- Pairing code auth is rate-limited (5/60s); phone connects out — but channel is currently `ws://` UNENCRYPTED → use TLS proxy for anything sensitive.
- PII (phone numbers, recipients, location) stripped from logs.

## Lessons Learned
- hermes-agent v0.20.0 does NOT ship android tools — plugin is required.
- aiohttp already present in Termux env (no extra install needed).
- Battery/load sensors need root in Termux (unrelated but noted for health script).

## Logs
- Daily logs: `~/CORE-OS/memory/` | Source clone: `~/CORE-OS/projects/hermes-android/`
