package megamek.client.ui.panels.alphaStrike.simpleForceBuilder;

import megamek.common.alphaStrike.cardDrawer.ASCard;

import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

class CardView extends JPanel {

    final static int CARD_WIDTH = 500;
    final static int MARGIN = 15;

    private final SimpleASForceBuilder forceBuilder;
    private final SimpleASUnitTableModel model;

    CardView(SimpleASForceBuilder forceBuilder) {
        this.forceBuilder = forceBuilder;
        this.model = forceBuilder.model;
        setPreferredSize(new Dimension(CARD_WIDTH + 2 * MARGIN, CARD_WIDTH * 8 / 10 + 2 * MARGIN));
    }

    private volatile SwingWorker<Image, Void> worker;
    private volatile Image resultImage;

    /**
     * Call this to start a new drawing operation. Any currently-running draw will be cancelled.
     */
    public void triggerUpdate() {
        // Cancel previous worker if still running
        SwingWorker<Image, Void> oldWorker = worker;
        if (oldWorker != null && !oldWorker.isDone()) {
            oldWorker.cancel(true);
        }

        worker = new SwingWorker<>() {

            @Override
            protected Image doInBackground() {
                return model.unitAt(forceBuilder.unitTable.getSelectedRow())
                      .map(ASCard::createCard)
                      .map(card -> card.getCardImage(CARD_WIDTH))
                      .orElse(null);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }

                try {
                    resultImage = get();  // get the image produced
                } catch (InterruptedException | CancellationException ex) {
                    return;
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                }

                // Notify panel to repaint with the new image
                repaint();
            }
        };

        worker.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the finished result if available
        if (resultImage != null) {
            g.drawImage(resultImage, 0, 0, this);
        }
    }
}
