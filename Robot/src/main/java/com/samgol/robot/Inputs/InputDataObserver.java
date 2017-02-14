package com.samgol.robot.Inputs;

public interface InputDataObserver<I extends InputData> {
	void onInput(I Input);
}
