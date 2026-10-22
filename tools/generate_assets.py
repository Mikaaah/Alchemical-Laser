"""Rebuild the original pixel-art, procedural audio and Ponder scene assets. Requires pillow, numpy, nbtlib and ffmpeg."""
from pathlib import Path
import json, math, random, subprocess, tempfile, wave, re
import numpy as np
from PIL import Image, ImageDraw
import nbtlib as n
R=Path(__file__).resolve().parents[1]/'src/main/resources'
A=R/'assets/alchemicallaser'
def js(path,data):
    path.parent.mkdir(parents=True,exist_ok=True); path.write_text(json.dumps(data,indent=2)+'\n')
def img(path,im):
    path.parent.mkdir(parents=True,exist_ok=True); im.save(path)
colors={'red':(255,83,80),'blue':(72,202,255),'purple':(188,108,255),'white':(255,243,197)}
# Original 16px Create-inspired metal and brass housing tiles, kept separate from Create assets.
for name,base in {'housing':(82,91,91),'brass':(181,137,70),'vent':(50,58,58),'trim':(111,123,119)}.items():
    im=Image.new('RGBA',(16,16),(*base,255)); d=ImageDraw.Draw(im); rng=random.Random(79)
    for y in range(16):
        for x in range(16):
            k=rng.choice([-8,-4,0,0,3,5]); d.point((x,y),fill=tuple(max(0,min(255,v+k)) for v in base)+(255,))
    d.rectangle((0,0,15,15),outline=tuple(max(0,v-30) for v in base)+(255,))
    d.line((1,1,14,1),fill=tuple(min(255,v+37) for v in base)+(255,))
    if name=='housing':
        for x,y in [(2,2),(13,2),(2,13),(13,13)]:d.rectangle((x,y,x+1,y+1),fill=(191,154,90,255))
        d.rectangle((4,5,11,10),fill=(35,43,46));d.line((5,7,10,7),fill=(126,207,195))
    if name=='vent':
        for y in [3,6,9,12]:d.line((3,y,12,y),fill=(20,26,28));d.line((3,y+1,12,y+1),fill=(104,113,108))
    img(A/f'textures/block/{name}.png',im)
