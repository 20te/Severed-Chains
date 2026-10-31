# Shared game data in worktrees

The canonical unpacked game data for this machine lives at
`C:\Users\20te\workspaces\Severed-Chains\files`.
Do not copy that entire directory into worktrees.

Run from a stopped worktree, using Windows PowerShell on the same NTFS volume:

```powershell
rtk proxy powershell -NoProfile -Command '& ([scriptblock]::Create((Get-Content -Raw tools/share-game-data.ps1))) -SharedFiles C:\Users\20te\workspaces\Severed-Chains\files'
```

The setup builds and verifies a replacement `files` directory before swapping it
in. Unchanged directories are junctions; unchanged files alongside private scripts
are hard links. They use the canonical bytes without duplicating the data.
Treat all linked data as read-only: editing a hard link edits the canonical file.

Patched scripts are ordinary private copies, seeded from the canonical pristine
patch backups when available. `files/patches` (cache and backups), metadata, and
the version marker stay private. The setup considers current and cached patch
lists and existing backups, including patches removed on this branch. Running
setup again preserves this worktree's private scripts and patch cache.

After changing `patches/scripts.csv`, rerun setup before launching. The runtime
refuses undeclared or shared patch targets. Shared mode skips ISO unpacking and
refuses incompatible unpacker versions. To upgrade the unpacked format, unpack
in the canonical checkout first, then create a fresh compatible worktree overlay;
an overlay from an older format is deliberately rejected.

Saves, configuration, mods, build output and logs stay local to each checkout.
ISOs remain in the canonical checkout; shared worktrees do not need their own.
Do not delete or move the canonical data while linked worktrees use it. Stop all
affected game instances before replacing canonical data. Do not recursively
delete through junctions; the setup script removes links without traversing them.

Commit the setup, guards and these instructions. Never commit extracted game
data, machine-specific `.shared-game-data.json`, saves or patch caches.
