# CdrGPPatrol

Administrative claim browser and patrol addon for **GriefPrevention**.

## Target

- Java 21
- Paper 1.21.10+ / 1.21.11
- GriefPrevention 16.18.7

## Features

- `/gppatrol [page]` GUI browser for GriefPrevention claims.
- 45 claims per page with Previous / Refresh / Next navigation.
- Owner/Admin Claim, claim ID, world, center, size, and area information.
- Click a claim to teleport to a safe location inside the claim.
- Teleport search rejects common hazards and stays inside claim boundaries.
- `/gppatroldebug` diagnostics with the first 10 claims.
- No Skript or SkBee dependency.

## Permission

`cdrgppatrol.admin` — defaults to OP.

## Build

```bash
mvn package
```

Output: `target/CdrGPPatrol-1.0.0.jar`

## Install

1. Install GriefPrevention 16.18.7.
2. Put `CdrGPPatrol-1.0.0.jar` in `plugins/`.
3. Restart the server.
4. Run `/gppatrol`.

## License

GPL-3.0-or-later.
