package com.samgol.robot.Inputs;

import java.util.HashSet;
import java.util.Set;

public abstract class InputSource<T extends InputData> {
    private Set<InputDataObserver<T>> observers = new HashSet<InputDataObserver<T>>();

    protected void onInput(T inputData) {
        for (InputDataObserver<T> observer : observers) {
            observer.onInput(inputData);
        }
    }

    public boolean addObserver(InputDataObserver<T> inputObserver) {
        return observers.add(inputObserver);
    }

    public boolean removeHandler(InputDataObserver<T> inputObserver) {
        return observers.remove(inputObserver);
    }
}
