# Step3: packed assets and rendering integration

The approved artwork is baked into gfx/ui/modern/packed/atlas.png, with391 icon entries and29 aliases, including eight frames for the shared battle item/addition/spell list cursor. The4096x2048 RGBA atlas uses32MiB of GPU texture storage without mipmaps; its PNG occupies approximately10MB on disk. Two transparent pixels around each packed rectangle prevent neighbouring artwork from bleeding through filtering.

## Rebuilding

From the repository root, run `python tools/modern-icons/build.py` with Python3 and Java25 available. Python only reads metadata and describes recipes; the standalone JDK utility performs deterministic cropping, rotation, palette baking, resizing and packing. It uses no added dependency and needs no unpacked game data. Authoring masters are unchanged. The source recipe table, index and alias table are retained alongside the packed atlas.

All targets are eight times their logical dimensions. Original32px goods retain256px targets, matching eight times their original PNG dimensions. Source visible bounds determine where each pose fits. Player and dialogue bounds were additionally read from the original local textures and recorded as small metadata files; the rebuild uses those committed measurements.

Palette recipes keep player encounter variants, door types, spirit elements, gold/red/green menu arrows, battle health colours and timing colours distinct. Recolouring uses source channel value rather than luminance so saturated red masters do not turn into dark gold arrows.

## Drawing paths

ModernIcons loads a separate filtered atlas through the existing Texture, QuadBuilder and TextureAtlasIcon facilities. It uses the renderer's texture alpha support. The atlas and mesh persist across scene transitions and are explicitly replaced during atlas reloads.

- Menus substitute known item and UI glyph metrics while leaving the original renderable animation, positioning, scaling, colour and clipping code in charge. Composed cursors retain distinct shadow/content blend passes. Partial-metric requests, unsupported palette overrides and unrelated UiTypes keep their existing drawing path. The Divine inventory spirit retains its original15px drawing width.
- Goods and spirit transformations select modern atlas icons using the existing registry identities. Unknown registrations use the original atlas. Goods retain original metrics and choose the modern replacement at draw time so cached menus can switch immediately. Dart's Divine overlay transformation remains in the existing character code.
- Exploration markers select packed poses using the current UV and CLUT values. Their original clocks, object attachment and encounter thresholds remain untouched. The earlier authoring note's yellow threshold was approximate: actual SMap logic uses0xf00, and integration leaves that logic unchanged.
- The red dialogue arrow retains its original visibility checks, frame selection, local offset, Y scale and world scissor.
- Battle commands, additional commands, targeting arrows, disabled crosses, element emblems and timing pictograms select packed frames while retaining their existing frame clocks, menu attachment, tint, alpha and blend choices. Text, numbers, full eye/meter assemblies, glow effects and addition borders remain original.
- World-map markers preserve tint vectors, zoom sizes, frame clocks and additive blending. Transport prompts preserve hold sequences, door mirroring, placement and input labels. Zoom diamonds and connectors preserve state mapping, brightness and blend modes; L2/R2 text and bars remain original.

Step4 now supplies the campaign-scoped Modern icons setting, read directly at draw time; see settings.md. Legacy rendering remains available through the existing fallback branches.

## Verification

- Java25 compilation passed using cached dependencies.
- `gradlew modernIconTest --offline` passed. The dedicated task runs independently of the repository's default test exclusion and does not start the game. It checks all383 rectangles, minimum8x dimensions, non-overlap, visible content, clear gutters, alias resolution, core inventory coverage and reduced-motion scroll frames.
- `gradlew copySupportFiles --offline` passed. Packaging includes atlas.png, atlas.json and aliases.json under the expected runtime directory, and excludes the modern authoring masters and recipe table.
- Inspected the packed atlas visually. This caught dark recoloured arrows, which were corrected before the final bake.
- Original texture assets remain unchanged; only the rendering consumers and the additive modern assets have changed. No commit was created.

## Progress and remaining checks

Steps1-3 are approved. Step4's setting and compatibility work is the current checkpoint; see settings.md.

No game boot or live gameplay comparison has been performed for this checkpoint. Tiny-size readability, visual alignment, target colour appearance, additive effects and animation smoothness need the planned gameplay checks. A compiled rendering path is not evidence of its final in-game appearance.
