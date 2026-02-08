package model.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class implementing the Subject role in the Observer pattern.
 * Manages a list of observers and provides methods to attach, detach, and
 * notify them.
 * This follows the Gang of Four (GoF) Observer design pattern.
 */
public abstract class Subject {
    private List<Observer> observers = new ArrayList<>();

    /**
     * Attaches an observer to this subject.
     *
     * @param o the observer to attach
     */
    public void attach(Observer o) {
        observers.add(o);
    }

    /**
     * Detaches an observer from this subject.
     *
     * @param o the observer to detach
     */
    public void detach(Observer o) {
        observers.remove(o);
    }

    /**
     * Notifies all attached observers by calling their update() method.
     */
    protected void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}
