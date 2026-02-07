package model.converter;

import exception.DataAccessException;
import exception.ValidationException;
import model.bean.MatchBean;
import model.domain.Field;
import model.domain.Match;
import model.domain.User;

public class MatchConverter {
    private MatchConverter() {
    }

    public static Match toEntity(MatchBean matchBean) {
        if (matchBean == null) {
            return null;
        }
        Match match = new Match();
        match.setId(matchBean.getMatchId());
        User organizer = new User();
        organizer.setId(matchBean.getOrganizerId());
        match.setOrganizer(organizer);
        Field field = new Field();
        field.setId(matchBean.getFieldId());
        match.setField(field);
        match.setDate(matchBean.getMatchDate());
        match.setTime(matchBean.getMatchTime());
        try {
            match.setMissingPlayers(matchBean.getMissingPlayers());
        } catch (ValidationException e) {
            throw new DataAccessException("Invalid match data: " + e.getMessage(), e);
        }
        match.setStatus(matchBean.getStatus());
        match.setJoinedPlayers(matchBean.getJoinedPlayers());
        return match;
    }

    public static MatchBean toBean(Match match) {
        if (match == null) {
            return null;
        }
        MatchBean matchBean = new MatchBean();
        matchBean.setMatchId(match.getId());
        matchBean.setOrganizerId(match.getOrganizer() != null ? match.getOrganizer().getId() : 0);
        matchBean.setFieldId(match.getField() != null ? match.getField().getId() : 0);
        matchBean.setMatchDate(match.getDate());
        matchBean.setMatchTime(match.getTime());
        matchBean.setMissingPlayers(match.getMissingPlayers());
        matchBean.setStatus(match.getStatus());
        matchBean.setJoinedPlayers(match.getJoinedPlayers());
        // city, sport, pricePerHour, etc. will be populated by enrichMatchBean()
        return matchBean;
    }
}