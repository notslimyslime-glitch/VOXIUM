# VOXIUM architecture (working proposal)

## Product shell

VOXIUM provides one consistent entry point:

- Home and installed-game library
- Engine picker
- Version and mod discovery
- Shared download queue and notifications
- Shared diagnostics and backup entry points
- Shared appearance and navigation preferences

## Engine boundaries

Treat each upstream launcher/runtime as an adapter at first, not as code to flatten into one giant module. Keep engine-owned launch configuration, authentication, game directories, and advanced settings isolated until compatibility is proven.

```
VOXIUM app shell
  ├── Shared library and catalogue UI
  ├── Shared downloads / storage coordination
  └── Engine router (one active session)
       ├── Java adapter -> ZalithLauncher2 / Pojav core
       ├── Bedrock adapter -> LeviLauncher-Android
       └── Starling adapter -> native Rust runtime
```

## Integration sequence

1. Audit each upstream repository, submodules, native binaries, build scripts, and licenses.
2. Identify launch entry points, storage assumptions, account flows, and required Activities/services.
3. Build a thin proof of concept for each engine independently.
4. Add the shared app shell and route into the upstream engine flows.
5. Unify only services that have compatible contracts (downloads, logs, backups, catalogue metadata).
6. Validate each engine on physical Android hardware.

## Non-goals for the first milestone

- Do not rewrite the Java or Rust engines.
- Do not merge all engine settings into a single universal settings screen.
- Do not promise arbitrary versions where an engine does not support them.
- Do not run multiple game sessions simultaneously.

## Device constraints

The initial target is Android 15 / 4 GB RAM. Keep the shell lightweight, avoid holding large catalogue/game assets in memory, paginate catalogue results, and release the previous engine process before starting another.

## Storage

Do not assume a single shared writable filesystem path. Android scoped storage and each upstream engine's expected paths must be audited first. Start with separate engine-owned data roots and add shared download staging only where safe.
