# Battle icon artwork checkpoint

Twenty-six selected source masters: eight retail command pose sheets, ten element emblems, one Change Addition pose sheet, five individual symbols and two legacy prompt pose sheets. Review battle-review.md or battle-preview.html. The static gallery's local image paths are checked, but its browser rendering has not been verified.

These are authoring assets. The game still uses its existing artwork. The manifest gfx/ui/modern/battle.json records identities, source rectangles, hashes, pose cells, palettes and integration constraints.

## Coverage and reuse

- All nine original command slots and 27 poses are mapped. Eight slots have new three-pose sheets. The legacy transform slot and current character-specific Transform reuse the previously approved spirit masters; Dart's Divine overlay and element choice remain separate.
- Selected retail commands retain pose order 0,1,2,1, the existing four-tick advance, unselected pose0, original bottom anchoring, variable16/24-pixel heights and original blend modes. The blue legacy shield remains distinct from green Guard.
- Change Addition retains three16x16 phases and its existing clock/reset behaviour. The separate detransformation indicator retains its own identity.
- Ten built-in element registry IDs have individual emblems. The none entry maps to magic.png. Unknown mod-provided elements, actions and character artwork keep their existing paths at integration.
- A neutral battle target master supplies the original blue/yellow/red health states, 16x23 sampling over16x24 geometry, eight bobbing offsets and target attachment. The thresholds remain greater than half, greater than quarter through half, and quarter or below.
- Disabled actions retain a bare red cross. The Dragoon timing star retains its blue CLUT0x33 and spinner path. The narrow timing pointer uses the original gold CLUT0x32 and8x24 bounds. Prompt arrow36 retains signed source orientation and final displayed direction.
- Twelve of the41 prompt-table entries are pictograms: bronze eye-button states, blue X-button states, two eye fragments and the prompt arrow. The overlapping eye fragments retain their original source rectangles for baking. The other29 entries are digits/text, panel pieces or glow/effect layers and stay on their existing paths.
- Live action names are InputCodepoints text, including platform-specific bindings. Those labels, portraits, numeric glyphs, full Dragoon eye assembly, meters, panels, glows and addition border effects are outside the icon-artwork scope. Preserve the existing reduced-motion suppression of addition border rotation.

## Source evidence and checks

Decoded local DRGN0.BIN/4113 textures using the battle upload positions: image704,256, palette banks704..752,496 and896..912,304. battle-original-metrics.json records source hashes, all27 command poses,41 prompt entries and six symbol samples. The evidence sheets show decoded pixels; they do not emulate PSX blending or constitute gameplay screenshots.

All26 PNGs passed checks for resolution, visible artwork, transparent padding and outer-edge alpha at most4/255. All33 pose regions passed separate boundary checks. Generated spacing is not always uniform: source rectangles use the clear gutters between poses, rather than assuming equal thirds. This avoids cutting into the sword artwork. The manifest check passed for26 masters,27 original command poses,ten distinct element IDs,twelve prompt mappings and29 excluded prompt entries. All referenced artwork and review image paths exist. Selected source hashes are recorded; a comparison against HEAD confirmed original textures and Java source remain unchanged.

The timing star's first draft used a red authoring colour; its selected refinement restores the blue colour seen with the actual gameplay palette. The final prompt and selected file are retained in battle-generation.json.

## Progress and limits

Step1 approved. Step2 exploration markers, equipment/consumables, goods/spirits, menu and battle artwork approved. World-map artwork is the current checkpoint, preserving frame identities, tint choices, zoom sizes and additive blending, and including transport prompts and zoom indicator pieces found during inspection.

Exact8x packing, palette baking, matte cleanup, per-frame alignment, rendering integration, settings switching and in-game animation/GPU checks remain pending. Raw generated padding must be fitted to original visible bounds before integration. No Java tests run for this artwork-only checkpoint.
