# Equipment and consumable artwork checkpoint

48 new transparent PNG masters cover standard ItemIcon IDs 0-46 and INVALID (57). WARNING (58) reuses the alert diamond; NONE (64) stays empty. `gfx/ui/modern/inventory.json` maps all 50 distinct named standard IDs and records dimensions, logical 16x16 sizes and content hashes.

Review `inventory-preview.html` for 72px and 16px views, or `inventory-review.md` for individual image links. These are artwork review files, not game screenshots. The generated masters are saved under `gfx/ui/modern/inventory/` and are not loaded by the game yet.

## Identity and provenance

- Preserve all existing IDs and shared aliases: BANDANA/HAIRBAND 16, SHIELD/KNEEPIECE 20, CLOAK/SCARF/CAPE 30.
- Keep Retail/Enhanced remapping and mod-provided icon handling when integrating; this checkpoint changes neither.
- The original inventory texture and 64 native glyphs were exported from local DRGN0 files 6665 and 6666 into `inventory-original-evidence.png` and `inventory-original-metrics.json`. They are reference documentation, not replacement assets. The new manifest targets the named ItemIcon entries rather than guessing uses for unreferenced glyph slots.
- Used the built-in image generation tool once per new master with the approved art-direction sheet as the style reference. `inventory-all-generation-records.json` stores the shared prompt, each subject and its original generated path. Reconstruct an exact prompt by replacing SUBJECT in basePrompt with the corresponding subject.

## Checks

- Asserted exact coverage of distinct standard `new ItemIcon(...)` IDs from source, uniqueness of the 48 new image IDs and existence of every mapped file.
- Decoded all 48 PNGs; each has RGBA colour type, source dimensions at least 128x128 (8x logical size), a transparent region, visible artwork and no outer-edge pixel above alpha 4/255. `inventory-validation.json` records dimensions, visible bounds and measured alpha ranges. Near-transparent edge pixels are retained in these masters; final edge cleanup/packing remains pending.
- Verified all 48 SHA-256 hashes and all 96 local image links in the review page.
- Visually inspected representative weapon and armour masters for subject, silhouette and style. Full-set visual approval remains the user's checkpoint.
- Browser screenshot validation could not be completed: the browser checker could not reach the local server, and the local-file viewer was blocked by browser security. No workaround was attempted after that security rejection. The temporary local server was stopped.
- No Java tests were run because no game code changed. No gameplay, active Retail/Enhanced switching, exact-size packing or GPU-memory checks are claimed.

## Progress

Step 1 approved. Step 2 exploration and equipment/consumable artwork checkpoints approved. Key-goods/spirit masters await review; see goods-spirits-production.md. Step 2 remains incomplete: menu, battle and world-map artwork remain. Rendering integration and the Modern icons switch are later steps.

Next within step 2: Dragoon spirit and key-goods artwork, retaining their registry identities and animation variants.
