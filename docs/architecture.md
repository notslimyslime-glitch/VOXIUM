# VOXIUM architecture

## 1. Product model: three isolated spaces

VOXIUM is one Android application with three distinct launcher spaces. The shared shell is responsible for navigation and switching; each space owns its own launcher context.

VOXIUM app
- Shared shell: home, space switcher, global appearance/navigation preferences, one-active-session coordinator
- Java Space: Java versions, profiles, mods, library, Java account connections/settings, ZalithLauncher2/Pojav-derived integration
- Bedrock Space: Bedrock versions, worlds, packs, library, Bedrock account connections/settings, LeviLauncher-Android integration
- Starling Space: Starling runtime versions, worlds, library, Starling-specific settings/content, native Rust integration

The spaces should feel independent to the user, but they are not three separate Android installations. They share one app package, process environment, permissions, and release cycle. Isolation must be implemented deliberately in app data models, storage paths, and engine boundaries.

## 2. Space isolation rules

| Area | Rule |
|---|---|
| Installed games and worlds | Keep engine-owned data roots separate; never silently point two engines at the same writable game directory |
| Accounts | Present account connections inside the relevant space. Let the upstream engine own tokens and authentication where required; do not copy credentials between engines |
| Settings | Store space-specific settings under that space's namespace. Keep only app-wide appearance/navigation preferences global |
| Versions | Show only versions supported by that space's engine. Starling's native runtime version list is not the same as a general Java JVM version list |
| Mods and packs | Use the correct content type and compatibility rules for each engine. Java mods are not automatically compatible with Bedrock or Starling |
| Downloads | A shared queue may coordinate transfers, but each download must have a destination and ownership space |
| Active session | Run one game session at a time. Stop/release the previous engine before starting another |

## 3. Proposed app structure

- app/shell/ for home, navigation and space switcher
- app/spaces/java/ for Java screens and state
- app/spaces/bedrock/ for Bedrock screens and state
- app/spaces/starling/ for Starling screens and state
- core/model/ for space, profile, version and content models
- core/storage/ for scoped storage and per-space paths
- core/downloads/ for queue with destination-space ownership
- core/session/ for one-active-session coordination
- engines/java/ for upstream Java launcher adapter
- engines/bedrock/ for LeviLauncher adapter
- engines/starling/ for Starling native runtime adapter

This is a target structure, not a claim that the upstream projects already fit these modules. First integration may need to launch their existing Activities and preserve their internal flows.

## 4. Integration sequence

1. Complete license, dependency, native-binary, secret, and build audits for all upstream projects.
2. Build each upstream project independently and document its launch entry point and storage assumptions.
3. Define the space model and prove that switching spaces restores the correct UI state without crossing data boundaries.
4. Add one thin engine integration at a time, preserving its native authentication and settings flow.
5. Connect real version and content catalogues with explicit compatibility metadata.
6. Build APKs through CI and test each engine on physical Android hardware.

## 5. Non-goals for the first milestone

- Do not rewrite the Java or Rust engines.
- Do not merge engine-specific account flows or advanced settings into one universal implementation.
- Do not promise arbitrary versions or cross-engine mod compatibility.
- Do not run multiple game sessions simultaneously.
- Do not redistribute Minecraft game files or APKs.

## 6. Device and storage constraints

The initial target is Android 15 with 4 GB RAM. Keep the shell lightweight, paginate catalogue results, avoid retaining large assets in memory, and release the previous engine process before starting another.

Android scoped storage and upstream directory expectations must be audited before selecting final paths. Start with engine-owned data roots. A shared download staging area is acceptable only when ownership and final destination are explicit.


## Detailed integration plan

See the [VOXIUM integration blueprint](integration-blueprint.md) for the proposed module layout, engine-by-engine integration strategy, adapter boundary, storage/account isolation, session lifecycle, build sequence, and unresolved audit questions.
