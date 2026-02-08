package view.fieldmanagerview;

import model.bean.MatchBean;
import view.View;

import java.util.List;

public interface FieldManagerView extends View {
    void displayDashboard();

    void displayPendingRequests(List<MatchBean> requests);

    void displayNotifications();
}