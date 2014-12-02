/*******************************************************************************
 * Copyright (c) 2013, 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *    Roman Flueckiger <roman.flueckiger@mac.com> - Bug 446866
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

/**
 * Implementation of {@link IMouseEventMatcher} that will check if editing
 * should be activated. For this it is possible to specify the region label to
 * react on, the mouse button that was used to click and if an editor is
 * registered for the cell on which the mouse click was executed. If no region
 * label is specified, only the mouse button and the presence of a cell editor
 * is evaluated.
 * <p>
 * If not specified, this matcher will react on the left mouse button.
 *
 * @author Dirk Fauth
 *
 */
public class CellEditorMouseEventMatcher implements IMouseEventMatcher {

    /**
     * The label that specifies the region on which this matcher should be
     * attached. If there is no region label specified, only the button and the
     * presence of a configured cell editor will be evaluated for the match.
     */
    private final String regionLabel;

    /**
     * The mouse button that need to be pressed or released for this matcher to
     * react.
     */
    private final int button;

    /**
     * Will create a new {@link CellEditorMouseEventMatcher} that will only
     * evaluate the presence of a cell editor and the mouse left click.
     */
    public CellEditorMouseEventMatcher() {
        this(null, MouseEventMatcher.LEFT_BUTTON);
    }

    /**
     * Will create a new {@link CellEditorMouseEventMatcher} that will only
     * evaluate the presence of a cell editor and the specified mouse click.
     *
     * @param button
     *            The mouse button that need to be pressed or released for this
     *            matcher to react.
     */
    public CellEditorMouseEventMatcher(int button) {
        this(null, button);
    }

    /**
     * Will create a new {@link CellEditorMouseEventMatcher} for the specified
     * grid region and the mouse left click.
     *
     * @param regionLabel
     *            the label that specifies the region this matcher should be
     *            attached.
     */
    public CellEditorMouseEventMatcher(String regionLabel) {
        this(regionLabel, MouseEventMatcher.LEFT_BUTTON);
    }

    /**
     * Will create a new {@link CellEditorMouseEventMatcher} for the specified
     * grid region and mouse button.
     *
     * @param regionLabel
     *            the label that specifies the region this matcher should be
     *            attached.
     * @param button
     *            The mouse button that need to be pressed or released for this
     *            matcher to react.
     */
    public CellEditorMouseEventMatcher(String regionLabel, int button) {
        this.regionLabel = regionLabel;
        this.button = button;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher#matches
     * (org.eclipse.nebula.widgets.nattable.NatTable,
     * org.eclipse.swt.events.MouseEvent,
     * org.eclipse.nebula.widgets.nattable.layer.LabelStack)
     */
    @Override
    public boolean matches(NatTable natTable, MouseEvent event,
            LabelStack regionLabels) {
        if ((this.regionLabel == null || (regionLabels != null && regionLabels
                .hasLabel(this.regionLabel))) && event.button == this.button) {

            // Bug 446866: if modifier keys are pressed (e.g. CTRL or SHIFT to
            // perform multi-selections) do NOT open editors.
            if (event.stateMask == SWT.NONE) {
                ILayerCell cell = natTable.getCellByPosition(
                        natTable.getColumnPositionByX(event.x),
                        natTable.getRowPositionByY(event.y));

                // Bug 407598: only perform a check if the click in the body
                // region was performed on a cell
                // cell == null can happen if the viewport is quite large and
                // contains not enough cells to fill it.
                if (cell != null) {
                    ICellEditor cellEditor = natTable.getConfigRegistry()
                            .getConfigAttribute(
                                    EditConfigAttributes.CELL_EDITOR,
                                    DisplayMode.EDIT,
                                    cell.getConfigLabels().getLabels());

                    if (cellEditor != null
                            && cellEditor.activateAtAnyPosition()) {
                        // if there is a cell editor configured for the cell
                        // that was clicked on, the match is found
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
