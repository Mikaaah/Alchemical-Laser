# Changelog

## 0.2.0

- Rebuilt the emitter around Create Deco’s permitted industrial cage lantern, with an andesite drive cap, brass focusing bands, and a recessed optical cartridge. Credits are in `THIRD_PARTY_NOTICES.md`.
- Moved the shaft connection to the top with a vertical rotation axis; side and underside shaft connections are closed. Move existing power connections above the emitter when upgrading.
- Removed the exposed lower cog and free-spinning floating lens. The lens now seats horizontally in the nozzle as a shallow 3D cartridge.
- Refined the four existing lens icons and added Growth, Convergence, Splitting, Power, and Eclipse lenses, including survival crafting, beam colors, audio pitches, Ponder registration and starter processing recipes.
- Updated all six Ponder scenes for overhead power and nine lenses. Existing block and colored-lens IDs remain stable.

### Verification

The Gradle build and all 22 dedicated-server GameTests passed on Minecraft 1.21.1, NeoForge 21.1.219 and Create 6.0.10-280. This includes overhead-only shaft connectivity, all five specialist processing recipes, reusable lenses, insufficient convergence input, and the existing belt/Depot, speed, redstone and item-conservation regressions. JSON, model/texture bindings, lens crafting/data-map entries, Ponder structure and translations also passed resource checks.

The preview is a software render of the actual model and textures. In-game graphical lighting, lens inventory presentation, audio, Ponder playback and optional recipe-viewer layouts still require a graphical-client review.

## 0.1.0

- Apply supplied branding: GitHub banner and in-game mod information logo.

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
