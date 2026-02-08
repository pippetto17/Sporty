package model.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * GoF Abstract Subject class.
 * Manages the list of observers and provides attach/detach/notifyObservers
 * methods.
 * Concrete subjects (like Match) extend this class.
 */
public abstract class Subject {
    private List<Observer> observers = new ArrayList<>();

    public void attach(Observer o) {
        observers.add(o);
    }

    public void detach(Observer o) {
        observers.remove(o);
    }

    protected void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}
