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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PercentageBarDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;

public class PercentageBarCellPainter extends CellPainterWrapper {

    public PercentageBarCellPainter() {
        this(2);
    }

    public PercentageBarCellPainter(int outerPadding) {
        super(new PaddingDecorator(new LineBorderDecorator(
                new PercentageBarDecorator(new TextPainter(false, false)),
                new BorderStyle()), outerPadding));
    }
}
