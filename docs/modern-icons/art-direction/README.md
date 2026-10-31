# Art direction checkpoint

Review `reference-sheet-v1.png`: player pointer, exit pointer, alert, potion, sword, and Attack command (left to right, top to bottom).

Additional reference: `dialogue-triangle-v1.png`, the red continue-dialogue marker. This is a single static reference pose, not its seven-frame production animation. Visually checked its downward red silhouette and restrained bevel. Saved PNG: 1341x1173; sampled corner alpha 0 and centre alpha 253. Generated with the built-in image generation tool.

### Dialogue triangle prompt

Use case: stylized-concept. Asset type: single fantasy RPG dialogue continue indicator, art-direction reference for the Legend of Dragoon icon remaster. Primary request: a small downward-pointing RED triangle that signals press to continue dialogue. Match a polished fantasy UI icon set with crisp silhouettes, subtle bevelled depth, controlled highlights from upper left, dark defined outer edge, and restrained metallic detail. Subject: one broad downward-pointing equilateral ruby-red triangular pointer with a solid rich red face and a thin darker garnet bevel, small restrained warm highlight along upper edge. It must read unmistakably as a simple red triangle at 16x14 logical pixels. Broad uninterrupted surfaces, no inner symbols, no exclamation mark, no gems, no filigree, no pedestal, no text. Single centered icon, fully inside canvas with generous transparent margin. Keep visual weight modest, no thick gold border. Smooth antialiased silhouette, sharp edges. TRUE transparent alpha everywhere outside triangle, no coloured backdrop, no black rectangle, no vignette, no checkerboard painted into image, no surrounding glow or shadow. This is one static art-direction reference pose; do not draw multiple frames or animate.

This is a concept sheet for style approval, not a production atlas. Its 1536x1024 canvas does not establish final cell dimensions. Individual 8x assets, animation frames, palette variants and actual gameplay-size checks remain pending. No game code or runtime textures changed.

Direction: polished fantasy, crisp outlines, restrained metallic bevels and readable silhouettes. The player marker keeps cyan; gameplay warning colours will still need their own variants. Avoid adding more ornament when producing small icons.

Generated with the built-in image generation tool. The PNG is RGBA; twelve sampled empty-background pixels across its edges and grid boundaries have alpha zero. Hidden RGB background colour may appear in viewers that ignore alpha. Visually reviewed the six subjects and spacing; transparency inside the pointer outlines and readability at gameplay size still require production verification.

## Generation prompt

Use case: stylized-concept. Asset type: one visual art-direction reference sheet for a Legend of Dragoon game icon remaster, not a production sprite atlas. Generate ONE transparent-background sheet containing exactly six isolated icons in a balanced 3-column by 2-row grid with generous empty spacing. Reading order: (1) downward-pointing cyan player-head triangle, an elongated slender open triangular pointer with luminous cyan edges and dark interior, clear tapered point; (2) emerald green door/exit downward pointer; (3) golden diamond alert badge with a dark exclamation mark; (4) small round blue healing potion bottle with cork and restrained silver collar; (5) single steel sword angled up-right, simple bronze crossguard and dark leather grip; (6) battle Attack command emblem, two crossed steel blades with a restrained bronze central rivet. Unified polished fantasy RPG interface style: crisp clean silhouettes, subtle bevelled depth, controlled highlights from upper left, restrained warm metal accents, clear strong outlines readable on bright and dark scenery. Rich but restrained cyan, emerald, gold and sapphire accents. Raster illustration with very smooth anti-aliased edges, sharp rather than blurry. Shape readability at 16-24 logical pixels is more important than ornament: broad forms, no microengraving, no elaborate filigree. Every object fully inside its cell, no overlap. Real transparent alpha outside the icons, no painted checkerboard, no background, no cards, no scene, no text labels or title, no watermark, no cast shadows outside silhouettes, no outer bloom. This sheet is only for reviewing the coherent visual direction; do not present gameplay or UI screenshot.

## Transparency refinement prompt

Edit the provided icon reference sheet. Preserve all six icons exactly: shapes, colours, arrangement, scale, highlights and outlines. Remove the entire dark coloured background and all surrounding glow/haze; make all empty space truly transparent alpha, including the empty interiors of the cyan and green triangular pointer outlines. Preserve interior dark material only where it belongs to the potion, metal or alert badge; triangular pointers should be open transparent inside their narrow luminous rims. No new objects, no labels, no checkerboard painted into pixels, no backdrop, no extra bloom. Output a transparent PNG reference sheet.

## Progress

Step 1 and the step 2 visual references are approved. See `../inventory.md` for the current artwork checkpoint and the remaining packing, rendering and settings work.
