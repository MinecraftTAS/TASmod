package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class VirtualMouse {

	private Map<Integer, VirtualKey> keyList = Maps.<Integer, VirtualKey>newHashMap();

	private int scrollwheel;

	private List<Integer> cursorX;

	private List<Integer> cursorY;

	public VirtualMouse(Map<Integer, VirtualKey> keyListIn) {
		Map<Integer, VirtualKey> copy = new HashMap<Integer, VirtualKey>();

		keyListIn.forEach((key, value) -> {
			copy.put(key, value.clone());
		});
		keyList = copy;
	}

	/**
	 * Creates a Keyboard, where the keys are all unpressed
	 */
	public VirtualMouse() {
		keyList.put(-101, new VirtualKey("MOUSEMOVED", -101));
		keyList.put(-100, new VirtualKey("LC", -100));
		keyList.put(-99, new VirtualKey("RC", -99));
		keyList.put(-98, new VirtualKey("MC", -98));
		keyList.put(-97, new VirtualKey("MBUTTON3", -97));
		keyList.put(-96, new VirtualKey("MBUTTON4", -96));
		keyList.put(-95, new VirtualKey("MBUTTON5", -95));
		keyList.put(-94, new VirtualKey("MBUTTON6", -94));
		keyList.put(-93, new VirtualKey("MBUTTON7", -93));
		keyList.put(-92, new VirtualKey("MBUTTON8", -92));
		keyList.put(-91, new VirtualKey("MBUTTON9", -91));
		keyList.put(-90, new VirtualKey("MBUTTON10", -90));
		keyList.put(-89, new VirtualKey("MBUTTON11", -89));
		keyList.put(-88, new VirtualKey("MBUTTON12", -88));
		keyList.put(-87, new VirtualKey("MBUTTON13", -87));
		keyList.put(-86, new VirtualKey("MBUTTON14", -86));
		keyList.put(-85, new VirtualKey("MBUTTON15", -85));
	}

	public void add(int keycode) {
		String keyString = Integer.toString(keycode);

		keyList.put(keycode, new VirtualKey(keyString, keycode));
	}

	public VirtualKey get(int keycode) {
		return keyList.get(keycode);
	}

	public VirtualKey get(String keyname) {
		Collection<VirtualKey> list = keyList.values();
		VirtualKey out = null;

		for (VirtualKey key : list) {
			if (key.getName().equalsIgnoreCase(keyname)) {
				out = key;
			}
		}
		return out;
	}

	public List<String> getCurrentPresses() {
		List<String> out = new ArrayList<String>();

		keyList.forEach((keycodes, virtualkeys) -> {
			if (keycodes >= 0) {
				if (virtualkeys.isKeyDown()) {
					out.add(virtualkeys.getName());
				}
			}
		});

		return out;
	}

	public Map<Integer, VirtualKey> getKeyList() {
		return this.keyList;
	}

	public List<VirtualMouseEvent> getDifference(VirtualMouse mouseToCompare) {
		List<VirtualMouseEvent> eventList = new ArrayList<VirtualMouseEvent>();

		keyList.forEach((keycodes, virtualkeys) -> {
			VirtualKey keyToCompare = mouseToCompare.get(keycodes);
			if (!virtualkeys.equals(keyToCompare)) {
				for (int i = 0; i < keyToCompare.getTimesPressed(); i++) {
					if (i == keyToCompare.getTimesPressed() - 1) {
						eventList.add(new VirtualMouseEvent(keycodes, keyToCompare.isKeyDown(), scrollwheel, mouseX, mouseY));
					}
				}
			}
		});
		return eventList;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new VirtualMouse(keyList);
	}

	public void setScrollWheel(int scrollwheel) {
		this.scrollwheel = this.scrollwheel + scrollwheel;
	}

	public void addCursor(int cursorX, int cursorY) {
		this.cursorX.add(cursorX);
		this.cursorY.add(cursorY);
	}
}
