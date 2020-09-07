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
package org.eclipse.nebula.widgets.nattable.layer.stack;

import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

/**
 * A pre-configured layer stack which includes the following layers (in that
 * order):
 * <ol>
 * <li>ColumnReorderLayer</li>
 * <li>ColumnGroupReorderLayer</li>
 * <li>ColumnHideShowLayer</li>
 * <li>ColumnGroupExpandCollapseLayer</li>
 * <li>SelectionLayer</li>
 * <li>ViewportLayer</li>
 * </ol>
 */
public class ColumnGroupBodyLayerStack extends AbstractIndexLayerTransform {

    private ColumnReorderLayer columnReorderLayer;
    private ColumnGroupReorderLayer columnGroupReorderLayer;
    private ColumnHideShowLayer columnHideShowLayer;
    private ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;

    public ColumnGroupBodyLayerStack(IUniqueIndexLayer underlyingLayer, ColumnGroupModel... columnGroupModel) {
        this.columnReorderLayer =
                new ColumnReorderLayer(underlyingLayer);
        this.columnGroupReorderLayer =
                new ColumnGroupReorderLayer(this.columnReorderLayer, columnGroupModel[columnGroupModel.length - 1]);
        this.columnHideShowLayer =
                new ColumnHideShowLayer(this.columnGroupReorderLayer);
        this.columnGroupExpandCollapseLayer =
                new ColumnGroupExpandCollapseLayer(this.columnHideShowLayer, columnGroupModel);
        this.selectionLayer =
                new SelectionLayer(this.columnGroupExpandCollapseLayer);
        this.viewportLayer =
                new ViewportLayer(this.selectionLayer);
        setUnderlyingLayer(this.viewportLayer);

        registerCommandHandler(new CopyDataCommandHandler(this.selectionLayer));
    }

    public ColumnReorderLayer getColumnReorderLayer() {
        return this.columnReorderLayer;
    }

    public ColumnGroupReorderLayer getColumnGroupReorderLayer() {
        return this.columnGroupReorderLayer;
    }

    public ColumnHideShowLayer getColumnHideShowLayer() {
        return this.columnHideShowLayer;
    }

    public ColumnGroupExpandCollapseLayer getColumnGroupExpandCollapseLayer() {
        return this.columnGroupExpandCollapseLayer;
    }

    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    public ViewportLayer getViewportLayer() {
        return this.viewportLayer;
    }

}
