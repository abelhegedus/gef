/*******************************************************************************
 * Copyright (c) 2014, 2016 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef.mvc.fx.gestures;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.mvc.fx.handlers.IOnStrokeHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnTypeHandler;
import org.eclipse.gef.mvc.fx.models.FocusModel;
import org.eclipse.gef.mvc.fx.parts.PartUtils;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * The {@link TypeGesture} is an {@link AbstractGesture} that handles keyboard input.
 *
 * @author mwienand
 *
 */
public class TypeGesture extends AbstractGesture {

	/**
	 * The type of the policy that has to be supported by target parts.
	 */
	public static final Class<IOnTypeHandler> ON_TYPE_POLICY_KEY = IOnTypeHandler.class;

	/**
	 * The type of the policy that has to be supported by target parts.
	 */
	public static final Class<IOnStrokeHandler> ON_STROKE_POLICY_KEY = IOnStrokeHandler.class;

	private Map<Scene, EventHandler<? super KeyEvent>> pressedFilterMap = new IdentityHashMap<>();
	private Map<Scene, EventHandler<? super KeyEvent>> releasedFilterMap = new IdentityHashMap<>();
	private Map<Scene, EventHandler<? super KeyEvent>> typedFilterMap = new IdentityHashMap<>();
	private Map<IViewer, ChangeListener<Boolean>> viewerFocusChangeListeners = new IdentityHashMap<>();

	private IViewer activeViewer;

	@Override
	protected void doActivate() {
		for (final IViewer viewer : getDomain().getViewers().values()) {
			// check if we have access to a FocusModel
			FocusModel focusModel = viewer.getAdapter(FocusModel.class);
			if (focusModel == null) {
				throw new IllegalStateException("Cannot find FocusModel.");
			}

			// store the key that is initially pressed so that we can wait for
			// it to be released
			final Set<KeyCode> pressedKeys = new HashSet<>();

			// register a viewer focus change listener to release the initially
			// pressed key when the window loses focus
			ChangeListener<Boolean> viewerFocusChangeListener = new ChangeListener<Boolean>() {
				@Override
				public void changed(
						ObservableValue<? extends Boolean> observable,
						Boolean oldValue, Boolean newValue) {
					// cannot abort if no activeViewer
					if (activeViewer == null) {
						return;
					}
					// check if any viewer is focused
					for (IViewer v : getDomain().getViewers().values()) {
						if (v.isViewerFocused()) {
							return;
						}
					}
					// cancel target policies
					for (IOnStrokeHandler policy : getActiveHandlers(
							activeViewer)) {
						policy.abortPress();
					}
					// clear active policies
					clearActiveHandlers(activeViewer);
					activeViewer = null;
					// close execution transaction
					getDomain().closeExecutionTransaction(TypeGesture.this);
					// unset pressed keys
					pressedKeys.clear();
				}
			};
			viewer.viewerFocusedProperty()
					.addListener(viewerFocusChangeListener);
			viewerFocusChangeListeners.put(viewer, viewerFocusChangeListener);

			// XXX: Input filters are only registered once per Scene. The
			// IViewer is determined from the individual events.
			Scene scene = viewer.getRootPart().getVisual().getScene();
			if (pressedFilterMap.containsKey(scene)) {
				continue;
			}

			// generate event handlers
			EventHandler<KeyEvent> pressedFilter = new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					boolean isInitialPress = false;
					if (pressedKeys.isEmpty()) {
						// determine viewer that contains the given target part
						Node targetNode = null;
						EventTarget target = event.getTarget();
						if (target instanceof Node) {
							targetNode = (Node) target;
							activeViewer = PartUtils.retrieveViewer(getDomain(),
									targetNode);
						} else if (target instanceof Scene) {
							// first focused viewer in that scene
							for (IViewer v : getDomain().getViewers()
									.values()) {
								if (v.getRootPart().getVisual()
										.getScene() == target) {
									if (v.isViewerFocused()) {
										activeViewer = v;
										break;
									}
								}
							}
							if (activeViewer != null) {
								targetNode = activeViewer.getRootPart()
										.getVisual();
							}
						} else {
							throw new IllegalStateException(
									"Unsupported event target: " + target);
						}

						if (activeViewer == null) {
							// no focused viewer could be found for the target
							// scene
							return;
						}

						// open execution transaction
						getDomain().openExecutionTransaction(TypeGesture.this);
						isInitialPress = true;

						// determine target policies on first key press
						setActiveHandlers(activeViewer,
								getTargetPolicyResolver().resolve(
										TypeGesture.this, targetNode, activeViewer,
										ON_STROKE_POLICY_KEY));
					}

					// store initially pressed key
					pressedKeys.add(event.getCode());

					// notify target policies
					for (IOnStrokeHandler policy : getActiveHandlers(
							activeViewer)) {
						if (isInitialPress) {
							policy.initialPress(event);
						} else {
							policy.press(event);
						}
					}
				}
			};
			pressedFilterMap.put(scene, pressedFilter);

