package controller;

import model.domain.User;
import model.domain.Role;

public class HomeController {
    private final User currentUser;
    private final ApplicationController applicationController;

    public HomeController(User user, ApplicationController applicationController) {
        this.currentUser = user;
        this.applicationController = applicationController;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Role getUserRole() {
        return Role.fromCode(currentUser.getRole());
    }

    public boolean isOrganizer() {
        // Use behavioral method from User entity
        return currentUser.isOrganizer();
    }

    public boolean isPlayer() {
        // Use behavioral method from User entity
        return currentUser.isPlayer();
    }

    // Metodo per ottenere la lista di partite (stub per ora)
    public String[] getMatches() {
        // TODO: implementare quando avremo la gestione delle partite
        return new String[]{
            "Match 1 - Football - 15/01/2026",
            "Match 2 - Basketball - 16/01/2026",
            "Match 3 - Tennis - 17/01/2026"
        };
    }

    // Metodo per organizzare una partita
    public void organizeMatch() {
        applicationController.navigateToOrganizeMatch(currentUser);
    }
}

