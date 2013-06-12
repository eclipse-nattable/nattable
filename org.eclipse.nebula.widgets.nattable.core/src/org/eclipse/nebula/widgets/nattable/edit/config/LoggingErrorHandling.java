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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.data.convert.ConversionFailedException;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;


/**
 * Error handling strategy that simply writes conversion/validation errors to the log. 
 * 
 * @author Dirk Fauth
 *
 */
public class LoggingErrorHandling extends AbstractEditErrorHandler {

	private static final Log log = LogFactory.getLog(LoggingErrorHandling.class);

	/**
	 * Create a new {@link LoggingErrorHandling} with no underlying {@link IEditErrorHandler}
	 */
	public LoggingErrorHandling() {
		super(null);
	}
	
	/**
	 * Create a new {@link LoggingErrorHandling} using the given {@link IEditErrorHandler} as
	 * the underlying to allow chaining of error handling.
	 * @param underlyingErrorHandler The underlying {@link IEditErrorHandler}
	 */
	public LoggingErrorHandling(IEditErrorHandler underlyingErrorHandler) {
		super(underlyingErrorHandler);
	}
	
	/**
	 * {@inheritDoc}
	 * After the error is handled by its underlying {@link IEditErrorHandler},
	 * the error will be logged as a warning.
	 */
	@Override
	public void displayError(ICellEditor cellEditor, Exception e) {
		super.displayError(cellEditor, e);
		//for ConversionFailedException and ValidationFailedException we only want to log the corresponding
		//message. Otherwise we need the whole stack trace to find unexpected exceptions
		if (!(e instanceof ConversionFailedException) && !(e instanceof ValidationFailedException)) {
			log.warn("Error whilst converting/validating cell value", e); //$NON-NLS-1$
		}
		else {
			log.warn("Error whilst converting/validating cell value: " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}
}
