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
package org.eclipse.nebula.widgets.nattable.data.validate;

import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;

/**
 * Exception for handling validation failures. As the API should not be modified
 * for the handling of this exception, it is a RuntimeException. To make use of
 * this exception it can be thrown on validation errors within
 * {@link IDataValidator#validate(int, int, Object)}. The handling of this
 * exception is done within {@link AbstractCellEditor} where the message is
 * stored and showed within a dialog on trying to commit.
 */
public class ValidationFailedException extends RuntimeException {

    private static final long serialVersionUID = 8965433867324718901L;

    public ValidationFailedException(String message) {
        super(message);
    }

    public ValidationFailedException(String message, Throwable t) {
        super(message, t);
    }
}
