"""Fail on missing owned assets, malformed recipes, invalid structures or missing tutorial storyboards."""
from pathlib import Path
import json,re,sys
import nbtlib
root=Path(__file__).resolve().parents[1];r=root/'src/main/resources';a=r/'assets/alchemicallaser'
for p in r.rglob('*.json'):json.loads(p.read_text())
for p in (a/'models').rglob('*.json'):
    model=json.loads(p.read_text())
    for texture in model.get('textures',{}).values():
        if texture.startswith('alchemicallaser:'):
            assert (a/'textures'/f'{texture.split(":",1)[1]}.png').is_file(),(p,texture)
    for element in model.get('elements',[]):
        assert all(lo<hi for lo,hi in zip(element['from'],element['to'])),p
for name,entry in json.loads((a/'sounds.json').read_text()).items():
    assert (a/'sounds'/f'{name}.ogg').stat().st_size>1000,name
for p in r.rglob('*.nbt'):
    n=nbtlib.load(p);assert 'size' in n and 'palette' in n and 'blocks' in n,p
    for b in n['blocks']:assert 0<=b['state']<len(n['palette']),p
for p in (r/'data/alchemicallaser/recipe').glob('*.json'):
    recipe=json.loads(p.read_text())
    if recipe['type']=='alchemicallaser:laser_processing':
        assert recipe['lens'].startswith('alchemicallaser:')
        assert recipe['duration']>0
plugin=(root/'src/main/java/nl/mikaaah/alchemicallaser/ponder/LaserPonderPlugin.java').read_text()
assert plugin.count('.addStoryBoard(')==6
for name in re.findall(r'addStoryBoard\("([^"]+)"',plugin):assert (a/'ponder'/f'{name}.nbt').is_file()
print('Resource validation passed: JSON, owned textures, models, seven OGG assets, structures, recipes and six storyboard registrations.')

lang=json.loads((a/'lang/en_us.json').read_text())
for file in (root/'src/main/java').rglob('*.java'):
    for key in re.findall(r'Component.translatable\("([^"\n]+)"',file.read_text()):
        if not key.endswith('.') and key.startswith(('goggles.alchemicallaser.','recipe.alchemicallaser.','itemGroup.')):assert key in lang,(file,key)
print('Static translation references passed.')
