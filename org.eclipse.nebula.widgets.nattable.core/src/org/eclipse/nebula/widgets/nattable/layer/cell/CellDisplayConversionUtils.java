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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;

public class CellDisplayConversionUtils {

    public static String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
        Object canonicalValue = cell.getDataValue();
        Object displayValue;

        IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                cell.getDisplayMode(),
                cell.getConfigLabels());

        if (displayConverter != null) {
            displayValue = displayConverter.canonicalToDisplayValue(cell, configRegistry, canonicalValue);
        } else {
            displayValue = canonicalValue;
        }

        return (displayValue == null) ? "" : String.valueOf(displayValue); //$NON-NLS-1$
    }
}
