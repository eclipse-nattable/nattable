/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class GenericLayerFixture extends AbstractLayerTransform {

	public GenericLayerFixture(ILayer underlyingLayer) {
		super(underlyingLayer);
	}
	
	public int getColumnIndexByPosition(int columnPosition) {
		return columnPosition;
	}

	public int getRowIndexByPosition(int rowPosition) {
		return rowPosition;
	}
}
