/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.ObservableHierarchyImpl;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualConnection extends VisualNode implements Node, Drawable, DependentNode,
		Connection, VisualConnectionProperties, ObservableHierarchy {

	public enum ConnectionType {
		POLYLINE, BEZIER
	};

	public enum ScaleMode {
		NONE, LOCK_RELATIVELY, SCALE, STRETCH, ADAPTIVE
	}

	private ObservableHierarchyImpl observableHierarchyImpl = new ObservableHierarchyImpl();

	private MathConnection refConnection;
	private VisualComponent first;
	private VisualComponent second;

	private ConnectionType connectionType = ConnectionType.POLYLINE;
	private ScaleMode scaleMode = ScaleMode.LOCK_RELATIVELY;

	private ConnectionGraphic graphic = null;

	private static double defaultLineWidth = 0.02;
	private static double defaultArrowWidth = 0.15;
	private static double defaultArrowLength = 0.4;
	private static double defaultBubbleSize = 0.15;
	public static double HIT_THRESHOLD = 0.2;
	private static Color defaultColor = Color.BLACK;

	private Color color = defaultColor;
	private double lineWidth = defaultLineWidth;

	private boolean hasArrow = true;
	private double arrowWidth = defaultArrowWidth;
	private double arrowLength = defaultArrowLength;

	private boolean hasBubble = false;
	private double bubbleSize = defaultBubbleSize;

	private LinkedHashSet<Node> children = new LinkedHashSet<Node>();
	private ComponentsTransformObserver componentsTransformObserver = null;

	public VisualConnection() {
	}

	public VisualConnection(MathConnection refConnection,
			VisualComponent first, VisualComponent second) {
		this.refConnection = refConnection;
		this.first = first;
		this.second = second;
		this.graphic = new Polyline(this);

		initialise();
	}

	protected void initialise() {
		addPropertyDeclaration(new PropertyDeclaration(this, "Line width",
				"getLineWidth", "setLineWidth", double.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Arrow width",
				"getArrowWidth", "setArrowWidth", double.class));

		LinkedHashMap<String, Object> arrowLengths = new LinkedHashMap<String, Object>();
		arrowLengths.put("short", 0.2);
		arrowLengths.put("medium", 0.4);
		arrowLengths.put("long", 0.8);

		addPropertyDeclaration(new PropertyDeclaration(this, "Arrow length",
				"getArrowLength", "setArrowLength", double.class, arrowLengths));

		LinkedHashMap<String, Object> hm = new LinkedHashMap<String, Object>();

		hm.put("Polyline", ConnectionType.POLYLINE);
		hm.put("Bezier", ConnectionType.BEZIER);

		addPropertyDeclaration(new PropertyDeclaration(this, "Connection type",
				"getConnectionType", "setConnectionType", ConnectionType.class, hm));

		LinkedHashMap<String, Object> hm2 = new LinkedHashMap<String, Object>();

		hm2.put("Lock anchors", ScaleMode.NONE);
		hm2.put("Bind to components", ScaleMode.LOCK_RELATIVELY);
		hm2.put("Proportional", ScaleMode.SCALE);
		hm2.put("Stretch", ScaleMode.STRETCH);
		hm2.put("Adaptive", ScaleMode.ADAPTIVE);

		addPropertyDeclaration(new PropertyDeclaration(this, "Scale mode",
				"getScaleMode", "setScaleMode", ScaleMode.class, hm2));

		componentsTransformObserver = new ComponentsTransformObserver(this);

		children.add(componentsTransformObserver);
		children.add(graphic);

		if (refConnection instanceof ObservableState)
			((ObservableState) refConnection).addObserver(new StateObserver() {
				public void notify(StateEvent e) {
					observableStateImpl.sendNotification(e);
				}
			});
	}

	public void setVisualConnectionDependencies(VisualComponent first,
			VisualComponent second, ConnectionGraphic graphic,
			MathConnection refConnection) {
		if (first == null)
			throw new NullPointerException("first");
		if (second == null)
			throw new NullPointerException("second");
		if (graphic == null)
			throw new NullPointerException("graphic");

		this.first = first;
		this.second = second;
		this.refConnection = refConnection;
		this.graphic = graphic;

		if (graphic instanceof Polyline)
			this.connectionType = ConnectionType.POLYLINE;
		else if (graphic instanceof Bezier)
			this.connectionType = ConnectionType.BEZIER;

		initialise();
	}

	@NoAutoSerialisation
	public ConnectionType getConnectionType() {
		return connectionType;
	}

	@NoAutoSerialisation
	public void setConnectionType(ConnectionType t) {
		if (connectionType != t) {
			observableHierarchyImpl.sendNotification(new NodesDeletingEvent(
					this, graphic));
			children.remove(graphic);
			observableHierarchyImpl.sendNotification(new NodesDeletedEvent(
					this, graphic));

			if (t == ConnectionType.POLYLINE) {
				Polyline p = new Polyline(this);
				this.graphic = p;

				VisualComponent v = this.getFirst();
				if (v == this.getSecond()) {
					ControlPoint cp1 = new ControlPoint();
					cp1.setPosition(new Point2D.Double(v.getX() - 1.0, v.getY() + 1.5));
					p.add(cp1);
					cp1.setHidden(true);
					ControlPoint cp2 = new ControlPoint();
					cp2.setPosition(new Point2D.Double(v.getX() + 1.0, v.getY() + 1.5));
					p.add(cp2);
					cp2.setHidden(true);
				}
			}

			if (t == ConnectionType.BEZIER) {
				Bezier b = new Bezier(this);
				b.setDefaultControlPoints();
				graphic = b;

				VisualComponent v = this.getFirst();
				if (v == this.getSecond()) {
					BezierControlPoint[] cp = b.getControlPoints();
					cp[0].setPosition(new Point2D.Double(v.getX() - 2.0, v.getY() + 2.0));
					cp[0].setHidden(true);
					cp[1].setPosition(new Point2D.Double(v.getX() + 2.0, v.getY() + 2.0));
					cp[1].setHidden(true);
				}
			}

			children.add(graphic);
			observableHierarchyImpl.sendNotification(new NodesAddedEvent(this,	graphic));

			graphic.invalidate();
			connectionType = t;
			observableStateImpl.sendNotification(new PropertyChangedEvent(this, "connectionType"));
		}
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public Color getDrawColor() {
		return getColor();
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double lineWidth) {
		if (lineWidth < 0.01)
			lineWidth = 0.01;
		if (lineWidth > 0.5)
			lineWidth = 0.5;
		this.lineWidth = lineWidth;

		invalidate();
	}

	@Override
	public Stroke getStroke() {
		return new BasicStroke((float) getLineWidth());
	}

	@Override
	public boolean hasArrow() {
		return hasArrow;
	}

	public void setArrow(boolean value) {
		hasArrow = value;
	}

	@Override
	public double getArrowWidth() {
		return arrowWidth;
	}

	public void setArrowWidth(double value) {
		if (value > 1)	value = 1;
		if (value < 0.1) value = 0.1;
		this.arrowWidth = value;

		invalidate();
	}

	@Override
	public double getArrowLength() {
		if (!hasArrow())
			return 0.0;
		return arrowLength;
	}

	public void setArrowLength(double value) {
		if (value > 1) value = 1;
		if (value < 0.1) value = 0.1;
		this.arrowLength = value;
		invalidate();
	}

	private void invalidate() {
		if (graphic != null) {
			graphic.invalidate();
		}
	}

	@Override
	public boolean hasBubble() {
		return hasBubble;
	}

	public void setBubble(boolean value) {
		hasBubble = value;
	}

	@Override
	public double getBubbleSize() {
		if (!hasArrow()) return 0.0;
		return bubbleSize;
	}

	public void setBubbleSize(double value) {
		if (value > 1)value = 1;
		if (value < 0.1) value = 0.1;
		this.bubbleSize = value;
		invalidate();
	}

	public Point2D getPointOnConnection(double t) {
		return graphic.getPointOnCurve(t);
	}

	public Point2D getNearestPointOnConnection(Point2D pt) {
		return graphic.getNearestPointOnCurve(pt);
	}

	@Override
	public void setParent(Node parent) {
		super.setParent(parent);
		invalidate();
	};

	@Override
	public void draw(DrawRequest r) {

	}

	public MathConnection getReferencedConnection() {
		return refConnection;
	}

	public boolean hitTest(Point2D pointInParentSpace) {
		return graphic.hitTest(pointInParentSpace);
	}

	@Override
	public Rectangle2D getBoundingBox() {
		return graphic.getBoundingBox();
	}

	public VisualComponent getFirst() {
		return first;
	}

	public VisualComponent getSecond() {
		return second;
	}

	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getReferencedConnection());
		return ret;
	}

	public ConnectionGraphic getGraphic() {
		return graphic;
	}

	@Override
	public Collection<Node> getChildren() {
		return children;
	}

	public void addObserver(HierarchyObserver obs) {
		observableHierarchyImpl.addObserver(obs);
	}

	public void removeObserver(HierarchyObserver obs) {
		observableHierarchyImpl.removeObserver(obs);
	}
	@Override
	public Point2D getFirstCenter() {
		return componentsTransformObserver.getFirstCenter();
	}

	@Override
	public Touchable getFirstShape() {
		return componentsTransformObserver.getFirstShape();
	}

	@Override
	public Point2D getSecondCenter() {
		return componentsTransformObserver.getSecondCenter();
	}

	@Override
	public Touchable getSecondShape() {
		return componentsTransformObserver.getSecondShape();
	}

	@Override
	public ScaleMode getScaleMode() {
		return scaleMode;
	}

	@Override
	public Point2D getCenter() {
		return graphic.getCenter();
	}

	public void setScaleMode(ScaleMode scaleMode) {
		this.scaleMode = scaleMode;
	}

}