# CBC Cryo-Cannon

CBC Cryo-Cannon is a NeoForge 1.21.1 addon for Create Big Cannons.

## Required versions

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.233
- Create 6.0.10
- Create Big Cannons 5.11.7
- Ritchie's Projectile Library 2.1.2, build 199
- Fungal Infection: Spore 2.2.0j


Build a cryo cannon from one Cryo-Breech, at least two Cryo Cannon Chambers,
and at least two Cryo Cannon Barrels. Assemble and fire it using standard CBC
cannon mounts and redstone controls. The Cryo-Breech accepts only Cryolite
Shells.

Mounted firing enforces the minimum structure and applies these exact values:

- 2/3/4+ chambers: 1.0/1.2/1.4 velocity multiplier.
- 2/3/4/5+ barrels: 3.0/2.1667/1.3333/0.5 degrees spread.

The defaults for a Cryolite Shell are 12 direct damage near impact, a 4
strength explosion, four-block freeze/cleanse range, and 140 ticks of
Cryo-Freezing. The effect maintains vanilla frozen ticks, vanilla shaking, and
Slowness II. Impact cleansing includes the supplied 19 CDU conversion pairs,
the seven documented biomass-family conversions, and organite destruction.

## Cryo-Feeder and Cryo-Hose

The feeder has nine Cryolite Shell slots and one `spore:ice_canister` slot. A
canister supplies 24,000 fuel and a successful load costs 1,000 fuel. The
default cycle is 120 ticks at 32 RPM or more, with 16 SU stress. Set
`rpm_affects_reload_rate` in the server config to opt into speed scaling.

Insert shells or canisters by using them on the feeder. Use a Create wrench on
the feeder's lower connector and then the Cryo-Breech's upper connector within
16 blocks. The generated hose is shown between the endpoints and saved on both
block entities. A completed cycle transfers exactly one shell into an open,
empty linked breech; fuel and inventory are consumed only after CBC accepts the
shell.

## Server configuration

NeoForge creates `cbc_cryo_cannon-server.toml` per world. It controls impact
damage, explosion radius, cleanse radius, freeze duration, canister and
per-shot fuel, cycle length, minimum RPM, stress impact, RPM scaling, and hose
length.

## Current compatibility notes

- Mounted CBC cannons retain feeder loading and render the hose against the
  rotating and pitching top attachment of the Cryo-Breech.
- Cryo Cannon Barrels render isolated, exposed-end, or connected-middle models
  from CBC's live cannon connections and retain that topology while mounted.
- Failure snow uses the active level containing the cannon mount. This allows
  moving-sublevel implementations to retain the snow when they expose the mount
  through that level, while ordinary mounts place snow in the parent world.
- Vanilla frozen rendering supplies the shiver. A separate entity-wide blue
  tint is not applied because a global shader-color hook would affect unrelated
  renderers.

## Verification

`./gradlew compileJava test build` passes with Java 21. Pure tests cover the
velocity table, spread table, feeder timing/fuel math, and cleansing mappings.
In-game acceptance testing should cover CBC assembly, mount firing, fuse
behavior, feeder transfer, hose drops, Spore conversion IDs, and a dedicated
server.