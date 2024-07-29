/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

/**
 * {@link IDragMode} to visualize a column as overlay.
 *
 * @since 2.3
 */
public class ColumnDragMode implements IDragMode {

    private MouseEvent initialEvent;
    private MouseEvent currentEvent;

    private int xOffset;
    private Image columnImage;
    protected ColumnImageOverlayPainter columnImageOverlayPainter = new ColumnImageOverlayPainter();

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        this.initialEvent = event;
        this.currentEvent = this.initialEvent;

        setColumnImage(natTable);

        natTable.forceFocus();

        natTable.addOverlayPainter(this.columnImageOverlayPainter);
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        this.currentEvent = event;

        natTable.redraw(0, 0, natTable.getWidth(), natTable.getHeight(), false);
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        natTable.removeOverlayPainter(this.columnImageOverlayPainter);

        if (this.columnImage != null) {
            this.columnImage.dispose();
        }

        natTable.redraw(0, 0, natTable.getWidth(), natTable.getHeight(), false);
    }

    protected MouseEvent getInitialEvent() {
        return this.initialEvent;
    }

    protected MouseEvent getCurrentEvent() {
        return this.currentEvent;
    }

    private void setColumnImage(NatTable natTable) {
        int columnPosition = natTable.getColumnPositionByX(this.currentEvent.x);

        IConfigRegistry configRegistry = natTable.getConfigRegistry();
        int y = 0;
        ILayerCell cell = null;

        Image image = null;
        GC gc = null;

        try {
            for (int rowPosition = 0; rowPosition < natTable.getRowCount(); rowPosition++) {
                cell = natTable.getCellByPosition(columnPosition, rowPosition);

                if (cell != null) {
                    Rectangle cellBounds = cell.getBounds();
                    this.xOffset = this.currentEvent.x - cellBounds.x;

                    if (image == null && gc == null) {
                        image = new Image(natTable.getDisplay(), cellBounds.width, natTable.getHeight());
                        gc = new GC(image);
                    }

                    ICellPainter cellPainter = cell.getLayer().getCellPainter(columnPosition, rowPosition, cell, configRegistry);
                    if (cellPainter != null) {
                        cellPainter.paintCell(cell, gc, new Rectangle(0, y, cellBounds.width, cellBounds.height), configRegistry);
                        y += cellBounds.height;
                    }
                }
            }

            if (image != null) {
                ImageData imageData = image.getImageData();
                imageData.alpha = 150;

                this.columnImage = new Image(natTable.getDisplay(), imageData);
            }

        } finally {
            if (gc != null) {
                gc.dispose();
            }

            if (image != null) {
                image.dispose();
            }
        }
    }

    private class ColumnImageOverlayPainter implements IOverlayPainter {

        @Override
        public void paintOverlay(GC gc, ILayer layer) {
            if (ColumnDragMode.this.columnImage != null && !ColumnDragMode.this.columnImage.isDisposed()) {
                gc.drawImage(
                        ColumnDragMode.this.columnImage,
                        ColumnDragMode.this.currentEvent.x - ColumnDragMode.this.xOffset,
                        0);
            }
        }
    }
}
