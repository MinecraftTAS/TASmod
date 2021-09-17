package de.scribble.lp.tasmod.shield;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class ShieldDownloader {
	private final ResourceLocation bottleshield = new ResourceLocation("tasmod:textures/shields/bottleshield.png");
	private final String defaultshield = "bottleshield";
	private final String cacheLocation = "tasmod/shields/";

	private Map<String, String> cacheNames;

	public static EntityLivingBase renderedEntity;

	@Deprecated
	public ResourceLocation getResourceLocationOld(EntityLivingBase entitylivingbaseIn) {
		String playerUUID = entitylivingbaseIn.getUniqueID().toString();
		String name = getShieldName(playerUUID);
		if (!name.contentEquals("bottleshield")) {
			return new ResourceLocation(cacheLocation + name);
		} else {
			return bottleshield;
		}
	}

	public ResourceLocation getResourceLocation(EntityLivingBase entitylivingbaseIn) {
		String uuid = entitylivingbaseIn.getUniqueID().toString();
		ResourceLocation cachedLocation = new ResourceLocation(cacheLocation + uuid);
		if (!rExists(cachedLocation)) {
			return bottleshield;
		} else {
			return cachedLocation;
		}
	}

	// ====================Downloading=====================

	public void onPlayerJoin(GameProfile profile) {
		String uuid = profile.getId().toString();
		loadTexture(uuid);
//		downloadFromTASTools(uuid, cacheLocation);
	}

	private void loadTexture(String uuid) {
		ResourceLocation cachedLocation = new ResourceLocation(cacheLocation + uuid);
		if (!rExists(cachedLocation)) {
			String urlname = "https://minecrafttas.com/" + uuid;
			URL url = null;
			TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
			try {
				url = new URL(urlname);
				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
				connection.setReadTimeout(5000);
				connection.setInstanceFollowRedirects(false);
				if (connection.getResponseCode() == HttpsURLConnection.HTTP_MOVED_PERM) {
					urlname = connection.getHeaderField("Location");
				} else {
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			IImageBuffer iib = new IImageBuffer() {
				public BufferedImage parseUserSkin(BufferedImage var1) {
					return var1;
				}

				public void skinAvailable() {
				}

			};
			ThreadDownloadImageData downloadedShield = new ThreadDownloadImageData(null, urlname, null, iib);
			textureManager.loadTexture(cachedLocation, downloadedShield);
		}
	}

	private boolean rExists(ResourceLocation resourceLocation) {
		ITextureObject resource = Minecraft.getMinecraft().getTextureManager().getTexture(resourceLocation);
		if (resource != null) {
			return true;
		}
		return false;
	}

	// =====================TASTools=====================

	@Deprecated
	public void downloadFromTASTools(String uuid, String location) {
		String name = getShieldName(uuid);
		if ((name != null) && (!name.isEmpty())) {
			String url = "https://raw.githubusercontent.com/ScribbleLP/MC-TASTools/1.12.2/shields/" + name;
			TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
			ResourceLocation cachedLocation = new ResourceLocation(location + name);
			if (!rExists(cachedLocation)) {
				IImageBuffer iib = new IImageBuffer() {
					public BufferedImage parseUserSkin(BufferedImage var1) {
						return var1;
					}

					public void skinAvailable() {
					}

				};
				ThreadDownloadImageData downloadedShield = new ThreadDownloadImageData(null, url, null, iib);
				textureManager.loadTexture(cachedLocation, downloadedShield);
			}
		}
	}

	@Deprecated
	private String getShieldName(String uuid) {
		cacheNames = downloadNames();
		if (cacheNames != null) {
			if (cacheNames.containsKey(uuid)) {
				return cacheNames.get(uuid);
			} else
				return defaultshield;
		} else
			return defaultshield;
	}

	@Deprecated
	private Map<String, String> downloadNames() {
		File feil = new File(Minecraft.getMinecraft().mcDataDir + File.separator + "playerstt.txt");
		URL url;
		Map<String, String> uuids = Maps.<String, String>newHashMap();

		try {
			url = new URL("https://raw.githubusercontent.com/ScribbleLP/MC-TASTools/1.12.2/shields/shieldnames.txt");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			uuids = mapNames(readThingsFromURL(url, feil));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return uuids;
	}

	@Deprecated
	private Map<String, String> mapNames(List<String> input) {
		Map<String, String> output = Maps.<String, String>newHashMap();
		for (int i = 0; i < input.size(); i++) {
			String line = input.get(i);
			if (line.contains(":")) {
				String[] split = line.split(":");
				output.put(split[0], split[1]);
			} else
				return null;
		}
		return output;
	}

	private List<String> readThingsFromURL(URL url, File file) throws IOException {
		ReadableByteChannel readableByteChannel;
		readableByteChannel = Channels.newChannel(url.openStream());
		FileOutputStream fileOutputStream;
		fileOutputStream = new FileOutputStream(file);
		fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
		fileOutputStream.close();
		return readThings(file);
	}

	private List<String> readThings(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		List<String> out = new ArrayList<String>();
		String s;
		while ((s = reader.readLine()) != null) {
			out.add(s);
		}
		reader.close();
		return out;
	}
}
