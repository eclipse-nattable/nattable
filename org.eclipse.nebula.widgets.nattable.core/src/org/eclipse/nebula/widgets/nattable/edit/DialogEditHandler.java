/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * {@link ICellEditHandler} implementation for editing within a dialog. It will
 * simply store the committed value so it can be propagated to the data model by
 * the caller of the dialog.
 * <p>
 * Note: On using this handler you are forced to call the UpdateDataCommand or
 * any other action to update the data model yourself!
 *
 * @author Dirk Fauth
 *
 */
public class DialogEditHandler implements ICellEditHandler {

    /**
     * The value that should be used to update the data model.
     */
    private Object committedValue;

    /**
     * This implementation will simply store the committed value in this
     * handler. This way the caller of the dialog that uses this handler is able
     * to update the data model and handle the selection itself.
     * <p>
     * This implementation always returns <code>true</code> leaving the editor
     * in control if it should be closed after commit. The behaviour is mainly
     * different in terms of where the editor is opened, inline or in a
     * subdialog. But as it is also related to the editor itself, e.g. a text
     * editor will remain visible while a NatCombo will disappear after commit,
     * the editor itself needs to take care of the closing.
     */
    @Override
    public boolean commit(Object canonicalValue, MoveDirectionEnum direction) {
        this.committedValue = canonicalValue;
        return true;
    }

    /**
     * @return The value that should be used to update the data model.
     */
    public Object getCommittedValue() {
        return this.committedValue;
    }

}
