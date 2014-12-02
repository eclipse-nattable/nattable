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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;

public class CellDisplayConversionUtils {

    public static String convertDataType(ILayerCell cell,
            IConfigRegistry configRegistry) {
        Object canonicalValue = cell.getDataValue();
        Object displayValue;

        IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER, cell.getDisplayMode(),
                cell.getConfigLabels().getLabels());

        if (displayConverter != null) {
            displayValue = displayConverter.canonicalToDisplayValue(cell,
                    configRegistry, canonicalValue);
        } else {
            displayValue = canonicalValue;
        }

        return (displayValue == null) ? "" : String.valueOf(displayValue); //$NON-NLS-1$
    }
}
