package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class VirtualMouse {

	private Map<Integer, VirtualMouseButton> keyList = Maps.<Integer, VirtualMouseButton>newHashMap();

	private int scrollwheel;

	private List<Integer> cursorX;

	private List<Integer> cursorY;

	public VirtualMouse(Map<Integer, VirtualMouseButton> keyListIn, int scrollwheel, List<Integer> cursorX, List<Integer> cursorY) {
		Map<Integer, VirtualMouseButton> copy = new HashMap<Integer, VirtualMouseButton>();

		keyListIn.forEach((key, value) -> {
			copy.put(key, value.clone());
		});
		keyList = copy;

		this.scrollwheel = scrollwheel;

		List<Integer> copyX = new ArrayList<Integer>();
		
		List<Integer> copyY = new ArrayList<Integer>();
		
		cursorX.forEach(action->{
			copyX.add(action);
		});
		
		this.cursorX=copyX;
		
		cursorY.forEach(action->{
			copyY.add(action);
		});
		
		this.cursorY=copyY;
		
	}

	/**
	 * Creates a Keyboard, where the keys are all unpressed
	 */
	public VirtualMouse() {
		scrollwheel=0;
		
		cursorX=new ArrayList<Integer>();
		
		cursorY=new ArrayList<Integer>();
		
		keyList.put(-101, new VirtualMouseButton("MOUSEMOVED", -101));
		keyList.put(-100, new VirtualMouseButton("LC", -100));
		keyList.put(-99, new VirtualMouseButton("RC", -99));
		keyList.put(-98, new VirtualMouseButton("MC", -98));
		keyList.put(-97, new VirtualMouseButton("MBUTTON3", -97));
		keyList.put(-96, new VirtualMouseButton("MBUTTON4", -96));
		keyList.put(-95, new VirtualMouseButton("MBUTTON5", -95));
		keyList.put(-94, new VirtualMouseButton("MBUTTON6", -94));
		keyList.put(-93, new VirtualMouseButton("MBUTTON7", -93));
		keyList.put(-92, new VirtualMouseButton("MBUTTON8", -92));
		keyList.put(-91, new VirtualMouseButton("MBUTTON9", -91));
		keyList.put(-90, new VirtualMouseButton("MBUTTON10", -90));
		keyList.put(-89, new VirtualMouseButton("MBUTTON11", -89));
		keyList.put(-88, new VirtualMouseButton("MBUTTON12", -88));
		keyList.put(-87, new VirtualMouseButton("MBUTTON13", -87));
		keyList.put(-86, new VirtualMouseButton("MBUTTON14", -86));
		keyList.put(-85, new VirtualMouseButton("MBUTTON15", -85));
	}

	public void add(int keycode) {
		String keyString = Integer.toString(keycode);

		keyList.put(keycode, new VirtualMouseButton(keyString, keycode));
	}

	public VirtualMouseButton get(int keycode) {
		return keyList.get(keycode);
	}

	public VirtualMouseButton get(String keyname) {
		Collection<VirtualMouseButton> list = keyList.values();
		VirtualMouseButton out = null;

		for (VirtualMouseButton key : list) {
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

	public Map<Integer, VirtualMouseButton> getKeyList() {
		return this.keyList;
	}

	public List<VirtualMouseEvent> getDifference(VirtualMouse mouseToCompare) {
		List<VirtualMouseEvent> eventList = new ArrayList<VirtualMouseEvent>();

		Iterator<Integer> cursorXIterator = cursorX.iterator();

		Iterator<Integer> cursorYIterator = cursorY.iterator();

		keyList.forEach((keycodes, virtualkeys) -> {

			VirtualMouseButton keyToCompare = mouseToCompare.get(keycodes);

		});
		return eventList;
	}

	@Override
	protected VirtualMouse clone() throws CloneNotSupportedException {
		return new VirtualMouse(keyList, scrollwheel, cursorX, cursorY);
	}

	public void setScrollWheel(int scrollwheel) {
		this.scrollwheel = this.scrollwheel + scrollwheel;
	}

	public void addCursor(int cursorX, int cursorY) {
		this.cursorX.add(cursorX);
		this.cursorY.add(cursorY);
	}

	public void resetCursor() {
		cursorX.clear();
		cursorY.clear();
	}
}
