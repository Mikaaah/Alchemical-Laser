# Alchemical Laser — Development Plan

This is the original 0.1.0 specification and implementation checklist. The optics updates add five specialist lenses, a permitted Create Deco lantern adaptation and, in 0.2.1, a controller-style overhead large-cog drive; see the current README and changelog.

Implementation status is recorded below. Checked implementation tasks mean source/assets are present; unperformed release checks remain unchecked. See `CHANGELOG.md` for exact validation evidence.
**Minecraft:** 1.21.1  
**Mod Loader:** NeoForge  
**Required Dependency:** Create 6.x  
**Target Release:** 0.1.0  
**Purpose:** Introduce a fully automatable laser processing system inspired by Create: Above & Beyond.

---

## 1. Core Concept

The Alchemical Laser is a kinetic processing machine capable of transforming items using concentrated laser beams.

Unlike conventional processing machines, the laser supports different wavelengths through interchangeable lenses, allowing a single machine to perform multiple types of item transformations.

The machine should integrate naturally into Create's automation ecosystem without requiring a custom GUI or complex multiblock structures.

**Core principles:**
- One machine with multiple interchangeable lenses.
- Process items directly on Mechanical Belts and Depots.
- Use Create rotational power.
- Support both item conversion and modification of existing item states.
- Use configurable, data-driven recipes.
- Provide high-quality animations, particles and sounds.
- Support complete factory automation.
- Include interactive Create Ponder tutorials.

## 2. Laser Machine

### 2.1 Construction and Placement

The Laser Emitter is a single-block kinetic machine.

- Mount directly above a Mechanical Belt or Depot.
- The default beam direction is downward.
- The processing target is the position directly below the emitter.
- Connect to Create's kinetic network using a mechanical shaft connection.
- Support Create wrench interactions for orientation and removal.
- Display machine information through Engineer's Goggles.

For the initial release, processing requires the target to be directly beneath the laser. Longer-distance and horizontal beams are excluded to keep processing reliable.

### 2.2 Operational States

The machine operates through five states.

| State | Behavior |
|---|---|
| Idle | No valid processing target; laser disabled |
| Ready | Valid lens, recipe and rotational power detected |
| Charging | Laser builds energy while the item is held |
| Processing | Laser beam fires and recipe progress advances |
| Completing | Item transforms, effects play and transport resumes |

The system should automatically transition between states without requiring player interaction.

### 2.3 Kinetic Power

The Laser Emitter consumes rotational power from Create's kinetic network.

- Require a minimum rotational speed.
- Consume Stress Units while operating.
- Processing duration decreases as rotational speed increases.
- Include configurable minimum and maximum effective processing speeds.
- Stop safely when power is lost.
- Resume interrupted recipes when appropriate.

**Suggested starting balance:**

| Rotation Speed | Processing Behavior |
|---|---|
| Below 16 RPM | Machine cannot operate |
| 16 RPM | Slow processing |
| 32 RPM | Standard processing |
| 64 RPM | Faster processing |
| 128 RPM | High-speed processing |
| 256 RPM | Maximum processing speed |

These are preliminary balancing values and should be configurable.

## 3. Belt and Depot Processing

### 3.1 Mechanical Belt Integration

The laser should behave similarly to Create's existing belt-processing machinery.

**Processing sequence:**

1. An item moves along a Mechanical Belt.
2. The laser checks whether the item matches an available recipe for its installed lens.
3. If a recipe matches, the item is held beneath the laser.
4. The laser charges and begins processing.
5. The beam strikes the item and the transformation completes.
6. The output replaces the processed input.
7. The belt resumes transporting the output.

**Important requirements:**
- No item duplication or accidental deletion.
- Correct handling of stacked items and recipe quantities.
- Support simultaneous item stacks at different positions on long belts.
- No processing when no valid recipe exists.
- Respect belt direction and movement.
- Prevent other machines from interfering with items reserved for laser processing.

### 3.2 Depot Integration

The same processing logic should work with Create Depots.

Items placed on a Depot are processed automatically when a valid lens and recipe are available.

Depot processing must support automated input and extraction using funnels, hoppers and other Create components.

### 3.3 Processing Performance

Processing runs server-side, while the client handles rendering and sounds.

Recipes should be checked when inputs or machine conditions change, rather than unnecessarily searching the entire recipe registry every tick.

Multiple lasers must function independently without causing excessive server load.

## 4. Optical Lens System

### 4.1 Interchangeable Lenses

The laser uses removable optical lenses to determine which recipes are available.

Four lenses are planned for the initial release.

| Lens | Intended Application |
|---|---|
| Red | Thermal and energetic transformations |
| Blue | Stabilization and cooling-related transformations |
| Purple | Alchemical and magical transformations |
| White | Advanced material processing |

