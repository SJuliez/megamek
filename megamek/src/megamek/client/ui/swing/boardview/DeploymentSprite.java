package megamek.client.ui.swing.boardview;

import megamek.client.ui.swing.util.UIUtil;
import megamek.common.Coords;

import java.awt.*;
import java.awt.image.ImageObserver;

public class DeploymentSprite extends HexSprite {

    private static Image deploymentProhibitedSprite;

//    private static final Color color = new Color(250, 250, 10, 80);
private static final Color color = new Color(250, 1, 10, 80);
    private static final Color outerColor = new Color(10, 255, 10, 160);

    private static float currentBoardScale = 0;

    public DeploymentSprite(BoardView boardView1) {
        super(boardView1, new Coords(0, 0));
    }

    @Override
    public void prepare() {
        updateBounds();
        deploymentProhibitedSprite = createNewHexImage();
        Graphics2D graph = (Graphics2D) deploymentProhibitedSprite.getGraphics();
        UIUtil.setHighQualityRendering(graph);
        graph.scale(bv.scale, bv.scale);

//        graph.setColor(color);
//        graph.setStroke(new BasicStroke(7));
//        graph.draw(HexDrawUtilities.getHexFullBorderLine(8));

        graph.setColor(color);
        graph.setStroke(new BasicStroke(3));
        graph.draw(HexDrawUtilities.getHexFullBorderLine(6));

        graph.dispose();
        currentBoardScale = bv.scale;
    }

    @Override
    public void drawOnto(Graphics g, int x, int y, ImageObserver observer, boolean makeTranslucent) {
        if (!isReady() || currentBoardScale != bv.scale) {
            prepare();
        }
        if (isReady()) { // for safety
            g.drawImage(deploymentProhibitedSprite, x, y, observer);
        }
    }

    @Override
    public boolean isReady() {
        return deploymentProhibitedSprite != null;
    }
}