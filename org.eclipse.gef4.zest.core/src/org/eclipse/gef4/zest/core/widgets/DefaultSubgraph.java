/*******************************************************************************
 * Copyright (c) 2009-2010 Mateusz Matela and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Mateusz Matela - initial API and implementation
 *               Ian Bull
 ******************************************************************************/
package org.eclipse.gef4.zest.core.widgets;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef4.common.properties.PropertyStoreSupport;
import org.eclipse.gef4.geometry.planar.Dimension;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.layout.IConnectionLayout;
import org.eclipse.gef4.layout.IEntityLayout;
import org.eclipse.gef4.layout.ILayoutContext;
import org.eclipse.gef4.layout.INodeLayout;
import org.eclipse.gef4.layout.ISubgraphLayout;
import org.eclipse.gef4.layout.LayoutProperties;
import org.eclipse.gef4.zest.core.widgets.custom.LabelSubgraph;
import org.eclipse.gef4.zest.core.widgets.custom.TriangleSubgraph;
import org.eclipse.gef4.zest.core.widgets.custom.TriangleSubgraph.TriangleParameters;
import org.eclipse.swt.graphics.Color;

/**
 * Default implementation of {@link ISubgraphLayout}. Every subgraph added to
 * Zest {@link GraphWidget} should inherit from this class. The default
 * implementation is very simple. A node pruned to this subgraph is minimized
 * and all connections adjacent to it are made invisible. No additional graphic
 * elements are added to the graph, but subclasses may add them.
 * 
 * @since 2.0
 */
public class DefaultSubgraph implements ISubgraphLayout {

	private PropertyStoreSupport ps = new PropertyStoreSupport(this);

	/**
	 * Default factory for {@link DefaultSubgraph}. It creates one subgraph for
	 * a whole graph and throws every node into it.
	 */
	public static class DefaultSubgraphFactory implements SubgraphFactory {
		private HashMap<ILayoutContext, IEntityLayout> contextToSubgraph = new HashMap<ILayoutContext, IEntityLayout>();

		public ISubgraphLayout createSubgraph(INodeLayout[] nodes,
				ILayoutContext context) {
			DefaultSubgraph subgraph = (DefaultSubgraph) contextToSubgraph
					.get(context);
			if (subgraph == null) {
				subgraph = new DefaultSubgraph(context);
				contextToSubgraph.put(context, subgraph);
			}
			subgraph.addNodes(nodes);
			return subgraph;
		}
	};

	public static class LabelSubgraphFactory implements SubgraphFactory {
		private Color defaultForegroundColor = ColorConstants.black;
		private Color defaultBackgroundColor = ColorConstants.yellow;

		/**
		 * Changes the default foreground color for newly created subgraphs.
		 * 
		 * @param c
		 *            color to use
		 */
		public void setDefualtForegroundColor(Color c) {
			defaultForegroundColor = c;
		}

		/**
		 * Changes the default background color for newly created subgraphs.
		 * 
		 * @param c
		 *            color to use
		 */
		public void setDefaultBackgroundColor(Color c) {
			defaultBackgroundColor = c;
		}

		public ISubgraphLayout createSubgraph(INodeLayout[] nodes,
				ILayoutContext context) {
			return new LabelSubgraph(nodes, context, defaultForegroundColor,
					defaultBackgroundColor);
		}
	};

	public static class TriangleSubgraphFactory implements SubgraphFactory {
		private TriangleParameters parameters = new TriangleParameters();

		public ISubgraphLayout createSubgraph(INodeLayout[] nodes,
				ILayoutContext context) {
			return new TriangleSubgraph(nodes, context,
					(TriangleParameters) parameters.clone());
		}

		/**
		 * 
		 * @return initial color of triangles created with this factory
		 */
		public Color getColor() {
			return parameters.color;
		}

		/**
		 * Changes the default color for newly created subgraphs.
		 * 
		 * @param color
		 *            color to use
		 */
		public void setColor(Color color) {
			parameters.color = color;
		}

		/**
		 * 
		 * @return initial direction of triangles created with this factory
		 */
		public int getDirection() {
			return parameters.direction;
		}

		/**
		 * Changes the default direction for newly cretaed subgraphs.
		 * 
		 * @param direction
		 *            direction to use, can be
		 *            {@link ILayoutProperties#DIRECTION_TOP_DOWN},
		 *            {@link ILayoutProperties#DIRECTION_BOTTOM_UP},
		 *            {@link ILayoutProperties#DIRECTION_LEFT_RIGHT}, or
		 *            {@link ILayoutProperties#DIRECTION_RIGHT_LEFT}
		 */
		public void setDirection(int direction) {
			parameters.direction = direction;
		}

