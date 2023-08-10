package com.minecrafttas.tasmod.virtual;

public class VirtualMouse2 {
	
	/**
	 * Creates a keyboard where all keys are unpressed
	 */
	public VirtualMouse2() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static enum VirtualMouseButtons{
		MOUSEMOVED(-101),
		LC(-100),
		RC(-99),
		MC(-98),
		MBUTTON4(-97),
		MBUTTON5(-96),
		MBUTTON6(-95),
		MBUTTON7(-94),
		MBUTTON8(-93),
		MBUTTON9(-92),
		MBUTTON10(-91),
		MBUTTON11(-90),
		MBUTTON12(-89),
		MBUTTON13(-88),
		MBUTTON14(-87),
		MBUTTON15(-86),
		MBUTTON16(-85);
		
		private int keyID;
		
		private VirtualMouseButtons(int keyID) {
			this.keyID = keyID;
		}
		
		public int getKeyID() {
			return keyID;
		}
	}
}
