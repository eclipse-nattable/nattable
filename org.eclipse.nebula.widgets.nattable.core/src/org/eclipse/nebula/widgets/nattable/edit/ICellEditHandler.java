/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit;

import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Handles the updating of the data bean with the new value provided by the
 * {@link ICellEditor}.
 */
public interface ICellEditHandler {

    /**
     * Commit the new value and handle the selection in the current NatTable
     * after commit. This is necessary to support spreadsheet like behaviour,
     * e.g. after committing a value entered in a text editor by pressing tab,
     * the selection should move to the right. To avoid unnecessary dependencies
     * in the editors itself, this is handled in here.
     *
     * @param canonicalValue
     *            The value to commit.
     * @param direction
     *            The direction to move the selection after commit.
     * @return <code>true</code> if the data update succeeded,
     *         <code>false</code> if something went wrong
     */
    public boolean commit(Object canonicalValue, MoveDirectionEnum direction);
}
