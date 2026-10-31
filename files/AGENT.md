# This directory is a shared-data overlay

Read `../AGENT.md` for setup and maintenance instructions.
Directories may be junctions and files may be hard links to canonical game data.
Treat linked content as read-only. Only declared patch targets and `patches/`
are private writable game data. Do not copy the whole canonical data here or
recursively delete through its junctions. Rerun `tools/share-game-data.ps1` after
changing the patch manifest, with the game stopped.

`.shared-game-data.json` records the canonical location, unpacker version and
private paths for runtime guards. It is local metadata and must remain ignored.
