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
        match.setOrganizerId(matchBean.getOrganizerId());
        match.setFieldId(matchBean.getFieldId());
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
        matchBean.setOrganizerId(match.getOrganizerId());
        matchBean.setFieldId(match.getFieldId());
        matchBean.setMatchDate(match.getDate());
        matchBean.setMatchTime(match.getTime());
        matchBean.setMissingPlayers(match.getMissingPlayers());
        matchBean.setStatus(match.getStatus());
        matchBean.setCity(null);
        matchBean.setSport(null);
        return matchBean;
    }
}