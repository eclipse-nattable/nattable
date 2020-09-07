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
package org.eclipse.nebula.widgets.nattable.edit.editor;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;

/**
 * Abstract implementation of {@link IEditErrorHandler} that by default calls
 * the underlying {@link IEditErrorHandler} to handle the error. This allows
 * chaining of {@link IEditErrorHandler}s to support multiple error handling
 * behaviour, e.g. displaying the error in a dialog and log the error.
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

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will call its underlying {@link IEditErrorHandler}.
     */
    @Override
    public void displayError(ICellEditor cellEditor, IConfigRegistry configRegistry, Exception e) {
        if (this.underlyingErrorHandler != null) {
            this.underlyingErrorHandler.displayError(cellEditor, configRegistry, e);
        }
        displayError(cellEditor, e);
    }
}
