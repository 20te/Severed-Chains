# World-map artwork checkpoint

Six selected masters cover24 unique frames/pieces: eight turning arrows, three destination diamonds, four Coolon flight poses, five Queen Fury opening-door poses, three zoom-level diamond states and one shared blue connector. Review world-review.md or world-preview.html. The static gallery's image paths are checked; its browser rendering has not been verified.

The initial inventory covered11 marker frames. Inspection found nine transport-prompt frames and four unique zoom-indicator pieces that also belong to the requested icon scope. Those omissions are now covered. The Queen Fury prompt is an opening door, rather than a ship picture. Zoom overlay entries0/1 reuse the same connector; entries5/6 contain L2/R2 text and bars and retain their original path.

## Preserved behaviour for integration

- Arrows keep eight frames, the existing vsync-adjusted clock, red player tint, blue destination tint and bright selection tint. Preserve all colour components, including the bright tint's blue multiplier2. Render size remains16 or8 according to zoom, with the original placement, target depths and extra world-view player Y adjustment.
- Destination diamonds keep three frames,16x16 sampling,10x10 drawing, the existing five-tick/vsync clock, nine Coolon locations and additive blending.
- Coolon keeps32x16 drawing, pose order0,1,2,3,0 and the existing two-tick/vsync clock. InputCodepoints labels remain unchanged.
- Queen Fury keeps five source poses, the exact15-entry hold/open/close sequence, three-tick/vsync clock,23x24 UV sampling, negative24x24 geometry and local offset22,-2. Keep the mirror at draw time; do not premirror the source sheet.
- Zoom retains all seven original draw pieces. Three diamond states correspond to local/continent/world zoom, with original transition mapping, positions, depths, map brightness and selected brightness multiplier8. Both connectors retain HALF_B_PLUS_HALF_F blending. L2/R2 labels and bars remain original.
- Terrain, smoke, continent names, location labels and input text are excluded. Preserve existing reduced-motion behaviour, cleanup and resource lifetime without introducing new clocks or flashing.

## Evidence and verification

world-original-metrics.json records source hashes from local DRGN0.BIN/5695, original frame rectangles, visible bounds, palettes and the seven zoom pieces. Evidence sheets decode original texture pixels at4x nearest-pixel scale for inspection; they are not gameplay screenshots and do not emulate PSX blending or draw-time tints.

All six PNGs passed resolution, visible-artwork, transparent-padding and outer-edge alpha checks (at most4/255). All24 pose regions passed individual boundary checks. The unused sixth door cell has alpha at most4/255 throughout. The door sheet was refined to remove its external backdrop; dark doorway interiors remain part of the icon. Both generation prompts and the selected output are recorded in world-generation.json.

The manifest check passed for six master hashes,24 pose regions,all20 animated source frames and all seven zoom draw pieces. All referenced artwork and review image paths exist. Original game code and textures remain unchanged. No Java tests were run for this artwork-only checkpoint.

## Progress and limits

Steps1 and2, including the world-map artwork, are approved. Step3 packing and rendering integration is the current checkpoint; see integration.md.

Next is step3, packing and rendering integration: bake colours, fit artwork to original bounds, produce exact8x assets and connect the modern drawing paths while preserving existing behaviour. The settings switch and compatibility/gameplay checks follow in the accepted plan. Raw generated sheets are not final runtime atlases. Tiny-size readability, matte cleanup, alignment, blending and in-game animation remain unverified.

Estimate communicated for this checkpoint:15-25 minutes, excluding user review. Remaining integration and testing are expected to take several hours; gameplay setup and visual alignment may change that estimate.
