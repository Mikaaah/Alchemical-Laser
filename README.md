![Create: Alchemical Lasers](docs/branding/banner.png)

Kinetic laser processing for **Minecraft 1.21.1 • NeoForge • Create 6.0.10**.

A single emitter processes items directly on Mechanical Belts and Depots. A caged industrial optical head hangs below an andesite drive with a vertical shaft connection on top. Nine reusable lenses select recipes and color the animated beam. Includes pixel-art optical cartridges, spatial audio and Create Ponder tutorials.

![Industrial laser model and nine optical lenses](docs/images/optics-0.2.0.png)

## How it works

1. Place an emitter above a horizontal belt or Depot, leaving one air block between them, like a Mechanical Press.
2. Connect a vertical shaft to the top drive and supply at least 16 RPM.
3. Right-click with any optical lens. Sneak-right-click with an empty hand to remove it.
4. Supply a matching item. The laser holds it during charging and processing, commits the transformation, then releases the output.

Speed scales processing time, up to the configured cap. A powered redstone signal disables the emitter. Interrupted cycles leave input unchanged; belt items are released, and Depots can restart when power returns.

## Features

- Native Create belt and Depot processing, including counted inputs and retained stack remainders.
- Nine reusable lenses with fitted 3D cartridges, colored continuous beams, charging particles and completion effects.
- Industrial lantern geometry and palette textures adapted from Create Deco with permission; see [credits](THIRD_PARTY_NOTICES.md).
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

**Updating from 0.1.0:** move emitter power connections from the side to the top. Existing emitter and colored lens item IDs are preserved.
