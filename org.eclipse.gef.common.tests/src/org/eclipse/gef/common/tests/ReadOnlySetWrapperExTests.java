/*******************************************************************************
 * Copyright (c) 2017 itemis AG and others.
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
package org.eclipse.gef.common.tests;

import org.eclipse.gef.common.beans.property.ReadOnlySetWrapperEx;
import org.eclipse.gef.common.tests.SetPropertyExTests.SetChangeExpector;
import org.junit.Test;

import javafx.beans.property.ReadOnlySetProperty;
import javafx.collections.FXCollections;

public class ReadOnlySetWrapperExTests {

	@Test
	public void readOnlyWrapperChangeNotifications() {
		ReadOnlySetWrapperEx<Integer> setWrapper = new ReadOnlySetWrapperEx<>(
				FXCollections.observableSet());
		ReadOnlySetProperty<Integer> roProperty = setWrapper
				.getReadOnlyProperty();
		SetChangeExpector<Integer> setChangeListener = new SetChangeExpector<>(
				roProperty);
		roProperty.addListener(setChangeListener);
		setChangeListener.addExpectation(null, 1);
		roProperty.add(1);
		setChangeListener.check();
		setChangeListener.addExpectation(null, 2);
		roProperty.add(2);
		setChangeListener.check();
		setChangeListener.addExpectation(1, null);
		roProperty.remove(1);
		setChangeListener.check();
		setChangeListener.addExpectation(2, null);
		roProperty.remove(2);
		setChangeListener.check();
	}
}
