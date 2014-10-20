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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.events.MouseEvent;

//Use the CellEditorMouseEventMatcher instead
@Deprecated
public class BodyCellEditorMouseEventMatcher implements IMouseEventMatcher {

    private Class<?> cellEditorClass;
    private final int button;

    public BodyCellEditorMouseEventMatcher(Class<?> cellEditorClass) {
        this(cellEditorClass, MouseEventMatcher.LEFT_BUTTON);
    }

    public BodyCellEditorMouseEventMatcher(Class<?> cellEditorClass, int button) {
        this.cellEditorClass = cellEditorClass;
        this.button = button;
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event,
            LabelStack regionLabels) {
        if (regionLabels != null && regionLabels.hasLabel(GridRegion.BODY)
                && event.button == this.button) {
            ILayerCell cell = natTable.getCellByPosition(
                    natTable.getColumnPositionByX(event.x),
                    natTable.getRowPositionByY(event.y));

            // Bug 407598: only perform a check if the click in the body region
            // was performed on a cell
            // cell == null can happen if the viewport is quite large and
            // contains not enough cells to fill it.
            if (cell != null) {
                ICellEditor cellEditor = natTable.getConfigRegistry()
                        .getConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                                DisplayMode.EDIT,
                                cell.getConfigLabels().getLabels());
                if (this.cellEditorClass.isInstance(cellEditor)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BodyCellEditorMouseEventMatcher other = (BodyCellEditorMouseEventMatcher) obj;
        if (this.cellEditorClass == null) {
            if (other.cellEditorClass != null)
                return false;
        } else if (this.cellEditorClass != other.cellEditorClass)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.cellEditorClass == null) ? 0 : this.cellEditorClass.hashCode());
        return result;
    }

}
