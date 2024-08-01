/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
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

public class CellDragMode implements IDragMode {

    private MouseEvent initialEvent;
    private MouseEvent currentEvent;

    private int xOffset;
    private int yOffset;
    private Image cellImage;
    protected CellImageOverlayPainter cellImageOverlayPainter = new CellImageOverlayPainter();

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        this.initialEvent = event;
        this.currentEvent = this.initialEvent;

        setCellImage(natTable);

        natTable.forceFocus();

        natTable.addOverlayPainter(this.cellImageOverlayPainter);
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        this.currentEvent = event;

        natTable.redraw(0, 0, natTable.getWidth(), natTable.getHeight(), false);
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        natTable.removeOverlayPainter(this.cellImageOverlayPainter);

        if (this.cellImage != null) {
            this.cellImage.dispose();
        }

        natTable.redraw(0, 0, natTable.getWidth(), natTable.getHeight(), false);
    }

    protected MouseEvent getInitialEvent() {
        return this.initialEvent;
    }

    protected MouseEvent getCurrentEvent() {
        return this.currentEvent;
    }

    private void setCellImage(NatTable natTable) {
        int columnPosition = natTable.getColumnPositionByX(this.currentEvent.x);
        int rowPosition = natTable.getRowPositionByY(this.currentEvent.y);
        ILayerCell cell = natTable.getCellByPosition(columnPosition, rowPosition);

        if (cell != null) {
            Rectangle cellBounds = cell.getBounds();
            this.xOffset = this.currentEvent.x - cellBounds.x;
            this.yOffset = this.currentEvent.y - cellBounds.y;
            Image image = new Image(natTable.getDisplay(), cellBounds.width, cellBounds.height);

            GC gc = new GC(image);
            try {
                IConfigRegistry configRegistry = natTable.getConfigRegistry();
                ICellPainter cellPainter = cell.getLayer().getCellPainter(columnPosition, rowPosition, cell, configRegistry);
                if (cellPainter != null) {
                    cellPainter.paintCell(cell, gc, new Rectangle(0, 0, cellBounds.width, cellBounds.height), configRegistry);
                }

                ImageData imageData = image.getImageData();
                imageData.alpha = 150;

                this.cellImage = new Image(natTable.getDisplay(), imageData);
            } finally {
                gc.dispose();
                image.dispose();
            }
        }
    }

    private class CellImageOverlayPainter implements IOverlayPainter {

        @Override
        public void paintOverlay(GC gc, ILayer layer) {
            if (CellDragMode.this.cellImage != null && !CellDragMode.this.cellImage.isDisposed()) {
                gc.drawImage(
                        CellDragMode.this.cellImage,
                        CellDragMode.this.currentEvent.x - CellDragMode.this.xOffset,
                        CellDragMode.this.currentEvent.y - CellDragMode.this.yOffset);
            }
        }

    }

}
