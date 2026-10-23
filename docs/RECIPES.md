# Laser processing recipes

Minecraft 1.21.1 uses the singular `recipe` directory: `data/<namespace>/recipe/<name>.json`.

```json
{
  "type": "alchemicallaser:laser_processing",
  "ingredient": { "tag": "minecraft:sand" },
  "input_count": 1,
  "lens": "alchemicallaser:power_lens",
  "result": { "id": "minecraft:glass", "count": 1 },
  "duration": 100,
  "minimum_rpm": 16,
  "secondary": { "id": "minecraft:quartz", "count": 1 },
  "secondary_chance": 0.1,
  "copy_components": false
}
```

`duration` is processing time in ticks at 32 RPM, plus the configured charging period. Speed scales processing time up to the effective speed cap. Lenses are reusable catalysts. A bulk cycle performs `floor(stack_count / input_count)` recipe operations in one processing duration, retaining any incomplete quantity. Outputs split into legal stack sizes. Secondary chance rolls independently for every operation. The same rules apply to belts, Depots and each dropped stack directly under the nozzle. Removing power, disabling the emitter or changing the lens restarts unfinished work without consuming input.

`secondary`, `input_count`, `duration`, `minimum_rpm`, `secondary_chance`, and `copy_components` are optional. Their defaults are respectively absent, 1, 100, 16, 1.0, and false.

Input and output items can belong to any installed mod. Output stacks accept Minecraft 1.21.1 data components. With `copy_components: true`, input components are copied and the explicit output component patch is applied afterward. For state-changing recipes, use a component-sensitive NeoForge ingredient to ensure the already-processed item no longer matches. Do not create a self-matching recipe on a Depot unless repeated processing is intended.

Recipes are selected in recipe-ID order if several match. `/reload` updates recipes; an interrupted cycle never consumes input before completion.

## KubeJS

Use the generic custom recipe API; no KubeJS addon is required:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'alchemicallaser:sand_to_glass' })
  event.custom({
    type: 'alchemicallaser:laser_processing',
    ingredient: { item: 'minecraft:amethyst_shard' },
    input_count: 2,
    lens: 'alchemicallaser:splitting_lens',
    result: { id: 'minecraft:quartz', count: 3 },
    duration: 160,
    minimum_rpm: 32
  }).id('kubejs:laser_quartz')
})
```

Place scripts in `kubejs/server_scripts/`. The generic JSON path is implemented; compatibility with a particular KubeJS build must be checked in that installation.

## Custom lenses

Lens appearance and audio pitch use a synced NeoForge item data map at `data/alchemicallaser/data_maps/item/laser_lenses.json`. Override existing lens entries or give another registered item lens behavior:

```json
{
  "values": {
    "minecraft:emerald": { "color": 5767026, "pitch": 1.1 }
  }
}
```

`color` is a decimal RGB value from 0 to 16777215; `pitch` is 0.5–2.0. A recipe can then require `"lens": "minecraft:emerald"`. The item remains reusable, with one stored in the emitter. Existing lens item textures are unchanged by a beam-color override.

## Built-in lenses (0.3.0)

| Lens ID | Starter transformation | Minimum RPM |
|---|---|---|
| `alchemicallaser:growth_lens` | Wheat seeds → wheat | 16 |
| `alchemicallaser:convergence_lens` | 4 amethyst shards → amethyst block | 32 |
| `alchemicallaser:splitting_lens` | Amethyst block → 4 amethyst shards | 32 |
| `alchemicallaser:power_lens` | Obsidian → crying obsidian | 64 |
| `alchemicallaser:eclipse_lens` | Sculk → echo shard | 128 |

These are reusable recipe catalysts. They do not apply hardcoded world-growth, multi-beam or damage effects. Use these lens IDs with the same recipe type and optional fields described above.
