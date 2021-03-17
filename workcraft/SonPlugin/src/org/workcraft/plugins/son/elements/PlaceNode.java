package org.workcraft.plugins.son.elements;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.son.util.Interval;

import java.awt.*;

public class PlaceNode extends MathNode implements Time, Probability {

    private Color foregroundColor = VisualCommonSettings.getBorderColor();
    private Color fillColor = VisualCommonSettings.getFillColor();
    private String label = "";
    private int errors = 0;

    private Interval startTime = new Interval(0, 9999);
    private Interval endTime = new Interval(0, 9999);
    private Interval duration = new Interval(0, 9999);

    protected Color durationColor = Color.BLACK;

    private boolean marked = false;
    private Color tokenColor = VisualCommonSettings.getBorderColor();

    private double  probability; // probability of occurrence of this PlaceNode
    protected Color probabilityColor = Color.BLACK;

    public void setMarked(boolean value) {
        if (marked != value) {
            marked = value;
            sendNotification(new PropertyChangedEvent(this, "marked"));
        }
    }

    public boolean isMarked() {
        return marked;
    }

    public void setErrors(int value) {
        if (errors != value) {
            errors = value;
            sendNotification(new PropertyChangedEvent(this, "errors"));
        }
    }

    public int getErrors() {
        return errors;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color value) {
        if (!foregroundColor.equals(value)) {
            foregroundColor = value;
            sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
        }
    }

    public void setFillColor(Color value) {
        if (!fillColor.equals(value)) {
            fillColor = value;
            sendNotification(new PropertyChangedEvent(this, "fillColor"));
        }
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setLabel(String value) {
        if (!label.equals(value)) {
            label = value;
            sendNotification(new PropertyChangedEvent(this, "label"));
        }
    }

    public String getLabel() {
        return label;
    }

    public Color getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(Color value) {
        if (!tokenColor.equals(value)) {
            tokenColor = value;
            sendNotification(new PropertyChangedEvent(this, "tokenColor"));
        }
    }

    @Override
    public void setStartTime(Interval value) {
        if (startTime != value) {
            startTime = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_START_TIME));
        }
    }

    @Override
    public Interval getStartTime() {
        return startTime;
    }

    @Override
    public void setEndTime(Interval value) {
        if (endTime != value) {
            endTime = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_END_TIME));
        }
    }

    @Override
    public Interval getEndTime() {
        return endTime;
    }

    @Override
    public void setDuration(Interval value) {
        if (duration != value) {
            duration = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_DURATION));
        }
    }

    @Override
    public Interval getDuration() { return duration; }

    public Color getDurationColor() {
        return durationColor;
    }

    public void setDurationColor(Color value) { this.durationColor = value; }

    @Override
    public void setProbability(double value) {
        if (this.probability != value) {
            this.probability = value;
            sendNotification(new PropertyChangedEvent(this, Probability.PROPERTY_PROBABILITY));
        }
    }

    @Override
    public double getProbability() { return this.probability; }

    public Color getProbabilityColor() { return probabilityColor; }

    public void setProbabilityColor(Color value) { this.probabilityColor = value; }

}
