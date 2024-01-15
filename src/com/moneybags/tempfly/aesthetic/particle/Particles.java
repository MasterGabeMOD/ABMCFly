package com.moneybags.tempfly.aesthetic.particle;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.DataBridge.DataValue;
import com.moneybags.tempfly.util.data.DataPointer;

public class Particles {

    private static Class<?> dustOptions = Particle.DustOptions.class;
    private static Class<?> blockData = org.bukkit.block.data.BlockData.class;
    private static TempFly tempfly;
    private static boolean oldParticles;

    public static void initialize(TempFly plugin) {
        tempfly = plugin;
        oldParticles = oldParticles();
    }

    public static boolean oldParticles() {
        String version = Bukkit.getVersion();
        int majorVersion = Integer.parseInt(version.split("\\.")[1]);
        return majorVersion <= 8; // 1.8 or below 
    }

    private static Random rand = new Random();

    public static void playAsync(final Location loc, final String s) {
        new BukkitRunnable() {
            @Override
            public void run() {
                play(loc, s);
            }
        }.runTaskAsynchronously(tempfly);
    }

    public static void play(Location loc, String s) {
        if (!oldParticles) {
            Particle particle = null;
            try {
                particle = Particle.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException e1) {
                try {
                    particle = Particle.valueOf(V.particleType.toUpperCase());
                } catch (IllegalArgumentException e2) {
                    particle = Particle.VILLAGER_HAPPY;
                }
            }

            Class<?> c = particle.getDataType();
            try {
                if (dustOptions.equals(c)) {
                    loc.getWorld().spawnParticle(particle, loc, 1,
                            new DustOptions(Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), 2f));
                } else if (blockData.equals(c)) {
                    loc.getWorld().spawnParticle(particle, loc, 1, Material.STONE.createBlockData());
                } else {
                    loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0.1);
                }
            } catch (Exception e) {
                loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0, 0.1);
            }
        } else {
            Effect particle = null;
            if (s != null && s.equalsIgnoreCase("ITEM_BREAK")) {
                s = "HAPPY_VILLAGER";
            }
            try {
                particle = Effect.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException e1) {
                try {
                    particle = Effect.valueOf(V.particleType);
                } catch (IllegalArgumentException e2) {
                    particle = Effect.valueOf("HAPPY_VILLAGER");
                }
            }
            loc.getWorld().playEffect(loc, particle, 1);
        }
    }

    public static String loadTrail(UUID u) {
        String particle = (String) tempfly.getDataBridge().getOrDefault(DataPointer.of(DataValue.PLAYER_TRAIL, u.toString()), null);
        if (V.debug) {
            Console.debug("", "------Loading particle trail------", "Player: " + u.toString(),
                    "Value from data: " + String.valueOf(particle), "Default trail enabled: " + V.particleDefault,
                    "Default trail is: " + V.particleType, "Returning trail: "
                            + (particle != null ? particle : (V.particleDefault ? V.particleType : "")),
                    "------End particle trail------", "");
        }
        return particle != null ? particle : (V.particleDefault ? V.particleType : "");
    }

    public static void setTrail(UUID u, String particle) {
        FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
        if (user != null) {
            user.setTrail(particle);
        } else {
            tempfly.getDataBridge().stageChange(DataPointer.of(DataValue.PLAYER_TRAIL, u.toString()), particle);
        }
    }

}
