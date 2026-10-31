# Menu icon artwork checkpoint

Twelve source masters: eight controller button prompts, separate unchecked/checked states and two eight-pose cursor sheets. See menu-review.md or menu-preview.html. The static gallery's image paths were checked; it was not browser-verified.

The authoring manifest gfx/ui/modern/menu.json covers 97 original menu glyphs. It records normal/blinking cursor families in four directions, transition scales, scroll-arrow phases, five static triangles and eight button labels. Existing player and dialogue triangle masters supply the outline and solid arrows through future palette baking. Glyph 35 is Stock text and remains excluded. The checked control retains an X.

Source metrics retain all 219 entries and their original primitive UVs, CLUTs, scales and ticks. Menu allocations override the texture page to 0x19. Source evidence is a decoded texture/primitive approximation, not a gameplay screenshot; fractional scale and PSX blending are not emulated. Text, punctuation, panels and highlight strips remain outside this artwork checkpoint. Status-effect names in menus are rendered as text, not separate pictograms.

Reduced-motion up/down frames remain 67/59. Left/right fade-in plays 111 down to 108 before repeating; fade-out plays 108 through 111. Integration must preserve existing clocks, mirror signs, colour overrides, placement, blend modes and attachment transforms.

## Verification

- Visually inspected all twelve selected masters, including button labels and both cursor pose sequences.
- All twelve PNGs contain visible artwork, transparent padding and outer-edge alpha at most 4/255. Dimensions and bounds are recorded in menu-validation.json.
- All sixteen cursor cells contain isolated visible artwork with clear boundaries. Per-cell source rectangles and bounds are in menu-pose-validation.json.
- Asserted exact coverage of 97 selected glyphs, all eight button labels, twelve master files and sixteen pose cells. Selected master hashes and the original checkbox texture hash are recorded in menu.json.
- No game source or original artwork was changed. No Java tests were needed for this artwork-only checkpoint.

## Progress and limits

Step 1 approved. Step 2 exploration markers, equipment/consumables, goods/spirits and menu artwork approved. Battle artwork is the current checkpoint, preserving command identities, poses, element colours and button meanings. World-map artwork follows after approval.

These are authoring assets. Exact 8x packing, palette baking, per-frame alignment, matte cleanup, rendering integration, settings switching and in-game animation checks remain pending. Raw generated padding must not be used as final alignment.
