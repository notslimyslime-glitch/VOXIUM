# VOXIUM integration blueprint

**Status:** proposed implementation plan  
**Product goal:** one Android app with three independent launcher spaces  
**Strategy:** reuse upstream engine code through adapters; do not rewrite the game engines

This document turns the three-space product direction into an integration plan. It is deliberately conservative: upstream projects are separate Android applications, and their exact build requirements must be proven before their code is combined.

## 1. The key architecture decision

Build VOXIUM as a **single Android application with a shared shell and three engine integration modules**.

Do not ship three separate launcher apps and do not try to run three game engines at once.

The three spaces are independent launcher environments from the user's point of view, not separate Android users or separate Android installations. They share one APK, package identity, app process environment, permission model, and release cycle. VOXIUM must provide logical isolation for each space.

### High-level structure

```text
VOXIUM (one Android application)
|
+-- app-shell
|   +-- Home and navigation
|   +-- Space switcher
|   +-- Global appearance and app preferences
|   +-- Session coordinator (one active game session)
|
+-- core
|   +-- Space/profile/version/content models
|   +-- Storage ownership and path registry
|   +-- Download queue and routing
|   +-- Logging and diagnostics
|
+-- engine-java
|   +-- Java launcher integration adapter
|   +-- Upstream Java launcher screens/activities as needed
|
+-- engine-bedrock
|   +-- Bedrock launcher integration adapter
|   +-- Upstream Bedrock screens/activities as needed
|
+-- engine-starling
    +-- Starling Android/native integration adapter
    +-- Rust/JNI/native libraries and launch activity as needed
```

This is a target layout, not a claim that the upstream repositories can currently be included as Gradle modules unchanged.

## 2. What “three environments” means

Each space owns its own launcher state:

| Resource | Java Space | Bedrock Space | Starling Space |
|---|---|---|---|
| Version catalogue | Java versions and supported loaders | Official Bedrock versions supported by the importer | Starling runtime/game versions actually supported |
| Content browser | Java mods and modpacks | Add-ons, resource packs, behaviour packs, worlds | Only content Starling explicitly supports |
| Accounts | Java account flow owned by the Java engine where possible | Bedrock/Xbox account flow owned by the Bedrock engine | Only the authentication flow Starling actually requires |
| Settings | Java memory, runtime, renderer, controls | Bedrock-specific settings | Starling-specific settings |
| Library | Java profiles and installations | Imported Bedrock installations and worlds | Starling installations and worlds |
| Data root | Java-owned root | Bedrock-owned root | Starling-owned root |

Never imply that all content is interchangeable. In particular, Starling is a native Rust implementation for a defined game target, not a general JVM that can run arbitrary Java versions or arbitrary Java mods.

## 3. Proposed repository layout

Start with a small, understandable VOXIUM-owned structure:

```text
VOXIUM/
  app/
    src/main/
      AndroidManifest.xml
      java/.../VoxiumApplication.kt
      java/.../MainActivity.kt
  core/
    model/
    storage/
    downloads/
    session/
    diagnostics/
  engines/
    java/
    bedrock/
    starling/
  docs/
    architecture.md
    engine-audit.md
    integration-blueprint.md
```

Gradle modules should be added incrementally. Do not create empty modules for every folder before their build requirements are known. First prove that the shared shell builds as a minimal Android app, then add one integration at a time.

## 4. Upstream integration method

### Preferred method: reuse source/modules behind adapters

For each upstream project:

1. Pin a specific upstream commit or release. Never build from a moving branch in a reproducible release.
2. Audit its license and the licenses of bundled libraries, native binaries, assets, and submodules.
3. Identify its Android application entry points, custom Application class, Activities, Services, providers, receivers, permissions, manifest placeholders, resources, and native libraries.
4. Separate reusable launcher/engine code from its standalone app shell.
5. Keep the upstream launch flow and engine-specific UI where replacing it would add risk.
6. Add a thin VOXIUM adapter that opens the upstream flow and reports status back to the shared shell where feasible.
7. Resolve duplicate resources, manifest declarations, Gradle plugins, namespaces, dependencies, and native ABI conflicts.
8. Build and test that integration before starting the next one.

### Why we cannot simply copy three apps into one

Android packages have one application identity and one merged manifest. Three upstream apps may each declare their own Application class, launcher Activity, providers, services, permissions, app labels, resources, and build configuration. A final APK cannot blindly use all three standalone application configurations at once.

