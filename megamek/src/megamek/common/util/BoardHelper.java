package megamek.common.util;

import megamek.common.*;
import megamek.common.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BoardHelper {

    public static int enclosingBoardId(Game game, BoardLocation boardLocation) {
        return game.getBoard(boardLocation).getEnclosingBoardId();
    }

    public static @Nullable Board enclosingBoard(Game game, BoardLocation boardLocation) {
        return game.getBoard(enclosingBoardId(game, boardLocation));
    }

    public static @Nullable Board enclosingBoard(Game game, int boardId) {
        return game.getBoard(game.getBoard(boardId).getEnclosingBoardId());
    }

    public static @Nullable Board enclosingBoard(Game game, Board board) {
        return game.getBoard(board.getEnclosingBoardId());
    }

    public static @Nullable Coords positionOnEnclosingBoard(Game game, Board board) {
        Board enclosingBoard = enclosingBoard(game, board);
        if (enclosingBoard != null) {
            return enclosingBoard.embeddedBoardPosition(board.getBoardId());
        } else {
            return null;
        }
    }

    public static @Nullable Coords positionOnEnclosingBoard(Game game, int boardId) {
        Board enclosingBoard = enclosingBoard(game, boardId);
        if (enclosingBoard != null) {
            return enclosingBoard.embeddedBoardPosition(boardId);
        } else {
            return null;
        }
    }

    public static boolean onDifferentGroundMaps(Game game, Entity attacker, Targetable target) {
        return game.isOnGroundMap(attacker) && game.isOnGroundMap(target)
                && !game.onTheSameBoard(attacker, target);
    }

    public static boolean isBoardEdge(Board board, Coords coords) {
        return (coords != null) && ((coords.getX() == 0) || (coords.getX() == board.getWidth() - 1)
                || (coords.getY() == 0) || (coords.getY() == board.getHeight() - 1));
    }

    public static List<Coords> topEdge(Board board) {
        return coordsRow(board, 0);
    }

    public static List<Coords> bottomEdge(Board board) {
        return coordsRow(board, board.getHeight() - 1);
    }

    public static List<Coords> leftEdge(Board board) {
        return coordsColumn(board, 0);
    }

    public static List<Coords> rightEdge(Board board) {
        return coordsColumn(board, board.getWidth() - 1);
    }

    public static List<Coords> coordsLine(Board board, Coords oneCoords, int facing) {
        List<Coords> result = new ArrayList<>();
        if (board.contains(oneCoords)) {
            result.add(oneCoords);
            Coords next = oneCoords.translated(facing);
            while (board.contains(next)) {
                result.add(next);
                next = oneCoords.translated(facing);
            }
            Coords opposite = oneCoords.translated((facing + 3) % 6);
            while (board.contains(opposite)) {
                result.add(opposite);
                opposite = oneCoords.translated((facing + 3) % 6);
            }
        }
        return result;
    }

    public static List<Coords> coordsRow(Board board, int y) {
        List<Coords> result = new ArrayList<>();
        for (int x = 0; x < board.getWidth() - 1; x++) {
            result.add(new Coords(x, y));
        }
        return result;
    }

    public static List<Coords> coordsColumn(Board board, int x) {
        List<Coords> result = new ArrayList<>();
        for (int y = 0; y < board.getHeight() - 1; y++) {
            result.add(new Coords(x, y));
        }
        return result;
    }

    private BoardHelper() { }
}
