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

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;

/**
 * An {@link IEditErrorHandler} will be used if on data conversion or validation
 * while editing via {@link ICellEditor} an error occurs. Such a handler is
 * usually registered in the {@link IConfigRegistry}, using the
 * {@link EditConfigAttributes}.
 * <p>
 * For some {@link ICellEditor}s they are also used for just in time
 * conversion/validation to render the wrong input immediately for feedback to
 * the user. This is done e.g. in the {@link TextCellEditor}.
 *
 * @author Dirk Fauth
 *
 * @see EditConfigAttributes#CONVERSION_ERROR_HANDLER
 * @see EditConfigAttributes#VALIDATION_ERROR_HANDLER
 */
public interface IEditErrorHandler {

    /**
     * Will remove styling or other decorations that indicate that an error
     * occurred. Only necessary to implement if the error handler adds special
     * styling or decorations on error.
     *
     * @param cellEditor
     *            The {@link ICellEditor} to remove the error styling from.
     */
    void removeError(ICellEditor cellEditor);

    /**
     * If an error occurs on conversion/validation of data, this method will be
     * called for showing that error to the user. Usually the message contained
     * within the given {@link Exception} will be shown to the user.
     *
     * @param cellEditor
     *            The {@link ICellEditor} on which the conversion/validation
     *            error occurred. Needed to add error styling or special
     *            handling.
     * @param e
     *            The {@link Exception} that contains information about the
     *            conversion/validation error. Used to show a more detailed
     *            description on the error to the user.
     */
    void displayError(ICellEditor cellEditor, Exception e);
}
