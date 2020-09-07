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
package org.eclipse.nebula.widgets.nattable.blink.event;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.swt.graphics.Rectangle;

public class BlinkEvent implements IVisualChangeEvent {

    private ILayer layer;

    public BlinkEvent(ILayer layer) {
        this.layer = layer;
    }

    @Override
    public ILayerEvent cloneEvent() {
        return new BlinkEvent(this.layer);
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        return Arrays.asList(new Rectangle(0, 0, this.layer.getHeight(), this.layer.getWidth()));
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        return true;
    }

}
