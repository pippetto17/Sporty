package view.recapview;

import controller.ApplicationController;
import model.bean.MatchBean;

/**
 * CLI implementation of RecapView.
 * This is a placeholder implementation - the recap functionality is not yet implemented for CLI.
 */
public class CLIRecapView implements RecapView {

    /**
     * Default constructor.
     */
    public CLIRecapView() {
        // Empty constructor - no initialization needed for placeholder implementation
    }

    @Override
    public void setApplicationController(ApplicationController applicationController) {
        // No-op: Application controller not needed for CLI placeholder implementation
    }

    @Override
    public void setMatchBean(MatchBean matchBean) {
        // No-op: Match bean not needed for CLI placeholder implementation
    }

    @Override
    public void display() {
        System.out.println("=== RECAP (CLI NOT IMPLEMENTED) ===");
    }

    @Override
    public void close() {
        // No-op: Nothing to close in CLI placeholder implementation
    }
}
