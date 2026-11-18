# VOXIUM

**Three spaces. One launcher.** VOXIUM is a hybrid Android launcher concept that keeps Java Edition, Bedrock Edition, and Starling in separate, switchable spaces inside one app.

## Product direction

VOXIUM should feel like three launcher environments hosted by one lightweight shell:

- **Java Space** — Java Edition versions, Java mods, profiles, and Java engine settings.
- **Bedrock Space** — Bedrock versions, worlds, resource packs, add-ons, and Bedrock-specific settings.
- **Starling Space** — the native Rust-based Starling runtime, its supported game version, data, and compatible content.

Each space has its own library, catalogue context, account connections, settings, and game-data boundary. Shared navigation and app appearance can live in the VOXIUM shell, but engine-specific authentication and launch flows remain with their engines where required.

Only one game session runs at a time. Switching spaces changes the active launcher environment; it does not merge game files or guarantee that every engine supports the same versions or content.

## Browser prototype

**[Open the VOXIUM UI preview](https://notslimyslime-glitch.github.io/VOXIUM/prototype/)**

The browser prototype demonstrates space switching, sample version lists, content browsing, library/settings screens, and an account entry point. Its catalogues are sample data: it does not sign in, install games, or launch engines.

## Engine integration plan

| Space | Existing project | Important constraint |
|---|---|---|
| Java | [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) / Pojav-derived core | JVM-based; compatibility depends on runtime, version, loader, and device |
| Bedrock | [LeviLauncher-Android](https://github.com/LiteLDev/LeviLauncher-Android) | Requires a legitimate compatible Bedrock installation and account |
| Starling | [Starling](https://github.com/starling-client/starling) | Native Rust implementation for a specific Java Edition release, not a general JVM replacement |

## Development

The initial test device is a Redmi 13C 5G running Android 15 with 4 GB RAM. Use browser preview for UI/catalogue-flow testing and a physical Android device for native gameplay, graphics, storage, input, and engine startup. Heavy Android builds should use cloud CI where practical.

See [Architecture](docs/architecture.md), [Integration blueprint](docs/integration-blueprint.md), and [Engine audit](docs/engine-audit.md).