		/**
		 * 
		 * @return maximum height of triangles created with this factory
		 */
		public double getReferenceHeight() {
			return parameters.referenceHeight;
		}

		/**
		 * Sets the maximum height for the triangle visualizing this subgraph.
		 * 
		 * @param referenceHeight
		 *            height to use
		 */
		public void setReferenceHeight(double referenceHeight) {
			parameters.referenceHeight = referenceHeight;
		}

		/**
		 * 
		 * @return maximum base length of triangles created with this factory
		 */
		public double getReferenceBase() {
			return parameters.referenceBase;
		}

		/**
		 * Sets the maximum base length for the triangle visualizing this
		 * subgraph.
		 * 
		 * @param referenceBase
		 *            base length to use
		 */

		public void setReferenceBase(double referenceBase) {
			parameters.referenceBase = referenceBase;
		}
	};

	/**
	 * Factory for {@link PrunedSuccessorsSubgraph}. It creates one subgraph for
	 * a whole graph and throws every node into it.
	 */
	public static class PrunedSuccessorsSubgraphFactory implements
			SubgraphFactory {
		private HashMap<ILayoutContext, IEntityLayout> contextToSubgraph = new HashMap<ILayoutContext, IEntityLayout>();

		public ISubgraphLayout createSubgraph(INodeLayout[] nodes,
				ILayoutContext context) {
			PrunedSuccessorsSubgraph subgraph = (PrunedSuccessorsSubgraph) contextToSubgraph
					.get(context);
			if (subgraph == null) {
				subgraph = new PrunedSuccessorsSubgraph(context);
				contextToSubgraph.put(context, subgraph);
			}
			subgraph.addNodes(nodes);
			return subgraph;
		}

		// TODO: move to Subgraph implementation
		/**
		 * Updates a label for given node (creates the label if necessary).
		 * 
		 * @param node
		 *            node to update
		 */
		public void updateLabelForNode(InternalNodeLayout node) {
			InternalLayoutContext context = node.getOwnerLayoutContext();
			PrunedSuccessorsSubgraph subgraph = (PrunedSuccessorsSubgraph) contextToSubgraph
					.get(context);
			if (subgraph == null) {
				subgraph = new PrunedSuccessorsSubgraph(context);
				contextToSubgraph.put(context, subgraph);
			}
			subgraph.updateNodeLabel(node);
		}

	};

	protected final InternalLayoutContext context;

	protected final Set<INodeLayout> nodes = new HashSet<INodeLayout>();

	protected boolean disposed = false;

	protected DefaultSubgraph(ILayoutContext context2) {
		if (context2 instanceof InternalLayoutContext) {
			this.context = (InternalLayoutContext) context2;
		} else {
			throw new RuntimeException(
					"This subgraph can be only created with LayoutContext provided by Zest Graph");
		}
	}

	public boolean isGraphEntity() {
		return false;
	}

	public void setSize(double width, double height) {
		// do nothing
		context.checkChangesAllowed();
	}

	public void setLocation(double x, double y) {
		// do nothing
		context.checkChangesAllowed();
	}

	public boolean isResizable() {
		return false;
	}

	public boolean isMovable() {
		return false;
	}

	public IEntityLayout[] getSuccessingEntities() {
		return new IEntityLayout[0];
	}

	public Dimension getSize() {
		Rectangle bounds = context.getBounds();
		return new Dimension(bounds.getWidth(), bounds.getHeight());
	}

	public double getPreferredAspectRatio() {
		return 0;
	}

	public IEntityLayout[] getPredecessingEntities() {
		return new IEntityLayout[0];
	}

	public Point getLocation() {
		Rectangle bounds = context.getBounds();
		return new Point(bounds.getX() + bounds.getWidth() / 2, bounds.getY()
				+ bounds.getHeight() / 2);
	}

	public boolean isDirectionDependant() {
		return false;
	}

	public void setDirection(int direction) {
		context.checkChangesAllowed();
		// do nothing
	}

	public void removeNodes(INodeLayout[] nodes) {
		context.checkChangesAllowed();
		for (int i = 0; i < nodes.length; i++) {
			if (this.nodes.remove(nodes[i])) {
				nodes[i].prune(null);
				LayoutProperties.setMinimized(nodes[i], false);
				refreshConnectionsVisibility(nodes[i].getIncomingConnections());
				refreshConnectionsVisibility(nodes[i].getOutgoingConnections());
			}
		}
		if (this.nodes.isEmpty()) {
			dispose();
		}
	}

