package com.minecrafttas.tasmod.savestates.typeadapters;

public class PathNavigateTypeAdapterFactory {

//	@Override
//	public JsonElement serialize(PathNavigate src, Type typeOfSrc, JsonSerializationContext context) {
//		JsonObject out = new JsonObject();
//		out.addProperty("class", src.getClass().getSimpleName());
//
//		List<Field> fields = Arrays.asList(src.getClass().getDeclaredFields());
//		fields.addAll(Arrays.asList(src.getClass().getSuperclass().getDeclaredFields()));
//		for (Field field : fields) {
//			field.setAccessible(true);
//			if (field.getType() == IAttributeInstance.class || field.getType() == PathFinder.class || field.getType() == NodeProcessor.class) {
//				continue;
//			}
//			if (field.getType() == EntityLiving.class) {
//				out.add("entity", context.serialize(src.entity));
//				continue;
//			}
//			if (field.getType() == World.class) {
//				out.add("world", context.serialize(src.world));
//				continue;
//			}
//			try {
//				out.add(field.getName(), context.serialize(field.get(src)));
//			} catch (IllegalArgumentException | IllegalAccessException e) {
//				e.printStackTrace();
//			}
//		}
//		return out;
//	}
//
//	@Override
//	public PathNavigate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//		JsonObject jsonObj = json.getAsJsonObject();
//
//		Class<? extends PathNavigate> clazz;
//		try {
//			clazz = Class.forName(jsonObj.get("class").getAsString(), false, getClass().getClassLoader()).asSubclass(PathNavigate.class);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		EntityLiving entity = context.deserialize(jsonObj.get("entity"), EntityLiving.class);
//		World world = context.deserialize(jsonObj.get("world"), World.class);
//
//		PathNavigate navigator;
//		try {
//			navigator = clazz.getConstructor(EntityLiving.class, World.class).newInstance(entity, world);
//		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		List<Field> fields = Arrays.asList(navigator.getClass().getDeclaredFields());
//		fields.addAll(Arrays.asList(navigator.getClass().getSuperclass().getDeclaredFields()));
//		for (Field field : fields) {
//			field.setAccessible(true);
//			if (field.getType() == IAttributeInstance.class || field.getType() == PathFinder.class || field.getType() == NodeProcessor.class || field.getType() == EntityLiving.class || field.getType() == World.class) {
//				continue;
//			}
//			try {
//				field.set(navigator, context.deserialize(jsonObj.get(field.getName()), field.getType()));
//			} catch (IllegalArgumentException | IllegalAccessException | JsonParseException e) {
//				e.printStackTrace();
//				continue;
//			}
//		}
//
//		return navigator;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
//		Class<T> rawType = (Class<T>) type.getRawType();
//		if (!World.class.isAssignableFrom(rawType))
//			return null;
//
//		@SuppressWarnings("unused")
//		final TypeAdapterFactory factory = this;
//
//		return new TypeAdapter<T>() {
//
//			@Override
//			public void write(JsonWriter outWriter, T value) throws IOException {
//				JsonObject out = new JsonObject();
//				PathNavigate src = (PathNavigate) value;
//
//				List<Field> fields = Arrays.asList(src.getClass().getDeclaredFields());
//				fields.addAll(Arrays.asList(src.getClass().getSuperclass().getDeclaredFields()));
//				for (Field field : fields) {
//					field.setAccessible(true);
//					Class<?> fieldClass = field.getType();
//					if (fieldClass == IAttributeInstance.class || fieldClass == PathFinder.class || fieldClass == NodeProcessor.class) {
//						continue;
//					}
//					if (field.getType() == EntityLiving.class) {
//						JsonElement tree = gson.getAdapter(new TypeToken<EntityLiving>() {
//						}).toJsonTree(src.entity);
//						out.add("entity", tree);
//						continue;
//					}
//					if (field.getType() == World.class) {
//						JsonElement tree = gson.getDelegateAdapter(factory, new TypeToken<World>() {
//						}).toJsonTree(src.world);
//						out.add("world", tree);
//						continue;
//					}
//					try {
//						TypeAdapter<?> adapter = gson.getDelegateAdapter(factory, TypeToken.get(fieldClass));
//						out.add(field.getName(), adapter.toJsonTree(field.get(src)));
//					} catch (IllegalArgumentException | IllegalAccessException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//
//			@Override
//			public T read(JsonReader in) throws IOException {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//		};
//	}
//
}
