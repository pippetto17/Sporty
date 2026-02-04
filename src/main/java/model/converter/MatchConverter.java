package model.converter;

import model.bean.MatchBean;
import model.domain.Match;

public class MatchConverter {
    private MatchConverter() {
    }

    public static Match toEntity(MatchBean matchBean) {
        if (matchBean == null) {
            return null;
        }
        Match match = new Match();
        match.setId(matchBean.getMatchId());
        model.domain.User organizer = new model.domain.User();
        organizer.setId(matchBean.getOrganizerId());
        match.setOrganizer(organizer);
        model.domain.Field field = new model.domain.Field();
        field.setId(matchBean.getFieldId());
        match.setField(field);
        match.setDate(matchBean.getMatchDate());
        match.setTime(matchBean.getMatchTime());
        match.setMissingPlayers(matchBean.getMissingPlayers());
        match.setStatus(matchBean.getStatus());
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
        matchBean.setCity(null);
        matchBean.setSport(null);
        return matchBean;
    }
}