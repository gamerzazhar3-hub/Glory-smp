package me.glorysmp.weapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class GlorySMPWeapons extends JavaPlugin implements Listener, TabExecutor {
    private NamespacedKey weaponKey;
    private final Map<String, Long> cooldowns = new HashMap<>();

    private enum Weapon {
        GLORY_SWORD("glory_sword", "§6👑 Glory Sword", Material.NETHERITE_SWORD, 1001, "Lightning effect + Strength"),
        FIRE_BLADE("fire_blade", "§c🔥 Fire Blade", Material.NETHERITE_SWORD, 1002, "Burning hits + flame particles"),
        ICE_KATANA("ice_katana", "§b❄ Ice Katana", Material.NETHERITE_SWORD, 1003, "Slowness + frost effects"),
        THUNDER_AXE("thunder_axe", "§e⚡ Thunder Axe", Material.NETHERITE_AXE, 1004, "Right-click lightning; 10s cooldown"),
        SHADOW_DAGGER("shadow_dagger", "§8🌑 Shadow Dagger", Material.NETHERITE_SWORD, 1005, "Speed + brief invisibility"),
        LIFESTEAL_SWORD("lifesteal_sword", "§c❤ Lifesteal Sword", Material.NETHERITE_SWORD, 1006, "Recover health on hit"),
        DRAGON_SCYTHE("dragon_scythe", "§5🐉 Dragon Scythe", Material.NETHERITE_HOE, 1007, "Weakness + dragon-breath particles"),
        POISON_KATANA("poison_katana", "§2☠ Poison Katana", Material.NETHERITE_SWORD, 1008, "Poison on hit"),
        STORM_SCYTHE("storm_scythe", "§3🌪 Storm Scythe", Material.NETHERITE_HOE, 1009, "Knockback + gust effect"),
        FROST_BOW("frost_bow", "§b🏹 Frost Bow", Material.BOW, 1010, "Slowness when an arrow hits"),
        METEOR_SWORD("meteor_sword", "§6☄ Meteor Sword", Material.NETHERITE_SWORD, 1011, "Small fire burst on hit"),
        SOUL_REAPER("soul_reaper", "§8💀 Soul Reaper", Material.NETHERITE_HOE, 1012, "Wither on hit"),
        BLOOD_SWORD("blood_sword", "§4🩸 Blood Sword", Material.NETHERITE_SWORD, 1013, "Bonus damage at low health"),
        MOON_BLADE("moon_blade", "§9🌙 Moon Blade", Material.NETHERITE_SWORD, 1014, "Night vision + speed"),
        SUN_BLADE("sun_blade", "§e☀ Sun Blade", Material.NETHERITE_SWORD, 1015, "Fire damage on hit"),
        EARTH_HAMMER("earth_hammer", "§2🌍 Earth Hammer", Material.NETHERITE_AXE, 1016, "Launches target upward"),
        WATER_TRIDENT("water_trident", "§3🌊 Water Trident", Material.TRIDENT, 1017, "Water-themed healing effects"),
        END_BLADE("end_blade", "§d✦ End Blade", Material.NETHERITE_SWORD, 1018, "End-themed strike + particles"),
        VOID_KATANA("void_katana", "§5🟣 Void Katana", Material.NETHERITE_SWORD, 1019, "Darkness + brief levitation"),
        EMERALD_BLADE("emerald_blade", "§a💚 Emerald Blade", Material.NETHERITE_SWORD, 1020, "Regeneration on hit");

        final String id, display, ability;
        final Material material;
        final int model;
        Weapon(String id, String display, Material material, int model, String ability) {
            this.id = id; this.display = display; this.material = material; this.model = model; this.ability = ability;
        }
        static Weapon byId(String id) {
            return Arrays.stream(values()).filter(w -> w.id.equalsIgnoreCase(id)).findFirst().orElse(null);
        }
    }

    @Override public void onEnable() {
        weaponKey = new NamespacedKey(this, "weapon_id");
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("gloryweapon").setExecutor(this);
        getCommand("gloryweapon").setTabCompleter(this);
        getLogger().info("GlorySMP V2 enabled: 20 weapons, no crafting.");
    }

    private ItemStack createWeapon(Weapon weapon) {
        ItemStack item = new ItemStack(weapon.material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(weapon.display);
        meta.setLore(List.of("§8Glory SMP V2", "§7Ability: " + weapon.ability, "§eLegendary Weapon", "§8ID: " + weapon.id));
        meta.setUnbreakable(true);
        meta.setCustomModelData(weapon.model);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, weapon.id);
        item.setItemMeta(meta);
        return item;
    }

    private Weapon getWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(weaponKey, PersistentDataType.STRING);
        return id == null ? null : Weapon.byId(id);
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("§6§lGlory SMP V2 — 20 Weapons");
            for (Weapon w : Weapon.values()) sender.sendMessage("§e• §f" + w.id + " §7— " + w.ability);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            Weapon w = Weapon.byId(args[1]);
            if (w == null) { sender.sendMessage("§cUnknown weapon ID. Use /gloryweapon list"); return true; }
            sender.sendMessage(w.display + " §7(ID: " + w.id + ")");
            sender.sendMessage("§7Ability: " + w.ability);
            sender.sendMessage("§7Cooldown: " + (w == Weapon.THUNDER_AXE ? "10 seconds" : "Passive / hit based"));
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("glory.admin")) { sender.sendMessage("§cYou need glory.admin permission."); return true; }
            Player target = Bukkit.getPlayerExact(args[1]);
            Weapon w = Weapon.byId(args[2]);
            if (target == null) { sender.sendMessage("§cPlayer must be online."); return true; }
            if (w == null) { sender.sendMessage("§cUnknown weapon ID. Use /gloryweapon list"); return true; }
            Map<Integer, ItemStack> leftovers = target.getInventory().addItem(createWeapon(w));
            leftovers.values().forEach(stack -> target.getWorld().dropItemNaturally(target.getLocation(), stack));
            sender.sendMessage("§aGave " + w.id + " to " + target.getName() + ".");
            target.sendMessage("§6You received " + w.display + "§6!");
            return true;
        }
        sender.sendMessage("§e/gloryweapon list");
        sender.sendMessage("§e/gloryweapon info <weapon>");
        sender.sendMessage("§e/gloryweapon give <player> <weapon> §7(OP only)");
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return partial(List.of("list", "info", "give"), args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) return partial(weaponIds(), args[1]);
        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("glory.admin"))
            return partial(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("glory.admin")) return partial(weaponIds(), args[2]);
        return List.of();
    }
    private List<String> weaponIds() { return Arrays.stream(Weapon.values()).map(w -> w.id).toList(); }
    private List<String> partial(List<String> values, String prefix) {
        String p = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(v -> v.toLowerCase(Locale.ROOT).startsWith(p)).collect(Collectors.toList());
    }

    @EventHandler public void onCraft(CraftItemEvent e) { e.setCancelled(true); }
    @EventHandler public void onSmith(SmithItemEvent e) { e.setCancelled(true); }
    @EventHandler public void onAnvil(PrepareAnvilEvent e) { e.setResult(null); }

    @EventHandler public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Weapon w = getWeapon(e.getItem());
        if (w == null) return;
        Player p = e.getPlayer();
        if (w == Weapon.THUNDER_AXE) {
            if (onCooldown(p, w, 10)) { p.sendMessage("§cThunder Axe is cooling down."); return; }
            var block = p.getTargetBlockExact(25);
            if (block == null) { p.sendMessage("§cAim at a block within 25 blocks."); return; }
            p.getWorld().strikeLightningEffect(block.getLocation().add(0.5, 1, 0.5));
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, block.getLocation().add(0.5, 1, 0.5), 35, .4, .7, .4, .05);
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, .7f, 1.2f);
            setCooldown(p, w, 10);
        } else if (w == Weapon.SHADOW_DAGGER) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1));
            p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 25, .5, .7, .5, .2);
        } else if (w == Weapon.MOON_BLADE) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1200, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
        } else if (w == Weapon.WATER_TRIDENT) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
            p.getWorld().spawnParticle(Particle.SPLASH, p.getLocation(), 25, .5, .6, .5, .1);
        }
    }

    @EventHandler public void onWeaponHit(EntityDamageByEntityEvent e) {
        Player attacker = null;
        Weapon weapon = null;
        if (e.getDamager() instanceof Player p) { attacker = p; weapon = getWeapon(p.getInventory().getItemInMainHand()); }
        else if (e.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player p) {
            attacker = p; weapon = getWeapon(p.getInventory().getItemInMainHand());
            if (weapon != Weapon.FROST_BOW) weapon = null;
        }
        if (attacker == null || weapon == null || !(e.getEntity() instanceof LivingEntity target)) return;
        Player p = attacker;
        switch (weapon) {
            case GLORY_SWORD -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
                target.getWorld().strikeLightningEffect(target.getLocation());
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 20, .4, .5, .4, .04);
            }
            case FIRE_BLADE, SUN_BLADE -> { target.setFireTicks(100); target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 18, .3, .5, .3, .02); }
            case ICE_KATANA -> { target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1)); target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 20, .4, .5, .4, .03); }
            case LIFESTEAL_SWORD -> heal(p, 2.0);
            case DRAGON_SCYTHE -> { target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1)); target.getWorld().spawnParticle(Particle.DRAGON_BREATH, target.getLocation().add(0, 1, 0), 18, .3, .4, .3, .02); }
            case POISON_KATANA -> { target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1)); target.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, target.getLocation().add(0, 1, 0), 12, .3, .4, .3); }
            case STORM_SCYTHE -> { target.setVelocity(target.getVelocity().add(p.getLocation().getDirection().multiply(.8).setY(.35))); target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation(), 15, .4, .3, .4, .03); }
            case FROST_BOW -> target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            case METEOR_SWORD -> { target.setFireTicks(60); target.getWorld().spawnParticle(Particle.LAVA, target.getLocation(), 8, .3, .3, .3); }
            case SOUL_REAPER -> { target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1)); target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 18, .3, .5, .3, .02); }
            case BLOOD_SWORD -> { if (p.getHealth() <= 8.0) e.setDamage(e.getDamage() + 2.0); }
            case EARTH_HAMMER -> target.setVelocity(target.getVelocity().setY(1.0));
            case END_BLADE -> { target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 20, .4, .5, .4, .2); p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0)); }
            case VOID_KATANA -> { target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0)); target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 0)); }
            case EMERALD_BLADE -> p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            default -> { }
        }
    }

    private void heal(Player p, double amount) {
        double max = p.getAttribute(Attribute.MAX_HEALTH) == null ? 20.0 : p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + amount));
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 1, 0), 5, .3, .3, .3);
    }
    private boolean onCooldown(Player p, Weapon w, int seconds) {
        String key = p.getUniqueId() + ":" + w.id;
        Long end = cooldowns.get(key);
        return end != null && end > System.currentTimeMillis();
    }
    private void setCooldown(Player p, Weapon w, int seconds) {
        cooldowns.put(p.getUniqueId() + ":" + w.id, System.currentTimeMillis() + seconds * 1000L);
    }
}
