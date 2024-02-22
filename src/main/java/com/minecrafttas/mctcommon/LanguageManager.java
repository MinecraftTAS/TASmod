package com.minecrafttas.mctcommon;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class LanguageManager {

    private static Set<String> modids = new HashSet<>();
    private static final Splitter SPLITTER = Splitter.on('=').limit(2);
    private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

    public static void onResourceManagerReload(Map<String, String> original, IResourceManager iResourceManager, List<String> languageList) {
        for (String language : languageList) { // Go through all loaded languages
            language = language.toLowerCase(); // Set everything to lowercase which prevents headaches in 1.10.2 and below
            for (String modid : modids) {      // Iterate through all registered modids
                HashMap<String, String> newTranslations = new HashMap<>();
                if (iResourceManager.getResourceDomains().contains(modid)) {
                    try {
                        newTranslations = getFromResourcePack(iResourceManager, modid, language);   // Load .json translations from resource pack
                    } catch (IOException var9) {
                    }
                }
                if (newTranslations.isEmpty()) {
                    try {
                        newTranslations = loadLang(getFromResources(modid, language, "lang")); // Load .lang files from resources
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (newTranslations.isEmpty()) {
                    newTranslations = loadJson(getFromResources(modid, language, "json")); // Load .json translations from resources
                }
                /**
                 * Making this put if absent here creates the following hirarchy:
                 * Resourcepack .lang beats
                 * Resourcepack .json beats
                 * Resources .lang beats
                 * Resources .json
                 *
                 * Lang is preferred over json and resourcepacks are preferred over json
                 */
                newTranslations.forEach(original::putIfAbsent);
            }
        }
    }

    private static InputStream getFromResources(String resourceDomain, String language, String fileending) {
        return LanguageManager.class.getResourceAsStream(String.format("/assets/%s/lang/%s.%s", resourceDomain, language, fileending));
    }

    private static HashMap<String, String> getFromResourcePack(IResourceManager iResourceManager, String resourceDomain, String language) throws IOException {
        String languageFile = String.format("lang/%s.json", language);
        HashMap<String, String> out = new HashMap<>();
        Collection<IResource> allResources = iResourceManager.getAllResources(new ResourceLocation(resourceDomain, languageFile));
        for (IResource iResource : allResources) {
            InputStream inputStream = iResource.getInputStream();
            out.putAll(loadJson(inputStream));
        }
        return out;
    }

    /**
     * Registers your mod to be processed by the language manager<br>
     * This will allow you to add .json and/or .lang files to assets/modid/lang<br>
     * with en_us.lang/en_us.json (<strong>lowercase!</strong>)
     * 
     * @param modid The modid of your mod
     */
    public static void registerMod(String modid) {
        modids.add(modid);
    }

    private static HashMap<String, String> loadJson(InputStream inputStream) {
        if (inputStream == null) {
            return new HashMap<String, String>();
        }
        Gson gson = new Gson();
        HashMap<String, String> template = new HashMap<>();
        
        @SuppressWarnings("unchecked")
		HashMap<String, String> out = (HashMap<String, String>) gson.fromJson(new InputStreamReader(inputStream), template.getClass());
        out.forEach((key, value) -> {
            value = PATTERN.matcher(value).replaceAll("%$1s");
        });
        return out;
    }

    private static HashMap<String, String> loadLang(InputStream inputStream) throws IOException {
        HashMap<String, String> out = new HashMap<>();
        if (inputStream == null) {
            return out;
        }
        for (String string : IOUtils.readLines(inputStream, StandardCharsets.UTF_8)) {
            if (!string.isEmpty() && string.charAt(0) != '#') {
                String[] key_value_pair = Iterables.toArray(SPLITTER.split(string), String.class);
                if (key_value_pair != null && key_value_pair.length == 2) {
                    String key = key_value_pair[0];
                    String value = PATTERN.matcher(key_value_pair[1]).replaceAll("%$1s");
                    out.put(key, value);
                }
            }
        }
        return out;
    }
}
