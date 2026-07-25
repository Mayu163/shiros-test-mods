# Device Action and Intent Journal

This repository-local journal records actions taken on this device for
**Shiro's Test Mod**, why each action was taken, and its result. It exists so
future work can distinguish intentional project changes, generated/ignored
test artifacts, and expected development-environment warnings.

## Objective

Create and validate a Minecraft 26.2 Fabric mod with:

- A naturally spawning **Fly Creeper** equipped with Elytra that seeks a
  village or Villager, emits a long visible firework boost when it takes off,
  flies, dives, and explodes as a bomb.
- A naturally spawning **CMD Creeper** that carries no more than two Fly
  Creepers, spawns with two, and throws them toward a village or Villager in
  immutable, terrain-aware gravity-driven curves that always rise at least 35
  blocks above release and automatically rise higher when required.
- A **Summon Creeper** with a golden helmet and spawn egg that produces a
  visual lightning strike when created, summons one Fly Creeper every 10
  seconds, and summons one fully loaded CMD Creeper every 30 seconds.
- A spawn egg for each custom type.
- The same natural-spawn requirements, biome coverage, weight, and group size
  as the vanilla Creeper for Fly and CMD Creepers.
- Vanilla chainmail/golden helmet visuals, a flight-long seven-color Fly
  firework trace with one launch sound, and explosion verification proving
  that obsidian and bedrock survive vanilla and custom Creepers.
- Vanilla textures/assets wherever possible.
- A real Minecraft server behavior suite and a visible client render test.
- Complete feature and implementation-timeline documentation in `README.md`.

## Scope and safety

- Project writes were limited to this repository and its ignored local
  environment/build directories.
- No system-wide Java installation or persistent machine environment setting
  was made.
- The repository-local helper changes `JAVA_HOME`, `PATH`, and
  `GRADLE_USER_HOME` only for the lifetime of its batch process.
- Dependencies, assets, and mapped sources were obtained through the official
  Fabric/Gradle/Minecraft development toolchain or the Eclipse Temurin
  distribution and retained under ignored directories.
- A visible Minecraft window was opened only after the user explicitly
  allowed visible testing.
- No external messages, uploads, deployments, or account changes were made.
- No destructive repository cleanup or reset command was used.

## Chronological action log

