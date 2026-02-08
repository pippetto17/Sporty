package model.converter;

import exception.DataAccessException;
import exception.ValidationException;
import model.bean.MatchBean;
import model.domain.Field;
import model.domain.Match;
import model.domain.User;

/**
 * Converter utility for transforming between Match domain entities and
 * MatchBean data transfer objects.
 * Implements the BCE (Boundary-Control-Entity) pattern for proper layer
 * separation.
 */
public class MatchConverter {
    private MatchConverter() {
    }

    /**
     * Converts a MatchBean to a Match domain entity.
     *
     * @param matchBean the match bean to convert
     * @return the Match entity, or null if matchBean is null
     * @throws DataAccessException if match data is invalid
     */
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

    /**
     * Converts a Match domain entity to a MatchBean.
     * Note: Field-related data (city, sport, price) must be enriched separately.
     *
     * @param match the match entity to convert
     * @return the MatchBean, or null if match is null
     */
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