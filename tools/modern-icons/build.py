"""Describe the approved icon crops, then bake them with the JDK's image library.

Run from the repository root: python tools/modern-icons/build.py
Requires Java 25; no Python imaging package or game files are needed.
"""
import json
import subprocess
from pathlib import Path

ROOT = Path('gfx/ui/modern')
OUT = ROOT / 'packed'
OUT.mkdir(exist_ok=True)


def read(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))


recipes = []
aliases = {}


def add(key, file, size, rect=None, fit=None, rotation=0, palette=None, pixels=None):
    assert key not in {r[0] for r in recipes}, key
    size = list(size)
    fit = fit or [0, 0, *size]
    recipes.append([key, str(file), *(rect or [-1, -1, -1, -1]),
                    *size, *fit, rotation,
                    ','.join(str(c & 0x7fff) for c in (palette or [])),
                    *(pixels or [size[0] * 8, size[1] * 8])])


def registry(id):
    return 'registry/' + id.replace(':', '/')


inventory = read(ROOT / 'inventory.json')
for a in inventory['assets']:
    if a.get('file'):
        add('item/' + str(a['id']), ROOT / a['file'], a['logicalSize'])

goods = read(ROOT / 'goods-spirits.json')
for a in goods['goods']:
    palette = None
    if 'paletteBake' in a:
        r, g, b = a['paletteBake']['tintRgb8']
        palette = [int(r * t / 255) >> 3 | (int(g * t / 255) >> 3) << 5 |
                   (int(b * t / 255) >> 3) << 10 for t in range(16, 256, 16)] + [32767]
    add(registry(a['atlasId']), ROOT / a['file'], a['logicalSize'],
        palette=palette, pixels=a['packingTarget'])
for source, dest in goods['modernAliases'].items():
    aliases[registry(source)] = registry(dest)
for source, dest in goods['standardItemOverrides'].items():
    aliases[registry(source)] = 'item/' + str(dest['id'])
spirits = goods['spirits']
for a in spirits['atlasFrames']:
    p = spirits['poses'][a['pose']]
    add(registry(a['atlasId']), ROOT / a['file'], a['logicalSize'], p['sourceRect'],
        p['originalVisibleBounds'], palette=spirits['states'][a['paletteIndex']]['palettePSX15'])
for state in spirits['states']:
    aliases['menu/' + str(state['inventoryGlyph'])] = registry('lod:' + state['element'] + '_0')
    aliases[registry(state['goodId'])] = registry('lod:' + state['element'] + '_0')
for p in goods['divineOverlays']['poses']:
    add(registry(p['atlasId']), ROOT / goods['divineOverlays']['file'], p['logicalSize'],
        p['sourceRect'], p['originalVisibleBounds'])

player = read(ROOT / 'player-master.json')
player_bounds = read('docs/modern-icons/player-original-bounds.json')
for frame in player['frames']:
    left = frame['originalVisibleLeft']
    for state in player['encounterStates']:
        add(f"player/{frame['index']}/{state['index']}", ROOT / player['image'], [16, 16],
            fit=player_bounds[frame['index']], palette=state['originalPalettePSX15'])
door = read(ROOT / 'door-master.json')
# Door animation reuses UV cells: identify by UV, rather than sequence position.
for p in {p['u']: p for p in door['spriteGroups']}.values():
    for state, palette in enumerate(door['originalPalettesPSX15']):
        add(f"door/{p['u']}/{state}", ROOT / door['image'], [8, 16],
            fit=p['visibleBounds'], palette=palette)
add('alert', ROOT / 'alert.png', [24, 24])
dialogue = read(ROOT / 'dialogue-triangle.frames.json')
dialogue_bounds = read('docs/modern-icons/dialogue-original-bounds.json')
for p in dialogue['frames']:
    add('dialogue/' + str(p['index']), ROOT / dialogue['image'], [16, 14],
        p['sourceRect'], dialogue_bounds[p['index']])

menu = read(ROOT / 'menu.json')
menu_poses = {(Path(p['file']).stem, p['index']): p for p in menu['poseCells']}
gold = [132, 198, 366, 594, 792, 1023, 32767]
for a in menu['glyphs']:
    r = a['recipe']
    file = r['master']
    rect = None
    if 'pose' in r:
        if file.endswith('dialogue-triangle.png'):
            rect = dialogue['frames'][r['pose']]['sourceRect']
        else:
            rect = menu_poses[(Path(file).stem, r['pose'])]['sourceRect']
    palette = gold if 'palette' in r else None
    add('menu/' + str(a['glyph']), file, a['logicalSize'], rect,
        a['originalVisibleBounds'], r.get('rotationClockwise', 0), palette)
    if 'palette' in r:
        # Retail stat comparisons override the CLUT with red/green.
        for clut, colour in [(0x7c2b, [4, 8, 16, 31, 32767]), (0x7c6b, [128, 512, 992, 32767])]:
            add(f"menu/{a['glyph']}/{clut}", file, a['logicalSize'], rect,
                a['originalVisibleBounds'], r.get('rotationClockwise', 0), colour)
