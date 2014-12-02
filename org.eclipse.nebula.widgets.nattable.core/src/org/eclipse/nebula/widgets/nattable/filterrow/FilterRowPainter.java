/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow;

import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.graphics.Image;

/**
 * Painter that is used to paint the cells of the filter row. In detail it is a
 * {@link TextPainter} that is wrapped and decorated with an
 * {@link ImagePainter} to indicate filter states.
 */
public class FilterRowPainter extends CellPainterWrapper {

    /**
     * The {@link ImagePainter} that is used to paint the icon in the filter row
     * cells.
     */
    private final ImagePainter filterIconPainter;

    /**
     * Creates a {@link FilterRowPainter} that uses the default
     * {@link FilterIconPainter}.
     */
    public FilterRowPainter() {
        this(new FilterIconPainter());
    }

    /**
     * Creates a {@link FilterRowPainter} that uses the given {@link Image} as
     * filter icon painter.
     *
     * @param filterIconPainter
     *            The {@link ImagePainter} that should be used to paint the icon
     *            in the filter row cells.
     */
    public FilterRowPainter(ImagePainter filterIconPainter) {
        this.filterIconPainter = filterIconPainter;
        setWrappedPainter(new CellPainterDecorator(new TextPainter(),
                CellEdgeEnum.RIGHT, filterIconPainter));
    }

    /**
     *
     * @return The {@link ImagePainter} that is used to paint the icon in the
     *         filter row cells.
     */
    public ImagePainter getFilterIconPainter() {
        return this.filterIconPainter;
    }
}
