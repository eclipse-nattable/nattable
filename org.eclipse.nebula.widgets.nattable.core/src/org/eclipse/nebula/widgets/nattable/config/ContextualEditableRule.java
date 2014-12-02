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
package org.eclipse.nebula.widgets.nattable.config;

import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public abstract class ContextualEditableRule implements IEditableRule {

    @Override
    public boolean isEditable(int columnIndex, int rowIndex) {
        throw new UnsupportedOperationException(
                this.getClass().getName()
                        + " is a ContextualEditableRule and has therefore to be called with context informations."); //$NON-NLS-1$
    }

    @Override
    public abstract boolean isEditable(ILayerCell cell,
            IConfigRegistry configRegistry);

}
