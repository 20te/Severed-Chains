# Step 5: gameplay verification

Status: testing stopped at the user's request. Steps 1–4, the shared-data setup,
and the battle cursor/equipment spacing corrections are approved. The remaining
checks below are deferred; Step 5 is not complete.

## Checked on 2026-10-31

Launched this worktree's packaged JAR with Java 25, using the canonical game
data overlay and the private `saves/icon_check` copy. No ISO copy was required.
The script patcher completed and the Seles save loaded successfully.

- The blue character marker and green scene marker render with the modern
  artwork. The blue marker remains attached above Dart while animating.
- Turning Modern icons off restored the original solid, visibly pixelated
  marker. Turning it back on restored the outlined modern marker without
  restarting the game or loading another scene.
- Modern icons appears immediately below Icon Set in User Interface. The
  existing Enhanced preference remained selected through both toggles.
- The existing options screen immediately redrew its checkbox and arrow
  controls on each toggle.
- Inventory rendered Healing Potion, Burn Out, Broad Sword, Bandana, Leather
  Armor, Leather Boots and Bracelet icons with visible detail and consistent
  alignment in their rows. No obvious clipping or neighbouring-atlas bleed was
  visible at the tested window size (3× game scale).
- The runtime log contained no ERROR, Exception or Failed entries at this
  checkpoint.
- After the user opened the Seles "Already dead...." dialogue, the modern red
  triangle rendered centered beneath the textbox, with clean edges and no
  obvious clipping or atlas bleed at the tested size. Confirming the dialogue
  removed both the textbox and red marker normally. Still snapshots did not
  establish smoothness throughout the complete animation cycle.
- The user supplied the early Seles battle against two Knights of Sandora.
  The battle command row rendered detailed sword, shield, chest, blue circular
  and red crossed icons with consistent placement and no obvious atlas bleed.
  Separate snapshots showed different sword rotation poses.
- Selecting Attack showed the modern blue target arrow. Right moved it from
  one Knight to the other; cancelling removed the arrow and restored commands.
  No attack was confirmed and no battle item was consumed during these checks.

Automated checks previously passed: two shared-data guard tests, three modern
setting tests and one packed-atlas integrity/coverage test. The sharing fixture
also passed initial setup, refresh and canonical-data preservation checks.

## Correction: shared battle list cursor

The user found that the blue item-selection cursor still used retail artwork,
including Change Addition. This is distinct from the enemy-target arrow checked
above. ItemListMenu, AdditionListMenu and SpellListMenu inherit ListMenu's drawing
path; that shared path had been omitted from modern integration.

ListMenu now selects eight modern `battle/list-cursor` frames with its existing
clock, transform, 15x8 geometry and additive blend. Modern icons Off retains the
original mesh. The frames reuse the approved triangle master, rotated right and
baked with the original cyan palette and per-frame visible bounds decoded from
SECT/DRGN0.BIN/4113/0 (recorded in battle-list-cursor-original.json).

The atlas now contains 391 entries and 29 aliases. Compilation and all four icon
tests passed. The game was rebuilt and restarted with both corrections, and the
user confirmed the result was good. The battle list cursor fix is user-approved;
this does not establish separate coverage of every spell or character fixture.

## Correction: compact equipment panel

The user also identified equipment artwork crossing row boundaries in the
compact equipped-items panel, while the Inventory screen remained correct.
`renderCharacterEquipment`, shared by Equipment and Status, spaces rows 14px
apart but was drawing the modern artwork across its full 16px slot. Its icons
now carry a 2px inset, applied only when choosing modern drawing metrics. This
centers 12px artwork inside the slot, leaving a 2px gap between adjacent rows.
The Inventory screen keeps its existing dimensions. The inset is resolved at
draw time, so switching Modern icons off restores legacy metrics even for
already allocated equipment icons. After the rebuild and restart, the user
approved the equipment spacing correction together with the battle cursor fix.

## Still open

The desktop automation tool can navigate menus, but its brief movement-key
presses did not move Dart. The user supplied the dialogue scene for inspection.
The test game is closed. Intended changes are staged; no commit was requested.
Command rotation uses held inputs (unlike enemy selection's pressed inputs),
so the tool's brief key presses also prevented navigating to the battle item
list. The user subsequently approved the shared list cursor correction.
Addition timing prompts and later character/transformation commands remain
unverified.

Full dialogue animation, remaining battle coverage, world-map icons, reduced motion, Retail preference switching,
saved campaign-file round trips and unknown-mod gameplay fixtures have not yet
been visually verified. This step is not complete and the full visual result
must not be described as approved. No commit has been created.