The integration must choose one VOXIUM Application class and one package identity, then deliberately merge only the compatible declarations. Any upstream initialization that lived in its own Application class must be moved into a safe, explicit initialization component or retained through an approved integration boundary.

Gradle application modules also cannot simply be nested as three independent apps inside one APK. Reusable Android library modules, source modules, or packaged engine libraries are the likely integration forms, but the right choice must be determined from each upstream build.

### Fallback method: separate engine packages

If an engine cannot legally or technically be embedded in the VOXIUM package, the fallback is to keep that engine as a separately installed companion app and have VOXIUM detect/launch it through a documented Android intent or deep link, if the upstream app supports one.

This fallback is less seamless and does not provide a true single-APK experience. It must be clearly labelled as an external engine, not presented as an embedded environment.

## 5. Engine-by-engine integration plan

### A. Java Space — ZalithLauncher2 / Pojav-derived code

**Role:** general Java Edition launcher environment.

Audit and prove:

- Exact upstream commit, license obligations, and all Pojav-derived notices.
- Gradle plugin, Kotlin, JDK, SDK, NDK and native build requirements.
- Which modules are required for launch versus optional UI/features.
- Application class and Activity entry points.
- Java runtime provisioning, architecture-specific libraries, renderer integration, input/control handling, and game-directory assumptions.
- Account flow, token storage, and whether it can remain entirely engine-owned.
- Whether its version/mod installers can be called independently of its original launcher shell.

Integration target:

- VOXIUM Java Space owns the entry screen, selected profile, and navigation.
- The existing Java engine owns the actual Java launch and any advanced screens that cannot safely be replaced.
- Java data and account state remain in the Java-owned area.
- Java version/mod compatibility is determined by the engine and loader, not by the shared UI.

**First proof:** build the upstream Java launcher unchanged, then build a VOXIUM shell that can open its launch Activity in a controlled prototype. Only after that should we attempt module extraction.

### B. Bedrock Space — LeviLauncher-Android

**Role:** Bedrock installation/import and launch environment.

Audit and prove:

- Exact upstream commit, Apache-2.0 notices, and dependency licenses.
- Gradle and Kotlin versions, CMake/NDK versions, native dependencies, ABI filters, Firebase/Google Services requirements, and build-time configuration.
- Application class, Activities, providers, receivers, permissions, and storage access.
- How the user imports or selects a legitimately obtained compatible Bedrock installation.
- Xbox/Microsoft authentication ownership and secure token storage.
- Version isolation, worlds, resource packs, behaviour packs, and native mods.
- Whether its app-specific initialization can be separated from its launcher screens and engine code.

Integration target:

- Bedrock Space keeps its own imported installation, worlds, packs, and account flow.
- VOXIUM does not bundle or distribute proprietary Minecraft APKs or game assets.
- The Bedrock engine remains responsible for its own compatibility and launch requirements.

**First proof:** build and run LeviLauncher independently with a legitimate compatible installation, then document its actual launch/storage boundary before adapting it.

### C. Starling Space — Starling Android / Rust runtime

**Role:** native Rust implementation for its supported Java Edition target.

Audit and prove:

- Exact Starling commit and MIT license scope, including submodules and packaged libraries.
- Supported Android ABIs and minimum Android version.
- Rust toolchain, cargo-ndk, Android NDK/API, JDK and SDK requirements from the current workflow.
- How the Android Activity is invoked and how JNI/native libraries are loaded.
- Graphics driver/device requirements, input bridge, orientation, assets, game-data import and save paths.
- Whether the runtime can be embedded as a library or must remain an Activity/package boundary.
- The exact game version supported by that Starling release.

Integration target:

- Starling Space has its own runtime identity, game data, worlds and settings.
- It is not shown as a general Java version selector.
- Java mods/loaders are not advertised as compatible unless Starling explicitly supports them.

**First proof:** build the upstream Starling Android artifact using its documented workflow and verify it launches on a supported ARM64 Android device before trying to package it into VOXIUM.

## 6. Shared contracts between the shell and engines

Keep the first contract small. The shared shell should not reach into private engine internals.

Conceptual Kotlin model:

```kotlin
enum class SpaceId {
    JAVA,
    BEDROCK,
    STARLING
}

data class SpaceSummary(
    val id: SpaceId,
    val displayName: String,
    val engineReady: Boolean
)

interface EngineAdapter {
    val spaceId: SpaceId

    suspend fun inspectAvailability(): EngineAvailability

    suspend fun getSummary(): SpaceSummary

    suspend fun prepareLaunch(profileId: String): LaunchPreparation

    suspend fun requestStop()
}
```

