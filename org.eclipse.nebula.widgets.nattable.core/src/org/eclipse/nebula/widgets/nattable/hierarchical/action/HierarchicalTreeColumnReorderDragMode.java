/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Specialization of the {@link ColumnReorderDragMode} to be used with a
 * {@link HierarchicalTreeLayer} to respect hierarchical levels.
 *
 * @since 1.6
 */
public class HierarchicalTreeColumnReorderDragMode extends ColumnReorderDragMode {

    private HierarchicalTreeLayer treeLayer;
    private Cursor currentCursor;

    protected ColumnGroupModel columnGroupModel;

    /**
     * @param treeLayer
     *            The {@link HierarchicalTreeLayer} needed to determine the
     *            level column boundaries.
     */
    public HierarchicalTreeColumnReorderDragMode(HierarchicalTreeLayer treeLayer) {
        this(treeLayer, null);
    }

    /**
     * Creates a drag mode that validates the drag operation based on the given
     * tree level structure and the given column group structure.
     *
     * @param treeLayer
     *            The {@link HierarchicalTreeLayer} needed to determine the
     *            level column boundaries.
     * @param model
     *            The {@link ColumnGroupModel} to perform column group based
     *            drag validation. Can be <code>null</code>.
     */
    public HierarchicalTreeColumnReorderDragMode(HierarchicalTreeLayer treeLayer, ColumnGroupModel model) {
        this.treeLayer = treeLayer;
        this.columnGroupModel = model;
        this.targetOverlayPainter = new HierarchicalColumnReorderOverlayPainter();
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        // ensure the default cursor is shown again
        this.natTable.setCursor(null);
        super.mouseUp(natTable, event);
    }

    @Override
    protected void fireMoveEndCommand(NatTable natTable, int dragToGridColumnPosition) {
        if (dragToGridColumnPosition < natTable.getColumnCount()) {
            int toIndex = natTable.getColumnIndexByPosition(dragToGridColumnPosition);
            if (toIndex < 0 && this.dragFromGridColumnPosition < dragToGridColumnPosition) {
                dragToGridColumnPosition++;
            }
        }
        super.fireMoveEndCommand(natTable, dragToGridColumnPosition);
    }

    @Override
    protected boolean isValidTargetColumnPosition(ILayer natLayer, int dragFromGridColumnPosition, int dragToGridColumnPosition) {
        int fromIndex = natLayer.getColumnIndexByPosition(dragFromGridColumnPosition);
        int toIndex = natLayer.getColumnIndexByPosition(dragToGridColumnPosition);

        // it is not allowed to drag a level header column and the index of a
        // level header is < 0
        if (fromIndex < 0) {
            return false;
        }

        if (toIndex < 0 && dragFromGridColumnPosition < dragToGridColumnPosition) {
            // get the position to the left of the level header
            toIndex = natLayer.getColumnIndexByPosition(dragToGridColumnPosition - 1);
        } else if (toIndex < 0 && dragFromGridColumnPosition > dragToGridColumnPosition) {
            return false;
        }

        int fromLevel = this.treeLayer.getLevelByColumnIndex(fromIndex);
        int toLevel = this.treeLayer.getLevelByColumnIndex(toIndex);
        if (fromLevel != toLevel && this.treeLayer.isShowTreeLevelHeader()) {
            return false;
        } else if (fromLevel != toLevel && !this.treeLayer.isShowTreeLevelHeader()) {
            // if no tree level headers are shown, test for the column to the
            // left to check for the right level border
            toLevel = this.treeLayer.getLevelByColumnIndex(toIndex - 1);
            if (fromLevel != toLevel) {
                return false;
            }
        }

        // if column group is configured do the column group checks
        if (this.columnGroupModel != null) {
            // Allow moving within the unbreakable group
            if (this.columnGroupModel.isPartOfAnUnbreakableGroup(fromIndex)) {
                return ColumnGroupUtils.isInTheSameGroup(fromIndex, toIndex, this.columnGroupModel);
            }

            boolean betweenTwoGroups = false;
            if (this.currentEvent != null) {
                int minX = this.currentEvent.x - GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
                int maxX = this.currentEvent.x + GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
                betweenTwoGroups = ColumnGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, this.columnGroupModel);
            }

            return (!this.columnGroupModel.isPartOfAnUnbreakableGroup(toIndex)) || betweenTwoGroups;
        }

        return super.isValidTargetColumnPosition(natLayer, dragFromGridColumnPosition, dragToGridColumnPosition);
    }

    /**
     * {@link IOverlayPainter} that visualizes reordering and changes the cursor
     * in case the target position is invalid, e.g. when trying to reorder
     * columns between levels.
     */
    private class HierarchicalColumnReorderOverlayPainter implements IOverlayPainter {

        private Cursor noOpsCursor;

        @Override
        public void paintOverlay(GC gc, ILayer layer) {
            int dragFromGridColumnPosition = getDragFromGridColumnPosition();

            if (HierarchicalTreeColumnReorderDragMode.this.currentEvent.x > HierarchicalTreeColumnReorderDragMode.this.natTable.getWidth()) {
                return;
            }

            CellEdgeEnum moveDirection = getMoveDirection(HierarchicalTreeColumnReorderDragMode.this.currentEvent.x);
            int dragToGridColumnPosition = getDragToGridColumnPosition(
                    moveDirection,
                    HierarchicalTreeColumnReorderDragMode.this.natTable.getColumnPositionByX(HierarchicalTreeColumnReorderDragMode.this.currentEvent.x));

            if (isValidTargetColumnPosition(HierarchicalTreeColumnReorderDragMode.this.natTable, dragFromGridColumnPosition, dragToGridColumnPosition)) {

                if (HierarchicalTreeColumnReorderDragMode.this.currentCursor != null) {
                    HierarchicalTreeColumnReorderDragMode.this.natTable.setCursor(null);
                    HierarchicalTreeColumnReorderDragMode.this.currentCursor = null;
                }

                int dragToColumnHandleX = -1;

                if (moveDirection != null) {
                    Rectangle selectedColumnHeaderRect = getColumnCell(HierarchicalTreeColumnReorderDragMode.this.currentEvent.x).getBounds();

                    switch (moveDirection) {
                        case LEFT:
                            dragToColumnHandleX = selectedColumnHeaderRect.x;
                            break;
                        case RIGHT:
                            dragToColumnHandleX = selectedColumnHeaderRect.x + selectedColumnHeaderRect.width;
                            break;
                        default:
                            break;
                    }
                }

                if (dragToColumnHandleX > 0) {
                    Color orgBgColor = gc.getBackground();
                    gc.setBackground(GUIHelper.COLOR_DARK_GRAY);
                    gc.fillRectangle(dragToColumnHandleX - 1, 0, 2, layer.getHeight());
                    gc.setBackground(orgBgColor);
                }
            } else if (HierarchicalTreeColumnReorderDragMode.this.currentCursor == null) {
                // show no ops cursor
                if (this.noOpsCursor == null) {
                    this.noOpsCursor = new Cursor(Display.getDefault(), SWT.CURSOR_NO);

                    HierarchicalTreeColumnReorderDragMode.this.natTable.addDisposeListener(new DisposeListener() {

                        @Override
                        public void widgetDisposed(DisposeEvent e) {
                            HierarchicalColumnReorderOverlayPainter.this.noOpsCursor.dispose();
                        }

                    });
                }

                HierarchicalTreeColumnReorderDragMode.this.natTable.setCursor(this.noOpsCursor);
                HierarchicalTreeColumnReorderDragMode.this.currentCursor = this.noOpsCursor;
            }
        }
    }
}