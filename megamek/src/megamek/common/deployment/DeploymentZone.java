package megamek.common.deployment;

import megamek.common.BoardLocation;
import megamek.common.Coords;
import megamek.common.Game;
import megamek.common.annotations.Nullable;

import java.io.Serializable;

public interface DeploymentZone extends Serializable {

    public static final int ANY_BOARD = -99;

    /**
     * Returns true when this deployment zone includes the given BoardLocation.
     *
     * <P>Note: This method may return true even if the given BoardLocation isn't actually on the board.</P>
     *
     * <P>Note: Forwards to {@link #canDeployTo(Game, Coords, int)}. Will typically not need to be overridden.</P>
     *
     * @param game          The game
     * @param boardLocation The BoardLocation to test
     * @return True when this deployment zone includes the given BoardLocation
     */
    default boolean canDeployTo(@Nullable Game game, @Nullable BoardLocation boardLocation) {
        return (game != null) && (boardLocation != null)
                && canDeployTo(game, boardLocation.getCoords(), boardLocation.getBoardId());
    }

    /**
     * Returns true when this deployment zone includes the given board location.
     *
     * <P>Note: This method may return true even if the given board location isn't actually on the board. For
     * better efficiency it should only test if the location fits the deployment zone criteria.</P>
     *
     * <P>Note: This method is called many times. The implementation should be efficient!</P>
     *
     * @param game    The game
     * @param boardId The Board to test
     * @return True when the given location is a legal deployment location
     */
    boolean canDeployTo(@Nullable Game game, @Nullable Coords coords, int boardId);

    default boolean mustWaitForOtherDeployment(Game game) {
        return false;
    }
}









/*

//    default boolean isEmpty(Game game) {
//        return allLocations(game).isEmpty();
//    }

     * Returns a list of all legal locations for this deployment zone. The list can be empty.
     * // @@MultiBoardTODO: get rid of this? check against board?
     * <P>Note: Depending on the type of deployment zone this list may contain coordinates that are not on a board!</P>
     * <P>Note: Depending on the type of deployment zone this list may be empty until some other deployment
     * has happened!</P>
     * <P>Note that this can be an expensive computation for some types of deployment zone and should be called
     * sparingly. Use {@link #canDeployTo(Game, BoardLocation)} when possible. During the lobby phase the result
     * cannot be cached as boards can still change.</P>
     *
     * @return A list of all legal deployment locations
//    List<BoardLocation> allLocations(Game game);

//    default List<Coords> allCoords(Game game, int boardId) {
//        return allLocations(game).stream()
//                .filter(bl -> bl.isOnBoard(boardId))
//                .map(BoardLocation::getCoords)
//                .collect(Collectors.toList());
//    }
     * Returns true when this deployment zone includes the given BoardLocation and if the given facing is also allowed.
     *
     * <P>Note: By default, this method forwards to {@link #canDeployTo(Game, BoardLocation)} and thus does not
     * care about the given facing. It may be overridden to take the facing into account.</P>
     *
     * @param boardLocation The BoardLocation to test
     * @param game
     * @param facing        The facing of the unit to test
     * @return True when the given BoardLocation and facing are legal
//    default boolean canDeployTo(BoardLocation boardLocation, Game game, int facing) {
//        return canDeployTo(, boardLocation);
//    }
    DeploymentZoneType getType();
     * Returns the ID of all boards that contain at least one hex where deployment is legal with this deployment zone.
     *
     * <P>Note: This method may be called many times. Provide an efficient implementation.</P>
     *
     * @return The board IDs of all boards that are at least partly convered by this deployment zone.
//    Set<Integer> getLegalBoardIds(Game game);

 */