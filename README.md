# quadrofleet-station
QuadroFleet Station Control

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