| Local date/time | Device action | Intention | Result |
|---|---|---|---|
| 2026-07-23 | Inspected `https://fabricmc.net/develop/` and the official template generator instructions. | Use the current official toolchain and version coordinates. | Selected Minecraft 26.2-compatible generator values. |
| 2026-07-23 | Generated **Shiro's Test Mod** with package `com.shiro193`; enabled split client/common sources; left every other advanced option disabled. | Match the requested project identity and source layout exactly. | Template downloaded/extracted and imported into the current repository as mod ID `shiros-test-mod`. |
| 2026-07-23 | Inspected repository contents and instructions before editing. | Preserve unrelated work and identify constraints. | No `AGENTS.md` was present; the starting project contained the generated example initializer/client initializer and example mixins. |
| 2026-07-23 | Prepared `.test-env/` with Eclipse Temurin JDK 25.0.3+9 and a repository-local Gradle cache; added `test-env.bat`. | Build Minecraft 26.2 without requiring a system-wide Java change. | Local helper environment became operational and remained ignored by Git. |
| 2026-07-23 | Ran Gradle version/build checks, generated VS Code run targets, and opened a visible template development client. | Prove the generated template and local environment worked before feature implementation. | Initial full build and visible client smoke test succeeded with `shiros-test-mod 1.0.0` loaded. |
| 2026-07-23 | Generated mapped Minecraft 26.2 common/client sources into `.test-env/` caches and performed read-only source/API inspection. | Use exact 26.2 signatures for entities, goals, POIs, passengers, spawn tables, equipment rendering, and GameTest. | Confirmed the vanilla Creeper/Villager types, Creeper spawn placement/rules, village POI tag, passenger-controller behavior, Creeper renderer layers, and Elytra equipment rendering path. |
| 2026-07-23 | Created this journal. | Maintain the requested durable record of actions and intentions. | `DEVICE_ACTIONS.md` added. |
| 2026-07-23 | Removed the generated example initializer behavior, example mixins, and mixin configuration; added the real common initializer. | Eliminate unused template behavior and avoid unnecessary runtime bytecode modification. | Mod now initializes entity, biome-spawn, item, and client renderer registrations without mixins. |
| 2026-07-23 | Added `ModEntities`, `FlyCreeper`, `CmdCreeper`, and `VillageTargeting`. | Implement the two requested mobs, village/Villager targeting, flight, dive, carrier capacity, and projectile deployment. | Both custom entity types compile against Minecraft 26.2 and subclass vanilla Creeper. |
| 2026-07-23 | Added actual Elytra chest equipment to Fly Creepers and two CMD passenger attachment points. | Make Fly Creepers genuine Elytra-equipped mobs and visibly represent both carried payloads. | Each Fly Creeper has `minecraft:elytra`; CMD accepts only Fly passengers and rejects a third. |
| 2026-07-23 | Added dynamic biome modifications that inspect each vanilla Creeper entry and copy its weight/minimum/maximum for both custom types. | Satisfy exact spawn parity even when a biome differs from the common `100 / 4-4` tuple. | Both types are inserted into all current Creeper-bearing biomes with per-biome parity. |
| 2026-07-23 | Registered the two spawn eggs, English names, and Spawn Eggs tab placement. | Provide the requested player-accessible creation items. | Both eggs resolve to the correct type and appear after the vanilla Creeper egg. |
| 2026-07-23 | Added item definitions pointing to `minecraft:item/creeper_spawn_egg`. | Reuse original game art. | No new item texture was created. |
| 2026-07-23 | Added `CreeperVariantRenderer` and `ElytraCreeperLayer` in the split client source set. | Reuse vanilla Creeper/Elytra visuals while supporting both custom types. | Renderer uses the vanilla Creeper model/texture, power layer, swelling/flash behavior, and vanilla Elytra equipment asset. |
| 2026-07-23 | Added dedicated `gameTest` and visible `clientGameTest` Loom run configurations plus server/client test entrypoints. | Turn every acceptance target into repeatable Minecraft-engine checks. | `runGameTest` writes `build/gametest-results.xml`; `runClientGameTest` opens a 1280×720 client and writes a screenshot. |
| 2026-07-23 | Ran iterative compilation and full builds while adapting to final Minecraft 26.2 mapped APIs. | Catch registration, renderer, component, and GameTest signature errors before runtime testing. | Compilation issues were resolved; subsequent full build passed. |
| 2026-07-23 | Ran the initial server behavior tests and inspected failures. | Validate behavior under real entity ticking rather than constructor-only checks. | Testing exposed two important assumptions: the first passenger can become a controlling passenger, and not every 26.2 Creeper biome uses the common spawn tuple. |
| 2026-07-23 | Overrode CMD controlling-passenger selection to return no controller and changed biome registration/tests to exact per-biome copying. | Keep both Fly passengers as cargo and meet true vanilla frequency parity. | CMD movement/look goals tick with two passengers; all Creeper biome entries match their local vanilla values. |
| 2026-07-23 | Reran the server suite after behavior corrections. | Verify registration, eggs, spawning, CMD throws, and Fly explosion together. | All 4 required tests passed. |
| 2026-07-23 08:38-08:41 | Opened the first automated visible client scene. | Validate resource reload, renderer creation, integrated-server synchronization, and screenshot capture. | Failed usefully: an Elytra `ItemStack` was created before registry components were bound, and an all-chunks render wait timed out. No screenshot was accepted. |
| 2026-07-23 | Made the render-layer Elytra stack lazy and replaced the strict all-chunks wait with a bounded tick wait. | Create component-bearing items only after registries are ready and keep the visual test deterministic. | Renderer resource reload completed without the earlier exception. |
| 2026-07-23 08:44-08:48 | Reran the visible test while diagnosing a `0 Fly / 0 CMD / 1 Villager` client count. | Separate renderer problems from monster lifetime and client tracking. | Hard difficulty alone did not fix it; logs showed no renderer or unknown-entity exception. |
| 2026-07-23 | Added persistent showcase entities, explicit server/client count assertions, and moved the display from a forced Y=80 to the generated superflat surface. | Prevent the frozen test actors from being culled during camera staging and make synchronization evidence explicit. | Both integrated server and client reported `3 Fly / 1 CMD / 1 Villager`; the visible test passed and captured a screenshot. |
| 2026-07-23 09:03 | Inspected the first passing screenshot at original resolution. | Perform visual QA beyond process exit and numeric counts. | Rejected the framing: it showed the underside of the platform even though assertions passed. |
| 2026-07-23 | Replaced transition-based camera movement with direct same-level player repositioning and lowered the aim point. | Put all tested entities visibly above the platform in the final evidence image. | Camera framing corrected. |
| 2026-07-23 09:05-09:06 | Opened the final visible client GameTest and inspected its screenshot. | Confirm actual in-game rendering, passenger presence, client synchronization, and asset reuse. | **PASS**. The image clearly shows the standalone winged Fly Creeper, Villager, and CMD carrier with two winged payloads. Screenshot: `build/client-gametest/screenshots/0000_shiros-test-mod-entity-scene.png`. |
| 2026-07-23 09:07-09:08 | Reran the then-current dedicated-server GameTest suite after gameplay cleanup. | Ensure visual-test edits and final CMD distance calculation did not regress behavior. | **PASS — all 4 then-registered required tests completed in 4.177 seconds.** |
| 2026-07-23 | Changed the CMD capacity/throw test to `EntitySpawnReason.NATURAL` and added a 140-block-padded Bell POI test with no Villagers inside either mob's 128-block search radius. | Directly prove natural CMD payload creation and the village fallback for both types without cross-test Villager interference. | The Fly destination exactly matched the Bell POI and the CMD acquired the same village target. |
| 2026-07-23 09:14-09:15 | Ran the expanded final dedicated-server GameTest suite. | Verify every gameplay target, including the new isolated village-POI case. | **PASS — all 5 required tests completed in 11.98 seconds.** |
| 2026-07-23 | Rewrote `README.md` with the complete feature inventory, IDs, spawn mechanics, asset policy, test design/results, commands, and project timeline. | Fulfill the requested permanent feature and timeline documentation. | README now reflects only final verified behavior and explicitly records the acceptance evidence. |
| 2026-07-23 | Inspected Minecraft 26.2's mapped airborne movement implementation and existing Fly/CMD code. | Base the new throw on the game's real movement rules rather than a visual approximation. | Confirmed the relevant per-tick vertical gravity (`0.08` for this mob) and drag (`0.98`) behavior and retained normal gravity for launched payloads. |
| 2026-07-23 | Reworked CMD launch calculation and Fly payload state into a ballistic ascent/descent. | Make a thrown Fly Creeper gain high initial speed, reach a relatively high altitude, curve over, and fall naturally into its target. | CMD now estimates gravity/drag flight time, applies `1.25`-`1.55` initial upward velocity plus target-relative horizontal speed, and the Fly arms at its apex while preserving gravitational vertical motion. |
| 2026-07-23 | Added launch telemetry and strengthened the CMD server GameTest. | Turn “curves high and hits the target” into deterministic real-engine acceptance criteria for both carried payloads. | The test requires initial speed, a climb of at least 6 blocks, apex crossing, negative vertical motion under enabled gravity, an armed fuse, and arrival within 2 blocks of the assigned target. |
| 2026-07-23 09:44 | Built the changed mod and ran its dedicated-server suite. | Compile the ballistic implementation and validate it under Minecraft entity ticks. | **PASS — build succeeded and all 5 server GameTests passed; the server suite completed in 10.93 seconds.** |
| 2026-07-23 | Extended the visible integrated-client test from a static scene to a real CMD throw. | Observe the trajectory in the actual rendered game and capture evidence at meaningful phases. | The test now keeps the Villager safe, enables the staged CMD/payload AI, asserts the airborne post-apex descent, then asserts target arrival and captures static, arc, and impact screenshots. |
| 2026-07-23 | Opened the first revised visible client run. | Validate the live ballistic scene. | The run stopped during registry synchronization because the existing village GameTest used padding `140`, while Minecraft 26.2 permits only `0` through `128`; gameplay did not start and no ballistic result was accepted. |
| 2026-07-23 | Changed village-test padding from `140` to the legal maximum `128` and retained an explicit no-Villager-within-128-block assertion. | Fix client registry compatibility without weakening the village fallback test's isolation condition. | The isolated Bell POI case remains valid and the dedicated-server suite passed all 5 tests again in 12.27 seconds. |
| 2026-07-23 09:51 | Opened the corrected visible ClientGameTest and inspected its three images at original resolution. | Complete real-environment behavioral and visual validation. | **PASS.** The real client captured the staged loadout, a winged payload high overhead after its apex and descending under gravity, and the launched payload at the Villager target. |
| 2026-07-23 | Updated `README.md` and this journal with the ballistic feature, acceptance thresholds, screenshots, compatibility correction, and test chronology. | Keep the requested feature/timeline documentation and device action record synchronized with the implemented behavior. | Both records now describe the final behavior and distinguish the failed pre-game registry check from the accepted real-environment run. |
| 2026-07-23 | Ran the final Gradle build after all source and documentation changes. | Package the exact finalized repository state for handoff. | **PASS — `BUILD SUCCESSFUL` in 3 seconds; remapped binary and source JARs were regenerated.** |
| 2026-07-23 | Inspected Minecraft 26.2's mapped `FireworkRocketEntity`, particle registry, and firework sound definitions. | Reuse the real game's firework presentation and avoid new assets. | Confirmed vanilla rockets use `ParticleTypes.FIREWORK` sparks and `SoundEvents.FIREWORK_ROCKET_LAUNCH`. |
| 2026-07-23 | Added a takeoff effect to both Fly Creeper launch paths. | Show a visible firework boost whenever an autonomous Fly Creeper or a CMD payload takes off. | Each takeoff now triggers one rocket-launch sound and emits six vanilla firework sparks per tick from a movement-relative point on the Creeper for 12 ticks. |
| 2026-07-23 | Added trigger/burst telemetry and assertions to the CMD and autonomous Fly server cases, then built the mod. | Prove both launch paths invoke the real particle emitter exactly once without regressing flight. | **PASS — compilation and packaging completed successfully in 8 seconds.** |
| 2026-07-23 10:09 | Ran the dedicated-server GameTest command. | Validate the firework effect and all existing entity behavior inside Minecraft. | The outer command reached its 60-second collection limit after Minecraft had completed; log/XML inspection confirmed a clean server shutdown and **all 5 required tests passed in 14.17 seconds with zero failure/error nodes**. |
| 2026-07-23 10:27-10:28 | Opened the visible ClientGameTest with a new firework checkpoint. | Verify the sparks render on the moving payload in a real OpenGL client while retaining the ballistic arc/impact checks. | **PASS — `BUILD SUCCESSFUL` in 1 minute 21 seconds.** The test asserted an active exact-once effect and captured four screenshots. |
| 2026-07-23 | Inspected `0001_shiros-test-mod-firework-takeoff.png` at original resolution. | Perform visual QA rather than accepting only telemetry and process exit. | **PASS.** Vanilla white firework sparks are clearly visible on the rising winged Fly Creeper. |
| 2026-07-23 | Inspected the refreshed ballistic-arc and impact screenshots at original resolution. | Confirm the added takeoff effect did not obscure the trajectory or target-arrival evidence. | **PASS.** The arc frame shows a long spark trail following the rising/curving payloads; the impact frame shows an armed, flashing Fly Creeper at the Villager. |
| 2026-07-23 | Updated `README.md` and this journal with the firework feature, exact behavior, tests, evidence paths, and chronology. | Keep the requested feature inventory, timeline, and device-action record current. | Documentation now identifies both takeoff paths and the four correctly numbered client screenshots. |
| 2026-07-23 | Ran the final Gradle build after the verified firework implementation. | Confirm the handoff source still packages into the distributable mod. | **PASS — `BUILD SUCCESSFUL` in 3 seconds; the binary and source JARs are current.** |
| 2026-07-23 | Inspected Minecraft 26.2's mapped `Creeper` fuse internals and all ballistic launch call sites. | Determine how to adjust Fly Creeper fuse timing while preserving vanilla explosion behavior. | Confirmed the Creeper's fuse duration is a private 30-tick `maxSwell` with no public setter; selected prediction-based ignition scheduling rather than reflection, mixins, or replacement explosion code. |
| 2026-07-23 | Connected the CMD ballistic simulation's flight-tick estimate to the launched payload. | Make the CMD predict hit time and adjust when the Fly Creeper's inherited fuse begins. | CMD records/passes a positive predicted hit tick; Fly schedules ignition at `max(1, predicted hit tick - 30 + 1)` to align vanilla detonation with arrival under Minecraft's tick order. |
| 2026-07-23 | Added predicted-impact, scheduled ignition, actual ignition, target-contact, and detonation telemetry plus strict server assertions. | Prove both payloads use the CMD prediction and actually explode on time. | Tests require the exact ignition formula/tick, contact within 3 ticks of prediction, detonation within 2 ticks, target distance within 2 blocks, and removal by the inherited explosion. |
| 2026-07-23 | Built the predicted-fuse implementation. | Compile all common/client changes and regenerate artifacts before runtime calibration. | **PASS — `BUILD SUCCESSFUL` in 6 seconds.** |
| 2026-07-23 10:37-10:38 | Ran the real dedicated-server GameTest suite. | Calibrate the one-tick fuse-order correction against actual Minecraft entity ticks. | **PASS on the first calibration — all 5 required tests completed in 14.27 seconds.** Both payloads met the prediction, ignition, contact, and detonation tolerances. |
| 2026-07-23 10:38-10:40 | Opened the first prediction-aware visible ClientGameTest. | Replace the old fixed impact delay with polling for prediction-tolerant target contact. | Runtime assertions and process exit passed, but visual QA rejected the impact frame because the screenshot occurred before the camera processed its new target. |
| 2026-07-23 10:41-10:42 | Added one camera-settle tick and reran the visible client. | Give the renderer time to apply the target lock. | Runtime passed again, but visual QA still rejected the frame because client entity interpolation lagged behind the authoritative server contact position. |
| 2026-07-23 10:43-10:44 | Pre-aimed the camera at the Villager before prediction polling and reran. | Remove camera rotation latency before the payload arrived. | Runtime passed and the Villager was centered, but the contact frame still showed interpolated payloads above it; the image was not accepted as timed-detonation evidence. |
| 2026-07-23 10:45-10:46 | Moved the evidence capture four ticks after prediction-aware contact and reran the visible client. | Capture after the server-tested ±2-tick detonation window while the explosion particles remain visible. | **PASS — `BUILD SUCCESSFUL` in 1 minute 20 seconds.** Original-resolution inspection accepted the frame: a vanilla Creeper explosion is visibly centered on the Villager while the other payload's firework trail remains overhead. |
| 2026-07-23 | Updated `README.md` and this journal with predicted-fuse behavior, formula, telemetry, test tolerances, runtime chronology, and final evidence. | Keep the requested feature/timeline documentation and device action record synchronized with the implementation. | Both records now distinguish the original apex-triggered implementation from the final CMD-predicted ignition schedule. |
| 2026-07-23 | Ran the final Gradle build after all predicted-fuse source and documentation changes. | Package the exact handoff implementation. | **PASS — `BUILD SUCCESSFUL` in 3 seconds; binary and source JARs were regenerated.** |
| 2026-07-23 | Changed the authoritative `mod_version` from `1.0.0` to `1.0.1` and updated current release references in `README.md` and this journal. | Produce the requested patch release without altering the recorded history of the original template smoke test. | Fabric metadata expands to version `1.0.1`; current artifact paths use the `1.0.1` filename. |
| 2026-07-23 | Ran Gradle `build` and inspected the generated archives plus the binary JAR's embedded `fabric.mod.json`. | Compile and verify the requested `1.0.1` patch-release JARs. | **PASS — `BUILD SUCCESSFUL` in 3 seconds.** Generated `shiros-test-mod-1.0.1.jar` (52,698 bytes) and `shiros-test-mod-1.0.1-sources.jar` (21,577 bytes); embedded ID/name/version are `shiros-test-mod` / `Shiro's Test Mod` / `1.0.1`. |
| 2026-07-23 | Simulated the old and proposed vertical launch speeds using Minecraft's `0.08` gravity and `0.98` drag. | Interpret “30% higher” as apex height and avoid the much larger overshoot that a raw 30% speed increase would produce. | Calibrated vertical-speed multiplier `1.161`; predicted apex ratios across 4-40 block launches are approximately `1.297`-`1.301`. |
| 2026-07-23 | Applied the calibrated speed multiplier and added reference/raised-apex telemetry. | Raise CMD payload trajectories by about 30% and make the change deterministic to test. | Upward launch velocity is approximately `1.45`-`1.80`; each payload reports both apex predictions and their ratio. |
| 2026-07-23 | Extended and densified the takeoff particle trail. | Make the vanilla firework effect long and plainly visible. | Duration increased from 12 to 32 ticks; two tight movement-relative emitters now request 10 + 6 vanilla `FIREWORK` sparks per tick, at least 512 per complete CMD trail. |
| 2026-07-23 | Bumped `mod_version` from `1.0.1` to `1.0.2` and expanded server/client acceptance assertions. | Prepare the requested patch release and prevent regressions in height or trace visibility. | Tests now require a `1.28`-`1.32` predicted apex multiplier, physical arrival near that apex, all 32 trace ticks, and at least 512 requested sparks per payload. |
| 2026-07-23 | Compiled the gameplay/test/version changes. | Catch API or source errors before Minecraft runtime validation. | **PASS — `BUILD SUCCESSFUL` in 7 seconds.** |
| 2026-07-23 11:28 | Ran the real dedicated-server GameTest suite with `shiros-test-mod 1.0.2`. | Validate the higher trajectories, complete dense trails, target prediction, and timed explosions under Minecraft ticks. | **PASS — all 5 required tests completed in 11.33 seconds.** |
| 2026-07-23 11:29-11:30 | Opened the visible ClientGameTest for version `1.0.2`. | Judge the higher arc and trace visibility in the real OpenGL client. | **PASS — `BUILD SUCCESSFUL` in 1 minute 14 seconds.** All static/takeoff/arc/impact checkpoints completed. |
| 2026-07-23 | Inspected the refreshed takeoff, arc, and impact frames at original resolution. | Perform visual QA on the requested changes and ensure fuse timing still works. | **PASS.** The arc frame shows a bright uninterrupted spark column across most of the screen to the higher payload; the impact frame still shows the prediction-timed explosion centered on the Villager. |
| 2026-07-23 | Updated `README.md` and this journal with the 30%-higher trajectory, 32-tick/512-spark trace, acceptance results, timeline, and `1.0.2` artifact paths. | Keep the requested feature inventory and device-action record current. | Documentation now distinguishes the earlier effects/releases from the final `1.0.2` behavior. |
| 2026-07-23 | Ran the final Gradle build after all `1.0.2` source and documentation changes. | Package the exact release implementation. | **PASS — `BUILD SUCCESSFUL` in 3 seconds; binary and source JARs are current.** |
| 2026-07-23 | Audited every GitHub publish candidate by path and size, scanned text candidates for high-risk credential signatures, and expanded `.gitignore` for build/runtime output, local test/tool state, credentials, logs, editor files, and operating-system debris. | Publish only reproducible source, documentation, Gradle Wrapper files, resources, tests, and CI configuration; keep generated JARs out of Git history and attach only the two `1.0.2` artifacts to the Release. | **PASS — no high-risk credential signature was found.** `.agents/`, `.gradle/`, `.test-env/`, `.vscode/`, `build/`, and `run/` are demonstrably ignored; the reviewed publish set contains 30 files and no generated JAR. |
| 2026-07-23 | Initialized the previously empty `.git` placeholder as a real repository with `main` as its initial branch. | Prepare the reviewed working tree for the requested GitHub publication. | Local Git metadata is now valid; no prior commits or user files were overwritten. |
| 2026-07-23 | Created the private GitHub repository `Mayu163/shiros-test-mods`, configured it as `origin`, and pushed the audited root commit to `main`. | Publish the complete, reproducible project while keeping access conservative because repository visibility was not specified. | **PASS — GitHub accepted `main`, set it as the default branch, and local `main` tracks `origin/main`.** |
| 2026-07-23 | Created the `v1.0.2` GitHub Release as a draft and uploaded only the verified binary and source JARs. | Keep the generated artifacts out of Git history while distributing both requested `1.0.2` products from the formal Release. | **PASS — both uploads report `uploaded` and GitHub's SHA-256 digests exactly match local verification:** binary `3acdc9c35d15fc95092c82b58e2d9fa14363869f27f1bc8d395c77d17d502d9a` (53,760 bytes), sources `360227161c3030adf35c5d62785f0f70a60d9eb3a106641be85ce93527bed856` (22,276 bytes). The draft is ready to publish after this audit record reaches `main`. |

## Test coverage and final evidence

### Full build

Command:

```powershell
.\test-env.bat build --console=plain
```

Final result: **PASS**

Produced:

- `build/libs/shiros-test-mod-1.0.2.jar`
- `build/libs/shiros-test-mod-1.0.2-sources.jar`

### Dedicated server GameTests

Command:

```powershell
.\test-env.bat runGameTest --console=plain --no-daemon
```

Final result: **PASS — 5/5 required tests**

Verified:

- Monster categories and vanilla Creeper placement/heightmap parity.
- Correct entity behind each spawn egg.
- Exact custom-vs-vanilla weight and group-size equality in every
  Creeper-bearing biome.
- Standard vanilla `100 / 4-4` entry remains represented.
- A CMD spawn carries exactly two Elytra-equipped Fly Creepers.
- A third payload is rejected.
- Both payloads are thrown, detached, marked launched, and start with high
  initial speed.
- Both payloads trigger exactly one takeoff firework effect and emit actual
  vanilla firework particle bursts.
- Both payloads complete all 32 firework-emission ticks and request at least
  512 vanilla firework sparks.
- Both payloads predict a `1.28`-`1.32`× higher apex, physically reach within
  `0.75` blocks of the raised prediction, cross the measured apex, and then
  have negative vertical velocity while normal gravity remains enabled.
- Both payloads receive a positive CMD-predicted hit time.
- Each ignition tick exactly equals
  `max(1, predicted hit tick - 30 + 1)` and the fuse actually starts on that
  scheduled tick.
- Both payloads reach within 2 blocks and within 3 ticks of their prediction,
  then detonate through the inherited Creeper explosion within 2 ticks of
  prediction.
- A Fly Creeper acquires a Villager, becomes airborne, dives, arms its fuse,
  triggers exactly one emitted firework effect, and completes its explosion.
- With no Villager inside an isolated 128-block search region, both custom
  types acquire a real Bell village POI and the Fly destination matches it.

Machine-readable report:

```text
build/gametest-results.xml
```

### Visible integrated-client GameTest

Command:

```powershell
.\test-env.bat runClientGameTest --console=plain --no-daemon
```

Final result: **PASS**

Verified:

- Minecraft 26.2/Fabric Loader/Fabric API and the mod initialize in a real
  OpenGL client.
- The integrated server and client each see exactly 3 Fly Creepers and 1 CMD
  Creeper, plus the staged Villager.
- The custom renderers survive resource reload.
- The standalone Fly Creeper and both CMD payloads render with Elytra wings.
- The Creepers use the original vanilla texture/model style.
- A launched payload reports one active takeoff effect and emitted particle
  bursts; its vanilla firework sparks are visible around it in the captured
  takeoff frame.
- The later arc frame visibly shows the full dense, continuous trail spanning
  most of the screen to the higher payload.
- The visible payload reports a valid CMD-predicted hit tick and its exact
  prediction-derived fuse schedule.
- A real CMD payload demonstrates a `1.28`-`1.32` raised-apex prediction,
  reaches the raised apex, is observed descending under gravity, and
  subsequently reaches within 2 blocks and within 3 ticks of its predicted
  hit time while armed.
- The final evidence frame is captured after the verified detonation window
  and visibly shows the explosion centered on the Villager target.
- All four current screenshot files exist and were visually inspected.

Evidence:

```text
build/client-gametest/screenshots/0000_shiros-test-mod-entity-scene.png
build/client-gametest/screenshots/0001_shiros-test-mod-firework-takeoff.png
build/client-gametest/screenshots/0002_shiros-test-mod-ballistic-arc.png
build/client-gametest/screenshots/0003_shiros-test-mod-ballistic-impact.png
build/client-gametest/logs/latest.log
```

Expected non-blocking development warnings observed:

- Offline development credentials cannot authenticate to Mojang/Realms.
- The generated client options contained anisotropic-filtering value `0`.
- Loom referenced `build/resources/client`, which is absent because the
  client resource source set intentionally has no files.

None caused an assertion failure, renderer failure, mod failure, or nonzero
exit in the final run.

## Repository files intentionally changed or added

- Build/test configuration: `build.gradle`, `gradle.properties`,
  `settings.gradle`, `test-env.bat`, `.gitignore`
- Metadata/resources: `fabric.mod.json`, English localization, three item
  definitions
- Common implementation: initializer, entity registration, Fly/CMD/Summon
  classes, village targeting, spawn eggs
- Client implementation: client initializer, Creeper variant renderer,
  Elytra render layer, and vanilla helmet rendering
- Tests: dedicated server GameTests and visible client GameTest
- Documentation: `README.md`, `DEVICE_ACTIONS.md`

The generated example mixin classes/configuration were removed because this
implementation requires no mixins.

## Final state

As of 2026-07-23, the requested mod behavior is implemented and the repository
is good to go for Minecraft 26.2 development/testing. The final binary should
be rebuilt from source before distribution if any subsequent code or
dependency version changes are made.

## 2026-07-23 13:10–13:26 current-worktree verification addendum

This addendum records the verification performed against the exact working
tree currently on this device. At the start of this pass, the repository
already contained the completed Fly/CMD implementation documented above plus
the requested, uncommitted version `1.1.0` expansion work. Existing dirty files
were preserved; no reset, checkout, cleanup, or unrelated source replacement
was performed.

| Local time | Device action | Intention | Result |
|---|---|---|---|
| 13:10 | Audited the current source tree, Git status, entity registrations, AI, renderers, resources, GameTests, and this journal; compared exact APIs with the locally mapped Minecraft 26.2/Fabric sources. | Verify the requested work without overwriting the existing uncommitted `1.1.0` expansion. | Confirmed Fly/CMD entity types, both requested eggs, exact per-biome vanilla Creeper spawn-entry copying, vanilla spawn placement/rule registration, Elytra equipment/rendering, Villager/village targeting, CMD two-passenger capacity, natural-spawn payload creation, and vanilla-asset use. |
| 13:10 | Ran `.\test-env.bat build --console=plain`. | Compile and package the exact current tree before runtime testing. | **PASS — `BUILD SUCCESSFUL`.** |
| 13:10–13:11 | Ran the dedicated Minecraft GameTest server. | Exercise registration, spawn parity, natural CMD payloads, throws, ballistic target arrival, Fly dive/explosion, and village POI fallback under real game ticks. | **PASS — all 7 registered required tests passed; the behavior phase completed in 11.88 seconds.** |
| 13:13–13:14 | Opened a fresh visible ClientGameTest using version `1.1.0`. | Replace stale `1.0.2` screenshots with evidence from the current sources. | The requested Fly/CMD/Summon scene rendered and screenshot `0000` was captured, but the extended test sampled the rainbow effect after only one color pulse while requiring two. Resource reload also reported that the new Summon egg used a nonexistent 26.2 special model. This run was rejected. |
| 13:16–13:18 | Inspected all 88 vanilla 26.2 spawn-egg client definitions and the exact CMD launch/firework tick schedule; changed the client assertion from a fixed wait to a bounded readiness poll and changed the invalid extra egg resource to the same verified vanilla Creeper egg model used by Fly/CMD. | Remove timing flakiness and eliminate a real resource error without creating a texture or changing which entity an egg spawns. | Project rebuilt successfully; both requested eggs remained unchanged and valid. |
| 13:19–13:20 | Reopened the visible ClientGameTest. | Verify the timing/resource corrections through the rendered client. | The timing correction reached the takeoff screenshot, then exposed a genuine packet collision: custom event ID `63` is intercepted globally by Minecraft and casts its entity to `Sniffer`. The Fly Creeper was disconnected before its own handler could run. This run was rejected. |
| 13:20–13:22 | Inspected mapped `ClientPacketListener`, `ClientboundEntityEventPacket`, `Entity`, and `LivingEntity` sources; changed the seven transient color-pulse IDs to the unused signed-negative range `-128..-122`. | Keep the effect entity-local and avoid Minecraft's global Guardian (`21`), Totem (`35`), and Sniffer (`63`) event handling. | Packet bytes remain signed, every custom event now reaches `FlyCreeper.handleEntityEvent`, and no mixin or custom network channel was required. |
| 13:22 | Ran the full Gradle build after the packet fix. | Compile/package the corrected common and client code. | **PASS — `BUILD SUCCESSFUL`.** |
| 14:02–14:03 | Reran the dedicated Minecraft GameTest server after the common-code event change. | Prove that the exact final source did not regress server behavior. | **PASS — all 7 registered required tests passed; the behavior phase completed in 11.38 seconds.** |
| 14:03–14:04 | Opened the final visible ClientGameTest and let its complete scripted sequence run. | Validate current resource reload, entity synchronization, vanilla-asset rendering, helmets, carried payloads, rainbow takeoff, slower/higher ballistic arc, target arrival, and explosion. | **PASS — `BUILD SUCCESSFUL` in 1 minute 28 seconds.** All four fresh screenshots were created; no unknown-model or entity-event/cast failure remained. |
| 14:04 | Inspected all four fresh screenshots at original 1280×720 resolution. | Perform visual QA instead of relying only on process exit. | **PASS.** The static scene shows the chainmail CMD helmet, golden Summon helmet, Elytra Fly, and two carried payloads; the arc shows red/orange/yellow/green/cyan/blue particles; the final frame shows the target-area explosion and naturally fading trail. |
| 14:05 | Updated `README.md` and this journal for version `1.1.0`. | Keep the durable feature inventory, implementation timeline, failures, fixes, and accepted evidence synchronized with the final source. | Documentation now covers all newly requested behavior and distinguishes historical `1.0.2` results from the then-current `1.1.0` prerelease acceptance. |
| 14:06 | Ran the final Gradle build and audited the diff, artifact metadata, server XML, and latest client log. | Package and inspect the exact documented handoff state. | **PASS — `BUILD SUCCESSFUL`; `git diff --check` is clean.** The embedded mod version is `1.1.0`; the XML contains 7 testcase nodes and no failures/errors; the latest client log contains no unknown-model, class-cast, protocol-disconnect, or failed-client-test marker. Artifacts are `shiros-test-mod-1.1.0.jar` (68,265 bytes) and `shiros-test-mod-1.1.0-sources.jar` (29,515 bytes). |

### Current accepted evidence

- Full build: **PASS**
- Dedicated server: **PASS — 7/7 registered tests**
- Visible integrated client: **PASS**
- Current binary:
  `build/libs/shiros-test-mod-1.0.5-beta.jar`
- Current sources:
  `build/libs/shiros-test-mod-1.0.5-beta-sources.jar`
- Machine-readable server report:
  `build/gametest-results.xml`
- Current visible evidence:
  `build/client-gametest/screenshots/0000_shiros-test-mod-entity-scene.png`
  through
  `build/client-gametest/screenshots/0003_shiros-test-mod-ballistic-impact.png`

Expected non-blocking development-client messages remain:

- offline development credentials cannot authenticate to Mojang/Realms;
- the generated test-client options contain anisotropic-filtering value `0`;
- Loom lists an absent `build/resources/client` directory because that source
  set intentionally has no client resources.

None caused a gameplay assertion failure, renderer/resource failure, network
disconnect, or nonzero exit in the accepted final run.

The exact current `1.0.5-beta` working tree is good to go for Minecraft 26.2
local development and testing. It contains the stable `1.0.3` gameplay feature
set plus the silent continuous trail and explicit Creeper-sourced Obsidian
protection fixes.

## 2026-07-23 14:37–14:41 beta publication addendum

| Local time | Device action | Intention | Result |
|---|---|---|---|
| 14:37 | Audited the working tree, remote branch/tag state, GitHub authentication, and the two `1.1.0` artifacts before publishing. | Avoid overwriting an existing branch/tag/release and bind the publication to the already tested files. | Confirmed `beta` and `v1.1.0` did not exist remotely, GitHub authentication was active for `Mayu163`, and only the stable `v1.0.2` Release existed. |
| 14:38 | Created local branch `beta`, staged the complete verified `1.1.0` source/resource/test/documentation state, and committed it as `cfa2ca5` (`Add Summon Creeper beta features`). | Keep `main` unchanged and publish the requested beta state as a coherent commit. | Commit contains 21 changed files; generated JARs remained excluded from Git history. |
| 14:39 | Pushed `beta` to `origin` and configured local upstream tracking. | Publish the current state to the requested GitHub branch. | **PASS — remote `beta` was created at `cfa2ca5e44a6e9c4b50e0e547f0fac04c61696ce`; `main` was not modified.** |
| 14:40 | Created GitHub prerelease `v1.1.0`, targeted it at `beta`, and uploaded exactly the binary and source JARs. | Distribute the tested patch-number `1.1.0` build without marking it as the stable/latest release. | **PASS — prerelease is published and not a draft:** `https://github.com/Mayu163/shiros-test-mods/releases/tag/v1.1.0`. |
| 14:40 | Queried the remote refs and GitHub Release asset metadata after publication. | Verify tag placement, prerelease flags, upload completion, sizes, and checksums independently of the create command. | `beta` and `v1.1.0` both resolved to `cfa2ca5e44a6e9c4b50e0e547f0fac04c61696ce`; both assets reported `uploaded`. Binary: 68,265 bytes, SHA-256 `d9e450116a81e2f6ea06b31cec09accd9641a3d99e1821a66240100dd28e8af5`. Sources: 29,515 bytes, SHA-256 `4c7d0160ad847054024e4849c06ec9f2f51d7207907abf49283af0525a92454c`. |