for name,c in colors.items():
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
    d.polygon([(5,1),(10,1),(14,5),(14,10),(10,14),(5,14),(1,10),(1,5)],fill=(49,43,36),outline=(32,29,25))
    d.polygon([(5,2),(10,2),(13,5),(13,10),(10,13),(5,13),(2,10),(2,5)],fill=(192,151,77))
    d.polygon([(5,4),(10,4),(11,6),(11,9),(9,11),(6,11),(4,9),(4,6)],fill=tuple(int(v*.64) for v in c))
    d.polygon([(6,4),(10,4),(11,6),(10,8),(7,10),(4,8),(4,6)],fill=c)
    d.line((5,5,8,5),fill=(255,255,240));d.point((5,6),fill=(255,255,240))
    img(A/f'textures/item/{name}_lens.png',im)
    js(A/f'models/item/{name}_lens.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'alchemicallaser:item/{name}_lens'}})
# Compact stepped emitter body with dark vents, brass bands and a downward optical nozzle.
elements=[]
def cub(lo,hi,tex):
    elements.append({'from':lo,'to':hi,'faces':{f:{'texture':'#'+tex} for f in ['north','south','east','west','up','down']}})
cub([1,7,1],[15,15,15],'housing');cub([0,13,0],[16,16,16],'brass');cub([1,6,1],[15,8,15],'trim')
cub([3,4,3],[13,6,13],'brass');cub([5,2,5],[11,4,11],'vent');cub([6,1,6],[10,2,10],'brass')
cub([3,8,.8],[13,12,1],'vent');cub([3,8,15],[13,12,15.2],'vent')
js(A/'models/block/laser_emitter.json',{'parent':'minecraft:block/block','textures':{k:f'alchemicallaser:block/{k}' for k in ['housing','brass','vent','trim']}|{'particle':'alchemicallaser:block/housing'},'elements':elements})
js(A/'blockstates/laser_emitter.json',{'variants':{f'facing={d}':{'model':'alchemicallaser:block/laser_emitter','y':v} for d,v in [('north',0),('east',90),('south',180),('west',270)]}})
js(A/'models/item/laser_emitter.json',{'parent':'alchemicallaser:block/laser_emitter'})
im=Image.new('RGBA',(16,32));d=ImageDraw.Draw(im)
for y in range(32):
    k=int(210+45*math.sin(y*math.pi/8));d.line((0,y,15,y),fill=(k,k,k,255))
img(A/'textures/effect/beam.png',im)
js(R/'pack.mcmeta',{'pack':{'pack_format':34,'description':'Create: Alchemical Lasers resources'}})
lang={'itemGroup.alchemicallaser':'Alchemical Lasers','block.alchemicallaser.laser_emitter':'Alchemical Laser Emitter',
      'goggles.alchemicallaser.lens':'Lens: %s','goggles.alchemicallaser.none':'None',
      'goggles.alchemicallaser.state':'Status: %s','goggles.alchemicallaser.minimum':'Minimum speed: %s RPM',
      'goggles.alchemicallaser.redstone':'Redstone permit: %s', 'recipe.alchemicallaser.laser_processing':'Alchemical Laser Processing',
      'recipe.alchemicallaser.duration':'%s ticks at 32 RPM', 'recipe.alchemicallaser.minimum':'Minimum speed: %s RPM', 'recipe.alchemicallaser.chance':'Secondary output: %s%%'}
for c in colors:lang[f'item.alchemicallaser.{c}_lens']=c.title()+' Optical Lens'
for s in ['idle','ready','charging','processing','completing']:lang['state.alchemicallaser.'+s]=s.title()
for s in ['lens_install','activation','charge','fire','beam','complete','shutdown']:lang['subtitles.alchemicallaser.'+s]='Laser '+s.replace('_',' ')
# Ponder production text uses translation keys rather than the source fallback strings.
scenes=(Path(__file__).resolve().parents[1]/'src/main/java/nl/mikaaah/alchemicallaser/ponder/LaserScenes.java').read_text()
for method in re.split(r'public static void ',scenes)[1:]:
    title=re.search(r'base\(b,u,"([^"]+)","([^"]+)"\)',method)
    if not title:continue
    key,heading=title.groups();prefix='alchemicallaser.ponder.'+key+'.'
    lang[prefix+'header']=heading
    for i,text in enumerate(re.findall(r'text\(s,u,"([^"]+)"',method),1):lang[prefix+'text_'+str(i)]=text
lang['alchemicallaser.ponder.tag.laser_processing']='Alchemical Lasers'
lang['alchemicallaser.ponder.tag.laser_processing.description']='Kinetic processing with interchangeable optical lenses'
lang['emi.category.alchemicallaser.laser_processing']='Alchemical Laser Processing'
js(A/'lang/en_us.json',lang)
js(R/'data/alchemicallaser/data_maps/item/laser_lenses.json',{'values':{f'alchemicallaser:{key}_lens':{'color':(rgb[0]<<16)|(rgb[1]<<8)|rgb[2],'pitch':{'red':.8,'blue':1.2,'purple':.95,'white':1.05}[key]} for key,rgb in colors.items()}})
# Survival crafting and a modest standalone set of transformations. Custom pack recipes can replace these.
def shaped(name,pattern,key,result,count=1):js(R/f'data/alchemicallaser/recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':result,'count':count}})
shaped('laser_emitter',['BSB','CEC',' G '],{'B':'create:brass_ingot','S':'create:shaft','C':'create:andesite_casing','E':'create:electron_tube','G':'minecraft:glass'},'alchemicallaser:laser_emitter')
for c,dye in [('red','red_dye'),('blue','blue_dye'),('purple','purple_dye'),('white','white_dye')]:
    shaped(c+'_lens',[' B ','GDG',' B '],{'B':'create:brass_nugget','G':'minecraft:glass','D':'minecraft:'+dye},'alchemicallaser:'+c+'_lens')
for name,item,out,lens,t in [('sand_to_glass','sand','glass','red',100),('clay_to_brick','clay_ball','brick','red',80),
                           ('magma_to_obsidian','magma_block','obsidian','blue',160),('amethyst_to_quartz','amethyst_shard','quartz','purple',120),
                           ('quartz_to_prismarine','quartz','prismarine_crystals','white',180)]:
    js(R/f'data/alchemicallaser/recipe/{name}.json',{'type':'alchemicallaser:laser_processing','ingredient':{'item':'minecraft:'+item},'lens':'alchemicallaser:'+lens+'_lens','result':{'id':'minecraft:'+out},'duration':t,'minimum_rpm':16})
