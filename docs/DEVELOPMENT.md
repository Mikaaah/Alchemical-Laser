# Development

Requires JDK 21. Run `./gradlew build` (or `gradlew.bat build` on Windows). The regular mod JAR and the separate sources JAR are written to `build/libs`.

Pinned development dependencies: Minecraft 1.21.1, NeoForge 21.1.219, Create 6.0.10-280, Ponder 1.0.82, Flywheel 1.0.6, Registrate MC1.21-1.3.0+67. Create is required at runtime; JEI and EMI are optional.

`./gradlew runGameTestServer` runs the automated in-game regression tests without a graphical client. `runClient` opens the development client for visual, Ponder and sound review. Install optional recipe viewers in the development run when reviewing those integrations.

Original resource generation lives in `tools/generate_assets.py` (Pillow, NumPy, nbtlib, ffmpeg). Assets are checked in; building does not require these generation tools. The sounds are original procedural synthesis, and the original pixel art is supplemented by the permitted Create Deco lantern adaptation documented in `THIRD_PARTY_NOTICES.md`. Run `python tools/redesign_optics.py` after the original generator to apply the current optics.

Release review must cover belt directions, stacked inputs, secondary outputs, extraction during processing, power/redstone interruption, lens changes, recipe reload, save/reload, multiple emitters, Ponder scenes and optional recipe viewers. Automated results and remaining manual checks belong in the release notes, not claims of unperformed playtesting.

The normal `build`/`check` runs `checkControllerModel`, a CPU check of the client model wrapper using the installed Create controller geometry. It checks both axes, rail selection, retained lantern quads and inventory/chunk render delegation. The rail geometry stays inside Create; it is selected from the baked dependency model during each client model reload.
