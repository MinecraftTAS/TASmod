package com.minecrafttas.tasmod.savestates.typeadapters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.item.Item;

public class ItemTypeAdapter extends TypeAdapter<Item> {

	@Override
	public void write(JsonWriter out, Item value) throws IOException {
		out.value(Item.getIdFromItem(value));
	}

	@Override
	public Item read(JsonReader in) throws IOException {
		if (!in.hasNext())
			return null;
		return Item.getItemById(in.nextInt());
	}

}
