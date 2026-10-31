# Player-head pointer artwork checkpoint

The approved fantasy silhouette has a neutral silver/white master at `gfx/ui/modern/player-master.png`. The game can tint this one master cyan, yellow or red and narrow it horizontally for animation. This keeps exactly the same silhouette across encounter states. It is artwork only; no renderer or settings changes are included in this checkpoint.

## Original evidence

`player-original-evidence.png` is a native-resolution export of the original 12 poses in all three encounter palettes, read from local `SUBMAP/big_arrow.tim` and `SUBMAP/small_arrow.tim`. It is documentation, not runtime content. First row cyan, second yellow, third red.

The player ANM has twelve groups and thirteen sequence entries: 0 through 11 followed by 0. Each sequence time is 1. Sprite width is 16; visible horizontal widths are 15,13,11,9,7,5,3,5,7,9,11,13 pixels, centered at x=7. Encounter accumulator thresholds are 0xA00 and 0x1000. `player-master.json` records these source measurements and the three original PSX palettes.

## Artwork and verification

- Built-in image generation produced the neutral master from the approved reference; prompt saved alongside this document.
- Measured master size: 1278x1230. Both a sampled outside corner and the hollow interior centre have alpha 0. Visually inspected the downward outline and bevel.
- Initial cyan/yellow/red sheet experiments were rejected: dimensions were consistent, but alpha masks differed by 183713 pixels for yellow and 148681 for red. Their varying geometry and frame heights could cause jitter. Discarded sheets are not staged or used.
- Use the one master with shared geometry and tint rather than independent generative pose/colour textures. This is a refinement of the asset approach, avoiding colour-change jitter by construction. Runtime transformation is deferred to the integration step.
- Master resolution exceeds 8x requirements. Exact-size packing, edge/matte checks, colour tint verification, gameplay-size readability and live animation remain pending. The neutral master itself is not the final on-screen colour.

## Progress

Step 1 approved. Step 2 visual direction, dialogue candidate and player master approved. Door/alert artwork checkpoint is documented in door-alert-production.md. Remaining inventory/menu/battle/world-map artwork is pending. No game code changes or gameplay checks have occurred.
