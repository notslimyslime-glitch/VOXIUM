# VOXIUM

A hybrid Android game launcher project exploring integrations for Java Edition, Bedrock Edition, and Starling.

## Project direction

- Reuse and integrate existing engines instead of rebuilding them.
- VOXIUM owns the shared home screen, navigation, game library, downloads, and shared settings.
- Keep engine-specific launch, account, and advanced settings flows with their respective engines where needed.
- Run one game engine/session at a time.
- Use a browser prototype for UI and catalogue-flow testing; validate native gameplay on a physical Android device.

## Current status

This repository starts with the product architecture and a lightweight browser prototype. The prototype uses sample catalogue data and does not launch games or install real versions/mods.

## Browser prototype

Open `prototype/index.html` in a browser, or enable GitHub Pages for the `/prototype` folder to preview it online.

## Planned engine integrations

| Engine | Role | Important constraint |
|---|---|---|
| ZalithLauncher2 / Pojav-based core | Java Edition | JVM-based; supported versions depend on runtime and upstream compatibility |
| LeviLauncher-Android | Bedrock Edition | Requires a legitimate compatible Bedrock installation/account |
| Starling | Native Rust implementation | Currently targets a specific Java Edition release; not a general JVM replacement |

## Development notes

The target development device is a Redmi 13C 5G on Android 15 with 4 GB RAM. Heavy Android builds should run in cloud CI rather than locally on the phone. Test actual gameplay on-device; browser preview cannot validate native graphics, storage, input, or engine startup.

See [Architecture](docs/architecture.md) and [Engine audit](docs/engine-audit.md).
