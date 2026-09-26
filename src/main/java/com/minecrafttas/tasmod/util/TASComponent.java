package com.minecrafttas.tasmod.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.UnaryOperator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

/**
 * Component helper class for modern Minecraft versions
 * 
 * @author Scribble
 */
public class TASComponent {

	private final MutableComponent component;
	private Style style = Style.EMPTY;

	private TASComponent(MutableComponent component) {
		this.component = component;
	}

	public TASComponent withStyle(ChatFormatting... colors) {
		for (ChatFormatting color : colors) {
			switch (color) {
				case BOLD:
					style = style.withBold(true);
					break;

				case ITALIC:
					style = style.withItalic(true);
					break;

				case UNDERLINE:
					style = style.withUnderlined(true);
					break;

				case STRIKETHROUGH:
					style = style.withStrikethrough(true);
					break;

				case OBFUSCATED:
					style = style.withObfuscated(true);
					break;

				case RESET:
					style = style.withBold(false)
						.withItalic(false)
						.withUnderlined(false)
						.withStrikethrough(false)
						.withObfuscated(false)
						.withColor(ChatFormatting.WHITE);
					break;
				default:
					style = style.withColor(color);
					break;
			}
		}
		return this;
	}

	public TASComponent withStyle(UnaryOperator<Style> unaryOperator) {
		style = unaryOperator.apply(style);
		return this;
	}

	public Component build() {
		return component.withStyle(style);
	}

	public static TASComponent literal(String text) {
		return new TASComponent(Component.literal(text));
	}

	public static TASComponent translatable(String string) {
		return translatable(string, new Object[] {});
	}

	public static TASComponent translatable(String string, Object... objects) {
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof TASComponent) {
				objects[i] = ((TASComponent) object).build();
			}
		}
		return new TASComponent(Component.translatable(string, objects));
	}

	public static TASComponent wrap(TASComponent saveComponent, ChatFormatting color) {
		return wrap(saveComponent).withStyle(color);
	}

	public static TASComponent wrap(TASComponent component) {
		return TASComponent.translatable("[%s]", component);
	}

	public static class CClickEvent {
		public static ClickEvent create(ClickEvent.Action action, String string) {
			try {
				// Try to use static factory methods
				if (ClickEvent.Action.SUGGEST_COMMAND == action) {
					Method m = ClickEvent.class.getMethod("suggestCommand", String.class);
					return (ClickEvent) m.invoke(null, string);
				} else if (ClickEvent.Action.OPEN_URL == action) {
					Method m = ClickEvent.class.getMethod("openUrl", String.class);
					return (ClickEvent) m.invoke(null, string);
				} else if (ClickEvent.Action.RUN_COMMAND == action) {
					Method m = ClickEvent.class.getMethod("runCommand", String.class);
					return (ClickEvent) m.invoke(null, string);
				} else if (ClickEvent.Action.CHANGE_PAGE == action) {
					Method m = ClickEvent.class.getMethod("changePage", int.class);
					return (ClickEvent) m.invoke(null, Integer.parseInt(string));
				}
				// Fallback to constructor
				Constructor<ClickEvent> c = ClickEvent.class.getDeclaredConstructor(ClickEvent.Action.class, String.class);
				c.setAccessible(true);
				return c.newInstance(action, string);
			} catch (Exception e) {
				return null;
			}
		}
	}

	public static class CHoverEvent {
		public static HoverEvent create(HoverEvent.Action action, TASComponent component) {
			try {
				if (HoverEvent.Action.SHOW_TEXT == action) {
					Method m = HoverEvent.class.getMethod("showText", Component.class);
					return (HoverEvent) m.invoke(null, component.build());
				}
				// Fallback to constructor
				Constructor<HoverEvent> c = HoverEvent.class.getDeclaredConstructor(HoverEvent.Action.class, Component.class);
				c.setAccessible(true);
				return c.newInstance(action, component.build());
			} catch (Exception e) {
				return null;
			}
		}
	}
}