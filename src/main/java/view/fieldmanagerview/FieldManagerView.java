package view.fieldmanagerview;

import model.bean.MatchBean;

import java.util.List;

import view.View;

public interface FieldManagerView extends View {
    void displayDashboard();

    void displayPendingRequests(List<MatchBean> requests);

    void displayNotifications();
}