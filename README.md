# Shiro's Test Mod

A Fabric mod for Minecraft 26.2 that adds two village-attacking Creeper
variants: an Elytra-equipped flying bomb and a carrier that deploys two of
those bombs.

## Status and compatibility

| Property | Value |
|---|---|
| Mod name | Shiro's Test Mod |
| Mod ID | `shiros-test-mod` |
| Java package | `com.shiro193` |
| Mod version | `1.0.2` |
| Minecraft | `26.2` |
| Fabric Loader | `0.19.3` or newer |
| Fabric API | `0.155.2+26.2` |
| Java | `25` or newer |
| Source layout | Split common and client sources |
| License | CC0-1.0 |
| Current validation status | Good to go — build, server GameTests, and visible client GameTest pass |

The project was created from the [official Fabric template
generator](https://fabricmc.net/develop/) with split client/common sources
enabled and every other advanced option disabled.

## Entity roles

The mod builds on two vanilla entity types:

- **Creeper** (`minecraft:creeper`) is the behavioral, attribute, fuse,
  explosion, model, and texture base for both new hostile mobs.
- **Villager** (`minecraft:villager`) is the preferred live target. If no
  Villager is found, a village point of interest is used as the fallback
  destination.

## Fly Creeper

IDs:

- Entity: `shiros-test-mod:fly_creeper`
- Spawn egg: `shiros-test-mod:fly_creeper_spawn_egg`

Features:

- Extends the vanilla Creeper, retaining Creeper attributes, charged state,
  fuse behavior, swelling animation, and explosion mechanics.
- Is equipped in its chest slot with a real vanilla Elytra item.
- Every takeoff emits a firework boost effect attached to the moving Creeper:
  32 ticks of vanilla `FIREWORK` spark particles and one vanilla
  firework-rocket launch sound.
- Two tight movement-relative emitters request 16 sparks per tick, producing
  a dense continuous trail of at least 512 sparks for a complete CMD launch.
- The boost occurs for both autonomous flight and CMD-launched payloads, and
  is triggered once for each actual takeoff.
- Searches within 128 blocks for the nearest living Villager.
- Falls back to the nearest point of interest tagged as part of a village
  when no Villager is available.
- Enters a cruise phase aimed 12 blocks above its destination.
- Enters a dive within 15 horizontal blocks of the destination.
- Arms the inherited vanilla Creeper fuse when the dive starts.
- A naturally attacking Fly Creeper steers directly through the air, ignores
  gravity while flying, and avoids fall damage during its active flight.
- A CMD-launched Fly Creeper instead follows a gravity-driven ballistic arc:
  it starts with a strong upward velocity, climbs to a high apex, then falls
  naturally toward its assigned target.
- Its predicted apex is approximately 30% higher than the previous/reference
  ballistic trajectory; automated bounds require a `1.28`-`1.32` height
  multiplier.
- During the ballistic descent, only horizontal guidance is corrected; the
  vertical movement remains Minecraft's normal gravity and drag.
- Receives a CMD-predicted impact time at launch. Its inherited 30-tick
  vanilla Creeper fuse is started on a calculated tick so detonation aligns
  with target arrival instead of always starting at the apex.
- Retains vanilla-style cat and ocelot avoidance plus basic water, stroll,
  and look goals when it is not attacking.
- Spawns naturally anywhere a vanilla Creeper is in the biome spawn table,
  using that biome's exact Creeper weight and group size.

## CMD Creeper

IDs:

- Entity: `shiros-test-mod:cmd_creeper`
- Spawn egg: `shiros-test-mod:cmd_creeper_spawn_egg`

Features:

- Extends the vanilla Creeper and renders as a normal Creeper.
- Can carry only Fly Creepers and enforces a hard maximum of two passengers.
- Every newly finalized CMD spawn, including natural and spawn-egg spawns,
  creates exactly two Fly Creeper payloads. Loading an existing entity,
  dimension travel, and conversion do not duplicate payloads.
- Treats both payloads as cargo; neither passenger takes control of the
  carrier's movement or look controls.
- Searches within 128 blocks for the nearest living Villager, with the same
  village point-of-interest fallback as the Fly Creeper.
- Walks toward a target while it is farther than 40 horizontal blocks away.
- Throws one payload at a time once within range.
- Takes the previous `1.25`-`1.55` upward-velocity range and applies a
  calibrated `1.161` multiplier, producing approximately `1.45`-`1.80`
  upward velocity and a roughly 30% higher apex.
- Calculates horizontal velocity from a discrete estimate of Minecraft's
  gravity/drag flight time to the target.
- Uses that same flight simulation to predict the payload's hit tick.
- Preserves the inherited 30-tick Creeper fuse and schedules ignition at
  `max(1, predicted hit tick - 30 + 1)`; the final `+1` compensates for
  Minecraft advancing the fuse before that tick's AI/movement.
- Assigns the target and fuse schedule before release; the payload keeps
  gravity enabled, crosses its apex, descends, corrects horizontally, and
  detonates around the predicted target-arrival time.
- Continues until both carried Fly Creepers have been thrown.
- Spawns naturally anywhere a vanilla Creeper is in the biome spawn table,
  using that biome's exact Creeper weight and group size.

### CMD-launched ballistic sequence

1. The CMD Creeper detaches one carried Fly Creeper and assigns the Villager
   or village destination.
2. The payload receives high upward speed plus distance-adjusted horizontal
   speed, while the same discrete gravity/drag simulation predicts its hit
   time.
3. Minecraft gravity and drag produce the climb and curved transition over
   the apex.
4. The payload starts its inherited 30-tick fuse on the prediction-derived
   ignition tick, preserves its natural vertical fall, and applies bounded
   horizontal guidance during descent.
5. It reaches and detonates in the destination area. The automated test
   requires a predicted apex multiplier between `1.28` and `1.32`, an actual
   apex within `0.75` blocks of the raised prediction, a closest target
   distance of no more than two blocks, target contact within three ticks of
   prediction, and detonation within two ticks of prediction.

## Natural spawning

Both custom types deliberately mirror vanilla Creeper spawning rather than
using a single hard-coded approximation:

- Category: `MONSTER`
- Peaceful difficulty: disallowed
- Placement: `ON_GROUND`
- Heightmap: `MOTION_BLOCKING_NO_LEAVES`
- Spawn predicate: vanilla `Monster.checkMonsterSpawnRules`, including the
  normal hostile-mob darkness and difficulty checks
- Biomes: every biome whose monster list contains a vanilla Creeper entry
- Weight and group size: copied from that biome's Creeper entry

The common vanilla entry in Minecraft 26.2 is weight `100`, group `4-4`.
Minecraft 26.2 also contains a Creeper biome entry with different values; the
mod mirrors that exception too. The GameTest checks every Creeper-bearing
biome rather than checking only the common values.

A naturally created CMD Creeper receives two payloads per carrier. Because
the CMD entry itself has the same group range as the corresponding Creeper
entry, a complete natural CMD group can contain multiple carriers, each with
its own two payloads.

## Spawn eggs and creative inventory

- Both eggs appear in the Spawn Eggs creative tab immediately after the
  vanilla Creeper Spawn Egg.
- Each egg is registered against its correct custom entity type and has its
  own English display name.
- Both item definitions intentionally reuse
  `minecraft:item/creeper_spawn_egg`; no custom item texture is included.
- A CMD Creeper created with its spawn egg receives two payloads.

Useful manual-test commands:

```mcfunction
/give @s shiros-test-mod:fly_creeper_spawn_egg
/give @s shiros-test-mod:cmd_creeper_spawn_egg
/give @s minecraft:villager_spawn_egg
```

Use the spawn eggs when testing creation behavior. A raw `/summon` command is
not used by the acceptance suite because command-created mobs do not follow
the same spawn-finalization path as natural or spawn-egg creation.

## Visuals and assets

No new texture files were created.

- Both mobs use the exact vanilla Creeper model and
  `minecraft:textures/entity/creeper/creeper.png`.
- The renderer reproduces vanilla Creeper swelling, white fuse flashes, and
  the charged-Creeper power layer.
- Fly Creepers use Minecraft's vanilla Elytra equipment asset, model, and
  texture in addition to carrying an actual Elytra item.
- Takeoff effects use Minecraft's vanilla firework spark particle and
  firework-rocket launch sound; no effect texture or sound was added.
- The CMD Creeper's two passenger attachment points place its payloads above
  the carrier.
- Both spawn eggs use the vanilla Creeper egg model.

## Source layout

Common gameplay and registrations live under:

```text
src/main/java/com/shiro193/
├── ShiroSTestMod.java
├── entity/
│   ├── CmdCreeper.java
│   ├── FlyCreeper.java
│   ├── ModEntities.java
│   └── VillageTargeting.java
├── item/ModItems.java
└── test/ShiroEntityGameTests.java
```

Client-only rendering and visible testing live under:

```text
src/client/java/com/shiro193/client/
├── ShiroSTestModClient.java
├── render/
│   ├── CreeperVariantRenderer.java
│   └── ElytraCreeperLayer.java
└── test/ShiroClientGameTest.java
```

The implementation does not use mixins.

## Build and test environment

This workspace contains an ignored, repository-local Eclipse Temurin JDK 25
and Gradle cache under `.test-env/`. `test-env.bat` selects them without
changing the machine-wide Java configuration.

```powershell
# Compile, validate, and create remapped binary/source JARs
.\test-env.bat build --console=plain

# Run the real Minecraft dedicated-server GameTest suite
.\test-env.bat runGameTest --console=plain --no-daemon

# Open a visible Minecraft client, stage the render scene, assert it, and capture it
.\test-env.bat runClientGameTest --console=plain --no-daemon

# Open a normal development client for manual play
.\test-env.bat runClient

# Regenerate mapped Minecraft sources
.\test-env.bat genSources

# Regenerate Visual Studio Code launch targets
.\test-env.bat vscode
```

After a build, the distributable mod is:

```text
build/libs/shiros-test-mod-1.0.2.jar
```

The matching source archive is:

```text
build/libs/shiros-test-mod-1.0.2-sources.jar
```

## Real-environment acceptance design

The test plan uses Minecraft itself rather than mocks.

| Target | Environment | Acceptance check |
|---|---|---|
| Registration and eggs | Dedicated GameTest server | Both types are monsters; both eggs resolve to the right type; placement and heightmap equal vanilla Creeper. |
| Natural spawn parity | Dedicated GameTest server | For every biome with a vanilla Creeper, each custom entry has the same weight, minimum group, and maximum group. |
| CMD capacity and loadout | Dedicated GameTest server | A created CMD has exactly two Elytra-equipped Fly passengers and rejects a third. |
| Long firework takeoff trail | Dedicated and visible client GameTests | An autonomous Fly Creeper and both CMD payloads each trigger exactly one takeoff effect. Every CMD payload completes 32 emission ticks and requests at least 512 vanilla firework sparks; the real-client arc frame shows a dense continuous trail spanning most of the view. |
| Higher predicted-fuse ballistic attack | Dedicated GameTest server | A nearby Villager is acquired and both payloads are thrown. Each predicts a `1.28`-`1.32`× apex, physically reaches within `0.75` blocks of it, receives the exact derived ignition schedule, reaches within 2 blocks and within 3 ticks of its hit prediction, then detonates within 2 ticks. |
| Fly attack | Dedicated GameTest server | A Fly Creeper acquires a Villager, becomes airborne, enters a dive, arms its fuse, and is removed by its completed explosion. |
| Village fallback | Dedicated GameTest server | With the maximum legal 128-block GameTest padding and no Villager inside the explicit 128-block query, both types acquire a real Bell village POI; the Fly destination equals the Bell position. |
| Client synchronization | Visible integrated-server client | Server and client each see 3 Fly Creepers, 1 CMD Creeper, and at least 1 Villager in the staged scene. |
| Rendering and live arc | Visible integrated-server client | Vanilla Creeper skins, Elytra wings, carrier payloads, names, and the Villager render without model/resource exceptions. Server-side assertions verify the predicted schedule, airborne descent, and prediction-tolerant target contact; screenshots capture the static scene, takeoff, arc, and timed explosion centered on the Villager. |

Latest verified results on 2026-07-23:

- `build`: **PASS**
- Server GameTests: **PASS — 5/5 required tests**
- Visible client GameTest: **PASS**
- Visible client counts: **3 Fly Creepers, 1 CMD Creeper, 1 Villager**
- Static scene:
  `build/client-gametest/screenshots/0000_shiros-test-mod-entity-scene.png`
- Initial firework takeoff:
  `build/client-gametest/screenshots/0001_shiros-test-mod-firework-takeoff.png`
- Higher ballistic arc and full long trace:
  `build/client-gametest/screenshots/0002_shiros-test-mod-ballistic-arc.png`
- Prediction-timed detonation:
  `build/client-gametest/screenshots/0003_shiros-test-mod-ballistic-impact.png`

The development client may log expected offline-development warnings about
Mojang/Realms authentication, a persisted anisotropic-filtering option, and
an empty client-resources output directory. They did not affect mod loading,
entity synchronization, rendering, assertions, or the successful process
exit.

### Optional manual destructive-world check

Use a disposable Hard-difficulty world because the mobs explode:

1. Create a flat platform and place a Villager 20-30 blocks away.
2. Place a Fly Creeper with its egg and verify that takeoff produces firework
   sparks and a rocket-launch sound before it gains altitude, dives, flashes,
   and explodes near the Villager.
3. Place a CMD Creeper with its egg and visually confirm two Fly passengers.
4. Verify that each thrown payload produces its own firework boost, launches
   upward quickly, leaves a long continuous spark trace, follows the visibly
   higher curved path, begins flashing on its prediction-derived schedule,
   and explodes as it reaches the target area.
5. Set night and use an ordinary Creeper-valid dark area to observe natural
   spawning over time. Treat the spawn-table GameTest as the deterministic
   frequency proof; random observation alone is not statistically reliable.

## Implementation timeline

All dates are local device dates.

| Date | Milestone |
|---|---|
| 2026-07-23 | Inspected the official Fabric development/template instructions. |
| 2026-07-23 | Generated **Shiro's Test Mod** for Minecraft 26.2 with package `com.shiro193`, split client/common sources enabled, and all other advanced options disabled; imported it into this repository. |
| 2026-07-23 | Prepared the repository-local Temurin JDK 25/Gradle environment, generated IDE launch targets and mapped sources, completed a full build, and smoke-tested a visible vanilla template client. |
| 2026-07-23 | Identified the current mapped Creeper, Villager, POI, passenger, biome-spawn, renderer, Elytra, and Fabric GameTest APIs. |
| 2026-07-23 | Replaced the example code/mixins with entity, targeting, spawn, spawn-egg, renderer, localization, and automated-test implementations. |
| 2026-07-23 | Added exact per-biome spawn parity after testing found that Minecraft 26.2 has more than one Creeper spawn-table tuple. |
| 2026-07-23 | Corrected CMD passenger control after real server ticks showed that the first passenger otherwise becomes a mob controller; both Fly Creepers now remain cargo. |
| 2026-07-23 | Passed the dedicated server suite for registration, spawn parity, CMD behavior, and Fly bomb behavior. |
| 2026-07-23 | Exercised the visible client runner, fixed early Elytra item creation during resource reload, removed an all-chunks render wait, and corrected showcase persistence/camera staging. |
| 2026-07-23 | Passed the final visible client test and visually inspected the screenshot. |
| 2026-07-23 | Added an isolated real-Bell village-POI fallback case, changed the CMD loadout case to a natural spawn reason, and passed all 5 server GameTests. |
| 2026-07-23 | Reworked CMD-thrown payloads into high-speed gravity arcs: distance-aware launch velocity, apex-triggered fuse, natural vertical descent, horizontal terminal guidance, and trajectory telemetry. |
| 2026-07-23 | Extended the real server test to require launch speed, a six-block climb, apex crossing, gravity descent, fuse arming, and arrival within two blocks for both payloads; all 5 tests passed. |
| 2026-07-23 | Extended the visible client test to run a real throw and capture descent/impact frames. Corrected the village test's invalid `140` padding to Minecraft's legal maximum `128`, reran both suites, and passed. |
| 2026-07-23 | Produced the final remapped binary and source JARs. |
| 2026-07-23 | Inspected the vanilla rocket implementation and added a 12-tick, entity-following vanilla firework spark trail plus launch sound to autonomous and CMD-deployed Fly Creeper takeoffs. |
| 2026-07-23 | Added exact-once trigger and emitted-burst assertions to the real server suite, added a visible takeoff checkpoint/screenshot, and passed all server and visible-client checks. |
| 2026-07-23 | Inspected the private vanilla Creeper fuse implementation, reused its fixed 30-tick fuse, and replaced apex ignition for CMD payloads with CMD-predicted ignition scheduling based on the ballistic gravity/drag simulation. |
| 2026-07-23 | Added predicted-hit, scheduled/actual ignition, target-contact, and detonation telemetry. The server suite now requires both payloads to contact within three ticks and detonate within two ticks of their predictions; all 5 tests passed. |
| 2026-07-23 | Reworked the visible impact checkpoint to poll prediction-aware target contact, pre-aim the camera, and wait through the verified detonation window. The accepted frame shows the explosion centered on the Villager. |
| 2026-07-23 | Bumped the release patch version from `1.0.0` to `1.0.1` and regenerated the distributable binary/source JARs. |
| 2026-07-23 | Calibrated a `1.161` vertical-speed multiplier against Minecraft gravity/drag, raising predicted CMD payload apex height by approximately 30% across the launch range. |
| 2026-07-23 | Extended the firework effect from 12 to 32 ticks and replaced the single six-spark emitter with two tight emitters totaling 16 sparks per tick. |
| 2026-07-23 | Added raised-apex and full 32-tick/512-spark acceptance checks, passed all 5 server tests and the visible client test, visually accepted the long-trace/higher-arc frames, and released version `1.0.2`. |

For the device-level audit trail, including intentions and failed test
iterations, see [DEVICE_ACTIONS.md](DEVICE_ACTIONS.md).