These applications are thematic defaults, not hardcoded restrictions. Modpack developers can assign any compatible recipe to any lens.

### 4.2 Lens Interaction

- Right-click the emitter with a lens to install it.
- Right-click with a different lens to exchange it.
- Shift-right-click with an empty hand to retrieve the installed lens.
- Display the active lens visually inside the emitter.
- Each lens changes the beam color and corresponding particle effects.
- Lenses are reusable and do not lose durability.

### 4.3 Lens Rendering

Each lens should have an individual item texture and a corresponding appearance when installed.

The emitter model remains identical regardless of the lens type.

## 5. Laser Processing Recipes

### 5.1 Recipe System

Introduce a dedicated `laser_processing` recipe type.

Recipes must be fully data-driven and configurable using datapacks.

Each recipe should support:

- Input item or item tag.
- Input quantity.
- Required lens.
- Output item and quantity.
- Processing duration.
- Optional minimum RPM requirement.
- Optional secondary output or processing probability.

### 5.2 Transformation Types

**Item Conversion**

Converts an input into an entirely different item.

Example:

`Unstable Crystal → Purple Laser → Resonant Crystal`

**Item-State Modification**

Changes the state or properties of an existing item.

Examples:
- Uncharged component → Charged component.
- Unstable component → Stabilized component.
- Untreated material → Irradiated material.

Where appropriate, use Minecraft's data components to store and modify item states.

**Multi-Stage Processing**

Allow different laser recipes to be chained together.

Example:

`Raw Crystal → Red Laser → Heated Crystal → Blue Laser → Stabilized Crystal`

Each processing stage should operate as an independent recipe and support complete belt automation.

### 5.3 Recipe Integration

Required:
- Native datapack recipe support.
- Compatibility with KubeJS recipe additions and removals.
- Recipe reloading without restarting Minecraft.
- JEI integration.
- EMI integration.

Recipe viewer support should display the required lens, ingredients, output, processing duration and any minimum speed requirements.

### 5.4 Compatibility

Recipes should accept ordinary Minecraft items and items registered by other mods.

No hard dependencies on TNK3-specific custom items or progression systems.

The mod must remain fully functional as a standalone Create addon.

## 6. Sound Design

The laser should have custom audio that responds dynamically to its operating state.

### 6.1 Sound Events

| Event | Sound Design |
|---|---|
| Lens installation | Mechanical click with a glass-like chime |
| Activation | Short electrical startup sequence |
| Charging | Rising electronic whine |
| Firing | Sharp laser discharge |
| Processing | Sustained humming beam with electrical crackling |
| Completion | Resonant energy pulse |
| Shutdown | Descending electrical tone |

### 6.2 Dynamic Audio

- Laser pitch responds to processing intensity.
- Lens variants have subtly different sound profiles.
- Audio transitions smoothly between charging, firing and shutdown.
- Multiple nearby lasers must not produce excessively loud overlapping sounds.
- Continuous audio stops correctly when a machine shuts down, unloads or loses power.
- Sounds are spatialized and attenuate naturally with distance.

Audio should feel mechanical and energetic, rather than like generic magical spell effects.

## 7. Visual Effects and Animations

### 7.1 Machine Animations

The emitter should have animated mechanical components.

- A rotating internal mechanism driven by kinetic power.
- A visible lens that glows during operation.
- Moving mechanical parts during charging.
- A brief energy discharge upon completion.

### 7.2 Laser Beam

The beam should be rendered as an animated effect rather than a collection of disconnected particles.

Requirements:
- Continuous beam connecting the emitter and processing target.
- Appropriate color based on the installed lens.
- Brighter central beam with a softer surrounding glow.
- Subtle animated energy movement.
- Accurate alignment with the item being processed.
- Immediate termination when processing stops.

### 7.3 Particles

Each processing cycle should include:

**Charging**
- Energy particles drawn toward the lens.
- Gradual increase in lens brightness.

**Processing**
- Small impact sparks.
- Lens-colored particles around the item.
- Subtle fluctuations in beam intensity.

**Completion**
- Brief flash.
- Small outward particle burst.
- Beam retracts or fades.

Particles should remain visible without obscuring the entire production line.

### 7.4 Performance

- Limit particle spawning.
- Avoid unnecessary rendering when the machine is offscreen.
- Correctly synchronize animations between client and server.
- Respect Minecraft particle settings.
- Support many active emitters within the same factory.

## 8. Redstone and Automation

### 8.1 Redstone Control

The laser should support ordinary redstone control.

By default, a powered redstone signal disables the emitter, matching the behavior of many Create machines.

