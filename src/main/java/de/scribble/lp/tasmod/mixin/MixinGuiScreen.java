package de.scribble.lp.tasmod.mixin;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.playback.InputPlayback;
import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen{
	@Shadow
	private int width;
	@Shadow
	private Minecraft mc;
	@Shadow
	private int height;
	@Shadow
	private int touchValue;
	@Shadow
	private int eventButton;
	@Shadow
	private long lastMouseEvent;
	@Shadow(remap = false)
	private boolean keyHandled;
	@Shadow(remap = false)
	private boolean mouseHandled;
	@Inject(method = "handleInput", at = @At(value = "HEAD"),cancellable = true)
	public void redirectHandleInput(CallbackInfo ci) throws IOException {
		if (Mouse.isCreated())
        {
        	VirtualMouseAndKeyboard.prepareMouseEvents();
        	while (Mouse.next()) {
        		//Get the slot index hovered over with the mouse
        		int slotindex=-1;
        		if((GuiScreen)(Object)this instanceof GuiContainer) {
        			GuiContainer container = (GuiContainer)(GuiScreen)(Object)this;
        			int X=calcX(Mouse.getEventX());
        			int Y=calcY(Mouse.getEventY());
        			if(container.getSlotAtPosition(X,Y) != null) {
        				slotindex=container.getSlotAtPosition(X, Y).slotNumber;
        			}
        		}
        		VirtualMouseAndKeyboard.fillMouseEvents(Mouse.getEventButton()-100, Mouse.getEventButtonState(), Mouse.getDWheel(), calcX(Mouse.getEventX()), calcY(Mouse.getEventY()), slotindex);
        		
        	}
        	VirtualMouseAndKeyboard.fillMouseEventsWithPlayback();
        	while(VirtualMouseAndKeyboard.nextMouseEvent()) {
        		this.mouseHandled=false;
        		if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Pre((GuiScreen)(Object)this))) continue;
        		this.handleMouseInput();
        		if (((GuiScreen)(Object)this).equals(this.mc.currentScreen) && !this.mouseHandled) net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Post((GuiScreen)(Object)this));
        	}
        }
		if (Keyboard.isCreated()) {
			VirtualMouseAndKeyboard.prepareKeyboardEvents();
			while (Keyboard.next()) {
				VirtualMouseAndKeyboard.fillKeyboardEvents(Keyboard.getEventKey(), Keyboard.getEventKeyState(),	Keyboard.getEventCharacter());
				
			}
			VirtualMouseAndKeyboard.fillKeyboardEventsWithPlayback();
			while (VirtualMouseAndKeyboard.nextKeyboardEvent()) {
				this.keyHandled=false;
				if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Pre((GuiScreen)(Object)this))) continue;
				this.handleKeyboardInput();
				if (((GuiScreen)(Object)this).equals(this.mc.currentScreen) && !this.keyHandled) net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Post((GuiScreen)(Object)this));
			}
		}
		ci.cancel();
	}
	@Shadow
	protected abstract void handleKeyboardInput();
	@Shadow
	protected abstract void handleMouseInput();
	private int calcX(int X) {
    	return X* this.width / this.mc.displayWidth;
    }
	private int calcY(int Y) {
    	return this.height-Y* this.height / this.mc.displayHeight - 1;
    }
	private int uncalcX(int i) {
    	return i*this.mc.displayWidth/this.width;
    }
	private int uncalcY(int j) {
    	return (this.mc.displayHeight*(this.height - j -1) / this.height);
    }
	
    private void customHandleKeyboard() {
    	char c0 = VirtualMouseAndKeyboard.getEventChar();
        //TASMod: Run typed key through the virtual keyboard
        c0=VirtualMouseAndKeyboard.runCharThroughKeyboard(c0, VirtualMouseAndKeyboard.getEventKeyboardButtonState());
        
        int i = VirtualMouseAndKeyboard.getEventKeyboardButton() == 0 ? VirtualMouseAndKeyboard.getEventChar() + 256 : VirtualMouseAndKeyboard.getEventKeyboardButton();
        VirtualMouseAndKeyboard.runThroughKeyboard(i, VirtualMouseAndKeyboard.getEventKeyboardButtonState());
        if (VirtualMouseAndKeyboard.getEventKeyboardButton() == 0 && c0 >= ' ' || VirtualMouseAndKeyboard.getEventKeyboardButtonState())
        {
            this.keyTyped(c0, VirtualMouseAndKeyboard.getEventKeyboardButton());
        }

        this.mc.dispatchKeypresses();
    }
    @Shadow
	abstract void keyTyped(char c0, int eventKeyboardButton);
	
	private void customHandleMouseInput() {
		int i = uncalcX(VirtualMouseAndKeyboard.getEventX()) * this.width / this.mc.displayWidth;
		int j = this.height - uncalcY(VirtualMouseAndKeyboard.getEventY()) * this.height / this.mc.displayHeight - 1;

		int k = VirtualMouseAndKeyboard.getEventMouseButton() + 100;
		VirtualMouseAndKeyboard.runThroughKeyboard(k-100, VirtualMouseAndKeyboard.getEventMouseButtonState());
		if(InputPlayback.isPlayingback()) {
			Mouse.setCursorPosition(uncalcX(i), uncalcY(j));
		}
		if (VirtualMouseAndKeyboard.getEventMouseButtonState()) {
			if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) {
				return;
			}

			this.eventButton = k;
			this.lastMouseEvent = Minecraft.getSystemTime();
			this.mouseClicked(i, j, this.eventButton);
		} else if (k != -1) {
			if (this.mc.gameSettings.touchscreen && --this.touchValue > 0) {
				return;
			}

			this.eventButton = -1;
			this.mouseReleased(i, j, k);
		} else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
			long l = Minecraft.getSystemTime() - this.lastMouseEvent;
			this.mouseClickMove(i, j, this.eventButton, l);
		}
	}
	@Shadow
	protected abstract void mouseClickMove(int i, int j, int eventButton2, long l);
	@Shadow
	protected abstract void mouseReleased(int i, int j, int k);
	@Shadow
	protected abstract void mouseClicked(int i, int j, int eventButton2);
	@Inject(method = "handleMouseInput", at =@At(value = "HEAD"), cancellable = true)
	public void injectMouseInput(CallbackInfo ci) {
		customHandleMouseInput();
		ci.cancel();
	}
	@Inject(method = "handleKeyboardInput", at =@At(value = "HEAD"), cancellable = true)
	public void injectKeyboardInput(CallbackInfo ci) {
		customHandleKeyboard();
		ci.cancel();
	}
}
