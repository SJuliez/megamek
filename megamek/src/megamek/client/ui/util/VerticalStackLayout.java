package megamek.client.ui.util;

import java.awt.*;

/**
 * A simple vertical stacking layout manager. BoxLayout has a few annoying habits, namely it requires dealing with
 * AlignmentX values, and it stretches components to fill its area. This LayoutManager is similar to BoxLayout but
 * without these habits.
 * <p>
 * Components are laid out top-to-bottom, top- and left-aligned. Each component's preferred height is respected (no
 * stretching vertically); the layout leaves empty space at the bottom and right. Depending on a parameter at
 * construction, it respects each component's preferred width or stretches components horizontally to container width.
 */
public class VerticalStackLayout implements LayoutManager2 {

    private final int gap;
    private final boolean usePreferredWidth;

    /**
     * @param gap               spacing between components
     * @param usePreferredWidth whether to preserve preferred widths
     */
    public VerticalStackLayout(int gap, boolean usePreferredWidth) {
        this.gap = gap;
        this.usePreferredWidth = usePreferredWidth;
    }

    public VerticalStackLayout() {
        this(0, false);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int availableWidth = parent.getWidth() - insets.left - insets.right;
            int y = insets.top;
            for (Component component : parent.getComponents()) {
                if (!component.isVisible()) {
                    continue;
                }
                Dimension pref = component.getPreferredSize();
                int width = usePreferredWidth ? pref.width : availableWidth;
                int height = pref.height;
                component.setBounds(insets.left, y, width, height);
                y += height + gap;
            }
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int width = 0;
            int height = 0;
            boolean first = true;

            for (Component component : parent.getComponents()) {
                if (!component.isVisible()) {
                    continue;
                }
                Dimension pref = component.getPreferredSize();
                width = Math.max(width, pref.width);
                if (!first) {
                    height += gap;
                }
                height += pref.height;
                first = false;
            }
            return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int width = 0;
            int height = 0;
            boolean first = true;

            for (Component component : parent.getComponents()) {
                if (!component.isVisible()) {
                    continue;
                }
                Dimension min = component.getMinimumSize();
                width = Math.max(width, min.width);
                if (!first) {
                    height += gap;
                }
                height += min.height;
                first = false;
            }
            return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
        }
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.0f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.0f;
    }

    @Override
    public void invalidateLayout(Container target) { }
}
