package megamek.common;

import megamek.common.annotations.Nullable;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public final class MultiBoardHelper {

//    public static boolean hasEnclosingBoard(Game game, int boardId) {
//        return boardExists(game.getBoard(boardId).getEnclosingBoard());
//    }
//
//    public static @Nullable Board getEnclosingBoard(Game game, Board board) {
//        return game.getBoard(board.getEnclosingBoard());
//    }
//
//    public static boolean boardExists(int boardId) {
//        return gameBoards.containsKey(boardId);
//    }
//
//    public static void connectBoards(int lowerBoardId, int higherBoardId, Coords coords) {
//        if (!boardExists(lowerBoardId) || !boardExists(higherBoardId)) {
//            LogManager.getLogger().error("Can't set an enclosing board for non-existent boards.");
//            return;
//        }
//        Board lowerBoard = getBoard(lowerBoardId);
//        Board higherBoard = getBoard(higherBoardId);
//        if ((lowerBoard.inAtmosphere() && !higherBoard.inSpace()) || (lowerBoard.onGround() && !higherBoard.inAtmosphere())
//                || lowerBoard.inSpace() || higherBoard.onGround()) {
//            LogManager.getLogger().error("Can only enclose a ground map in an atmo map or an atmo map in a space map.");
//            return;
//        }
//        if (!higherBoard.contains(coords)) {
//            LogManager.getLogger().error("Higher map doesn't contain the given coords.");
//            return;
//        }
//        lowerBoard.setEnclosingBoard(higherBoardId);
//        higherBoard.setEmbeddedBoard(lowerBoardId, coords);
//    }
//
//    /** @return True when both given units are not null and reside on the same board. */
//    public static boolean onTheSameBoard(@Nullable Targetable entity1, @Nullable Targetable entity2) {
//        return (entity1 != null) && (entity2 != null) && (entity1.getBoardId() == entity2.getBoardId());
//    }
//
//    /**
//     * Returns true when both given units or objects are on directly connected boards, such as a ground map
//     * and its enclosing atmospheric map. Returns false if they are on connected maps that are one or more
//     * other maps "apart", such as a ground map and a connected high-atmo map.
//     *
//     * @param entity1 The first unit or object to test
//     * @param entity2 The second unit or object to test
//     *
//     * @return True when both units or objects are on directly connected boards
//     */
//    public static boolean onDirectlyConnectedBoards(@Nullable Targetable entity1, @Nullable Targetable entity2) {
//        if ((entity1 != null) && (entity2 != null)) {
//            Board board1 = getBoard(entity1);
//            Board board2 = getBoard(entity2);
//            return (board1.getEnclosingBoard() == board2.getBoardId()) || (board2.getEnclosingBoard() == board1.getBoardId());
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * Returns true when both given units or objects are on vertically connected boards. A ground map, its
//     * enclosing atmospheric map and that map's enclosing high-atmo map are vertically connected.
//     * A high-atmo map may enclose multiple atmospheric maps which in turn may enclose multiple ground maps;
//     * such ground maps are all connected in the sense that a fighter unit can reach them all but they do
//     * not count as vertically connected; e.g., no attacks are possible between them.
//     *
//     * @param entity1 The first unit or object to test
//     * @param entity2 The second unit or object to test
//     *
//     * @return True when both units or objects are on vertically connected boards
//     */
//    public static boolean onConnectedBoards(@Nullable Targetable entity1, @Nullable Targetable entity2) {
//        if ((entity1 != null) && (entity2 != null)) {
//            Board board1 = getBoard(entity1);
//            Board board2 = getBoard(entity2);
//            return (board1.getEnclosingBoard() == board2.getBoardId()) || (board2.getEnclosingBoard() == board1.getBoardId());
//        } else {
//            return false;
//        }
//    }
//
//    public static boolean isOnGround(BoardLocation boardLocation) {
//        return getBoard(boardLocation).onGround();
//    }
//
//    /**
//     * Returns a list of IDs of all enclosing boards of the given board. These are at most two other boards;
//     * for a ground board, the enclosing atmospheric board (if present) and that one's enclosing high-atmo
//     * map (if present). For an atmospheric map, this will be at most the enclosing high-atmo map (if present);
//     * for any space map, the returned List will be empty.
//     *
//     * @param boardId The board to find enclosed boards for
//     * @return All enclosing boards in the hierarchy of the given board (between zero and two boards)
//     */
//    public static List<Integer> getAllEnclosingBoards(int boardId) {
//        List<Integer> allEnclosingBoards = new ArrayList<>();
//        if (hasEnclosingBoard(boardId)) {
//            Board board = getBoard(boardId);
//            Board enclosingBoard = getEnclosingBoard(board);
//            allEnclosingBoards.add(enclosingBoard.getBoardId());
//            if (hasEnclosingBoard(enclosingBoard.getBoardId())) {
//                Board secondEnclosingBoard = getEnclosingBoard(enclosingBoard);
//                allEnclosingBoards.add(secondEnclosingBoard.getBoardId());
//            }
//        }
//        return allEnclosingBoards;
//    }
}
