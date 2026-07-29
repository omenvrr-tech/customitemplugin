# CustomItems — Fool's Magic plugin

## What this does

- **The Fool's Magic card** — a paper item with no ability of its own.
  - Right-click it to cycle its aura radius: **Off → 5 → 10 → 25 → Off** (edit `radius-cycle`
    in `config.yml` to change these values). You have to be holding it to right-click it.
  - Once set, the aura is active from **anywhere in the holder's inventory** — hotbar,
    main inventory, or offhand — it does not need to stay in their hand.
  - While the radius is active, **every AbilityItem** (see below) belonging to
    **any player within that radius** — including the card holder's own other special
    items — will silently fail to activate. The card itself is never suppressed.
  - **Pauses automatically while the holder is dead** (between death and respawn), and
    resumes the moment they respawn.
  - **Indestructible as a dropped item**: immune to fire, lava, and explosions, never
    despawns, and is flagged Unbreakable (no durability loss). It can still be picked up,
    moved, or dropped normally — it just can't be destroyed once it exists.
- **AbilityItem framework** — an extensible base for every other special item you add.
  `DemonicHammerItem` is included as a working example/template (right-click AoE
  damage + knockback). To add a new special item:
  1. Create a class implementing `CustomItem` (and `AbilityItem` if it has a
     right-click ability).
  2. Register it in `CustomItemsPlugin#onEnable`:
     `itemRegistry.register(new YourNewItem());`
  3. Done — the Fool's Magic card automatically suppresses it. No other code changes needed.

## Requirements

- **Paper 1.21.4+** (needed for the `assets/<namespace>/items/*.json` resource-pack
  format and the `DataComponentTypes.ITEM_MODEL` component your items use).
- Java 21.
- Maven, to build.

## Building

```
cd customitems-plugin
mvn package
```

The compiled jar will be at `target/customitems.jar`. Drop it in your server's `plugins/` folder.

If `mvn package` fails to resolve `paper-api`, make sure you have internet access to
`https://repo.papermc.io/repository/maven-public/` (declared in `pom.xml`), or swap in
whatever Paper API version matches your server.

## Resource pack

The `resourcepack/` folder is your (now-fixed) texture pack — `blank:foolsmagic` and
`blank:demonichammer` — zip its **contents** (not the folder itself) into a
`resourcepack.zip` and host/apply it the way you normally do (server resource pack,
`resource-pack` in `server.properties`, etc.). The plugin's item creation code points
at these exact resource locations, so as long as namespace/paths match, textures will
show correctly.

## Commands

- `/giveitem <item-id> [player]` — give yourself or someone else a registered item.
  (`fools_magic`, `demonic_hammer`)
- `/itemslist` — list every registered item id.

Both require the `customitems.give` permission (defaults to op).

## Notes / things you'll likely want to tweak

- Suppression currently only checks players **in the same world** as the target
  (block-radius auras don't really make sense across worlds). If you want a
  same-server-wide aura regardless of world, change `SuppressionManager`.
- The Fool's radius is stored **on the item stack itself** via PersistentDataContainer,
  so it survives restarts/trades and different cards can have different settings.
- `SuppressionManager.isSuppressed()` scans online players each time an ability is
  used — fine for normal server sizes. If you have hundreds of concurrent players and
  notice lag, cache active Fool radii and update them only when a card is toggled
  instead of scanning inventories on every check.
