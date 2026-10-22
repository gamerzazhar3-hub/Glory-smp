# GlorySMP V2 — Paper 1.21.11

A source project for a Paper 1.21.11 plugin with 20 tagged custom weapons, item abilities, Thunder Axe right-click lightning (10s cooldown), `/gloryweapon list`, `/gloryweapon info <weapon>`, `/gloryweapon give <player> <weapon>`, tab completion, and crafting/smithing/anvil-result blocking. No custom armor is included.

## Build the JAR
### Easiest option if you only have a phone
1. Extract this ZIP.
2. Create a GitHub repository and upload all extracted project files/folders (including `.github/workflows/build.yml`).
3. Open the repository's **Actions** tab and select **Build GlorySMP V2 JAR**. If needed, tap **Run workflow**.
4. When the run finishes successfully, open that run and download the **GlorySMP-V2-JAR** artifact. Extract the downloaded artifact ZIP to get the `.jar`.

### Build on a computer
- Install Java 21 and Gradle 8.14+.
- In this folder run: `gradle build`
- The output JAR will be in `build/libs/GlorySMP_V2_Final-2.0.0.jar`.

Upload the resulting `.jar` to your hosting provider's Paper/Spigot `plugins` folder, then restart the server. Make sure the server runs a compatible Paper version.

## Commands
- `/gloryweapon list` — show all weapon IDs and abilities.
- `/gloryweapon info <weapon>` — show ability information.
- `/gloryweapon give <player> <weapon>` — give a weapon (OP / `glory.admin`).

Weapon IDs: `glory_sword`, `fire_blade`, `ice_katana`, `thunder_axe`, `shadow_dagger`, `lifesteal_sword`, `dragon_scythe`, `poison_katana`, `storm_scythe`, `frost_bow`, `meteor_sword`, `soul_reaper`, `blood_sword`, `moon_blade`, `sun_blade`, `earth_hammer`, `water_trident`, `end_blade`, `void_katana`, `emerald_blade`.

## Notes
- This archive contains source code and an automated GitHub Actions build workflow, not a precompiled JAR. A successful build still needs to be tested on a backup/test server before production use.
- The custom model data values are assigned, but custom PNG textures/resource pack files are not included yet; without a resource pack, weapons use their vanilla item appearance.
- Crafting table recipes, smithing, and anvil output are blocked. This is not a general server-wide recipe-removal system for every possible third-party plugin GUI.
