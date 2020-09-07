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
package org.eclipse.nebula.widgets.nattable.data.convert;

import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;

/**
 * Exception for handling conversion failures. As the API should not be modified
 * for the handling of this exception, it is a RuntimeException. To make use of
 * this exception it can be thrown on conversion errors within
 * {@link IDisplayConverter#displayToCanonicalValue(Object)}. The handling of
 * this exception is done within {@link AbstractCellEditor} where the message is
 * stored and showed within a dialog on trying to commit.
 */
public class ConversionFailedException extends RuntimeException {

    private static final long serialVersionUID = -755775784924211402L;

    public ConversionFailedException(String message) {
        super(message);
    }

    public ConversionFailedException(String message, Throwable t) {
        super(message, t);
    }
}
