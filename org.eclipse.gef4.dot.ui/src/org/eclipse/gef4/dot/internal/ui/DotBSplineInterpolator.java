/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.dot.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef4.fx.nodes.Connection;
import org.eclipse.gef4.fx.nodes.IConnectionInterpolator;
import org.eclipse.gef4.fx.nodes.IConnectionRouter;
import org.eclipse.gef4.geometry.planar.BezierCurve;
import org.eclipse.gef4.geometry.planar.CubicCurve;
import org.eclipse.gef4.geometry.planar.ICurve;
import org.eclipse.gef4.geometry.planar.Line;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.PolyBezier;

/**
 * A {@link DotBSplineInterpolator} is a {@link IConnectionRouter router} that
 * creates a {@link PolyBezier} geometry corresponding to a single B-spline. It
 * expects that the start, end, and control points of the {@link Connection} it
 * routes correspond to what can be specified through the 'pos' attribute of
 * edges within Graphviz DOT as follows (if multiple splines are specified
 * through the 'pos' attribute, they have to be represented through multiple
 * connections).
 * <p>
 * The {@link DotBSplineInterpolator} expects that the connection's
 * {@link Connection#getControlPoints() control points} represent control points
 * of connected cubic Bézier segments in the form 'p, (p, p, p)+'. In case the
 * start point equals the first control point, or the end points equals the last
 * control point, they are ignored when constructing the B-spline. In case this
 * is not the case, linear segments are added from the start point to the first
 * control point and the last control point to the end point respectively.
 *
 * @author anyssen
 *
 */
public class DotBSplineInterpolator implements IConnectionInterpolator {

	@Override
	public ICurve interpolate(Connection connection) {
		Point start = connection.getStartPoint();
		Point end = connection.getEndPoint();

		// return a line in case we have no start or end point or the points do
		// not correctly specify bezier segments.
		List<Point> controlPoints = connection.getControlPoints();
		int numControlPoints = controlPoints.size();
		if (start == null || end == null) {
			return new Line(0, 0, 0, 0);
		} else if (numControlPoints < 4 || (numControlPoints % 3 != 0)) {
			return new Line(start, end);
		}

		// start and end point may be equal to the first and last control points
		// or else we need to add a line segment to connect each.
		List<BezierCurve> segments = new ArrayList<>();
		Point c = controlPoints.get(0);
		if (!start.equals(c)) {
			segments.add(new Line(start, c));
		}
		// process segments
		c = controlPoints.get(1);
		for (int i = 2; i + 2 < numControlPoints; i += 3) {
			segments.add(new CubicCurve(c, controlPoints.get(i),
					controlPoints.get(i + 1), controlPoints.get(i + 2)));
			// keep track of the last control point of the respective segment
			// (which is the start point of the next segment)
			c = controlPoints.get(i + 2);
		}
		c = controlPoints.get(numControlPoints - 1);
		if (!end.equals(c)) {
			segments.add(new Line(c, end));
		}
		return new PolyBezier(segments.toArray(new BezierCurve[] {}));
	}

}