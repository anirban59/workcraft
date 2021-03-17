package org.workcraft.plugins.son.elements;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

@DisplayName("ChannelPlace")
@SVGIcon("images/son-node-channel_place.svg")
public class VisualChannelPlace extends VisualPlaceNode {

    private static double size = 1.2;
    private static float strokeWidth = 0.2f;

    public VisualChannelPlace(ChannelPlace cplace) {
        super(cplace);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        double s = size - strokeWidth;
        Shape shape = new Ellipse2D.Double(-s / 2, -s / 2, s, s);
        g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setStroke(new BasicStroke(strokeWidth));
        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        g.draw(shape);

        drawToken(r);
        drawErrorInLocalSpace(r);
        drawDurationInLocalSpace(r);
        drawProbabilityInLocalSpace(r);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

}
