/*******************************************************************************
 * Copyright (c) 2014 Jonas Hugo, Markus Wahl.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - initial API
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Selection model that holds markers such as anchors and last selection
 * properties in order to keep them up-to-date after underlying data has
 * changed.
 */
public interface IMarkerSelectionModel extends ISelectionModel {

    /**
     * @return point of the anchor expressed in position coordinates
     */
    Point getSelectionAnchor();

    /**
     * @return point of the last selected cell expressed in position coordinates
     */
    Point getLastSelectedCell();

    /**
     * @return rectangle of the last selected region expressed in position
     *         coordinates
     */
    Rectangle getLastSelectedRegion();

    /**
     * @param position
     *            coordinate of the anchor
     */
    void setSelectionAnchor(Point position);

    /**
     * @param position
     *            coordinate of the last selected
     */
    void setLastSelectedCell(Point position);

    /**
     * Will set the Rectangle object of the last selected region to be the same
     * as the parameter object region.
     *
     * @param region
     *            the last selection position region
     */
    void setLastSelectedRegion(Rectangle region);

    /**
     * Will copy the information of the parameters to the already existing
     * Rectangle object of last selected region.
     *
     * @param x
     *            origin of the last selection position region
     * @param y
     *            origin of the last selection position region
     * @param width
     *            of the last selection position region
     * @param height
     *            of the last selection position region
     */
    void setLastSelectedRegion(int x, int y, int width, int height);
}