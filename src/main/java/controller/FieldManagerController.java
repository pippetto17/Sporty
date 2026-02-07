package controller;

import exception.AuthorizationException;
import exception.ValidationException;
import model.bean.FieldBean;
import model.bean.MatchBean;
import model.converter.FieldConverter;
import model.converter.MatchConverter;
import model.dao.DAOFactory;
import model.dao.FieldDAO;
import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;
import model.domain.User;
import model.utils.Constants;

import java.util.List;

public class FieldManagerController {
    private static final String ERROR_NOT_OWNER = Constants.ERROR_NOT_FIELD_OWNER;
    private final User fieldManager;
    private final FieldDAO fieldDAO;
    private final MatchDAO matchDAO;

    public FieldManagerController(User fieldManager, DAOFactory daoFactory)
            throws ValidationException {
        if (!fieldManager.isFieldManager()) {
            throw new ValidationException(Constants.ERROR_NOT_FIELD_MANAGER);
        }
        this.fieldManager = fieldManager;
        this.fieldDAO = daoFactory.getFieldDAO();
        this.matchDAO = daoFactory.getMatchDAO();
        this.matchDAO.deleteExpiredMatches();
    }

    public void addNewField(FieldBean fieldBean) {
        var field = FieldConverter.toEntity(fieldBean);
        field.setManager(fieldManager);
        fieldDAO.save(field);
    }

    public List<FieldBean> getMyFields() {
        return fieldDAO.findByManagerId(fieldManager.getId()).stream()
                .map(FieldConverter::toBean)
                .toList();
    }

    public List<MatchBean> getPendingRequests() {
        return matchDAO.findPendingForManager(fieldManager.getId()).stream()
                .map(match -> {
                    var bean = MatchConverter.toBean(match);
                    var field = match.getField();
                    if (field != null) {
                        bean.setFieldName(field.getName());
                    }
                    return bean;
                })
                .toList();
    }

    public void approveMatch(int matchId) throws AuthorizationException {
        validateMatchOwnership(matchId);
        matchDAO.updateStatus(matchId, MatchStatus.APPROVED);
    }

    public void rejectMatch(int matchId) throws AuthorizationException {
        validateMatchOwnership(matchId);
        matchDAO.updateStatus(matchId, MatchStatus.REJECTED);
    }

    public MatchBean getRequestDetails(int matchId) throws AuthorizationException {
        Match match = matchDAO.findById(matchId);
        if (match == null) {
            throw new AuthorizationException("Match not found: " + matchId);
        }

        validateMatchOwnership(matchId);

        var bean = MatchConverter.toBean(match);
        if (match.getField() != null) {
            bean.setFieldName(match.getField().getName());
        }
        return bean;
    }

    private void validateMatchOwnership(int matchId) throws AuthorizationException {
        List<Match> pendingMatches = getPendingMatches();
        boolean isOwner = pendingMatches.stream()
                .anyMatch(m -> m.getId() == matchId);

        if (!isOwner) {
            throw new AuthorizationException(
                    "Match " + matchId + " does not belong to any of your fields or is not pending");
        }
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

    public List<Match> getPendingMatches() {
        return matchDAO.findPendingForManager(fieldManager.getId());
    }

    public record DashboardData(int totalFields, int pendingRequests, int todayBookings, double weekRevenue) {
    }

    /* Not implemented yet */
    public void updateField(FieldBean fieldBean) throws AuthorizationException {
        validateOwnership(fieldBean.getFieldId());
        var field = FieldConverter.toEntity(fieldBean);
        field.setManager(fieldManager);
        field.setId(fieldBean.getFieldId());
        fieldDAO.save(field);
    }

    public void deleteField(int fieldId) throws AuthorizationException {
        validateOwnership(fieldId);
        fieldDAO.delete(fieldId);
    }

    private void validateOwnership(int fieldId) throws AuthorizationException {
        boolean isOwner = getMyFields().stream()
                .anyMatch(f -> f.getFieldId() == fieldId);
        if (!isOwner) {
            throw new AuthorizationException(ERROR_NOT_OWNER + fieldId);
        }
    }
}