These are design sketches, not compiled interfaces. The real API must reflect what each upstream engine can actually expose. Some engines may only support launching an Activity and returning no structured launch result initially.

Use typed results for errors such as missing game files, unsupported ABI, missing runtime, unavailable account, incompatible version, and permission denial. Do not pass account passwords or raw authentication tokens through the shared shell.

## 7. Storage and account boundaries

Proposed logical layout (final physical paths depend on Android scoped-storage rules and upstream requirements):

```text
app-private/
  spaces/
    java/
      profiles/
      versions/
      libraries/
      assets/
      worlds/
      settings/
    bedrock/
      installations/
      worlds/
      resource_packs/
      behaviour_packs/
      settings/
    starling/
      runtime/
      game_data/
      worlds/
      settings/
  shared/
    download-staging/
    backups/
    diagnostics/
```

Rules:

- Each engine gets an explicit data-root configuration or retains its own upstream data root.
- Never move or rewrite upstream data directories until backup/restore and migration behavior are understood.
- Shared download staging is temporary only. Every download records its destination space and final owner.
- Do not assume Android public folders are freely writable on modern Android.
- Account UI is space-specific, but credentials should remain with the engine's established secure storage implementation wherever possible.
- Never log access tokens, passwords, or authentication responses.

## 8. One-session coordinator

The shared coordinator prevents two game sessions from being launched concurrently.

Conceptual lifecycle:

```text
IDLE
  -> PREPARING(space)
  -> RUNNING(space)
  -> STOPPING(space)
  -> IDLE
```

If the user switches spaces while a game is running, show the running-session state and require the game to stop before launching another engine. Do not force-kill an engine as the normal stop mechanism. Use its supported stop/exit flow, then release resources and update state.

## 9. Build and test sequence

| Milestone | Deliverable | Pass condition |
|---|---|---|
| 0. Source audit | Pinned commits, license/dependency inventory, build requirements | No unknown redistribution or signing assumptions |
| 1. Shell foundation | Minimal VOXIUM Android app and three-space navigation | Builds and switches spaces without engine code |
| 2. Java proof | Java engine launch boundary documented and connected | Java upstream flow opens and can be exited |
| 3. Bedrock proof | Bedrock launch/import boundary documented and connected | Legitimate installation can be selected and launched |
| 4. Starling proof | Rust runtime artifact and Android launch boundary documented | Supported runtime opens on target hardware |
| 5. Isolation | Per-space settings, data roots, libraries and account boundaries | Switching spaces never cross-loads another space's state |
| 6. Catalogues | Real version/content metadata and compatibility filters | Results are sourced, searchable and correctly scoped |
| 7. Release candidate | CI build, diagnostics and device test report | Install/upgrade/launch/exit tested on physical devices |

Browser testing is suitable for shell navigation and catalogue UI. It cannot validate Android manifests, native library loading, graphics drivers, game storage, authentication SDKs, or gameplay.

## 10. Device constraints and CI

The initial device is a Redmi 13C 5G, Android 15, 4 GB RAM. Keep the shell lightweight, paginate catalogues, avoid loading large mod metadata/assets into memory, and ensure only one game session runs at a time.

Use cloud CI for heavy Android/Rust builds where possible. Do not put signing keys, API secrets, Firebase service credentials, or upstream release credentials in source control. Create VOXIUM-owned signing credentials only when release signing is needed.

## 11. Risks and decisions still open

These must be answered by source/build inspection, not guesses:

- Can each upstream engine be built as an Android library or must some remain standalone Activities?
- Which upstream Application initializers can be safely extracted?
- Are there conflicting dependency versions, AndroidX/Compose versions, native libraries, or manifest providers?
- Which ABIs are supported by all three engines? Do not assume one ABI matrix.
- Which upstream licenses and transitive dependencies permit the intended redistribution?
- What are the actual storage APIs and migration requirements for each engine?
- Can the existing authentication flows be reused without duplicating or weakening credential storage?
- Does Starling's current runtime support the exact target device/GPU combination?

## 12. Immediate next actions

1. Pin upstream commits and record their license files and submodule revisions.
2. Create a reproducible build attempt for each upstream project independently.
3. Record each project's Application/Activity/Service/provider declarations and native library outputs.
4. Create the minimal VOXIUM Android shell with the three spaces and no engines.
5. Integrate one engine only after its independent build and launch boundary are understood.

**Do not import all three repositories at once.** The fastest route is one verified integration at a time, with a working build after every milestone.
