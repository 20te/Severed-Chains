# First combat arena: discovery and baseline

Worktree: first-combat-arena-remaster/Severed-Chains
Branch: codex/first-combat-arena-remaster

## Verified identity
The first scripted fight is encounter 477 (0x1dd), knight_of_sandora_knight_of_sandora, on stage 2.
Cut 10 player script (DRGN21/36/1) sets var[47] to 2 and calls scriptMapTransition(-1, 0x1dd).
GameVarParam maps var[47] to battleStage; SMap resolves the encounter through LodEncounters.LEGACY.
The commander battle is later: cut 725 script DRGN21/726/1 calls encounter 384 (0x180), also stage 2.

## Asset and rendering map
Battle.loadStage uses DRGN0/(2497 + stage), hence DRGN0/2499:
- 1: original MCQ backdrop, 256 x 384; 128 x 256 VRAM words.
- 2: 4-bit TIM atlas, 256 x 256 pixels at VRAM word x448,y0; CLUT 64 x 16.
- 0: TMD model and animation directory, loaded by loadStageTmdAndAnim.
Battle.renderSkybox uses original camera angles, MCQ dimensions and repeated segments; preserve all offsets.
McqBuilder reconstructs 16-pixel tiles with per-tile CLUT progression.
Battle.loadStageTim uploads pixels/CLUT and backs up the stage CLUT. renderBattleStage applies texture animations before queuing models.
Lighting: loadStageAmbiance copies Ambiance.stageAmbiance_801134fc[2] into the per-battle DEFF manager; applyStageAmbiance sets ambient and three lights. Script setters, ambiance restore and palette darkening must remain functional.
Shadows: BattleEntity27c calls SEffe.renderBttlShadow; shared shadow model uses model shadow type, attachment, size and offset. Battle loads shadow.tim. ModelEffect13c also calls the shared shadow renderer: any adjustment must be stage-specific and respect scripted shadow state.

## Inspected originals
build/arena-baseline/backdrop-original.png shows amber clouds, mountain silhouettes and dark forest.
build/arena-baseline/arena-tim-first-palette.png shows ground, ruined building facades, branches and debris.
The atlas inspection uses only the first palette; bright red areas are palette placeholders, not approved scene colors.
Preserve landmarks, horizon and periodic width. Improve subtle cloud/forest detail first, then surface detail consistent with that palette.
Do not paint an atlas from the first-palette preview alone: inspect polygon CLUT assignments and animation regions before editing.

## Validation status
Step 1 complete. Java 25 compilation and all three ArenaBaselineTest checks pass (zero skipped/failures/errors).
The original opening camera is preserved: the fixture does not use Harness.startBattle, which deliberately skips that movement.
Captured and visually inspected the opening camera, combat stance, Dart sword attack/hit effects, enemy response and ground shadows.
Representative original views: build/arena-baseline/opening-original.png, combat-original.png, attack-original.png.
The test asserts fresh new-game state, a loaded monster with full HP, and monster HP loss after the attack.
No spell has been verified; the attack satisfies the requested attack-or-spell baseline.
The capture uses the new-game party (Dart only) and seeded RNG, injected into the verified encounter/stage. It does not replay the full Seles conversation.
Opening title texture warnings were fixture artifacts from premature title deallocation; corrected by releasing title assets together with the battle transition. The final run has no such warnings.
No production renderer or visual assets have changed.

## Reproduce
Use Java 25 and run:
rtk proxy ./gradlew.bat test --tests legend.game.ArenaBaselineTest -I tools/arena-capture.init.gradle --offline --console=plain

The init script enables only explicitly selected tests, supplies required java.util access, and places generated resources where the mod loader expects them.
The isolated fixture supplies SDL input focus, initializes registries on the render thread, and waits for title assets before cleanup.
Required extracted assets and all four local disc images are present in this worktree. Personal saves were not copied.

