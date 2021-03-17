package org.workcraft.plugins.son.elements;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_E)
@DisplayName ("Event")
@SVGIcon("images/son-node-event.svg")

public class VisualEvent extends VisualComponent implements VisualTransitionNode {

    private static double size = VisualCommonSettings.getNodeSize();
    private static double strokeWidth = VisualCommonSettings.getStrokeWidth();
    protected static double labelOffset = 0.0;

    protected Font probabilityFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.35f);
    protected static String nDP = "%.2f"; // used to restrict display of a float or double to 2 decimal places.

    private static final Positioning weightLabelPositioning = Positioning.TOP_RIGHT;
    private RenderedText weightRenderedText = new RenderedText("", probabilityFont, weightLabelPositioning, new Point2D.Double(0.0, 0.0));

    private static final Positioning probabilityLabelPositioning = Positioning.BOTTOM_RIGHT;
    private RenderedText probabilityRenderedText = new RenderedText("", probabilityFont, probabilityLabelPositioning, new Point2D.Double(0.0, 0.0));

    public VisualEvent(Event event) {
        super(event);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, "Fault",
                value -> getReferencedComponent().setFaulty(value),
                () -> getReferencedComponent().isFaulty())
                .setCombinable().setTemplatable());
    }

    @Override
    public Event getReferencedComponent() {
        return (Event) super.getReferencedComponent();
    }
    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        double xy = -size / 2 + strokeWidth / 2;
        double wh = size - strokeWidth;
        Shape shape = new Rectangle2D.Double(xy, xy, wh, wh);
        g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        g.setStroke(new BasicStroke((float) strokeWidth));
        g.draw(shape);

        drawWeightInLocalSpace(r);
        drawProbabilityInLocalSpace(r);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
        drawFault(r);
    }

    @Override
    public void drawFault(DrawRequest r) {
        if (SONSettings.isErrorTracing()) {
            Graphics2D g = r.getGraphics();
            GlyphVector glyphVector = null;
            Rectangle2D labelBB = null;

            Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.5f);

            if (isFaulty()) {
                glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "1");
            } else {
                glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "0");
            }

            labelBB = glyphVector.getVisualBounds();
            Point2D bitPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getCenterY());
            g.drawGlyphVector(glyphVector, -(float) bitPosition.getX(), -(float) bitPosition.getY());
        }
    }

    private void cacheProbabilityRenderedText() {
        String probability = "P: " + String.format(nDP, getProbability());

        Point2D offset = getOffset(probabilityLabelPositioning);
        if (probabilityLabelPositioning.ySign < 0) {
            offset.setLocation(offset.getX(), offset.getY() - labelOffset);
        }
        else {
            offset.setLocation(offset.getX(), offset.getY() + labelOffset);
        }

        if (probabilityRenderedText.isDifferent(probability, probabilityFont, probabilityLabelPositioning, offset)) {
            probabilityRenderedText = new RenderedText(probability, probabilityFont, probabilityLabelPositioning, offset);
        }
    }

    protected void drawProbabilityInLocalSpace(DrawRequest r) {
        if (SONSettings.getProbabilityVisibility()) {
            cacheProbabilityRenderedText();
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(ColorUtils.colorise(getProbabilityColor(), d.getColorisation()));
            probabilityRenderedText.draw(g);
        }
    }

    private void cacheWeightRenderedText() {
        String weight = "W: " + getWeight();

        Point2D offset = getOffset(weightLabelPositioning);
        if (weightLabelPositioning.ySign < 0) {
            offset.setLocation(offset.getX(), offset.getY() - labelOffset);
        }
        else {
            offset.setLocation(offset.getX(), offset.getY() + labelOffset);
        }

        if (weightRenderedText.isDifferent(weight, probabilityFont, weightLabelPositioning, offset)) {
            weightRenderedText = new RenderedText(weight, probabilityFont, weightLabelPositioning, offset);
        }
    }

    protected void drawWeightInLocalSpace(DrawRequest r) {
        if (SONSettings.getProbabilityVisibility()) {
            cacheWeightRenderedText();
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(ColorUtils.colorise(getProbabilityColor(), d.getColorisation()));
            weightRenderedText.draw(g);
        }
    }

    @Override
    public void cacheRenderedText(DrawRequest r) {
        super.cacheRenderedText(r);
        cacheWeightRenderedText();
        cacheProbabilityRenderedText();
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();

        if (SONSettings.getProbabilityVisibility()) {
            bb = BoundingBoxHelper.union(bb, weightRenderedText.getBoundingBox());
            bb = BoundingBoxHelper.union(bb, probabilityRenderedText.getBoundingBox());
        }

        return bb;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return (Math.abs(pointInLocalSpace.getX()) <= 0.5 * size) && (Math.abs(pointInLocalSpace.getY()) <= 0.5 * size);
    }

    @Override
    public Event getMathTransitionNode() {
        return (Event) this.getReferencedComponent();
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
        ((Event) getReferencedComponent()).setLabel(label);
    }

    @Override
    public String getLabel() {
        super.getLabel();
        return ((Event) getReferencedComponent()).getLabel();
    }

    public void setFaulty(Boolean fault) {
        ((Event) getReferencedComponent()).setFaulty(fault);
    }

    @Override
    public boolean isFaulty() {
        return ((Event) getReferencedComponent()).isFaulty();
    }

    @Override
    public Color getForegroundColor() {
        return ((Event) getReferencedComponent()).getForegroundColor();
    }

    @Override
    public void setForegroundColor(Color foregroundColor) {
        ((Event) getReferencedComponent()).setForegroundColor(foregroundColor);
    }

    @Override
    public void setFillColor(Color fillColor) {
        ((Event) getReferencedComponent()).setFillColor(fillColor);
    }

    @Override
    public Color getFillColor() {
        return ((Event) getReferencedComponent()).getFillColor();
    }

    public void setStartTime(String time) {
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event) getReferencedComponent()).setStartTime(input);
    }

    public String getStartTime() {
        return ((Event) getReferencedComponent()).getStartTime().toString();
    }

    public void setEndTime(String time) {
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event) getReferencedComponent()).setEndTime(input);
    }

    public String getEndTime() {
        return ((Event) getReferencedComponent()).getEndTime().toString();
    }

    public void setDuration(String time) {
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event) getReferencedComponent()).setDuration(input);
    }

    public String getDuration() { return ((Event) getReferencedComponent()).getDuration().toString(); }

    public double getProbability() { return getReferencedComponent().getProbability(); }

    public void setProbability(double value) { getReferencedComponent().setProbability(value); }

    public int getWeight() { return getReferencedComponent().getWeight(); }

    public void setWeight(int value) { getReferencedComponent().setWeight(value); }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualEvent) {
            VisualEvent srcComponent = (VisualEvent) src;
            setFaulty(srcComponent.isFaulty());
        }
    }

    public Color getProbabilityColor() { return getReferencedComponent().getProbabilityColor(); }

    public void setProbabilityColor(Color value) { getReferencedComponent().setProbabilityColor(value); }

}
