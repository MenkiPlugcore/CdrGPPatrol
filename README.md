# CdrGPPatrol

Administrative claim browser and patrol addon for **GriefPrevention**.

## Target

- Java 21
- Paper 1.21.10+ / 1.21.11
- GriefPrevention 16.18.7

## v1.5.0 Features

- `/gppatrol [page]` GUI browser with 45 claims per page.
- Search claim owners and filter by world / claim type.
- Sort by world, owner, area, or claim ID.
- Left-click claim for safe teleport.
- Right-click claim for **Claim Inspector**.
- Native GriefPrevention trust viewer for Manager, Build, Container, and Access permissions.
- Per-admin **Claim Border Visualizer** with terrain-following particles.
- **Owner Status**: online/offline, last seen, total claims, and total owned claim area.
- **Abandoned Claim Scanner** for owner inactivity thresholds.
- Presets: 7d, 30d, 60d, 90d plus custom day/week thresholds.
- Scanner excludes online owners and owners with unknown last-seen timestamps.
- Scanner sorts results by **Oldest Offline First**.
- Scanner summary shows candidate claim count, unique owners, and total candidate area.
- Scanner results still support safe teleport, Claim Inspector, Trust Viewer, and Border Visualizer.
- No automatic claim deletion or ownership changes.
- No Skript or SkBee dependency.

## Abandoned Scanner

```text
/gppatrol abandoned
/gppatrol abandoned 7d
/gppatrol abandoned 30d
/gppatrol abandoned 60d
/gppatrol abandoned 90d
/gppatrol abandoned 45d
/gppatrol abandoned 2w
/gppatrol abandoned off
```

`/gppatrol abandoned` without a threshold defaults to 30 days.

The scanner is intentionally conservative:

- Player must be offline.
- Last-seen timestamp must be known.
- Offline duration must meet or exceed the selected threshold.
- Admin claims are never treated as abandoned candidates.
- Scanner is read-only; it never deletes claims automatically.

The bottom-right scanner control in `/gppatrol` can cycle:

```text
Off -> 7d -> 30d -> 60d -> 90d -> Off
```

Right-click the scanner control to turn it off immediately.

## Commands

```text
/gppatrol [page]
/gppatrol search <player|clear>
/gppatrol world <world|all>
/gppatrol type <all|player|admin>
/gppatrol sort <world|owner|largest|smallest|id>
/gppatrol abandoned [7d|30d|60d|90d|custom|off]
/gppatrol reset
/gppatroldebug
```

## Permission

`cdrgppatrol.admin` — defaults to OP.

## Build

```bash
mvn package
```

Output: `target/CdrGPPatrol-1.5.0.jar`

## Install

1. Install GriefPrevention 16.18.7.
2. Put `CdrGPPatrol-1.5.0.jar` in `plugins/`.
3. Restart the server.
4. Run `/gppatrol`.

## License

GPL-3.0-or-later.