## Review checkpoint
Completed: step 1, arena identification, asset trace and original runtime baseline.
Next: step 2 edits the original backdrop with image generation, integrates a stage-2-only optional replacement with fallback, and verifies camera alignment and horizontal repetition.
Remaining: arena textures, lighting/existing shadows, final before/after regression validation.

## Step 2 current review
The user requested a stronger remake matching the approved Disc 1 Seles backgrounds and required image-generation-only image edits. Inspected the local Seles overlook and cut-10 remasters and used the overlook as a style reference.
Installed gfx/battle/seles/backdrop.png: 1024 x 1536, retaining original logical 256 x 384 placement and period. Exact selected generation prompts are in gfx/battle/seles/prompts.json.
The stage-2-only rendering path uses the existing QuadBuilder/Texture/QueuedModelStandard primitives and the original MCQ camera offsets and monochrome brightness. Missing, malformed and incompatible-dimension assets retain the original MCQ path. Both stage reload and battle deallocation release the optional texture.
Verification: remastered runtime test passes all three checks, including replacement selection and attack HP loss. Separate missing and malformed asset runs also pass all three checks with replacement selection asserted false.
Current review captures: build/arena-disc1-final/opening.png, combat.png, attack.png.
Step 2 remains incomplete: the stronger artwork has improved realistic clouds, mountain geology and conifer detail, but image-generation-only corrections have not eliminated the visible horizontal repeat and top clear-color joins. No deterministic image processing was used. Do not call this seamless or approve it as final.
Next action is to agree the stronger art direction and continue seam correction within step 2. Step 3 will then improve ground, facades and the prominent treeline geometry textures to the same standard while preserving UVs and animation.

## Step 2 continuation, 2026-11-01
Selected a further ImageGen-only edge revision. Repeat-edge RGB mean error improved from 9.71 to 8.30; the original is 3.07. A half-offset generation experiment was rejected because it changed geography without reducing error. Selected and rejected generation references are in gfx/battle/seles/seam-review.json.
Fixed the top/bottom clear-color join in the stage-2 rendering path by vertically clamping the texture and extending the same quad UV mapping beyond the original image height. No image pixels were processed. The original logical image mapping, horizontal period, camera offsets and scripted brightness remain unchanged.
Final capture run passes all three tests. Visually inspected build/arena-clamped-final/opening.png and attack.png: the top band is gone, but a horizontal repeat join is still clearly visible during the attack camera. This fails the seamless-repeat acceptance criterion. Step 2 is not complete, and step 3 has not started.
The current draft is preserved for review; do not describe it as production-ready or seamless. Further automatic generation has not reliably satisfied this constraint.

## Step 2 final review candidate, 2026-11-01
This section supersedes the earlier step-2 blocker reports.
A new ImageGen-only revision softens only the extreme side edges with natural atmospheric haze. Selected generation: exec-ddb4ac29-dd05-4bae-9d01-545d60464176.png. Exact prompt and metrics: gfx/battle/seles/final-seam-prompt.json.
The repeat-edge RGB mean mismatch is now 3.22 (original: 3.07), down from 8.30. The large color discontinuity is substantially reduced to original-level visual continuity. Boundaries are not pixel-identical, and the repeated composition may still be recognizable; no deterministic image processing was performed.
Validated the final asset in the opening camera, normal combat and Dart attack/hit effects with all three ArenaBaselineTest checks passing. Added and passed SelesBattleBackdropTest, checking preserved 2:3 aspect and edge mismatch at most 4.0. Earlier missing/malformed fallback runs passed; the final asset only changes authored pixels, not fallback logic.
Final views: build/arena-seam-final/opening.png, combat.png, attack.png. The top band is gone. The remaining low-resolution treeline/ruined surfaces are arena model textures, intentionally awaiting step 3.
Step 2 is ready for user visual validation. No broader arena regression, spell, battle re-entry or final lighting verification is claimed here; those belong to later steps.
Next, step 3 will inspect polygon palette/UV assignments and animated atlas regions, then improve ground, ruined surfaces and treeline textures to match the selected backdrop, preserving mapping and animation.

