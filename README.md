# Create: Alchemical Lasers

Kinetic laser processing for **Minecraft 1.21.1 • NeoForge • Create 6.0.10**.

A single emitter processes items directly on Mechanical Belts and Depots. Interchangeable optical lenses select its recipes and color the animated beam. Original mechanical models, pixel textures, spatial audio and Create Ponder tutorials are included.

## How it works

1. Place an emitter above a horizontal belt or Depot, leaving one air block between them, like a Mechanical Press.
2. Connect a shaft along its horizontal shaft axis and supply at least 16 RPM.
3. Right-click with a red, blue, purple or white lens. Sneak-right-click with an empty hand to remove it.
4. Supply a matching item. The laser holds it during charging and processing, commits the transformation, then releases the output.

Speed scales processing time, up to the configured cap. A powered redstone signal disables the emitter. Interrupted cycles leave input unchanged; belt items are released, and Depots can restart when power returns.

## Features

- Native Create belt and Depot processing, including counted inputs and retained stack remainders.
- Four reusable lenses with visible installation, colored continuous beams, charging particles and completion effects.
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

These are standalone examples. Modpacks can replace them with their own progression. Towns & Kingdoms is not required.

[Custom recipes and KubeJS](docs/RECIPES.md) · [Development specification](docs/DEVELOPMENT_PLAN.md)

## Requirements

Minecraft 1.21.1, Java 21, NeoForge 21.1.219 or newer, and Create 6.0.10 (6.0.x). Use Create's required matching dependencies. JEI, EMI and KubeJS are optional.

Version **0.1.0** is the first implementation. See [release notes](CHANGELOG.md) for the verification status and remaining manual checks.
