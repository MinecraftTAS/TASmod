package de.scribble.lp.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import de.scribble.lp.tasmod.util.ContainerSerialiser;

public class VirtualMouse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5389661329436686190L;

	private Map<Integer, VirtualKey> keyList = Maps.<Integer, VirtualKey>newHashMap();

	private int scrollwheel = 0;

	private int cursorX = 0;

	private int cursorY = 0;

	public VirtualMouse(Map<Integer, VirtualKey> keyListIn, int scrollwheel, int cursorX, int cursorY, List<PathNode> path) {
		Map<Integer, VirtualKey> copy = new HashMap<Integer, VirtualKey>();

		keyListIn.forEach((key, value) -> {
			copy.put(key, value.clone());
		});
		keyList = copy;

		this.scrollwheel = scrollwheel;

		this.cursorX = cursorX;

		this.cursorY = cursorY;

		List<PathNode> pathCopy = new ArrayList<PathNode>();
		path.forEach(pathNode -> {
			pathCopy.add(pathNode);
		});
		this.path = pathCopy;

	}

	/**
	 * Creates a Keyboard, where the keys are all unpressed
	 */
	public VirtualMouse() {
		keyList.put(-101, new VirtualKey("MOUSEMOVED", -101));
		keyList.put(-100, new VirtualKey("LC", -100));
		keyList.put(-99, new VirtualKey("RC", -99));
		keyList.put(-98, new VirtualKey("MC", -98));
		keyList.put(-97, new VirtualKey("MBUTTON4", -97));
		keyList.put(-96, new VirtualKey("MBUTTON5", -96));
		keyList.put(-95, new VirtualKey("MBUTTON6", -95));
		keyList.put(-94, new VirtualKey("MBUTTON7", -94));
		keyList.put(-93, new VirtualKey("MBUTTON8", -93));
		keyList.put(-92, new VirtualKey("MBUTTON9", -92));
		keyList.put(-91, new VirtualKey("MBUTTON10", -91));
		keyList.put(-90, new VirtualKey("MBUTTON11", -90));
		keyList.put(-89, new VirtualKey("MBUTTON12", -89));
		keyList.put(-88, new VirtualKey("MBUTTON13", -88));
		keyList.put(-87, new VirtualKey("MBUTTON14", -87));
		keyList.put(-86, new VirtualKey("MBUTTON15", -86));
		keyList.put(-85, new VirtualKey("MBUTTON16", -85));

		this.scrollwheel = 0;

		this.cursorX = 0;

		this.cursorY = 0;

		addPathNode();
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
			if (keycodes <= 0) {
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

		List<PathNode> path = mouseToCompare.getPath();

		if (path.size() != 1) {
			for (int i = 0; i < path.size() - 1; i++) {
				PathNode currentNode = path.get(i);
				PathNode nextNode = path.get(i + 1);

				boolean flag = false;

				for (VirtualKey key : nextNode.keyList.values()) {
					if (!key.equals(currentNode.keyList.get(key.getKeycode()))) {
						eventList.add(new VirtualMouseEvent(key.getKeycode(), key.isKeyDown(), nextNode.scrollwheel, nextNode.cursorX, nextNode.cursorY));
						flag = true;
						break;
					}
				}
				if (!flag) {
					eventList.add(new VirtualMouseEvent(-101, false, nextNode.scrollwheel, nextNode.cursorX, nextNode.cursorY));
				}

			}
		} else {
			keyList.forEach((keycodes, virtualkeys) -> {

				VirtualKey keyToCompare = mouseToCompare.get(keycodes);

				if (!virtualkeys.equals(keyToCompare)) {
					eventList.add(new VirtualMouseEvent(keycodes, keyToCompare.isKeyDown(), scrollwheel, cursorX, cursorY));
				}

			});
		}
		return eventList;
	}

	public boolean isSomethingDown() {
		for (VirtualKey key : keyList.values()) {
			if (key.isKeyDown()) {
				return true;
			}
		}
		return false;
	}

	public boolean isSomethingDown(Map<Integer, VirtualKey> keyList) {
		for (VirtualKey key : keyList.values()) {
			if (key.isKeyDown()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public VirtualMouse clone() {
		return new VirtualMouse(keyList, scrollwheel, cursorX, cursorY, path);
	}

	public void setCursor(int x, int y) {
		cursorX = x;
		cursorY = y;
	}

	public void setScrollWheel(int scrollwheel) {
		this.scrollwheel = scrollwheel;
	}

	List<PathNode> path = new ArrayList<PathNode>();

	public List<PathNode> getPath() {
		return path;
	}
	
	public String getPathAsString() {
		String out="";
		for(PathNode node: path) {
			out=out.concat(node.toString());
		}
		return out;
	}

	public void addPathNode() {
		path.add(new PathNode(keyList, scrollwheel, cursorX, cursorY));
	}

	public void resetPath() {
		path.clear();
		addPathNode();
	}

	public class PathNode implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2221602955260299028L;

		private Map<Integer, VirtualKey> keyList = Maps.<Integer, VirtualKey>newHashMap();

		public int scrollwheel = 0;

		public int cursorX = 0;

		public int cursorY = 0;

		public PathNode(Map<Integer, VirtualKey> keyList, int scrollwheel, int cursorX, int cursorY) {
			Map<Integer, VirtualKey> copy = new HashMap<Integer, VirtualKey>();

			keyList.forEach((key, value) -> {
				copy.put(key, value.clone());
			});
			this.keyList = copy;
			this.scrollwheel = scrollwheel;
			this.cursorX = cursorX;
			this.cursorY = cursorY;
		}

		public PathNode() {
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

			this.scrollwheel = 0;

			this.cursorX = 0;

			this.cursorY = 0;
		}

		@Override
		public String toString() {
			String keyString = "";
			List<String> strings = new ArrayList<String>();

			keyList.forEach((keycodes, virtualkeys) -> {
				if (virtualkeys.isKeyDown()) {
					strings.add(virtualkeys.getName());
				}
			});
			if (!strings.isEmpty()) {
				String seperator = ",";
				for (int i = 0; i < strings.size(); i++) {
					if (i == strings.size() - 1) {
						seperator = "";
					}
					keyString = keyString.concat(strings.get(i) + seperator);
				}
			}
			if (keyString.isEmpty()) {
				return "MOUSEMOVED," + scrollwheel + "," + cursorX + "," + cursorY;
			} else {
				return keyString + "," + scrollwheel + "," + cursorX + "," + cursorY;
			}
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

	}

	public void clear() {
		keyList.forEach((keycode, key) -> {
			key.setPressed(false);
		});
		resetPath();
	}

	@Override
	public String toString() {
		List<String> stringy = getCurrentPresses();
		String keyString = "";
		if (!stringy.isEmpty()) {
			String seperator = ",";
			for (int i = 0; i < stringy.size(); i++) {
				if (i == stringy.size() - 1) {
					seperator = "";
				}
				keyString = keyString.concat(stringy.get(i) + seperator);
			}
		}
		String pathString = "";
		if (!path.isEmpty()) {
			String seperator = "->";
			for (int i = 0; i < path.size(); i++) {
				if (i == path.size() - 1) {
					seperator = "";
				}
				pathString = pathString.concat("[" + path.get(i).toString() + "]" + seperator);
			}
		}
		return ContainerSerialiser.SectionsV1.MOUSE.getName()+":"+keyString + ";" + pathString;
	}
	
	public void setPath(List<PathNode> path) {
		List<PathNode> pathCopy = new ArrayList<PathNode>();
		path.forEach(pathNode -> {
			pathCopy.add(pathNode);
		});
		this.path = pathCopy;
	}

}
