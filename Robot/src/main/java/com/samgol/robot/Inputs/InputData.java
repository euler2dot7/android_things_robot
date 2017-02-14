package com.samgol.robot.Inputs;

public class InputData {
	private InputType inputType;
	protected String defStringRep;
	protected boolean permanent = true;


	public InputData(InputType inputType) {
		this(inputType, true);
	}

	public InputData(InputType inputType, boolean permanent) {
		this.inputType = inputType;
		defStringRep = inputType.name();
		this.permanent = permanent;

	}


	public InputType getInputType() {
		return inputType;
	}

	public String getDefStringRep() {
		return defStringRep;
	}

	public boolean eq(InputData ev) {
		boolean result = false;
		if (ev != null && defStringRep != null) {
			result = defStringRep.equalsIgnoreCase(ev.getDefStringRep());
		}
		return result;
	}

	public boolean isPermanent() {
		return permanent;
	}
}
