# CdrGPPatrol

Administrative claim browser and patrol addon for **GriefPrevention**.

## Target

- Java 21
- Paper 1.21.10+ / 1.21.11
- GriefPrevention 16.18.7

## v1.3.0 Features

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
- **Claim Border Visualizer** from the Claim Inspector.
- Border particles are visible only to the admin who activated them.
- Border rendering follows terrain height and highlights all four corners with vertical particle pillars.
- Adaptive particle spacing keeps very large claims within a configurable particle budget.
- Visualizations auto-expire, can be toggled off from the Inspector, stop on logout/world change, and are cleaned up when the plugin disables.
- Visualizer refuses activation when the admin is in another world or too far from the claim.
- Safe teleport search rejects common hazards and stays inside claim boundaries.
- `/gppatroldebug` diagnostics with the first 10 claims.
- No Skript or SkBee dependency.

## Claim Inspector Flow

```text
/gppatrol
  -> Left Click claim  = Safe Teleport
  -> Right Click claim = Claim Inspector
       -> Visualize Border
       -> Safe Teleport
       -> View Trust
       -> Back to Patrol
```

The border visualizer is intentionally client-scoped: it sends particles only to the admin who enabled it and does not modify blocks or GriefPrevention claim data.

## Visualizer Defaults

```yaml
visualizer:
  duration-seconds: 15
  refresh-ticks: 10
  spacing: 2
  max-points-per-pass: 320
  max-distance: 96
  corner-pillar-height: 4.0
  particle-size: 1.0
  color:
    red: 40
    green: 210
    blue: 255
```

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

Output: `target/CdrGPPatrol-1.3.0.jar`

## Install

1. Install GriefPrevention 16.18.7.
2. Put `CdrGPPatrol-1.3.0.jar` in `plugins/`.
3. Restart the server.
4. Run `/gppatrol`.

## License

GPL-3.0-or-later.
