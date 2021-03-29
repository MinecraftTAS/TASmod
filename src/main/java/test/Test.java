package test;

import java.util.List;

import org.lwjgl.input.Keyboard;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.virtual.VirtualKeyboardEvent;

public class Test {

	public static void main(String[] args) {
		//My code, my rules...
		ClientProxy.virtual.updateNextKeyboard(Keyboard.KEY_H, true, 'h');
		List<VirtualKeyboardEvent> events=ClientProxy.virtual.getCurrentKeyboardEvents();
		
		events.forEach(action->{
			System.out.println(action.getKeyCode()+","+action.isState()+","+action.getCharacter());
		});
		System.out.println("\n");
		ClientProxy.virtual.updateNextKeyboard(Keyboard.KEY_H, true, 'k');
		ClientProxy.virtual.updateNextKeyboard(Keyboard.KEY_A, true, 'a');
		
		ClientProxy.virtual.clearNextKeyboard();
		events=ClientProxy.virtual.getCurrentKeyboardEvents();
		
		events.forEach(action->{
			System.out.println(action.getKeyCode()+","+action.isState()+","+action.getCharacter());
		});
	}

}
