package model.notification;

public interface Subject {

    void attach(Observer observer);

    void detach(Observer observer);

    void notifyObservers();
}