## 2026-07-25 silent trail and Obsidian protection

| Local time | Device action | Intention | Result |
|---|---|---|---|
| 16:55–17:00 | Traced the CMD payload trail, mapped client firework implementation, vanilla Creeper explosion path, and entity-based explosion calculator. | Identify the repeated sound source and the narrowest Creeper-only block-protection point. | Confirmed each color event invoked `createFireworks`, whose client starter plays a blast sound. Selected silent colored dust particles and a calculator mixin that vetoes Obsidian only for Creeper sources. |
| 17:00 | Replaced repeated firework explosions with every-tick colored dust at two movement-relative emitters, retained the white spark core, and added exact-one launch-sound and continuous-emission telemetry. | Produce a long visible rainbow trail with one launch sound and no repeated firework blast sounds. | Common and client sources compiled successfully. |
| 17:00 | Added `CreeperExplosionProtectionMixin` and strengthened the explosion arena with maximal-power calculator assertions plus real vanilla/Fly/CMD/Summon detonations and dirt controls. | Make Obsidian protection explicit and prove ordinary block damage remains enabled. | Mixin loaded in the real server and client environments; the new calculator assertions passed. |
| 17:01 | Ran the first dedicated GameTest suite. | Exercise the new behavior in Minecraft itself. | Six tests passed; the four-Creeper explosion arena exceeded its old 55-tick completion allowance before all entities finished. No Obsidian or trail assertion failed. |
| 17:02–17:03 | Increased only the multi-Creeper arena allowance to 80 ticks, added unfinished-entity diagnostics, and reran the dedicated suite. | Remove timing flakiness without weakening behavioral assertions. | **PASS — all 7 registered required GameTests passed.** |
| 17:03–17:04 | Ran the visible integrated-client GameTest and inspected the refreshed takeoff and ballistic-arc screenshots at original resolution. | Verify actual client synchronization, silent-particle trail rendering, color continuity, and existing entity/trajectory visuals. | **PASS — client test exited successfully; screenshots show a long continuous white-core trail with distinct red/orange/yellow/green/cyan color segments.** |
| 17:17–17:20 | Re-versioned the verified working tree as `1.0.4-beta`, rebuilt the distributable artifacts, reran both real Minecraft suites, pushed `main`, and published a GitHub prerelease. | Deliver the completed fixes without replacing the stable `v1.0.3` release. | Build, all 7 server GameTests, visible client GameTest, release tag, and asset metadata were verified. |

