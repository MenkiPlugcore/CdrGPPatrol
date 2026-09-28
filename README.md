# CdrGPPatrol

Administrative claim browser and patrol addon for **GriefPrevention**.

## Target

- Java 21
- Paper 1.21.10+ / 1.21.11
- GriefPrevention 16.18.7

## v1.1.0 Features

- `/gppatrol [page]` GUI browser with 45 claims per page.
- Search claim owners with `/gppatrol search <player>`.
- Filter claims by world with `/gppatrol world <world|all>`.
- Filter claim type with `/gppatrol type <all|player|admin>`.
- Sort by world, owner A-Z, largest area, smallest area, or claim ID.
- Persistent per-admin runtime filter state while navigating and refreshing the GUI.
- GUI controls for World Filter, Claim Type, Sorting, Reset, Search status, and filter summary.
- Tab completion for owner names, worlds, filter types, and sort modes.
- Owner/Admin Claim, claim ID, world, center, size, and area information.
- Click a claim to teleport to a safe location inside the claim.
- Teleport search rejects common hazards and stays inside claim boundaries.
- `/gppatroldebug` diagnostics with the first 10 claims.
- No Skript or SkBee dependency.

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

Output: `target/CdrGPPatrol-1.1.0.jar`

## Install

1. Install GriefPrevention 16.18.7.
2. Put `CdrGPPatrol-1.1.0.jar` in `plugins/`.
3. Restart the server.
4. Run `/gppatrol`.

## License

GPL-3.0-or-later.
