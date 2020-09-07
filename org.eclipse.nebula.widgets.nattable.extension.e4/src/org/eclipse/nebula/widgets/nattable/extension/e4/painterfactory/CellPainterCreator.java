/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.e4.painterfactory;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;

/**
 * Functional interface to create an instance of an {@link ICellPainter}.
 */
@FunctionalInterface
public interface CellPainterCreator {

    /**
     * Create an {@link ICellPainter} using the given painter properties.
     *
     * @param painterProperties
     *            The painter properties for painter initialization.
     * @param underlying
     *            The {@link ICellPainter} that should be applied as wrapped
     *            painter to the created {@link ICellPainter}. Only works if the
     *            created {@link ICellPainter} is a {@link CellPainterWrapper}.
     * @return An instance of {@link ICellPainter}.
     */
    ICellPainter createCellPainter(Map<String, Object> painterProperties, ICellPainter underlying);
}
