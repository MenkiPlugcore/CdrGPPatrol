# CdrGPPatrol

Administrative claim browser and patrol addon for **GriefPrevention**.

## Target

- Java 21
- Paper 1.21.10+ / 1.21.11
- GriefPrevention 16.18.7

## v1.2.0 Features

- `/gppatrol [page]` GUI browser with 45 claims per page.
- Search claim owners with `/gppatrol search <player>`.
- Filter claims by world and claim type.
- Sort by world, owner A-Z, largest area, smallest area, or claim ID.
- Persistent per-admin runtime filter state while navigating and refreshing the GUI.
- Left-click a claim for safe teleport.
- Right-click a claim to open the **Claim Inspector**.
- Claim Inspector shows owner, claim ID, type, subclaim state, world, size, area, child claims, modified timestamp, and exact bounds.
- Native GriefPrevention trust summary for Manager, Build, Container, and Access permissions.
- Read-only **Trust Viewer** with pagination for large trust lists.
- UUID trust entries are resolved to offline player names when available; non-player/public entries remain visible.
- Safe teleport search rejects common hazards and stays inside claim boundaries.
- `/gppatroldebug` diagnostics with the first 10 claims.
- No Skript or SkBee dependency.

## Claim Inspector Flow

```text
/gppatrol
  -> Left Click claim  = Safe Teleport
  -> Right Click claim = Claim Inspector
       -> Safe Teleport
       -> View Trust
       -> Back to Patrol
```

Trust Viewer is intentionally read-only in v1.2.0. It does not add, remove, or modify GriefPrevention permissions.

## Commands

```text
/gppatrol [page]
/gppatrol search <player|clear>
/gppatrol world <world|all>
/gppatrol type <all|player|admin>
/gppatrol sort <world|owner|largest|smallest|id>
/gppatrol reset
/gppatroldebug
```

## Permission

`cdrgppatrol.admin` — defaults to OP.

## Build

```bash
mvn package
```

Output: `target/CdrGPPatrol-1.2.0.jar`

## Install

1. Install GriefPrevention 16.18.7.
2. Put `CdrGPPatrol-1.2.0.jar` in `plugins/`.
3. Restart the server.
4. Run `/gppatrol`.

## License

GPL-3.0-or-later.
