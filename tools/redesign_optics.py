"""Rebuild 0.2 optics from the checked-in, credited Create Deco reference and textures.

Requires Pillow and nbtlib. No Create Deco installation is needed to build or play.
The source lantern and its three palette textures remain third-party assets.
"""
from pathlib import Path
import copy, json, re
from PIL import Image, ImageDraw
import nbtlib as n

ROOT = Path(__file__).resolve().parents[1]
R = ROOT / 'src/main/resources'
A = R / 'assets/alchemicallaser'
COLORS = {'red': 0xee6559, 'blue': 0x69bfe3, 'purple': 0xb895e5, 'white': 0xeee4ba,
          'growth': 0x86c65b, 'convergence': 0xf0b65d, 'splitting': 0x65d6d1,
          'power': 0xe77e47, 'eclipse': 0xa286dc}

def js(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n')

def cube(name, lo, hi, texture, uv=None):
    faces = {}
    for face in ['north', 'east', 'south', 'west', 'up', 'down']:
        faces[face] = {'texture': '#' + texture}
        faces[face]['uv'] = uv if uv is not None else [0,0,16,16]
    return {'name': name, 'from': lo, 'to': hi, 'faces': faces}

def ring(elements, name, y, outer, inner, height, tex, uv=None):
    lo, hi, il, ih = 8-outer/2, 8+outer/2, 8-inner/2, 8+inner/2
    for label, start, end in [('north', [lo,y,lo], [hi,y+height,il]),
                              ('south', [lo,y,ih], [hi,y+height,hi]),
                              ('west', [lo,y,il], [il,y+height,ih]),
                              ('east', [ih,y,il], [hi,y+height,ih])]:
        elements.append(cube(name+'_'+label, start, end, tex, uv))

# Invert and enlarge the actual Deco lantern: its mounting foot becomes the top
# of the hanging cage. Retain the source UVs so its industrial grille reads clearly.
reference = json.loads((ROOT/'tools/reference/industrial_lantern.json').read_text())
elements = []
for original in reference['elements']:
    e = copy.deepcopy(original)
    e['name'] = 'lantern_' + e['name']
    lo, hi = original['from'], original['to']
    e['from'] = [8+(lo[0]-8)*4/3, 12-hi[1], 8+(lo[2]-8)*4/3]
    e['to'] = [8+(hi[0]-8)*4/3, 12-lo[1], 8+(hi[2]-8)*4/3]
    # A 1/16 px shell avoids zero-thickness culling and preserves two-sided grilles.
    for i in range(3):
        if e['from'][i] == e['to'][i]:
            e['from'][i] -= 1/32; e['to'][i] += 1/32
    e['faces']['up'], e['faces']['down'] = e['faces']['down'], e['faces']['up']
    elements.append(e)

# Four copper/brass cage ties, distinct horizontal focusing bands and a dark nozzle.
for x in [3.75, 11.5]:
    for z in [3.75, 11.5]:
        elements.append(cube('cage_stay', [x,4,z], [x+.75,12,z+.75], 'industrial', [10,6,11,8]))
ring(elements, 'upper_optical_band', 9.9, 8.75, 6.5, .65, 'brass', [10,6,16,8])
ring(elements, 'lower_optical_band', 3.5, 9, 5.5, .75, 'brass', [10,6,16,8])
ring(elements, 'nozzle_collar', 2.5, 7.5, 4.5, 1, 'industrial', [10,6,16,8])
ring(elements, 'nozzle_lip', 1.25, 6.75, 4.5, 1.25, 'brass', [10,6,16,8])
ring(elements, 'lens_seat', .75, 6.5, 4.5, .5, 'industrial', [10,6,16,8])
# Keep the lantern's geometry intact and hang it six pixels lower, leaving
# Create's original top rail/roller/cog contact dimensions unchanged.
for e in elements:
    e['from'][1] -= 6
    e['to'][1] -= 6
ring(elements, 'controller_lantern_coupling', 6, 8, 4, 2, 'brass', [10,6,16,8])
# ClientSetup adds Create's baked top rails at model bake time. Keep those
# native assets in the Create dependency instead of copying them into this mod.
model = {'credit': 'Create Deco lantern adaptation and Create rotation speed controller top geometry. See THIRD_PARTY_NOTICES.md.',
         'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout_mipped',
         'textures': {'cage': 'alchemicallaser:block/deco_industrial_cage',
                      'industrial': 'alchemicallaser:block/deco_industrial_cage',
                      'brass': 'alchemicallaser:block/deco_brass_cage',
                      'lamp': 'alchemicallaser:block/deco_lamp_core',

                      'particle': 'alchemicallaser:block/deco_industrial_cage'},
         'elements': elements,
         'display': {'gui': {'rotation':[30,225,0], 'translation':[0,2,0], 'scale':[.75,.75,.75]},
                     'ground': {'translation':[0,3,0], 'scale':[.4,.4,.4]},
                     'fixed': {'rotation':[0,180,0], 'scale':[.7,.7,.7]}}}
js(A/'models/block/laser_emitter.json', model)

# 16px inventory optics: restrained brass edges, hand-placed stepped facets and a
# single light source. Distinct glyphs keep the specialist lenses recognizable.
outline = [(5,1),(10,1),(14,5),(14,10),(10,14),(5,14),(1,10),(1,5)]
rim = [(5,2),(10,2),(13,5),(13,10),(10,13),(5,13),(2,10),(2,5)]
glass = [(6,4),(9,4),(11,6),(11,9),(9,11),(6,11),(4,9),(4,6)]
for name, rgb in COLORS.items():
    c = (rgb>>16, rgb>>8&255, rgb&255)
    dark = tuple(round(v*.48) for v in c)
    shade = tuple(round(v*.73) for v in c)
    bright = tuple(round(v*.55+255*.45) for v in c)
    im = Image.new('RGBA',(16,16)); d = ImageDraw.Draw(im)
    d.polygon(outline, fill='#302c2b'); d.polygon(rim, fill='#8c633c')
    d.line([(2,8),(2,5),(5,2),(10,2),(12,4)], fill='#e4c484', width=1)
    d.line([(3,10),(5,12),(10,12),(12,10)], fill='#b68c50')
    d.polygon(glass, fill=dark)
    d.polygon([(6,4),(9,4),(11,6),(8,8),(4,8),(4,6)], fill=c)
    d.polygon([(4,8),(8,8),(11,6),(11,9),(9,11),(6,11)], fill=shade)
    d.line([(5,6),(6,5),(8,5)], fill=bright)
    d.point((5,6),fill='#fff1d3')
    # Specialist optical etchings, without covering the readable glass silhouette.
    if name == 'growth':
        d.line([(7,10),(7,8),(9,6)],fill=bright); d.line([(7,8),(6,7)],fill=bright)
    elif name == 'convergence':
        for pts in [[(5,8),(7,8)],[(10,6),(8,8)],[(10,10),(8,8)]]: d.line(pts,fill=bright)
        d.point((8,8),fill='#fff1d3')
    elif name == 'splitting':
        d.line([(5,8),(8,8),(10,6)],fill=bright); d.line([(8,8),(10,10)],fill=bright)
    elif name == 'power':
        d.line([(9,5),(7,8),(9,8),(7,11)],fill='#ffe8b4')
    elif name == 'eclipse':
        d.ellipse((5,5,10,10),fill='#e3c98e');d.ellipse((6,5,10,9),fill='#352e4b')
    # Small opposing mounting notches give every optic the same mechanical fit.
    d.point((1,7), fill='#e4c484');d.point((14,8),fill='#b68c50')
    im.save(A/f'textures/item/{name}_lens.png')
    # A few merged strips follow the pixel silhouette, giving the cartridge real
    # thickness without emitting hundreds of individual pixel cubes. GUI projects
    # the same hand-placed art face-on; the mounted cartridge lies horizontal.
    strips=[]
    for z in range(16):
        xs=[x for x in range(16) if im.getpixel((x,z))[3]>0]
        if not xs: continue
        bounds=(min(xs),max(xs)+1)
        if strips and strips[-1][0:2]==list(bounds) and strips[-1][3]==z:
            strips[-1][3]=z+1
        else: strips.append([*bounds,z,z+1])
    lens_elements=[]
    for x,X,z,Z in strips:
        e=cube('optical_cartridge',[x,7.25,z],[X,8.75,Z],'rim',[10,6,16,8])
        for face in ['up','down']:
            e['faces'][face]={'texture':'#glass','uv':[x,z,X,Z]}
        lens_elements.append(e)
    js(A/f'models/item/{name}_lens.json', {'parent':'minecraft:block/block', 'gui_light':'front',
       'render_type':'minecraft:cutout_mipped',
       'textures':{'glass':f'alchemicallaser:item/{name}_lens',
                   'rim':'alchemicallaser:block/deco_brass_cage',
                   'particle':f'alchemicallaser:item/{name}_lens'},
       'elements':lens_elements,
       'display':{'gui':{'rotation':[90,0,0],'scale':[1,1,1]},
                  'fixed':{'rotation':[0,0,0],'scale':[1,1,1]},
                  'ground':{'translation':[0,1.5,0],'scale':[.5,.5,.5]},
                  'firstperson_righthand':{'rotation':[25,-20,0],'scale':[.65,.65,.65]},
                  'thirdperson_righthand':{'rotation':[60,0,0],'translation':[0,2,0],'scale':[.5,.5,.5]}}})

values = {f'alchemicallaser:{key}_lens': {'color':rgb,'pitch':pitch} for (key,rgb),pitch in
          zip(COLORS.items(),[.8,1.2,.95,1.05,1.15,.9,1.3,.7,.6])}
js(R/'data/alchemicallaser/data_maps/item/laser_lenses.json', {'values':values})

lang = json.loads((A/'lang/en_us.json').read_text())
for name in COLORS:
    lang[f'item.alchemicallaser.{name}_lens'] = name.title()+ (' Optical Lens' if name in ['red','blue','purple','white'] else ' Lens')
scenes = (ROOT/'src/main/java/nl/mikaaah/alchemicallaser/ponder/LaserScenes.java').read_text()
for method in re.split(r'public static void ',scenes)[1:]:
    title = re.search(r'base\(b,u,"([^"]+)","([^"]+)"\)',method)
    if not title: continue
    key, heading = title.groups(); prefix = 'alchemicallaser.ponder.'+key+'.'
    lang[prefix+'header'] = heading
    for i,text in enumerate(re.findall(r'text\(s,u,"([^"]+)"',method),1): lang[prefix+'text_'+str(i)]=text
js(A/'lang/en_us.json', lang)

# New lenses are reusable recipe selectors, with balanced, reversible vanilla
# demonstrations rather than hardcoded world growth, extra beams or damage modes.
for name, ingredient in [('growth','minecraft:moss_block'),('convergence','minecraft:amethyst_shard'),
                         ('splitting','minecraft:prismarine_shard'),('power','create:electron_tube'),
                         ('eclipse','minecraft:echo_shard')]:
    key = {'B':{'item':'create:brass_nugget'},'G':{'item':'minecraft:glass'},'C':{'item':ingredient}}
    if name == 'eclipse':
        key['G']={'item':'minecraft:tinted_glass'}
        key['B']={'item':'create:brass_ingot'}
    js(R/f'data/alchemicallaser/recipe/{name}_lens.json', {'type':'minecraft:crafting_shaped',
       'pattern':[' B ','GCG',' B '], 'key':key, 'result':{'id':f'alchemicallaser:{name}_lens','count':1}})
examples = [('growth_wheat','wheat_seeds',1,'wheat',1,'growth',160,16),
            ('convergence_amethyst','amethyst_shard',4,'amethyst_block',1,'convergence',160,32),
            ('splitting_amethyst','amethyst_block',1,'amethyst_shard',4,'splitting',160,32),
            ('power_obsidian','obsidian',1,'crying_obsidian',1,'power',240,64),
            ('eclipse_sculk','sculk',1,'echo_shard',1,'eclipse',400,128)]
for name,src,count,out,amount,lens,duration,rpm in examples:
    js(R/f'data/alchemicallaser/recipe/{name}.json',{'type':'alchemicallaser:laser_processing',
       'ingredient':{'item':'minecraft:'+src}, 'input_count':count, 'lens':f'alchemicallaser:{lens}_lens',
       'result':{'id':'minecraft:'+out,'count':amount}, 'duration':duration,'minimum_rpm':rpm})

# Replace the tutorial drive above each emitter with a horizontal large cog.
p = A/'ponder/laser_workshop.nbt'; structure = n.load(p)
for b in structure['blocks']:
    state = structure['palette'][int(b['state'])]
    if str(state['Name']) in ['create:shaft','create:large_cogwheel']:
        state['Name']=n.String('create:large_cogwheel')
        state['Properties']['axis'] = n.String('x')
        b['pos'][1] = n.Int(4); b['pos'][2] = n.Int(3)
        if 'nbt' in b: b['nbt']['id']=n.String('create:large_cogwheel')
structure.save(p)
print('Rebuilt controller-top lantern laser, nine optics and overhead large-cog Ponder drive.')