## 2026-07-25 global high-arc invariant and flight-long trail

| Local time | Device action | Intention | Result |
|---|---|---|---|
| 17:36–17:42 | Traced every Fly Creeper launch entry point, the CMD capture/throw path, ballistic tick logic, and trail lifecycle. | Find the shared cause of ground-skimming selections, in-flight course changes, and the trail ending during long flights. | Confirmed CMD owned the only production calculation, Fly reapplied target guidance every tick, and trail emission was hard-capped at 80 ticks. |
| 17:42–17:52 | Moved trajectory planning into `FlyCreeper`, added a 35-block minimum apex plus automatic elevated-target/terrain raising, fixed the complete horizontal and vertical schedule at launch, rejected replanning during active flight, and made trail emission follow flight completion instead of a timer. | Enforce one global invariant for every direct or CMD launch and keep the visual trail alive through the latter half. | Compilation passed; iterative server runs corrected a no-AI test setup, exact floating-point boundary reporting, and long-distance chunk ticking. |
| 17:52–18:03 | Added and ran the real CMD capture test plus a direct-launch matrix covering zero/short/diagonal/90-block distances, high and low targets, elevated origin, a 48-block ridge, replanning, and horizontal/vertical velocity disturbance. | Prove low, flat, ground-skimming, terrain-clipping, and mid-flight course changes cannot be selected. | **PASS — all 8 required dedicated-server GameTests passed.** Every measured arc reached at least 35 blocks; the high-target and ridge cases automatically selected higher plans; every trail had a maximum emission gap of one tick. |
| 17:54–18:04 | Updated the visible integrated-client test to wait for a true latter-half descent, assert the active no-gap trail there, capture all four frames, and inspect the arc screenshot at original resolution. | Verify actual rendered continuity rather than relying only on server telemetry. | **PASS — integrated-client test exited successfully.** The latter-half arc frame visibly shows a continuous white-core, multicolor trail; exact-one launch sound, targeting, fuse prediction, contact, and rendering checks also passed. |
| 18:02–18:04 | Ran a clean full build, the final server suite, and the final client suite after locking both velocity axes. | Validate the exact final source state. | **PASS — build successful, server 8/8, client successful.** Expected offline Mojang/Realms, anisotropic-filtering, and empty client-resource warnings remained non-blocking. |
| 18:24–18:27 | Re-versioned the verified update as `1.0.5-beta`, rebuilt it, reran both real Minecraft suites, advanced `beta`, and published the GitHub prerelease with binary and sources. | Deliver the completed flight invariant without updating remote `main`. | **PASS — clean build, server 8/8, visible client test, beta push, prerelease metadata, and both uploaded assets were verified.** |
