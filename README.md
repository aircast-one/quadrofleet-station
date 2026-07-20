# quadrofleet-station

QuadroFleet Station Control.

## What this repository adds: macOS support

The original QuadroFleet Station did not run on macOS. This repository ports it
to macOS (Apple Silicon and Intel):

- Native library discovery on macOS — GStreamer from
  `/Library/Frameworks/GStreamer.framework`, SDL2 (gamepad support) from
  Homebrew paths; both overridable via `-Dgstreamer.path=...` / `-Dsdl2.path=...`.
- A self-contained macOS `.dmg` installer with bundled JRE, GStreamer, and
  SDL2 — nothing to preinstall on the target machine.
- CI release pipeline that builds and publishes the macOS installer
  (alongside Windows `.msi` and Linux `.deb`).

To build and run from source on macOS, see [INSTALL_MAC.md](INSTALL_MAC.md).

## Downloads

Self-contained installers (bundled JRE + GStreamer + SDL2, no prerequisites) are
published on each release: **[Releases](../../releases)** — macOS `.dmg`, Windows
`.msi`, Linux `.deb`.

## Running from source

- macOS (Apple Silicon / Intel): see [INSTALL_MAC.md](INSTALL_MAC.md)

## Releasing

Releases are cut with the versioning helpers and published by
`.github/workflows/aircast-release.yml`:

```bash
make release.patch     # aircast-v1.0.0 -> aircast-v1.0.1   (production)
make release.minor     # aircast-v1.0.0 -> aircast-v1.1.0
make release.major     # aircast-v1.0.0 -> aircast-v2.0.0
make release.dev       # aircast-v1.0.1-dev.1               (prerelease)
make release.staging   # aircast-v1.0.0-staging.1           (prerelease)
```

Each pushes an `aircast-v*` tag that triggers the workflow to build the
installers on macOS/Windows/Linux runners and attach them to a GitHub release.
The packaging scripts under `scripts/` (`package-mac.sh`, `package-linux.sh`,
`package-windows.ps1`) can also be run locally.
