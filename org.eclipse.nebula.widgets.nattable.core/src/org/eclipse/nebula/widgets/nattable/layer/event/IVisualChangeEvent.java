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
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

/**
 * An event which indicates a visible change to one or more cells in the layer.
 * A visible change simply indicates that one or more cells should be redrawn.
 * It does not imply a structural change to the layer. This means that cached
 * structure does not need to be invalidated due to visible change events.
 */
public interface IVisualChangeEvent extends ILayerEvent {

    /**
     * @return the layer that the visible change event is originating from.
     */
    public ILayer getLayer();

    /**
     * @return the position rectangles that have changed and need to be redrawn.
     *         If no rectangles are returned, then the receiver should assume
     *         that the entire layer is changed and will need to be redrawn.
     */
    public Collection<Rectangle> getChangedPositionRectangles();

}
