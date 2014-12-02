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
package org.eclipse.nebula.widgets.nattable.copy.serializing;

import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellDisplayConversionUtils;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public class CopyFormattedTextToClipboardSerializer extends
        CopyDataToClipboardSerializer {

    public CopyFormattedTextToClipboardSerializer(ILayerCell[][] copiedCells,
            CopyDataToClipboardCommand command) {
        super(copiedCells, command);
    }

    @Override
    protected String getTextForCell(ILayerCell cell) {
        return CellDisplayConversionUtils.convertDataType(cell, getCommand()
                .getConfigRegistry());
    }
}
