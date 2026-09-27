package com.minecrafttas.tasmod.savestates.typeadapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.minecrafttas.mctcommon.json.FineGson;
import com.minecrafttas.mctcommon.json.FineTypeAdapter;

import net.minecraft.item.Item;

public class ItemTypeAdapter extends FineTypeAdapter {

	@Override
	public JsonElement serialize(Object obj, FineGson fineJson, Class<?> clazz) throws RuntimeException {
		Item value = (Item) obj;
		return new JsonPrimitive(Item.getIdFromItem(value));
	}

	@Override
	public Object deserialize(JsonElement element, Class<?> clazz, FineGson fineJson, Object existing) {
		JsonPrimitive value = element.getAsJsonPrimitive();
		return Item.getItemById(value.getAsInt());
	}

}
