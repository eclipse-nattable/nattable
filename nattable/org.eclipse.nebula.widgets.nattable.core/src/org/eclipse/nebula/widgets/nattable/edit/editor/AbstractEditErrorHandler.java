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
package org.eclipse.nebula.widgets.nattable.edit.editor;

/**
 * Abstract implementation of IEditErrorHandler that by default calls
 * the underlying IEditErrorHandler to handle the error.
 */
public abstract class AbstractEditErrorHandler implements IEditErrorHandler {

	protected IEditErrorHandler underlyingErrorHandler;
	
	public AbstractEditErrorHandler(IEditErrorHandler underlyingErrorHandler) {
		this.underlyingErrorHandler = underlyingErrorHandler;
	}
	
	public void removeError(ICellEditor cellEditor) {
		if (underlyingErrorHandler != null) {
			underlyingErrorHandler.removeError(cellEditor);
		}
	}
	
	public void displayError(ICellEditor cellEditor, Exception e) {
		if (underlyingErrorHandler != null) {
			underlyingErrorHandler.displayError(cellEditor, e);
		}
	}
}