js(R/'data/alchemicallaser/loot_table/blocks/laser_emitter.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'alchemicallaser:laser_emitter'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
js(R/'data/minecraft/tags/block/mineable/pickaxe.json',{'replace':False,'values':['alchemicallaser:laser_emitter']})
# Procedural original audio: glass click, motor startup, rising oscillator, discharge, seamless hum, pulse and spin-down.
sr=44100;rng=np.random.default_rng(902)
sounds={}
for name,duration in [('lens_install',.18),('activation',.3),('charge',.6),('fire',.22),('beam',1.0),('complete',.32),('shutdown',.35)]:
    t=np.arange(round(sr*duration))/sr;noise=rng.normal(0,1,len(t)); smooth=np.convolve(noise,np.ones(7)/7,mode='same')
    if name=='beam':
        a=.18*np.sin(2*np.pi*160*t)+.065*np.sin(2*np.pi*640*t)*(1+.3*np.sin(2*np.pi*12*t))+.04*sum(np.sin(2*np.pi*k*t+phase) for k,phase in zip([397,503,761,997],rng.uniform(0,2*np.pi,4)))/4
        # All frequencies and modulation periods are integral across this seamless one-second loop.
    elif name=='charge':a=(.18*np.sin(2*np.pi*(260*t+600*t*t))+.06*np.sin(2*np.pi*(520*t+900*t*t)))*np.sin(np.pi*t/duration)**.6
    elif name=='lens_install':a=(.23*np.sin(2*np.pi*1800*t)+.12*np.sin(2*np.pi*2700*t)+.08*smooth)*np.exp(-t*32)
    elif name=='fire':a=(.28*np.sin(2*np.pi*(1200*t-1600*t*t))+.14*smooth)*np.exp(-t*17)
    elif name=='complete':a=(.2*np.sin(2*np.pi*660*t)+.12*np.sin(2*np.pi*990*t)+.06*np.sin(2*np.pi*1320*t))*np.exp(-t*12)
    elif name=='activation':a=(.15*np.sin(2*np.pi*(120*t+300*t*t))+.06*smooth)*np.sin(np.pi*t/duration)
    else:a=(.16*np.sin(2*np.pi*(500*t-550*t*t))+.03*smooth)*(1-t/duration)
    if name!='beam':a[:120]*=np.linspace(0,1,120);a[-120:]*=np.linspace(1,0,120)
    folder=A/'sounds';folder.mkdir(exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix='.wav') as tmp:
        with wave.open(tmp.name,'wb') as wav:wav.setnchannels(1);wav.setsampwidth(2);wav.setframerate(sr);wav.writeframes((np.clip(a,-1,1)*32767).astype('<i2').tobytes())
        subprocess.run(['ffmpeg','-v','error','-y','-i',tmp.name,'-c:a','libvorbis','-q:a','5',str(folder/(name+'.ogg'))],check=True)
    sounds[name]={'subtitle':'subtitles.alchemicallaser.'+name,'sounds':[{'name':'alchemicallaser:'+name,'attenuation_distance':12}]}
js(A/'sounds.json',sounds)
# A real Create belt schematic with saved controller/index metadata, two emitters, a Depot and side shaft connections.
blocks=[];palette=[]
def block(x,y,z,name,props=None,nbt=None):
    state={'Name':n.String(name)}
    if props:state['Properties']=n.Compound({k:n.String(v) for k,v in props.items()})
    state=n.Compound(state)
    if state not in palette:palette.append(state)
    b=n.Compound({'pos':n.List[n.Int]([x,y,z]),'state':n.Int(palette.index(state))})
    if nbt:b['nbt']=n.Compound(nbt)
    blocks.append(b)
for x in range(7):
    for z in range(7):block(x,0,z,'minecraft:stone')
for x in range(7):
    block(x,1,3,'create:belt',{'facing':'east','slope':'horizontal','part':'start' if x==0 else 'end' if x==6 else 'middle','casing':'false','waterlogged':'false'},
          {'id':n.String('create:belt'),'Controller':n.IntArray([0,1,3]),'IsController':n.Byte(x==0),'Length':n.Int(7),'Index':n.Int(x),'Speed':n.Float(-32),'Casing':n.String('NONE'),'Inventory':n.Compound()})
for x in [2,4]:
    block(x,3,3,'alchemicallaser:laser_emitter',{'facing':'north'},{'id':n.String('alchemicallaser:laser_emitter'),'Speed':n.Float(32)})
    block(x,3,4,'create:shaft',{'axis':'z','waterlogged':'false'},{'id':n.String('create:shaft'),'Speed':n.Float(32)})
block(2,1,1,'create:depot',{'waterlogged':'false'},{'id':n.String('create:depot')})
block(5,3,3,'minecraft:lever',{'face':'wall','facing':'east','powered':'false'})
structure=n.File({'DataVersion':n.Int(3955),'size':n.List[n.Int]([7,5,7]),'palette':n.List[n.Compound](palette),
                  'blocks':n.List[n.Compound](blocks),'entities':n.List[n.Compound]()},gzipped=True)
(A/'ponder').mkdir(exist_ok=True)
structure.save(A/'ponder/laser_workshop.nbt')
print('Generated original textures, models, crafting/processing recipes, seven audio events and Ponder structure.')

# Apply the current optics after generating the legacy base assets.
subprocess.run([__import__("sys").executable, str(Path(__file__).with_name("redesign_optics.py"))], check=True)
