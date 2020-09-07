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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.swt.graphics.Rectangle;

public class LayerEventFixture implements IVisualChangeEvent {

    @Override
    public ILayerEvent cloneEvent() {
        return new LayerEventFixture();
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        return true;
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        return null;
    }

    @Override
    public ILayer getLayer() {
        return null;
    }

}