			EventHandler<KeyEvent> releasedFilter = new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					boolean isFinalRelease = pressedKeys.size() == 1
							&& pressedKeys.contains(event.getCode());

					// notify target policies
					for (IOnStrokeHandler policy : getActiveHandlers(
							activeViewer)) {
						if (isFinalRelease) {
							policy.finalRelease(event);
						} else {
							policy.release(event);
						}
					}

					// check if the last pressed key is released now
					if (isFinalRelease) {
						// clear active policies and close execution transaction
						// only when the initially pressed key is released
						clearActiveHandlers(activeViewer);
						activeViewer = null;
						getDomain().closeExecutionTransaction(TypeGesture.this);
					}
					pressedKeys.remove(event.getCode());
				}
			};
			releasedFilterMap.put(scene, releasedFilter);

			EventHandler<KeyEvent> typedFilter = new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					// System.out.println("typed " + event);
					if (pressedKeys.isEmpty()) {
						getDomain().openExecutionTransaction(TypeGesture.this);
					}

					// determine viewer that contains the given target part
					EventTarget target = event.getTarget();
					Node targetNode = null;
					if (target instanceof Node) {
						targetNode = (Node) target;
					} else if (target instanceof Scene) {
						// first focused viewer in that scene
						for (IViewer v : getDomain().getViewers().values()) {
							if (v.getRootPart().getVisual()
									.getScene() == target) {
								if (v.isViewerFocused()) {
									targetNode = v.getRootPart().getVisual();
									break;
								}
							}
						}
					} else {
						throw new IllegalStateException(
								"Unsupported event target: " + target);
					}

					IViewer targetViewer = PartUtils.retrieveViewer(getDomain(),
							targetNode);
					Collection<? extends IOnTypeHandler> policies = getTargetPolicyResolver()
							.resolve(TypeGesture.this, targetNode,
									targetViewer, ON_TYPE_POLICY_KEY);
					// active policies are unnecessary because TYPED is not a
					// gesture, just one event at one point in time
					for (IOnTypeHandler policy : policies) {
						policy.type(event, pressedKeys);
					}
					if (pressedKeys.isEmpty()) {
						getDomain().closeExecutionTransaction(TypeGesture.this);
					}
				}
			};
			typedFilterMap.put(scene, typedFilter);

			scene.addEventFilter(KeyEvent.KEY_PRESSED, pressedFilter);
			scene.addEventFilter(KeyEvent.KEY_RELEASED, releasedFilter);
			scene.addEventFilter(KeyEvent.KEY_TYPED, typedFilter);
		}
	}

	@Override
	protected void doDeactivate() {
		for (IViewer viewer : getDomain().getViewers().values()) {
			viewer.viewerFocusedProperty()
					.removeListener(viewerFocusChangeListeners.remove(viewer));
			Scene scene = viewer.getRootPart().getVisual().getScene();
			if (pressedFilterMap.containsKey(scene)) {
				scene.removeEventFilter(KeyEvent.KEY_PRESSED,
						pressedFilterMap.remove(scene));
			}
			if (releasedFilterMap.containsKey(scene)) {
				scene.removeEventFilter(KeyEvent.KEY_RELEASED,
						releasedFilterMap.remove(scene));
			}
			if (typedFilterMap.containsKey(scene)) {
				scene.removeEventFilter(KeyEvent.KEY_TYPED,
						typedFilterMap.remove(scene));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends IOnStrokeHandler> getActiveHandlers(IViewer viewer) {
		return (List<IOnStrokeHandler>) super.getActiveHandlers(viewer);
	}
}
