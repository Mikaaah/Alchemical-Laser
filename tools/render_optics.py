"""Orthographic resource-model inspection, not an in-game screenshot.

Usage: python tools/render_optics.py /path/to/create.jar /tmp/optics-preview.png
Renders the actual JSON geometry and UVs with nearest-neighbor texture sampling.
"""
import json, math, sys, zipfile
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw

root=Path(__file__).resolve().parents[1]
a=root/'src/main/resources/assets/alchemicallaser'
model=json.loads((a/'models/block/laser_emitter.json').read_text())
create=zipfile.ZipFile(sys.argv[1])
textures={}
for key,value in model['textures'].items():
    ns,path=value.split(':')
    if ns=='create':
        import io
        im=Image.open(io.BytesIO(create.read('assets/create/textures/'+path+'.png')))
    else: im=Image.open(a/('textures/'+path+'.png'))
    textures[key]=np.array(im.convert('RGBA'))

W,H=1100,760
canvas=np.zeros((H,W,3),dtype=float);canvas[:]=[37,36,38]
depth=np.full((H,W),-1e10)
eye=np.array([.8,.65,1.0]);eye/=np.linalg.norm(eye)
right=np.cross([0,1,0],eye);right/=np.linalg.norm(right)
up=np.cross(eye,right)
scale=21

def project(p):
    p=np.array(p)-[8,6,8]
    return np.array([550+p@right*scale,350-p@up*scale,p@eye])

def triangle(points,uvs,tex,shade):
    p=np.array([project(v) for v in points]);uvs=np.array(uvs)
    x0=max(0,int(p[:,0].min()));x1=min(W-1,math.ceil(p[:,0].max()))
    y0=max(0,int(p[:,1].min()));y1=min(H-1,math.ceil(p[:,1].max()))
    yy,xx=np.mgrid[y0:y1+1,x0:x1+1];xx=xx+.5;yy=yy+.5
    x,y=p[:,0],p[:,1];den=(y[1]-y[2])*(x[0]-x[2])+(x[2]-x[1])*(y[0]-y[2])
    if abs(den)<1e-9:return
    w0=((y[1]-y[2])*(xx-x[2])+(x[2]-x[1])*(yy-y[2]))/den
    w1=((y[2]-y[0])*(xx-x[2])+(x[0]-x[2])*(yy-y[2]))/den
    w2=1-w0-w1
    z=w0*p[0,2]+w1*p[1,2]+w2*p[2,2]
    uv=w0[...,None]*uvs[0]+w1[...,None]*uvs[1]+w2[...,None]*uvs[2]
    tx=np.clip((uv[:,:,0]/16*tex.shape[1]).astype(int),0,tex.shape[1]-1)
    ty=np.clip((uv[:,:,1]/16*tex.shape[0]).astype(int),0,tex.shape[0]-1)
    pixels=tex[ty,tx]
    sl=np.s_[y0:y1+1,x0:x1+1]
    mask=(w0>=-1e-6)&(w1>=-1e-6)&(w2>=-1e-6)&(z>depth[sl])&(pixels[:,:,3]>127)
    canvas[sl][mask]=pixels[:,:,:3][mask]*shade
    depth[sl][mask]=z[mask]

def render_element(e,texture_map,transform=None):
    x,y,z=e['from'];X,Y,Z=e['to']
    faces={'north':([[X,Y,z],[x,Y,z],[x,y,z],[X,y,z]],[0,0,-1]),
           'south':([[x,Y,Z],[X,Y,Z],[X,y,Z],[x,y,Z]],[0,0,1]),
           'west':([[x,Y,z],[x,Y,Z],[x,y,Z],[x,y,z]],[-1,0,0]),
           'east':([[X,Y,Z],[X,Y,z],[X,y,z],[X,y,Z]],[1,0,0]),
           'up':([[x,Y,z],[X,Y,z],[X,Y,Z],[x,Y,Z]],[0,1,0]),
           'down':([[x,y,Z],[X,y,Z],[X,y,z],[x,y,z]],[0,-1,0])}
    for face,fd in e['faces'].items():
        vertices,normal=faces[face]
        vertices=np.array(vertices,dtype=float);normal=np.array(normal,dtype=float)
        rotation=e.get('rotation',{})
        if rotation.get('angle',0):
            angle=math.radians(rotation['angle']);c,s=math.cos(angle),math.sin(angle)
            matrix={'x':[[1,0,0],[0,c,-s],[0,s,c]],'y':[[c,0,s],[0,1,0],[-s,0,c]],'z':[[c,-s,0],[s,c,0],[0,0,1]]}[rotation['axis']]
            matrix=np.array(matrix);origin=np.array(rotation['origin'])
            vertices=(vertices-origin)@matrix.T+origin;normal=matrix@normal
        if transform is not None:
            matrix,offset=transform
            vertices=vertices@matrix.T+offset;normal=matrix@normal
        if np.dot(normal,eye)<=0: continue
        u,v,U,V=fd.get('uv',[0,0,16,16]);uv=[[u,v],[U,v],[U,V],[u,V]]
        k=fd.get('rotation',0)//90;uv=uv[k:]+uv[:k]
        tex=texture_map[fd['texture'][1:]]
        shade={'up':1.,'down':.5,'east':.83,'west':.7,'north':.7,'south':.93}[face]
        for ids in [[0,1,2],[0,2,3]]:
            triangle([vertices[i] for i in ids],[uv[i] for i in ids],tex,shade)

for e in model['elements']:render_element(e,textures)
# Native horizontal controller roller; model axis Y is rotated to Z.
import io, copy
shaftmodel=json.loads(create.read('assets/create/models/block/shaft.json'))
shafttextures={k:np.array(Image.open(io.BytesIO(create.read('assets/create/textures/'+v.split(':')[1]+'.png'))).convert('RGBA')) for k,v in shaftmodel['textures'].items()}
matrix=np.array([[1,0,0],[0,0,-1],[0,1,0]])
for e in shaftmodel['elements']: render_element(e,shafttextures,(matrix,np.array([0,16,0])))
# A mounted Eclipse cartridge, transformed exactly like the block-entity renderer.
lensmodel=json.loads((a/'models/item/eclipse_lens.json').read_text())
lensmap={}
for k,v in lensmodel['textures'].items(): lensmap[k]=np.array(Image.open(a/('textures/'+v.split(':')[1]+'.png')).convert('RGBA'))
for original in lensmodel['elements']:
    e=copy.deepcopy(original)
    for field in ['from','to']:
        e[field]=[(value-8)*.46+(-4.96 if axis==1 else 8) for axis,value in enumerate(e[field])]
    render_element(e,lensmap)
out=Image.fromarray(np.clip(canvas,0,255).astype('uint8'))
d=ImageDraw.Draw(out)
d.text((35,25),'CREATE: ALCHEMICAL LASERS / 0.2.2',fill='#e3c98e',font_size=24)
d.text((35,63),'Brass housing · side shaft drive · industrial lantern optics',fill='#b4ada1',font_size=18)
for i,name in enumerate(['red','blue','purple','white','growth','convergence','splitting','power','eclipse']):
    lens=Image.open(a/f'textures/item/{name}_lens.png').resize((48,48),Image.Resampling.NEAREST)
    x=110+i*100;out.paste(lens,(x,640),lens);d.text((x-12,700),name.title(),fill='#d4c9b7',font_size=13)
d.text((35,735),'Actual model and textures; lighting differs in-game.',fill='#88827a',font_size=13)
out.save(sys.argv[2])
