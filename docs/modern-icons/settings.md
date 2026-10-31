# Step4: Modern icons setting and compatibility

Modern icons is a campaign-scoped checkbox in User Interface, immediately after Icon Set. It defaults On for new and existing campaigns that do not contain the new value. English and Polish labels/help explain that Off restores the current Icon Set preference. The existing Retail/Enhanced setting and its item remapping are unchanged.

Every modern draw selection reads the active ConfigCollection. There is no separate enabled flag or change callback to become stale after campaign loading, copying, resetting or detached preset editing. Inactive/detached configuration edits do not change the running campaign's icons.

## Switching and caches

Menus, markers, dialogue, battle UI, checkbox controls, world-map overlays and spirit transformations already select their draw path each frame. Goods now keep their original atlas metrics and registry identities in the cached UiType. The shared menu renderer selects the modern equivalent when drawing, so an existing goods renderable can switch back without rebuilding the menu or its mesh.

Known modern-only goods aliases have entries with the same legacy index-zero fallback used before modern artwork existed. Unknown mod-provided icons keep their original path. Goods index lookup now primes the lazy type before reading its index, preventing the first newly constructed goods icon from incorrectly taking index zero merely because the map was not loaded yet.

Switching changes no animation clock, position, reduced-motion setting, input binding, inventory data or registry identity. It does not allocate or rebuild textures. The single modern atlas remains resident at32MiB even while Off, allowing immediate switching without repeated allocations; normal atlas reloads still replace its texture and mesh.

## Verification

Java25 compilation passed. `gradlew modernIconTest --offline` ran four tests with no failures or skips:

- Packed-atlas integrity and coverage.
- Default On, campaign/User Interface metadata, editable/help flags and Boolean serialization round trips.
- Off/On changes preserve both Retail and Enhanced preferences.
- Campaign-value copying and resetting work without change callbacks.

The setting is registered alongside Icon Set, and the options sorter keeps their rows together while retaining the normal localized alphabetical sort for other settings. All drawing consumers were checked for the shared active-setting lookup. No original texture was replaced, and nothing was committed.

These checks do not boot the game or prove the visual result of switching. Live row placement, saved campaign-file loading, redraws of already-open menus, unknown-mod fixtures and actual GPU behaviour remain for step5's gameplay comparison.

## Progress

Steps1-4 approved. Step5 is in progress: final checks and in-game comparisons across exploration, dialogue, inventory, battle and world-map icons, including On/Off, existing icon-set choices and reduced motion. See gameplay-verification.md for completed checks and remaining coverage. Do not treat the task as visually complete before those checks.