	public INodeLayout[] getNodes() {
		InternalNodeLayout[] result = new InternalNodeLayout[nodes.size()];
		int i = 0;
		for (Iterator<INodeLayout> iterator = nodes.iterator(); iterator
				.hasNext();) {
			result[i] = (InternalNodeLayout) iterator.next();
			if (!context.isLayoutItemFiltered(result[i].getNode())) {
				i++;
			}
		}
		if (i == nodes.size()) {
			return result;
		}

		INodeLayout[] result2 = new INodeLayout[i];
		System.arraycopy(result, 0, result2, 0, i);
		return result2;
	}

	public Object[] getItems() {
		GraphNode[] result = new GraphNode[nodes.size()];
		int i = 0;
		for (Iterator<INodeLayout> iterator = nodes.iterator(); iterator
				.hasNext();) {
			InternalNodeLayout node = (InternalNodeLayout) iterator.next();
			// getItems always returns an array of size 1 in case of
			// InternalNodeLayout
			result[i] = (GraphNode) node.getItems()[0];
			if (!context.isLayoutItemFiltered(node.getNode())) {
				i++;
			}
		}
		if (i == nodes.size()) {
			return result;
		}

		GraphNode[] result2 = new GraphNode[i];
		System.arraycopy(result, 0, result2, 0, i);
		return result2;
	}

	public int countNodes() {
		return nodes.size();
	}

	public void addNodes(INodeLayout[] nodes) {
		context.checkChangesAllowed();
		for (int i = 0; i < nodes.length; i++) {
			if (this.nodes.add(nodes[i])) {
				nodes[i].prune(this);
				LayoutProperties.setMinimized(nodes[i], true);
				refreshConnectionsVisibility(nodes[i].getIncomingConnections());
				refreshConnectionsVisibility(nodes[i].getOutgoingConnections());
			}
		}
	}

	protected void refreshConnectionsVisibility(IConnectionLayout[] connections) {
		for (int i = 0; i < connections.length; i++) {
			LayoutProperties.setVisible(
					connections[i],
					!LayoutProperties.isPruned(connections[i].getSource())
							&& !LayoutProperties.isPruned(connections[i]
									.getTarget()));
		}
	}

	/**
	 * Makes sure that value returned by {@link #getLocation()} will be equal to
	 * current location of this subgraph.
	 */
	protected void refreshLocation() {
		// do nothing, to be reimplemented in subclasses
	}

	/**
	 * Makes sure that value returned by {@link #getSize()} will be equal to
	 * current size of this subgraph.
	 */
	protected void refreshSize() {
		// do nothing, to be reimplemented in subclasses
	}

	protected void applyLayoutChanges() {
		// do nothing
	}

	protected void dispose() {
		if (!disposed) {
			context.removeSubgrah(this);
			disposed = true;
		}
	}

	public Object getProperty(String name) {
		if (LayoutProperties.ASPECT_RATIO_PROPERTY.equals(name)) {
			return getPreferredAspectRatio();
		} else if (LayoutProperties.LOCATION_PROPERTY.equals(name)) {
			return getLocation();
		} else if (LayoutProperties.MOVABLE_PROPERTY.equals(name)) {
			return isMovable();
		} else if (LayoutProperties.RESIZABLE_PROPERTY.equals(name)) {
			return isResizable();
		} else if (LayoutProperties.SIZE_PROPERTY.equals(name)) {
			return getSize();
		} else if (LayoutProperties.DIRECTION_DEPENDANT_PROPERTY.equals(name)) {
			return isDirectionDependant();
		} else {
			return ps.getProperty(name);
		}
	}

	public void setProperty(String name, Object value) {
		if (LayoutProperties.LOCATION_PROPERTY.equals(name)) {
			Point p = (Point) value;
			setLocation(p.x, p.y);
		} else if (LayoutProperties.SIZE_PROPERTY.equals(name)) {
			Dimension size = (Dimension) value;
			setSize(size.width, size.height);
		} else if (LayoutProperties.DIRECTION_PROPERTY.equals(name)) {
			if (value instanceof Integer) {
				setDirection((Integer) value);
			}
		} else {
			ps.setProperty(name, value);
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		ps.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		ps.removePropertyChangeListener(listener);
	}

}