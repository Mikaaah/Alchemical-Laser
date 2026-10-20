# Alchemical Laser

A planned Create addon for Minecraft 1.21.1 on NeoForge, focused on one kinetic laser processing machine with interchangeable optical lenses.

**Status:** Planning • **Target release:** 0.1.0 • **Required dependency:** Create 6.x

This repository starts with the development specification. There is no playable mod or release JAR yet; the features below are planned for the first release.

## Planned Features

- Process items directly on Create Mechanical Belts and Depots.
- Power the Laser Emitter through Create's kinetic network, with speed-dependent processing.
- Switch between reusable red, blue, purple and white optical lenses.
- Define transformations through datapack recipes, with KubeJS recipe support.
- View recipes through JEI and EMI integrations.
- Control production using redstone and inspect the emitter with Engineer's Goggles.
- See animated mechanical parts, continuous colored beams and restrained impact particles.
- Hear spatial charging, firing, processing and shutdown sounds.
- Learn setup, power, processing, lenses, automation and redstone through six Create Ponder scenes.

## Designed for Automation

Install an emitter above a belt or Depot, connect rotational power and insert a lens. Matching items are held during charging and processing, transformed, then released to continue through the production line.

The addon is intended to work independently of Towns & Kingdoms 3. Pack-specific items and progression will not be required.

## Development Roadmap

1. NeoForge project foundation and kinetic emitter.
2. Recipes, belt and Depot processing, and safe input/output handling.
3. Four interchangeable lenses and their visual representation.
4. Models, textures, animations, beams, particles and custom audio.
5. Redstone, goggles, recipe viewers and all six Ponder scenes.
6. Multiplayer, interruption, automation and performance checks before release.

Read the [full development plan](docs/DEVELOPMENT_PLAN.md) for behavior, scope and release requirements.

The initial scope is limited to the laser. Mirrors, long-distance transmission, combat lasers and additional processing machines are excluded.
