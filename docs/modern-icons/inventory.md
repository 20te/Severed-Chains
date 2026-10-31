# Modern fantasy icons: step 1 inventory

Branch: codex/modern-fantasy-icons. Base: 3449ce42e.
The separate worktree starts from committed HEAD; unrelated uncommitted backgrounds and model work stay in the original checkout.

## Review scope

Polished fantasy artwork, 8x source dimensions, rendered at existing gameplay size. Modern icons default On; Off restores the existing Retail/Enhanced preference. Text, portraits, scenery and scene effects are excluded. Icon borders, cursors, prompts and status symbols are included; whole window frames and meters are not icon replacements.

## Coverage catalogue

| Family | Source and drawing entry | Frames and variants to preserve |
| --- | --- | --- |
| Player head triangle | submap/MapIndicator.java and SMap.renderTriangleIndicators; SUBMAP/savepoint/0 | 16x16 logical sprite; original data has 12 sprite groups and 13 sequence records (not 13 distinct images); 3 encounter palette selections; preserve each sequence duration and reduced-motion behaviour |
| Doors and exits | MapIndicator and SUBMAP/savepoint/1 | 8x16 logical quad, 8x15 sampled region; 12 sprite groups, 11 sequence records; 4 indicator-type palette selections; up to 20 placements |
| Alert symbol | MapIndicator.renderAlertIndicator; SMap.renderAlertIndicator | 24x24; original UV 24,64; palette 976,464; preserve object attachment, height offset and visibility |
| Dialogue continue triangle (red) | Text.java textboxArrowObjs, renderTextboxArrow and updateTextboxArrowSprite | Seven 16x14 sprite frames, UV 64 + frame * 16; palette 1008,484; render Y scale 0.875; preserve SHOW_ARROW visibility, textbox positioning, animation timing and reduced-motion behaviour. Explicitly included despite text glyphs being excluded |
| Equipment and consumables | inventory/ItemIcon.java; uiFile.itemIcons_c6a4; Menus renderables | IDs 0 through 46, invalid 57, warning 58; NONE 64 remains invisible; aliases share IDs; Retail remapping in LodMod.createIconMapping must remain effective |
| Dragoon spirits in inventory | inventory/DragoonSpiritIcon.java; uiFile.uiElements_0000 | Eight spirit types, glyphs 13 through 20 |
| Animated spirit symbols | LodMod.registerAtlasIcons | Eight palettes times three 16x16 frames; divine overlay has two 8x16 pieces; preserve palette meaning and overlay assembly |
| Key goods | gfx/goods/*.png; GoodsIcon and texture atlas registration | 21 committed PNGs, registry IDs derived from filenames; logical 16x16; retain mod registry identity |
| Menu cursors, arrows and symbols | inventory/screens/controls/Glyph.java; SItem.allocateUiElement and initGlyph; Menus.uploadRenderable | Up spinner glyphs 61-68; down 53-60; reduced-motion still frames 67 and 59; other menu glyph groups are defined by SItem menu arrays and UiType metrics |
| Menu status symbols | SItem.renderCharacterStatusEffect; shared UI glyph data | Preserve status-bit precedence and existing blink timing; do not replace associated text |
| Retail battle commands | RetailBattleAction; BattleMenuStruct58.initIconObjs | Nine base command slots, three stored poses each; selected pose sequence 0,1,2,1; retain variable pose heights, tint, disabled cross and blend modes |
| Additional battle commands | SeveredBattleAction; gfx/ui/battle_icons.png; BattleHud | 16x16 cells; Change Addition uses three frames; TransformAction also samples this sheet; retain per-action frame counts |
| Battle targeting and status feedback | BattleHud.drawTargetArrow; BattleMenuStruct58; BattleUiParts | Target arrow has eight bobbing offsets; retain target colours, selection state, disabled cross and status conditions; exclude number/text glyphs |
| Battle element symbols | combat/bent/ElementIcon; BattleHud; gfx/ui element PNGs | Ten built-in entries: magic/none, physical, water, earth, dark, divine, thunder, light, wind, fire; preserve registered mod entries |
| Battle button prompts | BattleUiParts buttonVert; buttonPressHudMetrics in Battle | Preserve each input symbol and metrics, additive blending, timing feedback and reduced-motion settings |
| World-map arrows | wmap/MapMarker; WMap.mapArrow | Eight 16x16 texture cells; logical size changes 16 to 8 with zoom; three render tint choices (red, blue, bright); player, destination and selection uses |
| World-map destination markers | MapMarker; WMap.coolonPlaceMarker | Three 16x16 cells drawn at size 10; additive blending and shared tint choices |
| World-map transport prompts | CoolonQueenFuryOverlay | Four 32x16 Coolon poses and five Queen Fury opening-door poses; preserve hold sequences, negative door width, UV sampling, clocks and input labels |
| World-map zoom indicator | ZoomOverlay | Seven draw pieces: shared connector drawn twice, three level diamonds, two unchanged L2/R2 text/bar pieces; preserve state mapping, brightness, placement and blends |

Paths in the table are relative to src/main/java/legend/game unless otherwise stated.

## Integration constraints

- Use existing TextureAtlas/TextureAtlasIcon and alpha-texture drawing facilities; add no rendering library.
- Select modern artwork at drawing time while retaining original assets and registry IDs.
- Shared UI textures contain text and portraits alongside symbols: replace identified icon glyphs only.
- Preserve translation, placement, hit areas, animation timing, palette semantics and resource cleanup.
- Config uses the existing USER_INTERFACE settings area and campaign storage, beside IconSetConfigEntry.
- Unknown mod-provided icons retain their current drawing path.
- A new worktree does not contain ignored unpacked game data or the original checkout's uncommitted remasters; gameplay setup must use existing local data without copying unrelated changes.

## Verification and remaining evidence

Verified source definitions and callers across exploration, menus, inventory, battle and world map. Read the two local unpacked ANM headers to confirm group/sequence counts. Confirmed clean worktree before this document and created the named branch.

This is a source-level family catalogue, not an exported contact sheet. Data-driven shared UI glyph artwork, every sprite-group image, palette appearance and platform-specific input prompts still require visual inspection before their replacement is integrated. Do not interpret sequence-record counts as distinct animation-frame counts. No gameplay or artwork checks have run.

## Step ledger

- Completed: step 1, source inventory and isolated worktree.
- User approved step 1.
- Completed: step 2 artwork families, including world-map transport prompts and zoom indicator pieces, approved.
- Completed: step 3 packed atlas and rendering paths approved (see integration.md).
- Current: step 4 Modern icons setting and live fallback implemented; awaiting review (see settings.md).
- Review: art-direction/reference-sheet-v1.png and art-direction/README.md.
- Remaining: step 5 final checks and gameplay comparison from the accepted plan. Four focused atlas/configuration tests pass; final in-game checks remain pending.
