package model.observer;

/**
 * Observer interface implementing the Observer role in the Observer pattern.
 * Classes implementing this interface can subscribe to Subject instances
 * and receive notifications when the subject's state changes.
 * This follows the Gang of Four (GoF) Observer design pattern.
 */
public interface Observer {
    /**
     * Called when the observed subject's state changes.
     * Observers should pull any necessary data from the subject.
     */
    void update();
}
