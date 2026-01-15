package model.converter;

import model.bean.MatchBean;
import model.domain.Match;

public class MatchConverter {

    private MatchConverter() {
        // Private constructor to prevent instantiation
    }

    public static Match toMatch(MatchBean matchBean) {
        if (matchBean == null) {
            return null;
        }

        Match match = new Match();
        match.setMatchId(matchBean.getMatchId());
        match.setSport(matchBean.getSport());
        match.setMatchDate(matchBean.getMatchDate());
        match.setMatchTime(matchBean.getMatchTime());
        match.setCity(matchBean.getCity());
        match.setRequiredParticipants(matchBean.getRequiredParticipants());
        match.setOrganizerUsername(matchBean.getOrganizerUsername());
        match.setFieldId(matchBean.getFieldId());
        match.setPricePerPerson(matchBean.getPricePerPerson());
        match.setStatus(matchBean.getStatus());

        // Convert participants
        if (matchBean.getParticipants() != null) {
            match.setParticipants(matchBean.getParticipants());
        }

        return match;
    }

    public static MatchBean toMatchBean(Match match) {
        if (match == null) {
            return null;
        }

        MatchBean matchBean = new MatchBean();
        matchBean.setMatchId(match.getMatchId());
        matchBean.setSport(match.getSport());
        matchBean.setMatchDate(match.getMatchDate());
        matchBean.setMatchTime(match.getMatchTime());
        matchBean.setCity(match.getCity());
        matchBean.setRequiredParticipants(match.getRequiredParticipants());
        matchBean.setOrganizerUsername(match.getOrganizerUsername());
        matchBean.setFieldId(match.getFieldId());
        matchBean.setPricePerPerson(match.getPricePerPerson());
        matchBean.setStatus(match.getStatus());

        // Convert participants
        if (match.getParticipants() != null) {
            matchBean.setParticipants(match.getParticipants());
        }

        return matchBean;
    }

    // Alias methods for consistency
    public static Match toEntity(MatchBean bean) {
        return toMatch(bean);
    }

    public static MatchBean toBean(Match entity) {
        return toMatchBean(entity);
    }
}
