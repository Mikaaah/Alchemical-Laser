# Create Deco lantern assets

The Alchemical Laser model in version 0.2.0 adapts the industrial cage lantern from
**Create Deco 2.1.3**, by Kayla, Talrey, Ordana and Cassian.

Source project: https://github.com/talrey/CreateDeco

The lantern reference geometry is retained at `tools/reference/industrial_lantern.json`.
The following source palette textures are bundled under the addon namespace:

- `industrial_iron_lamp.png` → `deco_industrial_cage.png`
- `brass_lamp.png` → `deco_brass_cage.png`
- `light_default.png` → `deco_lamp_core.png`

These are third-party assets, used as a base with permission obtained and supplied
by Mikaaah for this mod. Their original authors retain all rights. This repository's
license does not relicense those assets or grant permission to reuse them elsewhere.
Create Deco is not required at runtime; no Create Deco code is bundled.

# Create controller fittings

Copyright (c) The Create Team / The Creators of Create.

The upper rails and textures are referenced from Create’s installed, baked Rotation Speed Controller model at client model bake time. The horizontal roller and large cogwheel also use Create’s own runtime models. No Create model JSON, textures or original Java code are redistributed by this addon. The render selects only the upper rails and combines their baked quads with the lantern body, including in inventory and after resource reload.

Source project and asset license: https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md

Create’s assets remain All Rights Reserved and retain their original ownership. The addon’s license does not relicense them.
