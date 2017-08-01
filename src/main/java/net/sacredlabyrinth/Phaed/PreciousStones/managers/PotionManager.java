/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * @author telaeris
 */
public class PotionManager {
    private PreciousStones plugin;

    /**
     *
     */
    public PotionManager() {
        plugin = PreciousStones.getInstance();
    }

    public void applyPotions(Player player, Field field) {

        if (field.hasFlag(FieldFlag.POTION_IGNORE_PLAYER)) {
            return;
        }

        HashMap<PotionEffectType, Integer> potions = field.getSettings().getPotions();

        for (Entry<PotionEffectType, Integer> potion : potions.entrySet()) {
            int intensity = potion.getValue();
            PotionEffectType pot = potion.getKey();

            if (!player.hasPotionEffect(pot)) {
                if (plugin.getPermissionsManager().has(player, "preciousstones.manual.bypass.potions")) {
                    if (plugin.getSettingsManager().isHarmfulPotion(pot)) {
                        return;
                    }
                }

                if (!player.hasPotionEffect(pot)) {
                    player.addPotionEffect(new PotionEffect(pot, 72000, intensity));
                }
                plugin.getPermissionsManager().allowFast(player);
                plugin.getPermissionsManager().allowFly(player);
            }
        }
    }

    public void removePotions(Player player, Field field) {
        HashMap<PotionEffectType, Integer> potions = field.getSettings().getPotions();

        for (PotionEffectType pot : potions.keySet()) {
            if (player.hasPotionEffect(pot)) {
                player.removePotionEffect(pot);
                plugin.getPermissionsManager().resetFast(player);
            }
        }
    }

    public void neutralizePotions(Player player, Field field) {
        List<PotionEffectType> noPotions = field.getSettings().getNeutralizePotions();

        for (PotionEffectType pot : noPotions) {
            if (player.hasPotionEffect(pot)) {
                player.removePotionEffect(pot);
                plugin.getPermissionsManager().resetFast(player);
                plugin.getCommunicationManager().showNoPotion(player, pot.getName());
            }
        }
    }

