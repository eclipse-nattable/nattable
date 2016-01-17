/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 448115, 449361, 485921
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export.excel;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.export.IExportFormatter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public class DefaultExportFormatter implements IExportFormatter {

    private final IDisplayConverter fallbackConverter = new DefaultDisplayConverter();

    @Override
    public Object formatForExport(ILayerCell cell, IConfigRegistry configRegistry) {
        Object dataValue = cell.getDataValue();
        IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                cell.getDisplayMode(),
                cell.getConfigLabels().getLabels());

        if (displayConverter == null) {
            displayConverter = this.fallbackConverter;
        }
        return displayConverter.canonicalToDisplayValue(cell, configRegistry, dataValue);
    }

}
