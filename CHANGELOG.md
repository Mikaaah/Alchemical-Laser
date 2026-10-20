# Changelog

## 0.1.0

First implementation of Create: Alchemical Lasers for NeoForge 1.21.1.

- Added the kinetic Laser Emitter, four reusable optical lenses and survival crafting recipes.
- Added native Create belt/Depot processing, recipe quantities, safe stack remainders, optional secondary outputs and item component copying.
- Added redstone disabling, goggles information, wrench support and a lens inventory capability.
- Added original textures, models, continuous beam rendering, mechanical animation, particles and seven synthesized sound assets.
- Added datapack/generic KubeJS recipes, JEI/EMI categories and six Ponder scenes with a custom structure.
- Added server configuration and automated in-game regression coverage.

### Verification

The Gradle build passed and all 15 dedicated-server GameTests passed against NeoForge 21.1.219 and Create 6.0.10-280. Coverage includes both belt directions; Depot conversion and stack conservation; 16, 32, 64, 128 and 256 RPM; below-minimum speed; redstone disable/restart; power-loss input retention; lens exchange; counted ingredients; component copying; guaranteed secondary outputs; and recipe codec roundtrips. Resource validation also passed.

Graphical playtesting, Ponder playback review, sound review, multiple-client synchronization, chunk unload/reload and optional viewer/KubeJS installation checks have not been performed. These checks remain necessary before treating this as a polished production release.

### Behavior notes

Processing uses Create's standard press spacing (one air block above the belt/Depot). Stress is reserved continuously while the emitter is connected, matching Create's kinetic accounting. Interrupted cycles restart without consuming partial inputs; no partial progress is persisted across world reloads. Lenses can be inserted and extracted through item-handler automation; Mechanical Arm and Deployer interactions have not been claimed as tested.
