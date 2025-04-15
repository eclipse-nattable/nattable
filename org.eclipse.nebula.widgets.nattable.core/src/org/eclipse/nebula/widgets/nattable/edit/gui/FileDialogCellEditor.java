/*******************************************************************************
 * Copyright (c) 2013, 2025 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.edit.gui;

import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.PlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

/**
 * This implementation is a proof of concept for special cell editors that wrap
 * dialogs. The {@link FileDialog} is wrapped by this implementation. It will
 * open the default file selection dialog on trying to activate the cell editor.
 */
public class FileDialogCellEditor extends AbstractDialogCellEditor {

    /**
     * The selection result of the {@link FileDialog}. Needed to update the data
     * model after closing the dialog.
     */
    private String selectedFile;
    /**
     * Flag to determine whether the dialog was closed or if it is still open.
     */
    private boolean closed = false;

    @Override
    public int open() {
        this.selectedFile = getDialogInstance().open();
        if (this.selectedFile == null) {
            this.closed = true;
            return Window.CANCEL;
        } else {
            commit(MoveDirectionEnum.NONE);
            this.closed = true;
            return Window.OK;
        }
    }

    @Override
    public FileDialog createDialogInstance() {
        this.closed = false;
        return new FileDialog(this.parent.getShell(), SWT.OPEN);
    }

    @Override
    public FileDialog getDialogInstance() {
        return (FileDialog) this.dialog;
    }

    @Override
    public Object getEditorValue() {
        return this.selectedFile;
    }

    @Override
    public void setEditorValue(Object value) {
        PlatformHelper.callSetter(getDialogInstance(), "setFileName", String.class, value != null ? value.toString() : null); //$NON-NLS-1$
    }

    @Override
    public void close() {
        // as the FileDialog does not support a programmatically way of closing,
        // this method is forced to do nothing
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

}
