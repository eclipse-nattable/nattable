/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *    Daniel Fritsch <danielw.fritsch@web.de> - Bug 460794
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Specialisation of TextCellEditor that performs a commit on edit. It is
 * intended to be used in a FilterRow, so filtering is triggered immediately on
 * entering a value. To optimize execution, the commit is triggered with a small
 * delay, so if a user enters multiple characters, the filter execution is not
 * executed for each key stroke, but only for the combination.
 *
 * @author Dirk Fauth
 *
 */
public class FilterRowTextCellEditor extends TextCellEditor {

    @Override
    protected Text createEditorControl(Composite parent, int style) {
        Text text = super.createEditorControl(parent, style);

        final ScheduledExecutorService service = Executors
                .newScheduledThreadPool(1);

        text.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                service.schedule(new KeyPressCommitRunnable(getEditorValue()),
                        150L, TimeUnit.MILLISECONDS);
            }
        });

        text.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                service.shutdownNow();
            }
        });

        return text;
    }

    /**
     * Runnable that gets started if a key is released and commits the data that
     * is currently set to the editor control. If the value which was used to
     * create the runnable is not the same as currently set to the editor
     * control, nothing will happen. This is to reduce the number of commit
     * executions if a user types several characters.
     *
     * @author Dirk Fauth
     *
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
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    if (getEditorControl() != null
                            && !getEditorControl().isDisposed()
                            && KeyPressCommitRunnable.this.toCommit != null
                            && KeyPressCommitRunnable.this.toCommit.equals(getEditorValue())) {
                        commit(MoveDirectionEnum.NONE, false);
                    }
                }
            });
        }
    }

}