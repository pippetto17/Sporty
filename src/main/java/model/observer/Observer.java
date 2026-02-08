package model.observer;

/**
 * GoF Observer interface (Pull Model).
 * Observers are notified when the Subject's state changes,
 * then pull the relevant data from the Subject.
 */
public interface Observer {
    void update();
}
