# Key goods and Dragoon spirits artwork checkpoint

21 new source masters are saved: 18 individual key-goods pictures, one shared spiral-stone master, one three-pose spirit-orb sheet and one two-pose Divine overlay sheet. Review `goods-spirits-review.md` for image links or `goods-spirits-preview.html` for a static gallery. The gallery has not been browser-verified; its local image links are checked separately. No browser workaround was attempted after the earlier security rejection.

`gfx/ui/modern/goods-spirits.json` records the selected masters, hashes, source regions, palettes, IDs and packing targets. These are authoring assets, not active game replacements. Neutral masters still require colour baking and aligned packing.

## Identity and reuse

- Preserve all 21 existing PNG-derived atlas IDs. Blue, red and yellow stones share a neutral spiral master with distinct colour recipes. Other pictures retain individual masters.
- Record all 37 registered goods, including eight explicit standard ItemIcon overrides and eight spirit goods. Standard overrides keep using the approved equipment/consumable assets.
- Record two modern-only aliases needed at integration: `lod:dragon_blocker` uses the existing `lod:dragon_block_staff` artwork identity; `lod:gold_dragoon_spirit_2` reuses the Gold Spirit. No registry, save or gameplay code was changed.
- Spirit inventory glyphs remain 13-20; Divine's original 15x16 logical size is recorded separately from the other 16x16 glyphs.
- Eight spirit palettes share three neutral poses, preserving all 24 existing `lod:<element>_<frame>` atlas IDs. Original palette words and the brightest-to-darkest ordering are retained for colour baking.
- Divine retains two 8x16 overlay IDs, the original additive blend, and the additional x=4 / x-scale=0.5 transform on base frames 1 and 2. The separate original pieces are included as evidence.

## Reference and refinement

Original 32x32 goods pictures were inspected. The original three spirit poses and two Divine overlay pieces were exported from local DRGN0 directory 4113. The source goods mapping and hashes are in `key-goods-original-mapping.json`.

Lavitz's first generated picture drifted toward a generic spiky-haired character. The selected picture was refined against the game's larger Lavitz portrait to recover his swept-back hair, stronger jaw and three-quarter pose. The first draft is not staged separately. Generation and refinement prompts are saved in `goods-spirits-generation-records.json`.

## Checks and limits

- All 21 masters decode as RGBA PNGs, exceed their 8x source-size requirements, contain visible artwork and have outer-edge alpha at most 4/255. Exact dimensions, alpha ranges and visible bounds are in `goods-spirits-validation.json`.
- All five pose cells contain isolated artwork inside their cell boundaries. Raw padding varies, so `spirits-pose-validation.json` and the manifest record generated and original bounds. Fit each pose into the corresponding original bounds when packing to prevent animation jitter.
- Asserted coverage of all 37 goods, all 21 original PNG atlas IDs, 24 unique spirit frame IDs, two overlay IDs and all referenced files. Verified original goods source hashes and recorded selected master hashes.
- Visually checked the new images, including the refined Lavitz picture, the three orb highlight poses and the two narrow overlays.
- Colour baking, exact-size packing, matte cleanup, live animation, additive blending, settings switching and GPU-memory checks remain pending. No Java tests ran because no game code changed.

## Progress

Step 1 approved. Step 2 exploration markers, standard inventory and key-goods/spirit artwork approved. Menu artwork is the current checkpoint. Battle icons and world-map artwork remain. Rendering integration and the Modern icons setting follow the artwork work.

Next within step 2: menu cursors, arrows and interface symbols, preserving their existing glyph identities and reduced-motion states.
