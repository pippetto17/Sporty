package controller;

import exception.ValidationException;
import model.bean.FieldBean;
import model.domain.User;
import java.util.List;

public class FieldManagerController {
    private static final String ERROR_NOT_OWNER = model.utils.Constants.ERROR_NOT_FIELD_OWNER;
    private final User fieldManager;
    private final model.dao.FieldDAO fieldDAO;
    private final model.dao.MatchDAO matchDAO;

    public FieldManagerController(User fieldManager, model.dao.DAOFactory daoFactory)
            throws ValidationException {
        if (!fieldManager.isFieldManager()) {
            throw new ValidationException(model.utils.Constants.ERROR_NOT_FIELD_MANAGER);
        }
        this.fieldManager = fieldManager;
        this.fieldDAO = daoFactory.getFieldDAO();
        this.matchDAO = daoFactory.getMatchDAO();
        this.matchDAO.deleteExpiredMatches();
    }

    public void addNewField(FieldBean fieldBean) {
        var field = model.converter.FieldConverter.toEntity(fieldBean);
        field.setManagerId(fieldManager.getId());
        fieldDAO.save(field);
    }

    public List<FieldBean> getMyFields() {
        return fieldDAO.findByManagerId(fieldManager.getId()).stream()
                .map(model.converter.FieldConverter::toBean)
                .toList();
    }

    public void updateField(FieldBean fieldBean) {
        validateOwnership(fieldBean.getFieldId());
        var field = model.converter.FieldConverter.toEntity(fieldBean);
        field.setManagerId(fieldManager.getId());
        field.setId(fieldBean.getFieldId());
        fieldDAO.save(field);
    }

    public void deleteField(int fieldId) {
        validateOwnership(fieldId);
        fieldDAO.delete(fieldId);
    }

    public List<model.bean.MatchBean> getPendingRequests() {
        return matchDAO.findPendingForManager(fieldManager.getId()).stream()
                .map(match -> {
                    var bean = model.converter.MatchConverter.toBean(match);
                    var field = fieldDAO.findById(match.getFieldId());
                    if (field != null) {
                        bean.setFieldName(field.getName());
                    }
                    return bean;
                })
                .toList();
    }

    public void approveMatch(int matchId) {
        matchDAO.updateStatus(matchId, model.domain.MatchStatus.APPROVED);
    }

    public void rejectMatch(int matchId) {
        matchDAO.updateStatus(matchId, model.domain.MatchStatus.REJECTED);
    }

    public DashboardData getDashboardData() {
        var fields = getMyFields();
        var pendingRequests = getPendingRequests();
        int totalFields = fields.size();
        int pendingCount = pendingRequests.size();
        return new DashboardData(totalFields, pendingCount, 0, 0.0);
    }

    public User getFieldManager() {
        return fieldManager;
    }

    private void validateOwnership(int fieldId) {
        getMyFields().stream()
                .filter(f -> f.getFieldId() == fieldId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ERROR_NOT_OWNER + fieldId));
    }

    public List<model.domain.Match> getPendingMatches() {
        return matchDAO.findPendingForManager(fieldManager.getId());
    }

    public record DashboardData(int totalFields, int pendingRequests, int todayBookings, double weekRevenue) {
    }
}