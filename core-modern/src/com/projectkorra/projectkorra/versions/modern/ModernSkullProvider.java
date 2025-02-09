package com.projectkorra.projectkorra.versions.modern;

import com.projectkorra.projectkorra.versions.ISkullProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class ModernSkullProvider implements ISkullProvider {

	@Override
	public ItemStack getSkull(String skin) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();

		if (meta == null) return new ItemStack(Material.DIRT);

		if (skin.matches("[\\w\\d_]{3,16}")) {
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(skin));
			skull.setItemMeta(meta);
			return skull;
		} else {
			try {
				if (skin.startsWith("https://") || skin.startsWith("http://")) {
					PlayerProfile profile = createProfileWithTexture(skin);
					meta.setOwnerProfile(profile);
					skull.setItemMeta(meta);
					return skull;
				}
			} catch (MalformedURLException e) {
				Bukkit.getServer().getLogger().severe(e.getMessage());
			}
		}
		skull.setItemMeta(meta);
		return skull;
	}

	private PlayerProfile createProfileWithTexture(String skinUrl) throws MalformedURLException {
		URL url = new URL(skinUrl);
		String texture = Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes());
		PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
		try {
			Field profileField = profile.getClass().getDeclaredField("properties");
			profileField.setAccessible(true);
			Object propertyMap = profileField.get(profile);
			Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
			Object property = propertyClass.getConstructor(String.class, String.class).newInstance("textures", texture);
			propertyMap.getClass().getMethod("put", Object.class, Object.class).invoke(propertyMap, "textures", property);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return profile;
	}
}
