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
package org.eclipse.nebula.widgets.nattable.copy.serializing;

import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellDisplayConversionUtils;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public class CopyFormattedTextToClipboardSerializer extends CopyDataToClipboardSerializer {

    public CopyFormattedTextToClipboardSerializer(ILayerCell[][] copiedCells, CopyDataToClipboardCommand command) {
        super(copiedCells, command);
    }

    @Override
    protected String getTextForCell(ILayerCell cell) {
        return CellDisplayConversionUtils.convertDataType(cell, getCommand().getConfigRegistry());
    }
}