## Step 3 final review, 2026-11-01
Completed the arena texture pass; awaiting user validation.
Inspected all nine polygon palettes and animation metadata using SelesArenaAssetsTest. Mapping evidence and decoded retail palette variants are under build/arena-textures/original. Original UVs, geometry, polygon CLUTs, transparency/STP and animation data remain unchanged.
The generated square arena.png improves compacted ground, ruined stone/wood, fences and woodland. A second ImageGen edit quieted the initially noisy ground. Exact generation prompts are in gfx/battle/seles/arena-prompts.json; dimensions/hash in arena-asset.json. No deterministic authored-image processing was used.
Integration uses the existing battle shader, Texture loading and queued-model texture binding. Only stage 2 sets reserved ctmd flag 0x40; other render calls were checked for flag collisions. Retail indexed lookup still determines transparency and current palette color. The remaster is modulated by the ratio to the original reference palette, with highlights limited to 1.5 times the retail color to retain scene tonality. Missing authored coverage retains original color. Translucent primitives and the original animated tile (144,64)-(208,128) retain their original appearance. Palette darkening remains live.
The optional atlas and 16-color reference texture are released with the stage model. Missing, malformed and wrong-size assets use retail fallback; runtime missing and malformed cases were tested, while the dimension guard is covered by inspection and the installed-asset test.
Validation: six focused tests pass (four runtime checks plus atlas-mapping and backdrop-continuity tests). Confirmed loaded replacement, active original smoke animation, full monster HP, attack damage, and visible ground darkening through SEffe.applyScreenDarkening(8), restored through multiplier 16 in finally. Separate missing and malformed atlas load tests pass.
Inspected build/arena-surfaces-review/opening.png, combat.png, attack.png and runtime/palette-dark.png. The HUD remains originally translucent: seeing the new floor through its panel is expected. An apparent HUD issue was investigated and confirmed as existing behavior, so no HUD change was kept.
Limits: original polygon silhouettes, cutout resolution and some mapped surface joins remain visible. Broad alternate-arena/spell/re-entry regression belongs to step 5. This step does not change lighting or shadow settings.
Next: step 4 will adjust this arena baseline lighting and existing ground-shadow presentation, verify grounding in idle/movement and during an attack, and preserve script-driven lighting changes.

## Step 3 revision after live review, 2026-11-01
The user rejected the apparent texture improvement and reported a black environment during attacks. This section supersedes prior step-3 readiness claims.
The first implementation still constrained authored RGB by native per-pixel palette indices and the native mask. Removed that constraint: static surfaces now use the generated RGBA colors and high-resolution cutouts directly. The new transparent atlas was generated with ImageGen only; exact prompt is in alpha-revision-prompt.json. Original UVs, stage geometry and animated/translucent smoke remain unchanged.
The full original 64x16 CLUT is snapshotted. Per-palette reference swatch 8 tracks the known stage-wide darkening modulation without imposing native pixel blocks on authored RGB. Original lighting uniforms remain unchanged. Arbitrary independent palette-entry edits have not been established as equivalent; stage smoke palette animation retains the original path.
Found a renderer binding bug: QueuedModel.useTexture previously restored main VRAM only when a draw had no overrides. Character-private VRAM can therefore remain in slot 1 when the arena overrides slots 0 and 2, making the arena sample incorrect indices/palettes. The shared method now restores main VRAM before applying explicit overrides; explicit character slot-1 overrides still win. This is a minimal shared rendering-state correction, with no character asset/model change.
Added a deliberate wrong-binding regression, also checking explicit private-VRAM precedence. Seven focused checks pass, covering full automatic addition, active smoke animation, image alpha/layout, palette darkening, texture binding, and backdrop continuity. Results are preserved in build/arena-corrected-final/test-results. Opaque atlas fallback was also exercised after the new RGBA validation. Earlier missing/malformed load checks remain recorded.
Inspected revised combat and attack captures in build/arena-corrected-final. Ground and cutouts now show authored detail directly, and the arena remains textured during the attack. Original geometric silhouettes remain polygonal; lighting and shadows are still awaiting step 4.
Preview uses ARENA_PREVIEW=true and only ArenaBaselineTest.test2_battleStartsAndMonsterHasHp; it leaves the game open until closed and keeps manual addition controls.

