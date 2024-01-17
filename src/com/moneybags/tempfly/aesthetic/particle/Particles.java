package com.moneybags.tempfly.aesthetic.particle;

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

import java.util.Random;
import java.util.UUID;

public class Particles {

    private static final Random rand = new Random();
    private static TempFly tempfly;
    private static boolean oldParticles;
    private static final String DEFAULT_PARTICLE = "HAPPY_VILLAGER";

    public static void initialize(TempFly plugin) {
        tempfly = plugin;
        oldParticles = oldParticles();
    }

    public static boolean oldParticles() {
        String version = Bukkit.getVersion();
        int majorVersion = Integer.parseInt(version.split("\\.")[1]);
        return majorVersion <= 8; // 1.8 or below 
    }

    public void playAsync(final Location loc, final String s) {
        new BukkitRunnable() {
            @Override
            public void run() {
                play(loc, s);
            }
        }.runTaskAsynchronously(tempfly);
    }

    private static Particle switchParticle(String s) {
        try {
            return Particle.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Particle.VILLAGER_HAPPY;
        }
    }

    public static void play(Location loc, String s) {
        if (!oldParticles) {
            Particle particle = switchParticle(s != null && s.equalsIgnoreCase("ITEM_BREAK") ? "HAPPY_VILLAGER" : s);

            Class<?> c = particle.getDataType();
            try {
                if (Particle.DustOptions.class.equals(c)) {
                    Color[] colors = {Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))};
                    loc.getWorld().spawnParticle(particle, loc, 1, new DustOptions(colors[rand.nextInt(colors.length)], 2f));
                } else if (org.bukkit.block.data.BlockData.class.equals(c)) {
                    loc.getWorld().spawnParticle(particle, loc, 1, Material.STONE.createBlockData());
                } else {
                    loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0.1);
                }
            } catch (Exception e) {
                loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0, 0.1);
            }
        } else {
            Effect particle = switchEffect(s != null && s.equalsIgnoreCase("ITEM_BREAK") ? "HAPPY_VILLAGER" : s);
            loc.getWorld().playEffect(loc, particle, 1);
        }
    }

    private static Effect switchEffect(String s) {
        try {
            return Effect.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Effect.valueOf(DEFAULT_PARTICLE);
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
