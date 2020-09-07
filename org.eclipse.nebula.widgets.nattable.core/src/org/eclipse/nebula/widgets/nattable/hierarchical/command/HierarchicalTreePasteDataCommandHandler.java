/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.PasteDataCommand;
import org.eclipse.nebula.widgets.nattable.copy.command.RowSpanningPasteDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * {@link ILayerCommandHandler} for handling {@link PasteDataCommand}s using the
 * {@link InternalCellClipboard} on a {@link HierarchicalTreeLayer}. Will treat
 * cells with row spanning as a single cell. Will also not copy cells of
 * collapsed nodes if the labels {@link HierarchicalTreeLayer#COLLAPSED_CHILD}
 * or {@link HierarchicalTreeLayer#NO_OBJECT_IN_LEVEL} are applied.
 * <p>
 * <b>Note:</b><br>
 * To work correctly the {@link HierarchicalTreeCopyDataCommandHandler} should
 * be registered for handling copy operations with the
 * {@link InternalCellClipboard}, or at least the {@link HierarchicalTreeLayer}
 * should be set as copyLayer to ensure the additional information added by the
 * {@link HierarchicalTreeLayer} can be inspected as the cells to copy are
 * collected on that layer and not the {@link SelectionLayer}.
 * </p>
 *
 * @since 1.6
 *
 * @see HierarchicalTreeCopyDataCommandHandler
 */
public class HierarchicalTreePasteDataCommandHandler extends RowSpanningPasteDataCommandHandler {

    /**
     * @param selectionLayer
     *            {@link SelectionLayer} that is needed to determine the
     *            position to paste the values to.
     * @param clipboard
     *            The {@link InternalCellClipboard} that contains the values
     *            that should be pasted.
     */
    public HierarchicalTreePasteDataCommandHandler(SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
        super(selectionLayer, clipboard);
    }

    @Override
    protected boolean isPasteAllowed(ILayerCell sourceCell, ILayerCell targetCell, IConfigRegistry configRegistry) {
        LabelStack configLabels = targetCell.getConfigLabels();
        if (configLabels.hasLabel(HierarchicalTreeLayer.COLLAPSED_CHILD)
                || configLabels.hasLabel(HierarchicalTreeLayer.NO_OBJECT_IN_LEVEL)) {
            return false;
        }
        return super.isPasteAllowed(sourceCell, targetCell, configRegistry);
    }
}
