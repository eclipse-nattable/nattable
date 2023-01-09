/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.filterrow;

import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;

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
     * Creates a {@link FilterRowPainter} that uses the given
     * {@link ImagePainter} as filter icon painter. Uses a {@link TextPainter}
     * that is decorated with the {@link ImagePainter} as filter row painter.
     *
     * @param filterIconPainter
     *            The {@link ImagePainter} that should be used to paint the icon
     *            in the filter row cells.
     */
    public FilterRowPainter(ImagePainter filterIconPainter) {
        this.filterIconPainter = filterIconPainter;
        setWrappedPainter(
                new CellPainterDecorator(new TextPainter(), CellEdgeEnum.RIGHT, filterIconPainter));
    }

    /**
     * Creates a {@link FilterRowPainter} that uses the given
     * {@link ICellPainter} as filter row painter. Needs the
     * {@link ImagePainter} that is used as decorator to be able to identify the
     * icon click.
     *
     * @param painter
     *            The {@link ICellPainter} that should be used as filter row
     *            painter.
     * @param filterIconPainter
     *            The {@link ImagePainter} that should be used to paint the icon
     *            in the filter row cells.
     *
     * @since 2.1
     */
    public FilterRowPainter(ICellPainter painter, ImagePainter filterIconPainter) {
        this.filterIconPainter = filterIconPainter;
        setWrappedPainter(painter);
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
