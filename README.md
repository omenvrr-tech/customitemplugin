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
- **Voidreaver** — legendary sword, `VoidreaverItem`. On-hit abilities (base
  damage/speed is whatever Netherite Sword normally has - no extra stat block, per
  spec):
  - **Void Slash** — every 4th hit sends a wave dealing 150% of that hit's damage to
    everything else within 4 blocks of the victim.
  - **Life Steal V** — heals the attacker for 5% of damage dealt, every hit.
  - **Ender's Grasp** — 20% chance per hit to reduce the victim's armor by 25% for 5s
    (via a temporary attribute modifier).
  - Craftable: Ender Pearl (corners) / Nether Star (top+bottom middle) / Netherite
    Ingot (left+right middle) / **Netherite Sword** (center — substituted for the
    non-existent "Wither Skeleton Sword"; change the `'W'` ingredient in
    `registerVoidreaverRecipe()` if you add a real item for that slot later).
  - Uses `OnHitAbilityItem` (a new interface, like `AbilityItem` but triggered on
    landing a melee hit instead of right-click) — also respects Fool's Magic
    suppression automatically.
  - Texture is a hand-generated pixel sprite on the standard `item/handheld` model
    (the same technique vanilla swords use — auto-extruded to real 3D depth in-hand).
    It is NOT a hand-modeled Blockbench mesh like `demonichammer.json` is; if you want
    that exact chunky-3D look from the reference art, that needs building in Blockbench.
- **Timekeeper's Watch** — `TimekeeperWatchItem`, real Blockbench model. Right-click:
  freezes every other living entity within 20 blocks for 5 seconds (players: walk
  speed set to 0; mobs: AI disabled), while you move normally. 90 second cooldown
  (tracked per-item via a timestamp, plus a cosmetic vanilla cooldown swipe on the
  clock icon). Freezing does **not** grant invulnerability — frozen targets can still
  be damaged by you or anything else, they just can't act. Particles/sound play on
  cast and around frozen targets for the duration. Uses `AbilityItem`, so it
  automatically respects Fool's Magic suppression like everything else.
- **Emperor's Crown** — `EmperorCrownItem`, real Blockbench model, worn in the helmet
  slot (base: Golden Helmet, so it equips the normal vanilla way). Purely passive —
  no right-click ability, so it's intentionally NOT suppressible by the Fool's Magic
  card. All logic lives in `CrownEffectsListener`:
  - **Silver Tongue** — 20% off villager trade prices while worn (temporarily patches
    the villager's trade list on interact, restores it on close so other players
    still see normal prices).
  - **Divisive Opinions** — cancels Invisibility potion effects on the wearer.
  - **Unshakable Presence** — cancels Slowness/Weakness/Nausea/Blindness/Poison/
    Wither/Hunger/Mining Fatigue/Unluck/Darkness on the wearer (edit
    `NEGATIVE_EFFECTS` in `CrownEffectsListener` to add/remove effects). Note: this
    also makes the wearer immune to Timekeeper's Watch's freeze, since that uses
    Slowness under the hood in some future revision — currently freeze uses walk
    speed/AI directly, not a potion effect, so it is NOT blocked by this; flagging in
    case you change the freeze implementation later.
  - **Golden Tongue** — hostile mobs within 20 blocks can't target the wearer,
    and creepers near the wearer won't explode.
  - **+5 hearts** while worn, via a temporary max-health attribute modifier
    (added/removed on equip/unequip, reconciled on login).

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
  (`fools_magic`, `demonic_hammer`, `voidreaver`, `timekeeper_watch`, `emperor_crown`)
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