## Step 4: lighting and existing shadows, 2026-11-01
Steps 1-3 approved; step 4 implemented for review; step 5 remains gated.

Battle.loadStageAmbiance adjusts only the stage-2 per-battle copy: ambient RGB (0.34, 0.29, 0.21), key (224, 214, 196), low fill (26, 12, 6), fire-side light (164, 86, 28). Original light directions and the global retail presets are unchanged. The white key is less harsh while the secondary light retains the amber atmosphere. No per-frame lighting clamp or override is installed: script setters, temporary ambiance and the existing stage-ambiance restore keep control.

SEffe.renderBttlShadow reuses the original subtractive mesh and texture. For stage 2, render scale multiplies scripted size by (1.08, 1, 0.94), and subtractive colour uses (0.42, 0.46, 0.52) to retain a warmer, less crushed contact shadow. The source model size, offset, shadow type and attachment are not modified. Hidden shadows stay hidden through the existing callers. ModelEffect13c uses the same stage-scoped adjustment; other stages retain their original draw settings. No new shadow asset, dynamic shadow system or dependency was added.

All eight focused checks pass (six runtime, two asset checks), with zero failures or skips; results are preserved under build/arena-lighting-final/test-results. The new runtime check exercises an alternate lighting preset and the script restore entry point, confirms the retail preset remains unchanged, checks the existing subtractive blend, tests repeated shadow rendering without accumulating scale, and verifies another stage retains its original shadow scale. Visually inspected opening, combat and full-addition captures under build/arena-lighting-final/runtime; the arena remains textured during the attack. Prior views remain under build/arena-corrected-final. Camera timing varies between runs, so the before/after views are not pixel-aligned comparisons. git diff --check passes.

Limitations: the original low-poly geometry and mapped joins remain visible, and the opening camera still exposes an approximate backdrop repeat transition. Spell, battle re-entry and broader alternate-arena runtime checks remain for step 5. This step does not claim those checks.

## Material revision after comparison, 2026-11-01
User requested another tree/fence/ground pass before final verification. Built-in ImageGen edited arena.png with the exact prompt preserved in gfx/battle/seles/material-revision-prompt.json; the generated RGBA was copied without pixel processing. Previous atlas remains in build/arena-before-material-revision.png. Ground now has irregular dusty patches, shallow ruts and embedded stones for painted relief. Timber and tree bark were repainted while retaining UV island placement.

The tree issue was rendering, not just asset detail: original stage part 6 uses CLUT 15708 (448,245) and translucent primitives, but none of its 16 palette entries have STP set. The blanket translucent-primitive exclusion kept the tree on original indexed pixels. The stage-2 shader now permits this specific opaque tree cutout palette to use authored RGBA, with zero STP to preserve its original opaque render pass. Smoke and other translucent materials retain their previous paths. Asset tests verify this palette assumption against the original data.

Eight focused checks pass with no failures/skips. Visually inspected runtime frame 70 (opening), 140 (sword strike) and 350 (combat) in build/arena-material-revision/runtime. Tree detail and ground relief are clearly visible; the background remains present during the attack. Fence improvement is modest, and mapped timber still appears soft at close range. Small cutout-edge artifacts and the backdrop repeat transition remain visible. No physical ground displacement or normal-map lighting was introduced. Spell/re-entry/alternate-arena runtime verification remains step 5, not executed in this revision.
