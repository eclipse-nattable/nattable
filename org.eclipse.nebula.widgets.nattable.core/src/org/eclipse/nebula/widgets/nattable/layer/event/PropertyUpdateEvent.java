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
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

public class PropertyUpdateEvent<T> implements IVisualChangeEvent {

    private final PropertyChangeEvent propertyChangeEvent = null;
    private final T sourceBean;
    private final String propertyName;
    private final Object newValue;
    private final Object oldValue;

    private ILayer layer;

    public PropertyUpdateEvent(ILayer layer, T sourceBean, String propertyName,
            Object oldValue, Object newValue) {
        this.layer = layer;
        this.sourceBean = sourceBean;
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // Interface methods

    @Override
    public ILayerEvent cloneEvent() {
        return new PropertyUpdateEvent<T>(this.layer, this.sourceBean,
                this.propertyName, this.oldValue, this.newValue);
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        this.layer = localLayer;
        return true;
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        return Arrays.asList(new Rectangle(0, 0, this.layer.getWidth(), this.layer
                .getHeight()));
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    // Accessors

    public PropertyChangeEvent getPropertyChangeEvent() {
        return this.propertyChangeEvent;
    }

    public T getSourceBean() {
        return this.sourceBean;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public Object getNewValue() {
        return this.newValue;
    }

    public Object getOldValue() {
        return this.oldValue;
    }
}
