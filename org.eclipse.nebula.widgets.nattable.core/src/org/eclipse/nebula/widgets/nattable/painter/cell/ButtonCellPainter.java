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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Paints a button and simulates a button click. It also notifies its listeners
 * when it is clicked.
 */
public class ButtonCellPainter extends AbstractCellPainter implements
        IMouseAction {
    private final ICellPainter buttonRaisedPainter;
    private final ICellPainter buttonPressedPainter;

    private int buttonFlashTime = 150;

    private int columnPosClicked;
    private int rowPosClicked;
    private boolean recentlyClicked;
    private final List<IMouseAction> clickLiseners = new ArrayList<IMouseAction>();

    /**
     * @param interiorPainter
     *            to paint the contents of the cell. This will be decorated with
     *            a button like look and feel.
     */
    public ButtonCellPainter(ICellPainter interiorPainter) {
        this.buttonPressedPainter = new BeveledBorderDecorator(interiorPainter,
                false);
        this.buttonRaisedPainter = new BeveledBorderDecorator(interiorPainter);
    }

    /**
     * @param buttonRaisedPainter
     *            cell painter to use for painting the button raised state.
     * @param buttonPressedPainter
     *            cell painter to use for painting the button pressed state.
     */
    public ButtonCellPainter(ICellPainter buttonRaisedPainter,
            ICellPainter buttonPressedPainter) {
        this.buttonRaisedPainter = buttonRaisedPainter;
        this.buttonPressedPainter = buttonPressedPainter;
    }

    @Override
    public void paintCell(final ILayerCell cell, final GC gc,
            final Rectangle bounds, final IConfigRegistry configRegistry) {
        if (this.recentlyClicked && this.columnPosClicked == cell.getColumnPosition()
                && this.rowPosClicked == cell.getRowPosition()) {
            this.buttonPressedPainter.paintCell(cell, gc, bounds, configRegistry);
        } else {
            this.buttonRaisedPainter.paintCell(cell, gc, bounds, configRegistry);
        }
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc,
            IConfigRegistry configRegistry) {
        return cell.getBounds().height;
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc,
            IConfigRegistry configRegistry) {
        return cell.getBounds().width;
    }

    private TimerTask getButtonFlashTimerTask(final ILayer layer) {
        return new TimerTask() {
            @Override
            public void run() {
                ButtonCellPainter.this.recentlyClicked = false;
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        layer.fireLayerEvent(new CellVisualChangeEvent(layer,
                                ButtonCellPainter.this.columnPosClicked, ButtonCellPainter.this.rowPosClicked));
                    }
                });
            }
        };
    }

    /**
     * Respond to mouse click. Simulate button press.
     */
    @Override
    public void run(final NatTable natTable, MouseEvent event) {
        NatEventData eventData = (NatEventData) event.data;
        this.columnPosClicked = eventData.getColumnPosition();
        this.rowPosClicked = eventData.getRowPosition();
        this.recentlyClicked = true;

        new Timer()
                .schedule(getButtonFlashTimerTask(natTable), this.buttonFlashTime);
        natTable.fireLayerEvent(new CellVisualChangeEvent(natTable,
                this.columnPosClicked, this.rowPosClicked));

        for (IMouseAction listener : this.clickLiseners) {
            listener.run(natTable, event);
        }
    }

    public void addClickListener(IMouseAction mouseAction) {
        this.clickLiseners.add(mouseAction);
    }

    public void removeClickListener(IMouseAction mouseAction) {
        this.clickLiseners.remove(mouseAction);
    }

    public void setButtonFlashTime(int flashTimeInMS) {
        this.buttonFlashTime = flashTimeInMS;
    }
}
