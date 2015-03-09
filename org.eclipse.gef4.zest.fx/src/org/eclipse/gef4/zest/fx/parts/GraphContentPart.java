/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API & implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.zest.fx.parts;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import org.eclipse.gef4.common.adapt.AdapterKey;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.layout.ILayoutContext;
import org.eclipse.gef4.layout.ILayoutAlgorithm;
import org.eclipse.gef4.mvc.fx.parts.AbstractFXContentPart;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXHoverPolicy;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.zest.fx.ZestProperties;
import org.eclipse.gef4.zest.fx.models.LayoutModel;

public class GraphContentPart extends AbstractFXContentPart<Group> {

	/**
	 * A property change event is fired as soon as {@link #activate()
	 * activation} is finished.
	 */
	public static final String ACTIVATION_COMPLETE_PROPERTY = "activationComplete";

	public GraphContentPart() {
		// we set the hover policy adapter here to disable hovering this part
		// TODO: move to NoHoverPolicy
		setAdapter(AdapterKey.get(AbstractFXHoverPolicy.class),
				new AbstractFXHoverPolicy() {
					@Override
					public void hover(MouseEvent e) {
					}
				});
	}

	@Override
	protected void addChildVisual(IVisualPart<Node, ? extends Node> child,
			int index) {
		getVisual().getChildren().add(index, child.getVisual());
	}

	@Override
	protected Group createVisual() {
		Group visual = new Group();
		visual.setAutoSizeChildren(false);
		return visual;
	}

	@Override
	protected void doActivate() {
		super.doActivate();
		pcs.firePropertyChange(ACTIVATION_COMPLETE_PROPERTY, false, true);
		setGraphLayoutContext();
	}

	@Override
	public void doRefreshVisual(Group visual) {
		// set layout algorithm on the context
		setGraphLayoutContext();
	}

	@Override
	public Graph getContent() {
		return (Graph) super.getContent();
	}

	@Override
	public List<Object> getContentChildren() {
		List<Object> children = new ArrayList<Object>();
		children.addAll(getContent().getEdges());
		children.addAll(getContent().getNodes());
		return children;
	}

	@Override
	protected void removeChildVisual(IVisualPart<Node, ? extends Node> child,
			int index) {
		getVisual().getChildren().remove(child.getVisual());
	}

	private void setGraphLayoutContext() {
		Object algo = getContent().getAttrs().get(ZestProperties.GRAPH_LAYOUT);
		if (algo instanceof ILayoutAlgorithm) {
			ILayoutAlgorithm layoutAlgorithm = (ILayoutAlgorithm) algo;
			ILayoutContext layoutContext = getViewer().getDomain()
					.getAdapter(LayoutModel.class)
					.getLayoutContext(getContent());
			if (layoutContext != null) {
				layoutContext.setStaticLayoutAlgorithm(layoutAlgorithm);
			}
		}
	}

}
