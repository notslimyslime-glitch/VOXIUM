# Upstream engine audit

This is a preliminary planning audit. Re-check the upstream default branches and exact dependency versions before importing code.

## Starling

- Repository: https://github.com/starling-client/starling
- Role: native Rust implementation for a specific Java Edition release, not a general JVM launcher.
- Upstream license noted in the initial review: MIT.
- Key checks before integration: supported game version, Android ABI coverage, graphics-driver requirements, asset/data import flow, native library packaging, and update cadence.
- User must provide legally obtained game files where required.

## LeviLauncher-Android

- Repository: https://github.com/LiteLDev/LeviLauncher-Android
- Role: Bedrock launcher/import and version-management flow.
- Upstream license noted in the initial review: Apache-2.0.
- Initial review found an Android app with native CMake integration and ARM64 filtering. Build scripts and Firebase configuration need a complete pass before reuse.
- Confirm exact compatible official Bedrock package/version and account/licensing requirements. Do not bundle or redistribute game APKs.

## ZalithLauncher2

- Repository: https://github.com/ZalithLauncher/ZalithLauncher2
- Role: Java Edition launcher based on a Pojav-derived launch core.
- Upstream license noted in the initial review: GPL-3.0 with additional project-specific naming/attribution conditions. Re-check the current LICENSE and all bundled dependencies before redistribution.
- Initial review found a modern Kotlin/Compose codebase and Gradle version catalog. The README's build requirements may be stale.
- Security note: an earlier review observed signing-related values in a committed Gradle properties file. Never reuse upstream signing credentials. Remove secrets from any fork and create fresh signing material.

## Hyper Launcher 3 reference

- Repository: https://github.com/ashmeet-op/Hyper_Launcher_v3
- Useful architectural reference for a shared launcher shell with separate Java and Rust engine integrations.
- Inspect its exact LICENSE, submodules, and bundled AAR license before copying any code. Reference architecture does not grant reuse rights.

## Required audit before code import

- [ ] Read current LICENSE and NOTICE files for each repository and submodule.
- [ ] Inventory native binaries and their licenses.
- [ ] Inventory Activities, services, providers, permissions, and intent contracts.
- [ ] Inventory Gradle, Kotlin, Rust, NDK, CMake, and SDK requirements.
- [ ] Check all API keys, signing config, Firebase files, and other secrets.
- [ ] Map each engine's data directory and storage permission flow.
- [ ] Build each upstream engine independently before attempting a combined app.
