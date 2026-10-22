# Changelog

## 0.2.2

- Replaced the controller top with a compact solid brass housing, matching the retained industrial lantern. Added steel collars around the existing horizontal axle.
- Restored ordinary Create shaft power from either end of the horizontal axle, including wrench rotation and both horizontal axes. Removed the overhead cog connection, custom kinetic mixin and controller model wrapper.
- Kept the lantern position, nine lenses, processing recipes, sounds and beam behavior. Updated the Ponder shaft layout, setup instructions and preview.

### Verification

The build and all 26 dedicated-server GameTests passed, including both side inputs, a rotated emitter, rejection of vertical shafts and the removed overhead cog drive, all five specialist lenses, belt/Depot processing, speed limits, redstone and item conservation. Resource validation passed. The preview uses actual model geometry/textures; graphical-client review remains manual.

## 0.2.1

- Kept the industrial lantern optics and replaced the andesite top cap with Create’s native Rotation Speed Controller top fittings, referenced directly from its baked model. No Create model or textures are bundled.
- Added a working friction drive from a horizontal large cogwheel placed directly above, with its axle perpendicular to the emitter’s roller. Input and emitter RPM are 1:1. Plain shafts, small cogs and incorrectly oriented cogs cannot power it.
- Automatically aligns placement beneath an existing horizontal large cog. Wrench rotation and either horizontal axle orientation are supported.
- Disconnects power when the drive cog is removed or rotated out of alignment.
- Moved the unchanged lantern down six pixels to preserve Create’s native roller/cog contact geometry. Belt/Depot placement stays at the same height; lens, beam and charge particles follow the lowered nozzle.
- Updated the Ponder schematic, setup explanation, model preview and upgrade instructions.

### Verification

The build and all 27 dedicated-server GameTests passed, including the five new cog-drive regressions and all existing lens, belt/Depot, speed, redstone and conservation tests. The client model-wrapper check passed for both axes, retaining the lantern and 44 native controller rail quads through inventory, fixed-display and chunk render paths. The textured preview is a software render; graphical-client lighting and Ponder playback remain manual review items.

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
