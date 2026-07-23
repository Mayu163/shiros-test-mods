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
  gravity-driven curves with an approximately 30%-higher apex that reach the
  target area.
- A spawn egg for each custom type.
- The same natural-spawn requirements, biome coverage, weight, and group size
  as the vanilla Creeper.
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
- Metadata/resources: `fabric.mod.json`, English localization, two item
  definitions
- Common implementation: initializer, entity registration, Fly/CMD classes,
  village targeting, spawn eggs
- Client implementation: client initializer, Creeper variant renderer,
  Elytra render layer
- Tests: dedicated server GameTests and visible client GameTest
- Documentation: `README.md`, `DEVICE_ACTIONS.md`

The generated example mixin classes/configuration were removed because this
implementation requires no mixins.

## Final state

As of 2026-07-23, the requested mod behavior is implemented and the repository
is good to go for Minecraft 26.2 development/testing. The final binary should
be rebuilt from source before distribution if any subsequent code or
dependency version changes are made.
