$ErrorActionPreference = 'Stop'
$toolsPath = if($PSScriptRoot) { $PSScriptRoot } else { (Resolve-Path tools).Path }
$setup = [scriptblock]::Create((Get-Content -LiteralPath (Join-Path $toolsPath 'share-game-data.ps1') -Raw))
$fixture = Join-Path (Split-Path $toolsPath) ('build\shared-data-test-' + [guid]::NewGuid())
$shared = Join-Path $fixture 'canonical'
$worktree = Join-Path $fixture 'worktree'
foreach($path in @("$shared\SECT", "$shared\textures", "$shared\patches\backups\SECT", "$worktree\patches", "$worktree\files", "$worktree\src\main\java\legend\game\unpacker")) {
  $null = New-Item -ItemType Directory -Path $path -Force
}
[IO.File]::WriteAllText("$worktree\src\main\java\legend\game\unpacker\Unpacker.java", 'private static final int VERSION = 5;')
[IO.File]::WriteAllText("$shared\version", '5')
[IO.File]::WriteAllText("$shared\SECT\script", 'canonical patched script')
[IO.File]::WriteAllText("$shared\patches\backups\SECT\script", 'pristine script')
[IO.File]::WriteAllText("$shared\SECT\asset", 'shared asset')
[IO.File]::WriteAllText("$shared\textures\image", 'shared texture')
[IO.File]::WriteAllText("$worktree\patches\scripts.csv", 'diff,SECT/script,scripts/test.diff')
[IO.File]::WriteAllText("$worktree\files\old-duplicate", 'disposable duplicate')
& $setup -SharedFiles $shared -Workspace $worktree
if((Get-Item "$worktree\files\textures").LinkType -ne 'Junction') { throw 'Missing directory junction' }
if((Get-Item "$worktree\files\SECT\asset").LinkType -ne 'HardLink') { throw 'Missing asset hard link' }
if((Get-Item "$worktree\files\SECT\script").LinkType) { throw 'Script is linked' }
if([IO.File]::ReadAllText("$worktree\files\SECT\script") -ne 'pristine script') { throw 'Script did not use pristine backup' }
[IO.File]::WriteAllText("$worktree\files\SECT\script", 'worktree patched script')
$null = New-Item -ItemType Directory -Path "$worktree\files\patches\cache"
[IO.File]::WriteAllText("$worktree\files\patches\cache\sentinel", 'private cache')
& $setup -SharedFiles $shared -Workspace $worktree
if([IO.File]::ReadAllText("$worktree\files\SECT\script") -ne 'worktree patched script') { throw 'Refresh lost private script' }
if([IO.File]::ReadAllText("$worktree\files\patches\cache\sentinel") -ne 'private cache') { throw 'Refresh lost private cache' }
if([IO.File]::ReadAllText("$shared\SECT\script") -ne 'canonical patched script') { throw 'Canonical script changed' }
if([IO.File]::ReadAllText("$shared\textures\image") -ne 'shared texture') { throw 'Refresh deleted junction target' }
if([IO.File]::ReadAllText("$shared\SECT\asset") -ne 'shared asset') { throw 'Refresh deleted hard-link target' }
Write-Output 'PASS: junctions, hard links, pristine copies, refresh, cache retention and canonical preservation.'
