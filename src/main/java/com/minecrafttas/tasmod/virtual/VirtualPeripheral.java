package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class VirtualPeripheral implements Serializable {
    protected final Set<Integer> pressedKeys;

    public VirtualPeripheral() {
        this.pressedKeys = new HashSet<>();
    }

    public VirtualPeripheral(Set<Integer> pressedKeys) {
        this.pressedKeys = pressedKeys;
    }

    public void setPressed(int keycode, boolean keystate) {
        if (keystate)
            pressedKeys.add(keycode);
        else
            pressedKeys.remove(keycode);
    }

    public void setPressed(String keyname, boolean keystate) {
        Integer keycode = VirtualKey2.getKeycode(keyname);
        if (keycode != null) {
            setPressed(keycode, keystate);
        }
    }

    public List<String> getCurrentPresses() {
        List<String> out = new ArrayList<>();
        pressedKeys.forEach(keycode -> {
            out.add(VirtualKey2.getName(keycode));
        });
        return out;
    }

    protected void clearPressedKeys() {
        pressedKeys.clear();
    }

    protected abstract <T extends VirtualPeripheral> List<? extends VirtualEvent> getDifference(T nextPeripheral);

    @Override
    public String toString() {
        return String.join(",", getCurrentPresses());
    }
}