package com.minecrafttas.common;

import com.google.gson.Gson;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class LanguageManager {

    private static Set<String> modids = new HashSet<>();
    private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

    public static Map<String, String> onResourceManagerReload(IResourceManager iResourceManager, List<String> languageList) {
        HashMap<String, String> out = new HashMap<>();
        for (String language : languageList) {
            language = language.toLowerCase();
            for (String modid : modids) {
                HashMap<String, String> map = new HashMap<>();
                if (iResourceManager.getResourceDomains().contains(modid)) {
                    try {
                        map = getFromResourcePack(iResourceManager, modid, language);
                    } catch (IOException var9) {
                    }
                }
                if (map.isEmpty()) {
                    map = getFromResources(modid, language);
                }
                out.putAll(map);
            }
        }

        return out;
    }

    private static HashMap<String, String> getFromResources(String resourceDomain, String language) {
        InputStream resource = LanguageManager.class.getResourceAsStream(String.format("/assets/%s/lang/%s.json", resourceDomain, language));
        return loadJson(resource);
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

    public static void registerModForLanguageManager(String modid) {
        modids.add(modid);
    }

    private static HashMap<String, String> loadJson(InputStream inputStream) {
        if(inputStream == null){
            return new HashMap<String, String>();
        }
        Gson gson = new Gson();
        HashMap<String, String> template = new HashMap<>();
        HashMap<String, String> out = (HashMap<String, String>)gson.fromJson(new InputStreamReader(inputStream), template.getClass());
        out.forEach((key, value) -> {
            value = PATTERN.matcher(value).replaceAll("%$1s");
        });
        return out;
    }
}
