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
package org.eclipse.nebula.widgets.nattable.formula.config;

import org.eclipse.nebula.widgets.nattable.formula.FormulaLayerPainter;
import org.eclipse.nebula.widgets.nattable.formula.command.FormulaCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.command.FormulaPasteDataCommandHandler;

/**
 * @since 1.4
 */
public class FormulaStyleLabels {

    /**
     * Style label for configuring the copy border.
     *
     * @see FormulaLayerPainter
     * @see FormulaCopyDataCommandHandler
     * @see FormulaPasteDataCommandHandler
     */
    public static final String COPY_BORDER_STYLE = "copyBorderStyle"; //$NON-NLS-1$

}
