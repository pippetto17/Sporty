package view.organizematchview;

import model.bean.MatchBean;
import view.View;

public interface OrganizeMatchView extends View {
    void displayMatchList();

    void displayNewMatchForm();


    void displayRecap(MatchBean matchBean);
}