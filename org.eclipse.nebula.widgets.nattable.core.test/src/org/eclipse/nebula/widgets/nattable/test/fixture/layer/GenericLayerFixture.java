/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        return columnPosition;
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        return rowPosition;
    }
}
