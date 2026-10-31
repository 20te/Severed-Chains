param(
  [Parameter(Mandatory=$true)][string]$SharedFiles,
  [string]$Workspace = (Get-Location).Path
)
$ErrorActionPreference = 'Stop'
$workspacePath = [IO.Path]::GetFullPath($Workspace).TrimEnd('\')
$sharedPath = (Resolve-Path -LiteralPath $SharedFiles).Path.TrimEnd('\')
$filesPath = Join-Path $workspacePath 'files'
$stagePath = Join-Path $workspacePath 'build\shared-game-data-staging'
$backupPath = Join-Path $workspacePath 'build\shared-game-data-previous'
function Assert-LocalPath([string]$path) {
  $absolute = [IO.Path]::GetFullPath($path)
  if(-not $absolute.StartsWith($workspacePath + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw "Path is outside the worktree: $absolute"
  }
}
foreach($path in @($filesPath, $stagePath, $backupPath)) { Assert-LocalPath $path }
if($sharedPath.StartsWith($workspacePath + '\', [StringComparison]::OrdinalIgnoreCase)) {
  throw 'Shared data must live outside this worktree.'
}
if([IO.Path]::GetPathRoot($sharedPath) -ne [IO.Path]::GetPathRoot($workspacePath)) {
  throw 'The shared data and worktree must be on the same NTFS volume for hard links.'
}
if((Test-Path -LiteralPath $stagePath) -or (Test-Path -LiteralPath $backupPath)) {
  throw 'A previous staging/backup directory exists under build. Inspect it before retrying.'
}
if((Test-Path -LiteralPath $filesPath) -and ((Get-Item -LiteralPath $filesPath -Force).Attributes -band [IO.FileAttributes]::ReparsePoint)) {
  throw 'The files root must be an ordinary directory, not a junction.'
}
$unpackerPath = Join-Path $workspacePath 'src\main\java\legend\game\unpacker\Unpacker.java'
$versionMatch = [regex]::Match([IO.File]::ReadAllText($unpackerPath), 'private static final int VERSION = (\d+);')
if(-not $versionMatch.Success) { throw 'Cannot determine this branch''s unpacker version.' }
$version = [int]$versionMatch.Groups[1].Value
if([int]([IO.File]::ReadAllText((Join-Path $sharedPath 'version')).Trim()) -ne $version) {
  throw 'Unpack the game in the shared checkout first: unpacker versions differ.'
}
$private = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
$privateDirectories = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
function Add-PrivatePath([string]$relative) {
  if([IO.Path]::IsPathRooted($relative)) { throw "Script path must be relative: $relative" }
  $relative = $relative.Replace('\', '/')
  $resolved = [IO.Path]::GetFullPath((Join-Path $filesPath $relative))
  if(-not $resolved.StartsWith($filesPath + '\', [StringComparison]::OrdinalIgnoreCase)) { throw "Invalid script path: $relative" }
  [void]$private.Add($relative)
  $parent = [IO.Path]::GetDirectoryName($relative.Replace('/', '\'))
  while($parent) {
    [void]$privateDirectories.Add($parent.Replace('\', '/'))
    $parent = [IO.Path]::GetDirectoryName($parent)
  }
}
function Read-PatchList([string]$path) {
  if(Test-Path -LiteralPath $path) {
    foreach($record in @(Get-Content -LiteralPath $path | ConvertFrom-Csv -Header kind,source,patch,note)) {
      if($record.kind -in @('diff', 'replacement')) { Add-PrivatePath $record.source }
      elseif($record.kind) { Add-PrivatePath $record.kind }
    }
  }
}
$oldPrivate = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
$oldMetadata = Join-Path $filesPath '.shared-game-data.json'
if(Test-Path -LiteralPath $oldMetadata) {
  $old = Get-Content -LiteralPath $oldMetadata -Raw | ConvertFrom-Json
  if($old.unpackedVersion -ne $version) { throw 'An older-version private overlay exists. Migrate it explicitly or use a fresh worktree.' }
  foreach($relative in $old.privatePaths) { [void]$oldPrivate.Add($relative); Add-PrivatePath $relative }
}
foreach($path in @((Join-Path $workspacePath 'patches\scripts.csv'), (Join-Path $sharedPath 'patches\cache\scripts.csv'), (Join-Path $filesPath 'patches\cache\scripts.csv'))) {
  Read-PatchList $path
}
$sharedBackups = Join-Path $sharedPath 'patches\backups'
if(Test-Path -LiteralPath $sharedBackups) {
  foreach($file in Get-ChildItem -LiteralPath $sharedBackups -Recurse -File) {
    Add-PrivatePath $file.FullName.Substring($sharedBackups.Length + 1)
  }
}
$linkedDirectories = [Collections.Generic.List[object]]::new()
$linkedFiles = [Collections.Generic.List[object]]::new()
$stats = @{privateFiles=0;privateBytes=0L}
$null = New-Item -ItemType Directory -Path $stagePath
$null = New-Item -ItemType Directory -Path (Join-Path $stagePath 'patches')
function Connect-Directory([string]$source, [string]$destination, [string]$relative) {
  foreach($child in Get-ChildItem -LiteralPath $source -Force) {
    $name = if($relative) { $relative + '/' + $child.Name } else { $child.Name }
    if($name -eq 'patches' -or $name -eq '.shared-game-data.json' -or $name -eq '.gitignore' -or $name -eq 'AGENT.md') { continue }
    $target = Join-Path $destination $child.Name
    if($child.PSIsContainer) {
      if($privateDirectories.Contains($name)) {
        $null = New-Item -ItemType Directory -Path $target
        Connect-Directory $child.FullName $target $name
      } else {
        $null = New-Item -ItemType Junction -Path $target -Target $child.FullName
        $linkedDirectories.Add([pscustomobject]@{path=$target;target=$child.FullName})
      }
    } elseif($private.Contains($name)) {
      # Seed branch scripts from the pristine backup, never another branch's patched bytes.
      $seed = Join-Path $sharedBackups $name
      if($oldPrivate.Contains($name) -and (Test-Path -LiteralPath (Join-Path $filesPath $name))) { $seed = Join-Path $filesPath $name }
      elseif(-not (Test-Path -LiteralPath $seed)) { $seed = $child.FullName }
      Copy-Item -LiteralPath $seed -Destination $target
      $stats.privateFiles++; $stats.privateBytes += (Get-Item -LiteralPath $target).Length
    } elseif($name -eq 'version') {
      Copy-Item -LiteralPath $child.FullName -Destination $target
    } else {
      $null = New-Item -ItemType HardLink -Path $target -Target $child.FullName
      $linkedFiles.Add([pscustomobject]@{path=$target;target=$child.FullName})
    }
  }
}
Write-Output 'Connecting shared data and creating private script paths...'
Connect-Directory $sharedPath $stagePath ''
# Replacement scripts may not exist in the canonical unpacked data yet.
foreach($relative in $private) {
  $parent = [IO.Path]::GetDirectoryName((Join-Path $stagePath $relative))
  if(-not (Test-Path -LiteralPath $parent)) { $null = New-Item -ItemType Directory -Path $parent -Force }
  $target = Join-Path $stagePath $relative
  if(-not (Test-Path -LiteralPath $target)) {
    $seed = Join-Path $sharedBackups $relative
    if($oldPrivate.Contains($relative) -and (Test-Path -LiteralPath (Join-Path $filesPath $relative))) { $seed = Join-Path $filesPath $relative }
    if(Test-Path -LiteralPath $seed) {
      Copy-Item -LiteralPath $seed -Destination $target
      $stats.privateFiles++; $stats.privateBytes += (Get-Item -LiteralPath $target).Length
    }
  }
}
foreach($name in @('.gitignore', 'AGENT.md')) {
  $existing = Join-Path $filesPath $name
  if(Test-Path -LiteralPath $existing) { Copy-Item -LiteralPath $existing -Destination (Join-Path $stagePath $name) }
}
if(Test-Path -LiteralPath (Join-Path $filesPath 'patches')) {
  # Only existing private cache directories may be retained.
  $cache = Get-Item -LiteralPath (Join-Path $filesPath 'patches') -Force
  if($cache.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Existing patch cache is shared; refusing to copy it.' }
  function Assert-PrivateTree([string]$directory) {
    foreach($child in Get-ChildItem -LiteralPath $directory -Force) {
      if($child.LinkType) { throw "Existing patch cache contains a link: $($child.FullName)" }
      if($child.PSIsContainer) { Assert-PrivateTree $child.FullName }
    }
  }
  Assert-PrivateTree $cache.FullName
  foreach($child in Get-ChildItem -LiteralPath $cache.FullName -Force) {
    Copy-Item -LiteralPath $child.FullName -Destination (Join-Path $stagePath 'patches') -Recurse
  }
}
$metadata = [ordered]@{schema=1;sharedRoot=$sharedPath;unpackedVersion=$version;privatePaths=@($private | Sort-Object);linkedDirectories=$linkedDirectories.Count;linkedFiles=$linkedFiles.Count;privateFiles=$stats.privateFiles;privateBytes=$stats.privateBytes}
[IO.File]::WriteAllText((Join-Path $stagePath '.shared-game-data.json'), ($metadata | ConvertTo-Json -Depth 4), [Text.UTF8Encoding]::new($false))
foreach($link in $linkedDirectories) {
  $item = Get-Item -LiteralPath $link.path -Force
  if($item.LinkType -ne 'Junction' -or $item.Target[0] -ne $link.target) { throw "Junction verification failed: $($link.path)" }
}
foreach($link in $linkedFiles) {
  if((Get-Item -LiteralPath $link.path -Force).LinkType -ne 'HardLink') { throw "Hard-link verification failed: $($link.path)" }
}
foreach($relative in $private) {
  $path = Join-Path $stagePath $relative
  if(Test-Path -LiteralPath $path) {
    if((Get-Item -LiteralPath $path -Force).LinkType) { throw "Script is not private: $relative" }
  }
}
function Remove-LocalTree([string]$path) {
  Assert-LocalPath $path
  $attributes = [IO.File]::GetAttributes($path)
  $isDirectory = $attributes -band [IO.FileAttributes]::Directory
  if($isDirectory -and -not ($attributes -band [IO.FileAttributes]::ReparsePoint)) {
    foreach($child in [IO.Directory]::EnumerateFileSystemEntries($path)) { Remove-LocalTree $child }
    [IO.Directory]::Delete($path)
  } elseif($isDirectory) {
    # Delete the junction itself; never traverse into its shared target.
    [IO.Directory]::Delete($path)
  } else {
    [IO.File]::Delete($path)
  }
}
Write-Output 'Links verified; replacing the worktree data directory...'
if(Test-Path -LiteralPath $filesPath) { [IO.Directory]::Move($filesPath, $backupPath) }
try { [IO.Directory]::Move($stagePath, $filesPath) }
catch {
  if(-not (Test-Path -LiteralPath $filesPath) -and (Test-Path -LiteralPath $backupPath)) { [IO.Directory]::Move($backupPath, $filesPath) }
  throw
}
if(Test-Path -LiteralPath $backupPath) { Remove-LocalTree $backupPath }
Write-Output ($metadata | ConvertTo-Json -Compress -Depth 3)
