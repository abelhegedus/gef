/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.examples.logo.parts;

import javafx.scene.Node;

import org.eclipse.gef4.fx.nodes.FXGeometryNode;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.geometry.planar.IShape;
import org.eclipse.gef4.mvc.examples.logo.model.AbstractFXGeometricElement;
import org.eclipse.gef4.mvc.examples.logo.model.FXGeometricShape;
import org.eclipse.gef4.mvc.parts.IVisualPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class FXGeometricShapePart extends
		AbstractFXGeometricElementPart<FXGeometryNode<IShape>> {

	@Override
	protected void attachToAnchorageVisual(
			org.eclipse.gef4.mvc.parts.IVisualPart<Node, ? extends Node> anchorage,
			String role) {
		// nothing to do
	};

	@Override
	public void attachToContentAnchorage(Object contentAnchorage, String role) {
		if (!(contentAnchorage instanceof AbstractFXGeometricElement)) {
			throw new IllegalArgumentException(
					"Cannot attach to content anchorage: wrong type!");
		}
		getContent().getAnchorages().add(
				(AbstractFXGeometricElement<?>) contentAnchorage);
	}

	@Override
	protected FXGeometryNode<IShape> createVisual() {
		return new FXGeometryNode<IShape>();
	}

	@Override
	protected void detachFromAnchorageVisual(
			IVisualPart<Node, ? extends Node> anchorage, String role) {
		// nothing to do
	}

	@Override
	public void detachFromContentAnchorage(Object contentAnchorage, String role) {
		getContent().getAnchorages().remove(contentAnchorage);
	}

	@Override
	public void doRefreshVisual(FXGeometryNode<IShape> visual) {
		FXGeometricShape content = getContent();

		if (visual.getGeometry() != content.getGeometry()) {
			visual.setGeometry(content.getGeometry());
		}

		if (content.getTransform() != null) {
			visual.relocate(content.getTransform().getTranslateX()
					+ visual.getLayoutBounds().getMinX(), content
					.getTransform().getTranslateY()
					+ visual.getLayoutBounds().getMinY());
		}

		// apply stroke paint
		if (visual.getStroke() != content.getStroke()) {
			visual.setStroke(content.getStroke());
		}

		// stroke width
		if (visual.getStrokeWidth() != content.getStrokeWidth()) {
			visual.setStrokeWidth(content.getStrokeWidth());
		}

		if (visual.getFill() != content.getFill()) {
			visual.setFill(content.getFill());
		}

		// apply effect
		super.doRefreshVisual(visual);
	}

	@Override
	public FXGeometricShape getContent() {
		return (FXGeometricShape) super.getContent();
	}

	@Override
	public SetMultimap<? extends Object, String> getContentAnchorages() {
		SetMultimap<Object, String> anchorages = HashMultimap.create();
		for (AbstractFXGeometricElement<? extends IGeometry> anchorage : getContent()
				.getAnchorages()) {
			anchorages.put(anchorage, "link");
		}
		return anchorages;
	}

	@Override
	public void setContent(Object model) {
		if (model != null && !(model instanceof FXGeometricShape)) {
			throw new IllegalArgumentException(
					"Only IShape models are supported.");
		}
		super.setContent(model);
	}

}