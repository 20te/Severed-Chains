# Dialogue triangle: first production artwork checkpoint

The approved red reference now has a seven-frame sprite-sheet candidate in `gfx/ui/modern/dialogue-triangle.png`. Its frame rectangles and measured visible bounds are recorded in `dialogue-triangle.frames.json` beside it. These files are not loaded by the game yet.

The original texture comes from `files/SECT/DRGN0.BIN/6669/4` in the original local checkout. `Scus94491BpeSegment.basicUiTexturesLoaded` uploads it, and `Text` selects seven 16x14 frames. Source inspection confirmed down-and-up bobbing rather than a rotation. Keep the existing clock, visibility flag, textbox-relative positioning, 0.875 render Y scale and reduced-motion handling when integrating.

## Validation

- PNG decoded as 8-bit RGBA; measured size 1896x830, split into four columns and two rows of 474x415 cells.
- Seven cells contain visible artwork, each bounded inside its own cell. The final cell has no pixels with alpha above 127. The sampled outer corner is fully transparent.
- Visually checked the red downward silhouettes, seven poses and down/up motion order.
- Artwork exceeds the planned 128x112 per-frame resolution. The image generator did not honour exact requested dimensions; this is a high-resolution master candidate, not a completed exact-8x atlas.
- Measured bounds vary slightly between frames (width 369-370, height 245-247 pixels); bobbing offsets are approximate. Alignment, opacity fringes, motion smoothness and readability still require gameplay-size review before this can be accepted for integration.
- No game renderer, settings, original assets or gameplay behaviour changed.

Generated with the built-in image generation tool. The exact prompt is saved in `dialogue-production-prompt.txt`.

## Progress

Step 1: approved. Step 2: visual direction and dialogue sprite-sheet candidate approved. Step 2 remains incomplete: exact-size production packing, other icon families and their variants remain. Rendering integration and the Modern icons switch are later steps.