for state in ['unchecked', 'checked']:
    add('checkbox/' + state, menu['checkbox'][state]['master'], [14, 14])

battle = read(ROOT / 'battle.json')
# Battle lists share a smaller right-facing cursor, separate from enemy targeting.
list_cursor = read('docs/modern-icons/battle-list-cursor-original.json')
for i, bounds in enumerate(list_cursor['visibleBounds']):
    add('battle/list-cursor/' + str(i), ROOT / dialogue['image'], list_cursor['logicalSize'],
        dialogue['frames'][0]['sourceRect'], fit=bounds, rotation=270,
        palette=[c for c in list_cursor['palettePSX15'] if c & 0x7fff])
battle_poses = {(p['file'], p['index']): p for p in battle['poseCells']}
for a in battle['commands']:
    if a['slot'] == 1:
        for i in range(3):
            aliases[f"battle/command/1/{i}"] = registry(f'lod:fire_{i}')
        continue
    for p in a['originalPoses']:
        cell = battle_poses[(Path(a['recipe']['master']).name, p['pose'])]
        add(f"battle/command/{a['slot']}/{p['pose']}", a['recipe']['master'],
            p['uv'][2:4], cell['sourceRect'], p['visibleBounds'])
for a in battle['elements']:
    add('element/' + a['registryId'].replace(':', '/'), a['master'], a['logicalSize'])
for i, palette in enumerate([player['encounterStates'][j]['originalPalettePSX15'] for j in [0, 1, 2]]):
    if i == 0:
        palette = [4096, 16384, 31744, 32767]  # Battle's healthy target is blue, not cyan.
    add('battle/target/' + str(i), battle['target']['master'], [16, 24],
        fit=[0, 0, 16, 23], palette=palette)
for name, key, size in [('disabledCross', 'disabled', [16, 16]),
                        ('timingStar', 'star', [16, 16]), ('timingArrow', 'timing-arrow', [8, 24])]:
    a = battle[name]
    add('battle/' + key, a['master'], size, fit=a['original']['visibleBounds'],
        palette=gold if name == 'timingArrow' else None)
for a in battle['additionalCommands']:
    for i in range(a.get('frameCount', 1)):
        p = battle_poses.get((Path(a['master']).name, i))
        add('battle/addition/' + str(i) if 'frameCount' in a else 'battle/detransform',
            a['master'], a['logicalSize'], p['sourceRect'] if p else None)
for a in battle['prompts']:
    r = a['recipe']
    cell = battle_poses.get((Path(r['master']).name, r.get('pose', 0)))
    add('battle/prompt/' + str(a['index']), r['master'], a['original']['uv'][2:4],
        cell['sourceRect'] if cell else None, a['original']['visibleBounds'],
        palette=gold if a['index'] == 36 else [4096, 16384, 31744, 32767] if a['index'] in (18, 19) else None)

world = read(ROOT / 'world.json')
for group, key in [('arrows', 'arrow'), ('destinationMarkers', 'destination'),
                   ('coolonPrompt', 'coolon'), ('queenFuryPrompt', 'door')]:
    a = world[group]
    for p in a['frames']:
        original = p['original']
        size = original.get('logicalSize', original['uv'][2:4])
        size = list(map(abs, size))
        add(f"world/{key}/{p['index']}", a['master'], size, p['sourceRect'], original['visibleBounds'])
for a in world['zoomPieces']:
    if a['index'] >= 5:
        continue
    r = a['recipe']
    p = next(p for p in world['poseCells'] if p['file'] == Path(r['master']).name and p['index'] == r['pose'])
    add('world/zoom/' + str(a['index']), r['master'], [32, 16], p['sourceRect'], a['original']['visibleBounds'])

# Every target is exact8x, except original32px goods, which intentionally retain256px detail.
assert len(recipes) > 250
assert all(Path(r[1]).is_file() for r in recipes)
assert all(r[-2] >= r[6] * 8 and r[-1] >= r[7] * 8 for r in recipes)
assert ({r[0] for r in recipes} & aliases.keys()) <= {registry(k) for k in goods['standardItemOverrides']}
for alias, target in aliases.items():
    seen = {alias}
    while target in aliases:
        assert target not in seen, (alias, target)
        seen.add(target)
        target = aliases[target]
    assert target in {r[0] for r in recipes}, (alias, target)
plan = OUT / 'recipes.tsv'
recipes.sort(key=lambda r: (-r[-1], -r[-2], r[0]))
plan.write_text('\n'.join('\t'.join(map(str, r)) for r in recipes) + '\n', encoding='utf-8')
(OUT / 'aliases.json').write_text(json.dumps(aliases, indent=2) + '\n', encoding='utf-8')
subprocess.run(['java', 'tools/modern-icons/Pack.java', str(plan)], check=True)
print(f'Packed {len(recipes)} icons and {len(aliases)} aliases.')
