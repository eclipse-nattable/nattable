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
package org.eclipse.nebula.widgets.nattable.edit.editor;

/**
 * Abstract implementation of {@link IEditErrorHandler} that by default calls
 * the underlying {@link IEditErrorHandler} to handle the error. This allows
 * chaining of {@link IEditErrorHandler}s to support multiple error handling
 * behaviour, e.g. displaying the error in a dialog and log the error.
 *
 * @author Dirk Fauth
 */
public abstract class AbstractEditErrorHandler implements IEditErrorHandler {

    /**
     * The underlying {@link IEditErrorHandler}
     */
    protected IEditErrorHandler underlyingErrorHandler;

    /**
     *
     * @param underlyingErrorHandler
     *            The underlying {@link IEditErrorHandler}
     */
    public AbstractEditErrorHandler(IEditErrorHandler underlyingErrorHandler) {
        this.underlyingErrorHandler = underlyingErrorHandler;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will call its underlying {@link IEditErrorHandler}.
     */
    @Override
    public void removeError(ICellEditor cellEditor) {
        if (this.underlyingErrorHandler != null) {
            this.underlyingErrorHandler.removeError(cellEditor);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will call its underlying {@link IEditErrorHandler}.
     */
    @Override
    public void displayError(ICellEditor cellEditor, Exception e) {
        if (this.underlyingErrorHandler != null) {
            this.underlyingErrorHandler.displayError(cellEditor, e);
        }
    }
}
