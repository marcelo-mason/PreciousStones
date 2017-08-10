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
        List<String> types = field.getSettings().getPotionTargets();
        if (types.size() == 0) {
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
        List<String> types = field.getSettings().getPotionTargets();
        if (types.size() == 0) {
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

    public void addEffectToEntity(Field field, LivingEntity entity, List<String> types) {
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

    public void removeEffectFromEntity(Field field, LivingEntity entity, List<String> types) {
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

    private boolean isMatchingLivingEntity(Entity entity, List<String> types) {
        if (entity instanceof Player) {
            return false;
        }

        if (entity instanceof Ambient) {
            if (types.stream().anyMatch("Ambient"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Ageable) {
            if (types.stream().anyMatch("Ageable"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Animals) {
            if (types.stream().anyMatch("Animals"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof ArmorStand) {
            if (types.stream().anyMatch("ArmorStand"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Bat) {
            if (types.stream().anyMatch("Bat"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Blaze) {
            if (types.stream().anyMatch("Blaze"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof CaveSpider) {
            if (types.stream().anyMatch("CaveSpider"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof ChestedHorse) {
            if (types.stream().anyMatch("ChestedHorse"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Chicken) {
            if (types.stream().anyMatch("Chicken"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Cow) {
            if (types.stream().anyMatch("Cow"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Creature) {
            if (types.stream().anyMatch("Creature"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Creeper) {
            if (types.stream().anyMatch("Creeper"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Donkey) {
            if (types.stream().anyMatch("Donkey"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof ElderGuardian) {
            if (types.stream().anyMatch("ElderGuardian"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof EnderDragon) {
            if (types.stream().anyMatch("EnderDragon"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Enderman) {
            if (types.stream().anyMatch("Enderman"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Endermite) {
            if (types.stream().anyMatch("Endermite"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Evoker) {
            if (types.stream().anyMatch("Evoker"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Flying) {
            if (types.stream().anyMatch("Flying"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Ghast) {
            if (types.stream().anyMatch("Ghast"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Giant) {
            if (types.stream().anyMatch("Giant"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Golem) {
            if (types.stream().anyMatch("Golem"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Guardian) {
            if (types.stream().anyMatch("Guardian"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Horse) {
            if (types.stream().anyMatch("Horse"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof HumanEntity) {
            if (types.stream().anyMatch("HumanEntity"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Husk) {
            if (types.stream().anyMatch("Husk"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Illager) {
            if (types.stream().anyMatch("Illager"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Illusioner) {
            if (types.stream().anyMatch("Illusioner"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof IronGolem) {
            if (types.stream().anyMatch("IronGolem"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Llama) {
            if (types.stream().anyMatch("Llama"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof MagmaCube) {
            if (types.stream().anyMatch("MagmaCube"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Monster) {
            if (types.stream().anyMatch("Monster"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Mule) {
            if (types.stream().anyMatch("Mule"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof MushroomCow) {
            if (types.stream().anyMatch("MushroomCow"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof NPC) {
            if (types.stream().anyMatch("NPC"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Ocelot) {
            if (types.stream().anyMatch("Ocelot"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Parrot) {
            if (types.stream().anyMatch("Parrot"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Pig) {
            if (types.stream().anyMatch("Pig"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof PigZombie) {
            if (types.stream().anyMatch("PigZombie"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Player) {
            if (types.stream().anyMatch("Player"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof PolarBear) {
            if (types.stream().anyMatch("PolarBear"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Rabbit) {
            if (types.stream().anyMatch("Rabbit"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Sheep) {
            if (types.stream().anyMatch("Sheep"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Shulker) {
            if (types.stream().anyMatch("Shulker"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Silverfish) {
            if (types.stream().anyMatch("Silverfish"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Skeleton) {
            if (types.stream().anyMatch("Skeleton"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof SkeletonHorse) {
            if (types.stream().anyMatch("SkeletonHorse"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Slime) {
            if (types.stream().anyMatch("Slime"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Snowman) {
            if (types.stream().anyMatch("Snowman"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Spellcaster) {
            if (types.stream().anyMatch("Spellcaster"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Spider) {
            if (types.stream().anyMatch("Spider"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Squid) {
            if (types.stream().anyMatch("Squid"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Stray) {
            if (types.stream().anyMatch("Stray"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Vex) {
            if (types.stream().anyMatch("Vex"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Villager) {
            if (types.stream().anyMatch("Villager"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Vindicator) {
            if (types.stream().anyMatch("Vindicator"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof WaterMob) {
            if (types.stream().anyMatch("WaterMob"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Witch) {
            if (types.stream().anyMatch("Witch"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Wither) {
            if (types.stream().anyMatch("Wither"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof WitherSkeleton) {
            if (types.stream().anyMatch("WitherSkeleton"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Wolf) {
            if (types.stream().anyMatch("Wolf"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof Zombie) {
            if (types.stream().anyMatch("Zombie"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof ZombieHorse) {
            if (types.stream().anyMatch("ZombieHorse"::equalsIgnoreCase)) {
                return true;
            }
        }
        if (entity instanceof ZombieVillager) {
            if (types.stream().anyMatch("ZombieVillager"::equalsIgnoreCase)) {
                return true;
            }
        }

        return false;
    }
}
