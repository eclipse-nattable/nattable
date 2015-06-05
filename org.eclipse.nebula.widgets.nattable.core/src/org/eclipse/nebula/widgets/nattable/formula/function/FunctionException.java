/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula.function;

/**
 * Exception that is fired on function evaluation.
 *
 * @since 1.4
 */
public class FunctionException extends RuntimeException {

    private static final long serialVersionUID = -9066291743277891365L;

    private final String errorMarkup;

    /**
     *
     * @param errorMarkup
     *            The value that should be shown in the cell marking that an
     *            error occurred.
     * @param msg
     *            The error message.
     */
    public FunctionException(String errorMarkup, String msg) {
        super(msg);
        this.errorMarkup = errorMarkup;
    }

    /**
     * @return The value that should be shown in the cell marking that an error
     *         occurred.
     */
    public String getErrorMarkup() {
        return this.errorMarkup;
    }

}
