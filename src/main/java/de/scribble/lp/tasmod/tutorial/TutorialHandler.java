package de.scribble.lp.tasmod.tutorial;

import org.lwjgl.input.Keyboard;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Renders an interactive tutorial to the game overlay.
 * 
 * @author ScribbleLP
 *
 */
public class TutorialHandler {

	private boolean istutorial;
	
	private int state;
	
	private int cooldowntime=60;
	
	private int cooldown=cooldowntime;
	
	
	public TutorialHandler() {
	}
	
	public TutorialHandler(int state) {
		this.state=state;
	}
	
	public void setState(int state) {
		cooldown=cooldowntime;
		this.state = state;
	}
	
	public int getState() {
		return state;
	}
	
	public void setTutorial(boolean isActive) {
//		istutorial=isActive; //TODO Update tutorial
	}
	
	public boolean isTutorial() {
		return istutorial;
	}
	/**
	 * Get the current text depending on the state
	 * @return String[]
	 */
	public String[] getTutorialText() {
		String[] textout;
        switch (state) {
		case 1:
			textout=text1;
			break;
		case 2:
			textout=text2;
			break;
		case 3:
			textout=text3;
			break;
		case 4:
			textout=text4;
			break;
		case 5:
			textout=text5;
			break;
		case 6:
			textout=text6;
			break;
		case 7:
			textout=text7;
			break;
		case 8:
			textout=text8;
			break;
		case 9:
			textout=text9;
			break;
		case 10:
			textout=text10;
			break;
		case 11:
			textout=text11;
			break;
		case 12:
			textout=text12;
			break;
		default:
			textout=new String[]{""};
			break;
		}
        return textout;
	}
	/**
	 * Checks for an action that advances or closes the tutorial
	 */
	private void checkForKeys() {
		switch (state) {
		case 0:
			break;
		case 1:
		case 2:
		case 5:
		case 8:
		case 11:
		
			if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)&&cooldown==0) {	//Checking for presses on the physical keyboard, since tickrate 0 is a thing
				advanceState();
			}
			break;
		case 9:
			if(Keyboard.isKeyDown(Keyboard.KEY_F8)&&cooldown==0) {
				advanceState();
			}
			break;
		case 10:
			if(Keyboard.isKeyDown(Keyboard.KEY_F9)&&cooldown==0) {
				advanceState();
			}
			break;
		case 12:
			if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)&&cooldown==0) {
				istutorial=false;
				ClientProxy.config.get("Tutorial","Enabled",true,"If the tutorial is enabled").set(false);
				ClientProxy.config.save();
			}
			break;
		}
	}
	/**
	 * Advances the tutorial state and adds cooldown
	 */
	public void advanceState() {
		cooldown=cooldowntime;
		state++;
	}
	Minecraft mc= Minecraft.getMinecraft();
	
	/**
	 * Main rendering event. This is needed so it can function in tickrate 0
	 * @param event
	 */
    @SubscribeEvent
    public void drawStuff(RenderGameOverlayEvent.Post event) {
		if (istutorial) {
			if (event.isCancelable() || event.getType() != ElementType.HOTBAR) {
				return;
			}
			checkForKeys();
			ScaledResolution scaled = new ScaledResolution(mc);
			int width = scaled.getScaledWidth();
			int height = scaled.getScaledHeight();
			String[] text = getTutorialText();
			
			
			for (int i = 0; i < text.length; i++) { //For every new element in the string array, a new line is created.
				String tex = text[i];
				new Gui().drawString(mc.fontRenderer, tex, width - mc.fontRenderer.getStringWidth(tex) - 10,
						10 + 10 * i, 0xFFFFFF); //Drawing the text on the screen
			}

			if (cooldown != 0) {
				cooldown--;		//Decreasing the cooldown of the button cooldown until it reaches 0
			}
    	}
    }
    //Every String in one place... maybe later with translations
	private final String[] text1={"1. Hi, welcome to this InTeRaCtIvE tutorial on how to use this mod.",
			"",
			"If you have already enough of this text,",
			"then type in '/playbacktutorial' to turn this off.",
			"",
			"If you change your mind after that",
			"then you can also enable it with '/playbacktutorial' again",
			"",
			"If you wish to know more,",
			"then continue by pressing RETURN on your keyboard"};
	
	private final String[] text2={"2. If you move your mouse you may realise that it feels very laggy.",
			"",
			"This is intentional so don't tell me the mod is bugged.",
			"The mod forces the camera to stay at 20fps/20ticks to ensure playback.",
			"",
			"Also when loading new chunks you may experience 'lagspikes of death'... Thats intentional too...",
			"I've made it so if the server lags, the client lags too...",
			"",
			"Press RETURN to continue"};
	
	private final String[] text3= {"3. This mod can record your inputs and save them to a file.",
			"To start a recording type in '/record' and then followed by a filename",
			"",
			"Objective: Open the chat and type '/record'"};
	
	private final String[] text4={"4. Now move around open your inventory etc...",
			"",
    		"To stop this, type in the same command again.",
    		"So open chat, press the up arrow and hit RETURN",
    		"",
    		"Objective: Type in '/record' again to stop the recording"};
	
	private final String[] text5={"5. Nice! Well, I can't really see what you have done but ok...",
    		"",
    		"You can also record stuff using the alias '/r' and if you don't enter a filename,",
    		"a random filename will be generated.",
    		"",
    		"If you are not happy with your recording, then record with the same filename",
    		"and it will overwrite the previous file",
    		"",
    		"Press RETURN to continue"};
	
	private final String[] text6={"6. You might have guessed it already,",
    		" but now you can play back that recording.",
    		"",
    		"Use either '/play' or '/p' plus the filename you entered in /record",
    		"to start playing back the file.",
    		"",
    		"When pressing TAB after '/play ' it will autocomplete the filenames you recorded",
    		"",
    		"Objective: Type in '/play tas' (Or the filename you typed earlier)"};
	
	private final String[] text7={"7. Now sit back and relax, as the mod plays Minecraft for you!"};
	
	private final String[] text8={"8. Here you go, the playback is finished!",
    		"",
    		"You can abort the playback with typing /play during a playback",
    		"",
    		"Press RETURN to continue"};
	
	private final String[] text9={"9. Let's do something fun! Press F8!",
    		"",
    		"Objective: Press F8"};
	
	private final String[] text10={"10. Haha, you are softlocked now xDxDxD",
			"",
			"Well not actually... This is the wonderous state of tickrate 0.",
			"The game is paused but you can still move your camera.",
			"",
			"Now, while still in tickrate 0, press F9 to advance one tick!",
			"",
			"<---You can see the tick counter over there",
			"",
			"Objective: Press F9"};
	
	private final String[] text11= {"11. Now hold W and advance a few ticks.",
			"You will see that you start walking",
			"",
			"Mouse buttons are buffered in mc, so if you press and release leftclick once",
			"after advancing it will execute.",
			"",
			"Im sure you want to play around with this so enjoy :D",
			"",
			"Press RETURN to continue"};
	
	private final String[] text12= {"12. To get out of this state you can press F8 again.",
			"",
			"Well I am kinda done for the moment... Hope this helped a bit",
			"",
			"If you have more questions ask them in the discord.",
			"",
			"Ah, before I forget, if you want to translate these lines into a different language hit me up!",
			"",
			"Press RETURN to close the tutorial"};
	
}
