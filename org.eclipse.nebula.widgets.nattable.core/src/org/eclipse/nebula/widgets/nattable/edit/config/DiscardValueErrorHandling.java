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
package org.eclipse.nebula.widgets.nattable.edit.config;

import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;

/**
 * Strategy class for conversion/validation failures. If the entered value is
 * not valid, it is simply discarded. Only handles errors on commit.
 *
 * @author Dirk Fauth
 */
public class DiscardValueErrorHandling extends AbstractEditErrorHandler {

    /**
     * Create a new {@link DiscardValueErrorHandling} with no underlying
     * {@link IEditErrorHandler}
     */
    public DiscardValueErrorHandling() {
        super(null);
    }

    /**
     * Create a new {@link DiscardValueErrorHandling} using the given
     * {@link IEditErrorHandler} as the underlying to allow chaining of error
     * handling.
     *
     * @param underlyingErrorHandler
     *            The underlying {@link IEditErrorHandler}
     */
    public DiscardValueErrorHandling(IEditErrorHandler underlyingErrorHandler) {
        super(underlyingErrorHandler);
    }

    /**
     * {@inheritDoc} After the error is handled by its underlying
     * {@link IEditErrorHandler}, the {@link ICellEditor} will be closed,
     * discarding the value.
     */
    @Override
    public void displayError(ICellEditor cellEditor, Exception e) {
        super.displayError(cellEditor, e);
        cellEditor.close();
    }

}
