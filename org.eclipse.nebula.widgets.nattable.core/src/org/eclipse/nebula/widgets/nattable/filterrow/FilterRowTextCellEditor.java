/*******************************************************************************
 * Copyright (c) 2014, 2023 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Daniel Fritsch <danielw.fritsch@web.de> - Bug 460794
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Specialisation of TextCellEditor that performs a commit on edit. It is
 * intended to be used in a FilterRow, so filtering is triggered immediately on
 * entering a value. To optimize execution, the commit is triggered with a small
 * delay, so if a user enters multiple characters, the filter execution is not
 * executed for each key stroke, but only for the combination.
 */
public class FilterRowTextCellEditor extends TextCellEditor {

    @Override
    protected Text createEditorControl(Composite parent, int style) {
        Text text = super.createEditorControl(parent, style);

        final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

        text.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                service.schedule(
                        new KeyPressCommitRunnable(getEditorValue()), 150L, TimeUnit.MILLISECONDS);
            }
        });

        PaintListener paintListener = new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                if (getEditorControl() != null && !getEditorControl().isDisposed()) {

                    // we need to retrieve the cell again to get the updated
                    // cell bounds in case the scrollbar becomes invisible
                    // because of filtering
                    ILayerCell cell = FilterRowTextCellEditor.this.layerCell.getLayer().getCellByPosition(
                            FilterRowTextCellEditor.this.layerCell.getColumnPosition(),
                            FilterRowTextCellEditor.this.layerCell.getRowPosition());

                    Rectangle cellBounds = cell.getBounds();
                    Rectangle editorBounds = cell.getLayer().getLayerPainter().adjustCellBounds(
                            cell.getColumnPosition(),
                            cell.getRowPosition(),
                            new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height));

                    editorBounds = calculateControlBounds(editorBounds);

                    if (editorBounds.x == 0) {
                        editorBounds.x += 1;
                        editorBounds.width -= 1;
                    }

                    getEditorControl().setBounds(editorBounds);
                }

            }
        };
        parent.addPaintListener(paintListener);

        text.addDisposeListener(e -> {
            service.shutdownNow();
            parent.removePaintListener(paintListener);
        });

        return text;
    }

    /**
     * Runnable that gets started if a key is released and commits the data that
     * is currently set to the editor control. If the value which was used to
     * create the runnable is not the same as currently set to the editor
     * control, nothing will happen. This is to reduce the number of commit
     * executions if a user types several characters.
     */
    private class KeyPressCommitRunnable implements Runnable {

        final String toCommit;

        KeyPressCommitRunnable(String toCommit) {
            this.toCommit = toCommit;
        }

        @Override
        public void run() {
            // the access to the editor needs to be executed in the display
            // thread
            Display.getDefault().syncExec(() -> {
                if (getEditorControl() != null
                        && !getEditorControl().isDisposed()
                        && KeyPressCommitRunnable.this.toCommit != null
                        && KeyPressCommitRunnable.this.toCommit.equals(getEditorValue())) {
                    commit(MoveDirectionEnum.NONE, false);
                }
            });
        }
    }

}