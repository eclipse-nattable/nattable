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
 * {@link IDragMode} to visualize a row as overlay.
 *
 * @since 2.3
 */
public class RowDragMode implements IDragMode {

    private MouseEvent initialEvent;
    private MouseEvent currentEvent;

    private int yOffset;
    private Image rowImage;
    protected RowImageOverlayPainter rowImageOverlayPainter = new RowImageOverlayPainter();

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        this.initialEvent = event;
        this.currentEvent = this.initialEvent;

        setRowImage(natTable);

        natTable.forceFocus();

        natTable.addOverlayPainter(this.rowImageOverlayPainter);
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        this.currentEvent = event;

        natTable.redraw(0, 0, natTable.getWidth(), natTable.getHeight(), false);
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        natTable.removeOverlayPainter(this.rowImageOverlayPainter);

        if (this.rowImage != null) {
            this.rowImage.dispose();
        }

        natTable.redraw(0, 0, natTable.getWidth(), natTable.getHeight(), false);
    }

    protected MouseEvent getInitialEvent() {
        return this.initialEvent;
    }

    protected MouseEvent getCurrentEvent() {
        return this.currentEvent;
    }

    private void setRowImage(NatTable natTable) {
        int rowPosition = natTable.getRowPositionByY(this.currentEvent.y);

        IConfigRegistry configRegistry = natTable.getConfigRegistry();
        int x = 0;
        ILayerCell cell = null;

        Image image = null;
        GC gc = null;

        for (int columnPosition = 0; columnPosition < natTable.getColumnCount(); columnPosition++) {
            cell = natTable.getCellByPosition(columnPosition, rowPosition);

            if (cell != null) {
                Rectangle cellBounds = cell.getBounds();
                this.yOffset = this.currentEvent.y - cellBounds.y;

                if (image == null && gc == null) {
                    image = new Image(natTable.getDisplay(), natTable.getWidth(), cellBounds.height);
                    gc = new GC(image);
                }

                ICellPainter cellPainter = cell.getLayer().getCellPainter(columnPosition, rowPosition, cell, configRegistry);
                if (cellPainter != null) {
                    cellPainter.paintCell(cell, gc, new Rectangle(x, 0, cellBounds.width, cellBounds.height), configRegistry);
                    x += cellBounds.width;
                }
            }
        }

        if (gc != null) {
            gc.dispose();
        }

        if (image != null) {
            ImageData imageData = image.getImageData();
            image.dispose();
            imageData.alpha = 150;

            this.rowImage = new Image(natTable.getDisplay(), imageData);
        }
    }

    private class RowImageOverlayPainter implements IOverlayPainter {

        @Override
        public void paintOverlay(GC gc, ILayer layer) {
            if (RowDragMode.this.rowImage != null && !RowDragMode.this.rowImage.isDisposed()) {
                gc.drawImage(
                        RowDragMode.this.rowImage,
                        0,
                        RowDragMode.this.currentEvent.y - RowDragMode.this.yOffset);
            }
        }
    }
}
