package view.RecapView;

import controller.ApplicationController;
import model.bean.MatchBean;
import view.View;

public interface RecapView extends View {
    void setApplicationController(ApplicationController applicationController);

    void setMatchBean(MatchBean matchBean);
}
