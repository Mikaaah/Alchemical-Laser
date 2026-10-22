![Create: Alchemical Lasers](docs/branding/banner.png)

Kinetic laser processing for **Minecraft 1.21.1 • NeoForge • Create 6.0.10**.

A single emitter processes items directly on Mechanical Belts and Depots. A caged industrial optical head hangs below Create’s brass speed-controller top fittings, powered by a large cogwheel directly above. Nine reusable lenses select recipes and color the animated beam. Includes pixel-art optical cartridges, spatial audio and Create Ponder tutorials.

![Industrial laser model and nine optical lenses](docs/images/optics-0.2.1.png)

## How it works

1. Place an emitter above a horizontal belt or Depot, leaving one air block between them, like a Mechanical Press.
2. Place a large cogwheel directly above, with a horizontal axle across the top channel, like a Rotation Speed Controller. Power that cogwheel with at least 16 RPM.
3. Right-click with any optical lens. Sneak-right-click with an empty hand to remove it.
4. Supply a matching item. The laser holds it during charging and processing, commits the transformation, then releases the output.

Speed scales processing time, up to the configured cap. A powered redstone signal disables the emitter. Interrupted cycles leave input unchanged; belt items are released, and Depots can restart when power returns.

## Features

- Native Create belt and Depot processing, including counted inputs and retained stack remainders.
- Nine reusable lenses with fitted 3D cartridges, colored continuous beams, charging particles and completion effects.
- Industrial lantern adapted from Create Deco with permission, paired with Create’s original controller fittings and textures; see [credits](THIRD_PARTY_NOTICES.md).
- Seven original sound events, including a spatial beam loop that stops when processing stops or the machine unloads.
- Engineer's Goggles information, Create wrench interaction and a one-slot lens item capability for automation.
- Datapack recipes and generic KubeJS custom recipes; optional component copying and probabilistic secondary outputs.
- Optional JEI and EMI recipe categories showing the lens, input, outputs, duration and minimum RPM.
- Six Ponder scenes: setup, kinetic power, belt processing, lens changes, sequential automation and redstone.

## Starter recipes

| Lens | Input | Output |
|---|---|---|
| Red | Sand | Glass |
| Red | Clay ball | Brick |
| Blue | Magma block | Obsidian |
| Purple | Amethyst shard | Quartz |
| White | Quartz | Prismarine crystals |
| Growth | Wheat seeds | Wheat |
| Convergence | 4 amethyst shards | Amethyst block |
| Splitting | Amethyst block | 4 amethyst shards |
| Power | Obsidian | Crying obsidian |
| Eclipse | Sculk | Echo shard |

These are standalone examples. Modpacks can replace them with their own progression. Towns & Kingdoms is not required.

[Custom recipes and KubeJS](docs/RECIPES.md) · [Development specification](docs/DEVELOPMENT_PLAN.md)

## Requirements

Minecraft 1.21.1, Java 21, NeoForge 21.1.219 or newer, and Create 6.0.10 (6.0.x). Use Create's required matching dependencies. JEI, EMI and KubeJS are optional.

Version **0.1.0** is the first implementation. See [release notes](CHANGELOG.md) for the verification status and remaining manual checks.

The specialist lenses are recipe selectors. Their starter recipes demonstrate growth, consolidation, splitting, energizing and sculk processing; datapacks and KubeJS can replace them. Power recipes require 64 RPM and the Eclipse example requires 128 RPM.

**Updating from 0.1.0 or 0.2.0:** replace the emitter’s shaft connection with a large cogwheel directly above. Its axle must be perpendicular to the emitter’s roller. A wrench rotates the emitter; placing it beneath an existing cog aligns it automatically. The lantern hangs slightly into the existing air gap, so the belt/Depot height stays the same. Existing emitter and lens item IDs are preserved.

The cog drive follows its input RPM at a 1:1 ratio. The speed-controller fittings provide the mechanical connection; processing speed follows the cogwheel driving the laser.
