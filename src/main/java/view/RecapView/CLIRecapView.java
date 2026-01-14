package view.RecapView;

import controller.ApplicationController;
import model.bean.MatchBean;

public class CLIRecapView implements RecapView {

    public CLIRecapView() {
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        // No-op
    }

    @Override
    public void setMatchBean(MatchBean matchBean) {
        // No-op
    }

    @Override
    public void display() {
        System.out.println("=== RECAP (CLI NOT IMPLEMENTED) ===");
    }

    @Override
    public void close() {
        // No-op
    }
}
