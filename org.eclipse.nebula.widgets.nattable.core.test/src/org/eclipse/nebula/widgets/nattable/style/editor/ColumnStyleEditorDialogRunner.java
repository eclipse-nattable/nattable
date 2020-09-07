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
package org.eclipse.nebula.widgets.nattable.style.editor;

import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.swt.widgets.Shell;

public class ColumnStyleEditorDialogRunner {

    public static void main(String[] args) throws Exception {
        Shell shell = new Shell();
        ColumnStyleEditorDialog dialog = new ColumnStyleEditorDialog(shell, new Style());
        dialog.open();
        System.out.println("Style: " + dialog.getNewColumnCellStyle());
    }

}