    public void addEffectToFieldEntities(Field field) {
        Stream<String> types = field.getSettings().getPotionTargets().stream();
        if (types.count() == 0) {
            return;
        }

        int longestRadius = field.getLongestSide() / 2;
        int height = field.getHeight();
        Collection<Entity> nearbyEntities = field.getLocation().getWorld().getNearbyEntities(field.getLocation(), longestRadius, height, longestRadius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                addEffectToEntity(field, (LivingEntity) entity, types);
            }
        }
    }

    public void removeEffectFromFieldEntities(Field field) {
        Stream<String> types = field.getSettings().getPotionTargets().stream();
        if (types.count() == 0) {
            return;
        }

        int longestRadius = field.getLongestSide() / 2;
        int height = field.getHeight();
        Collection<Entity> nearbyEntities = field.getLocation().getWorld().getNearbyEntities(field.getLocation(), longestRadius, height, longestRadius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                removeEffectFromEntity(field, (LivingEntity) entity, types);
            }
        }
    }

    public void addEffectToEntity(Field field, LivingEntity entity, Stream<String> types) {
        if (field.envelops(entity.getLocation())) {
            if (isMatchingLivingEntity(entity, types)) {
                HashMap<PotionEffectType, Integer> potions = field.getSettings().getPotions();

                for (Entry<PotionEffectType, Integer> potion : potions.entrySet()) {
                    int intensity = potion.getValue();
                    PotionEffectType pot = potion.getKey();

                    if (!entity.hasPotionEffect(pot)) {
                        entity.addPotionEffect(new PotionEffect(pot, 72000, intensity));
                    }
                }
            }
        }
    }

    public void removeEffectFromEntity(Field field, LivingEntity entity, Stream<String> types) {
        if (field.envelops(entity.getLocation())) {
            if (isMatchingLivingEntity(entity, types)) {
                HashMap<PotionEffectType, Integer> potions = field.getSettings().getPotions();

                for (Entry<PotionEffectType, Integer> potion : potions.entrySet()) {
                    PotionEffectType pot = potion.getKey();

                    if (entity.hasPotionEffect(pot)) {
                        entity.removePotionEffect(pot);
                    }
                }
            }
        }
    }

    private boolean isMatchingLivingEntity(Entity entity, Stream<String> types) {
        boolean affected = false;

        if (entity instanceof Player) {
            return false;
        }

        if (entity instanceof Ambient) {
            if (types.anyMatch("Ambient"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Ageable) {
            if (types.anyMatch("Ageable"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Animals) {
            if (types.anyMatch("Animals"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof ArmorStand) {
            if (types.anyMatch("ArmorStand"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Bat) {
            if (types.anyMatch("Bat"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Blaze) {
            if (types.anyMatch("Blaze"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof CaveSpider) {
            if (types.anyMatch("CaveSpider"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof ChestedHorse) {
            if (types.anyMatch("ChestedHorse"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Chicken) {
            if (types.anyMatch("Chicken"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Cow) {
            if (types.anyMatch("Cow"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Creature) {
            if (types.anyMatch("Creature"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Creeper) {
            if (types.anyMatch("Creeper"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Donkey) {
            if (types.anyMatch("Donkey"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof ElderGuardian) {
            if (types.anyMatch("ElderGuardian"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof EnderDragon) {
            if (types.anyMatch("EnderDragon"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Enderman) {
            if (types.anyMatch("Enderman"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Endermite) {
            if (types.anyMatch("Endermite"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Evoker) {
            if (types.anyMatch("Evoker"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Flying) {
            if (types.anyMatch("Flying"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Ghast) {
            if (types.anyMatch("Ghast"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Giant) {
            if (types.anyMatch("Giant"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Golem) {
            if (types.anyMatch("Golem"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Guardian) {
            if (types.anyMatch("Guardian"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Horse) {
            if (types.anyMatch("Horse"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof HumanEntity) {
            if (types.anyMatch("HumanEntity"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Husk) {
            if (types.anyMatch("Husk"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Illager) {
            if (types.anyMatch("Illager"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Illusioner) {
            if (types.anyMatch("Illusioner"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof IronGolem) {
            if (types.anyMatch("IronGolem"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Llama) {
            if (types.anyMatch("Llama"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof MagmaCube) {
            if (types.anyMatch("MagmaCube"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Monster) {
            if (types.anyMatch("Monster"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Mule) {
            if (types.anyMatch("Mule"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof MushroomCow) {
            if (types.anyMatch("MushroomCow"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof NPC) {
            if (types.anyMatch("NPC"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Ocelot) {
            if (types.anyMatch("Ocelot"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Parrot) {
            if (types.anyMatch("Parrot"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Pig) {
            if (types.anyMatch("Pig"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof PigZombie) {
            if (types.anyMatch("PigZombie"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Player) {
            if (types.anyMatch("Player"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof PolarBear) {
            if (types.anyMatch("PolarBear"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Rabbit) {
            if (types.anyMatch("Rabbit"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Sheep) {
            if (types.anyMatch("Sheep"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Shulker) {
            if (types.anyMatch("Shulker"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Silverfish) {
            if (types.anyMatch("Silverfish"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Skeleton) {
            if (types.anyMatch("Skeleton"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof SkeletonHorse) {
            if (types.anyMatch("SkeletonHorse"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Slime) {
            if (types.anyMatch("Slime"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Snowman) {
            if (types.anyMatch("Snowman"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Spellcaster) {
            if (types.anyMatch("Spellcaster"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Spider) {
            if (types.anyMatch("Spider"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Squid) {
            if (types.anyMatch("Squid"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Stray) {
            if (types.anyMatch("Stray"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Vex) {
            if (types.anyMatch("Vex"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Villager) {
            if (types.anyMatch("Villager"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Vindicator) {
            if (types.anyMatch("Vindicator"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof WaterMob) {
            if (types.anyMatch("WaterMob"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Witch) {
            if (types.anyMatch("Witch"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Wither) {
            if (types.anyMatch("Wither"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof WitherSkeleton) {
            if (types.anyMatch("WitherSkeleton"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Wolf) {
            if (types.anyMatch("Wolf"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof Zombie) {
            if (types.anyMatch("Zombie"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof ZombieHorse) {
            if (types.anyMatch("ZombieHorse"::equalsIgnoreCase)) {
                affected = true;
            }
        }
        if (entity instanceof ZombieVillager) {
            if (types.anyMatch("ZombieVillager"::equalsIgnoreCase)) {
                affected = true;
            }
        }

        return affected;
    }
}
