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
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.util.Geometry;
import org.workcraft.util.XmlUtil;
import org.workcraft.util.Geometry.CurveSplitResult;

public class Bezier implements ConnectionGraphic, ParametricCurve, StateObserver, SelectionObserver {
	private CubicCurve2D curve = new CubicCurve2D.Double();
	private CubicCurve2D visibleCurve = new CubicCurve2D.Double();

	private PartialCurveInfo curveInfo;
	private VisualConnectionProperties connectionInfo;

	private Node parent;
	private BezierControlPoint cp1, cp2;

	private Rectangle2D boundingBox = null;

	public Bezier(VisualConnection parent) {
		cp1 = new BezierControlPoint(this);
		cp2 = new BezierControlPoint(this);

		cp1.setPosition(Geometry.lerp(parent.getFirstCenter(), parent.getSecondCenter(), 0.3));
		cp2.setPosition(Geometry.lerp(parent.getFirstCenter(), parent.getSecondCenter(), 0.6));

		cp1.addObserver(this);
		cp2.addObserver(this);

		this.connectionInfo = parent;
		this.parent = parent;
	}

	public void draw(Graphics2D g) {
		g.setColor(connectionInfo.getDrawColor());
		g.setStroke(new BasicStroke((float)connectionInfo.getLineWidth()));

		g.draw(visibleCurve);

		DrawHelper
				.drawArrowHead(g, connectionInfo.getDrawColor(),
						curveInfo.arrowHeadPosition,
						curveInfo.arrowOrientation, connectionInfo
								.getArrowLength(), connectionInfo
								.getArrowWidth());
	}

	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}

	public void readFromXML(Element element) {
		Element anchors;
		anchors = XmlUtil.getChildElement("anchorPoints", element);
		if (anchors==null) return;
		List<Element> xap = XmlUtil.getChildElements(ControlPoint.class.getSimpleName(), anchors);
		if (xap==null) return;

		Element eap = xap.get(0);
		cp1.setX(XmlUtil.readDoubleAttr(eap, "X", 0));
		cp1.setY(XmlUtil.readDoubleAttr(eap, "Y", 0));

		eap=xap.get(1);
		cp2.setX(XmlUtil.readDoubleAttr(eap, "X", 0));
		cp2.setY(XmlUtil.readDoubleAttr(eap, "Y", 0));
	}

	public void writeToXML(Element element) {
		Element anchors = XmlUtil.createChildElement("anchorPoints", element);
		Element xap = XmlUtil.createChildElement(ControlPoint.class.getSimpleName(), anchors);
		XmlUtil.writeDoubleAttr(xap, "X", cp1.getX());
		XmlUtil.writeDoubleAttr(xap, "Y", cp1.getY());
		xap = XmlUtil.createChildElement(ControlPoint.class.getSimpleName(), anchors);
		XmlUtil.writeDoubleAttr(xap, "X", cp2.getX());
		XmlUtil.writeDoubleAttr(xap, "Y", cp2.getY());
	}

	private CubicCurve2D getPartialCurve(double tStart, double tEnd)
	{
		CubicCurve2D fullCurve = new CubicCurve2D.Double();
		fullCurve.setCurve(connectionInfo.getFirstCenter(), cp1.getPosition(), cp2.getPosition(), connectionInfo.getSecondCenter());

		CurveSplitResult firstSplit = Geometry.splitCubicCurve(fullCurve, tStart);
		CurveSplitResult secondSplit = Geometry.splitCubicCurve(firstSplit.curve2, (tEnd-tStart)/(1-tStart) );

		return secondSplit.curve1;
	}

	private void updateVisibleRange() {
		visibleCurve = getPartialCurve(curveInfo.tStart, curveInfo.tEnd);
	}

	@Override
	public Collection<Node> getChildren() {
		return Arrays.asList( new Node[] { cp1, cp2 });
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public void setParent(Node parent) {
		throw new RuntimeException ("Node does not support reparenting");
	}

	@Override
	public boolean hitTest(Point2D point) {
		return getDistanceToCurve(point) < VisualConnection.HIT_THRESHOLD;
	}


	public void update() {
		curve.setCurve(connectionInfo.getFirstCenter(), cp1.getPosition(), cp2.getPosition(), connectionInfo.getSecondCenter());

		boundingBox = curve.getBounds2D();
		boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);


		Point2D origin1 = new Point2D.Double();
		origin1.setLocation(connectionInfo.getFirstCenter());
		cp1.getParentToLocalTransform().transform(origin1, origin1);

		Point2D origin2 = new Point2D.Double();
		origin2.setLocation(connectionInfo.getSecondCenter());
		cp2.getParentToLocalTransform().transform(origin2, origin2);

		cp1.update(origin1);
		cp2.update(origin2);

		curveInfo = Geometry.buildConnectionCurveInfo(connectionInfo, this, 0);

		updateVisibleRange();
	}

	@Override
	public double getDistanceToCurve(Point2D pt) {
		return pt.distance(getNearestPointOnCurve(pt));
	}

	@Override
	public Point2D getNearestPointOnCurve(Point2D pt) {
		// FIXME: should be done using some proper algorithm

		Point2D nearest = new Point2D.Double(curve.getX1(), curve.getY1());
		double nearestDist = Double.MAX_VALUE;

		for (double t=0.01; t<=1.0; t+=0.01) {
			Point2D samplePoint = Geometry.getPointOnCubicCurve(curve, t);
			double distance = pt.distance(samplePoint);
			if (distance < nearestDist)	{
				nearestDist = distance;
				nearest = samplePoint;
			}
		}

		return nearest;
	}

	@Override
	public Point2D getPointOnCurve(double t) {
		return Geometry.getPointOnCubicCurve(curve, t);
	}

	@Override
	public void notify(StateEvent e) {
		update();
	}

	@Override
	public void notify(SelectionChangedEvent event) {
		boolean controlsVisible = false;
		for (Node n : event.getSelection())
			if (n==cp1 || n == cp2 || n == parent) {
				controlsVisible = true;
				break;
			}
			cp1.setHidden(!controlsVisible);
			cp2.setHidden(!controlsVisible);
	}

	@Override
	public void invalidate() {
		throw new RuntimeException ("Not implemented");
	}

}
