# Door and alert artwork checkpoint

Door markers reuse the approved `gfx/ui/modern/player-master.png` at their narrower logical size. No duplicate triangle bitmap is needed. The original group measurements, palettes, and animation sequence are recorded in `door-master.json` beside the master.

The separate gold exclamation diamond is saved as `gfx/ui/modern/alert.png`, with source metadata in `alert.json`. Generated using the built-in image generation tool from the approved reference and the original alert symbol; exact prompt saved in `door-alert-production-prompt.txt`.

## Original evidence and checks

- Exported native original textures in `door-original-evidence.png` (green, yellow, cyan rows) and `alert-original-evidence.png` from local unpacked small_arrow.tim and alert.tim.
- Door type-to-palette mapping is green, green, yellow, cyan. Four types share three distinct colours; preserve this mapping during integration.
- Door ANM has twelve groups but eleven sequence entries: 0,1,2,3,4,5,6,7,8,10,11. Group 9 is not in the sequence. Every sequence time is 2. Do not replace this with an unconditional twelve-frame loop.
- Original door logical size is 8x16 with 8x15 sampled pixels. Measured visible bounds for each group are saved in the metadata for alignment during integration.
- Alert logical size remains 24x24; preserve original object-relative positioning, visibility, UV origin 24,64 and palette data as reference.
- Visually checked the generated alert for a gold diamond, upright dark exclamation mark and separate dot. The PNG has RGBA colour type, resolution exceeding 192x192 (8x), a fully transparent sampled outside corner, and a visible centre.
- Metadata assertions checked sequence order, per-entry timing and palette counts. Staged whitespace check is required before delivery.

## Progress and limits

Step 1 approved. Step 2 visual direction, dialogue candidate, player master and door/alert checkpoint approved. Runtime tinting, alignment, exact-size packing, edge cleanup, readability and animation have not been tested in game. No game code or setting has changed.

Next within step 2: inventory equipment and consumable artwork, using existing ItemIcon IDs and Retail/Enhanced mapping. Remaining menu, battle and world-map families follow; integration and the Modern icons switch remain later steps.