The machine should pause or cancel processing safely when redstone control changes.

### 8.2 Automated Lens Exchange

For the initial release, lenses can be changed manually or through compatible item-interaction automation, where technically supported.

Automated lens swapping should not require a custom GUI or additional dedicated machine.

### 8.3 Factory Integration

The laser must work properly with:

- Mechanical Belts.
- Depots.
- Funnels.
- Chutes and Hoppers.
- Mechanical Arms where applicable.
- Redstone-controlled production lines.
- Other Create processing machines positioned along the same belt.

## 9. Create Ponder Integration

Full Ponder support is mandatory for the first release.

### Scene 1 — Laser Setup

Demonstrate placing the emitter above a Mechanical Belt and identifying its processing area.

### Scene 2 — Kinetic Power

Demonstrate connecting a shaft, providing rotational power and explaining how speed affects processing time.

### Scene 3 — Laser Processing

Demonstrate an item moving along a belt, stopping beneath the emitter, being processed and continuing along the belt.

### Scene 4 — Optical Lenses

Demonstrate installing, removing and exchanging colored lenses, including the resulting processing differences.

### Scene 5 — Advanced Automation

Demonstrate a production line using multiple lasers with different lenses to perform sequential transformations.

### Scene 6 — Redstone Control

Demonstrate activating and disabling the laser using redstone signals.

**Ponder requirements:**
- Proper animated Create-style scenes.
- Real moving machinery and visual effects.
- Clear instructional text.
- English localization.
- Ponder tags linking related components and tutorials.
- Scenes accessible directly from the Laser Emitter and lens items where relevant.

## 10. Development Phases

### Phase 1 — Foundation

- [x] Set up NeoForge 1.21.1 project.
- [x] Add Create as a required dependency.
- [x] Register Laser Emitter block and block entity.
- [x] Implement kinetic power consumption.
- [x] Implement initial mechanical model.

### Phase 2 — Processing System

- [x] Implement custom recipe type.
- [x] Integrate belt processing.
- [x] Implement Depot processing.
- [x] Implement input/output handling.
- [x] Implement recipe progress and interruption handling.
- [ ] Validate multiplayer synchronization.

### Phase 3 — Lens System

- [x] Register the four lenses.
- [x] Implement installation and removal.
- [x] Implement lens-specific recipe filtering.
- [x] Implement lens visualization.
- [x] Implement data-driven lens settings.

### Phase 4 — Visual and Audio Polish

- [x] Create final block and item textures.
- [x] Implement mechanical animations.
- [x] Implement animated laser beam.
- [x] Create custom particle effects.
- [x] Create custom sound assets.
- [x] Implement dynamic audio behavior.
- [ ] Optimize client-side rendering.

### Phase 5 — Create Integration

- [x] Implement Engineer's Goggles information.
- [x] Implement redstone control.
- [x] Add JEI/EMI recipe categories.
- [ ] Validate KubeJS/datapack compatibility.
- [x] Implement the six Ponder scenes.

### Phase 6 — Testing and Release

- [x] Test all processing speeds.
- [x] Test individual and stacked items.
- [x] Test belt direction and Depot compatibility.
- [ ] Test multiple lasers operating simultaneously.
- [x] Test interrupted recipes and power loss.
- [ ] Test chunk unloading and world reloads.
- [x] Test dedicated multiplayer servers.
- [ ] Test compatibility with other Create machinery.
- [ ] Verify sounds and effects clean up correctly.
- [ ] Verify all Ponder scenes and recipe displays.
- [x] Build release JAR.
- [ ] Prepare CurseForge description and changelog.

## 11. Release Requirements

Version 0.1.0 is considered complete when:

1. Laser processing works reliably on belts and Depots.
2. All four lens variants work correctly.
3. Custom recipes can be added or replaced through datapacks and KubeJS.
4. Machine processing responds correctly to kinetic power.
5. Redstone control works.
6. Sounds, animations and particles are fully implemented and synchronized.
7. JEI and EMI display laser recipes correctly.
8. All six Ponder scenes work.
9. The machine operates safely in multiplayer.
10. The mod works independently of Towns & Kingdoms 3.

## 12. Explicitly Excluded Features

The following features are outside the initial scope:

- Electroplating Station.
- Resonance Tuner.
- Induction Annealer.
- Vacuum Infuser.
- Magnetic Separator.
- Arboreal Extractor.
- Long-distance laser transmission.
- Laser reflection using mirrors.
- Laser damage or combat mechanics.
- World-seed-dependent alchemical recipes.
- Additional dimensions or progression systems.

The goal is to make **one highly polished, reliable and expandable Create processing machine**, rather than a collection of partially developed mechanics.
