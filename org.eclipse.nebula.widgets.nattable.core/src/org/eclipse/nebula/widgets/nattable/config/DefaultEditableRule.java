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

public class DefaultEditableRule extends EditableRule {

    private boolean defaultEditable;

    public DefaultEditableRule(boolean defaultEditable) {
        this.defaultEditable = defaultEditable;
    }

    @Override
    public boolean isEditable(int columnIndex, int rowIndex) {
        return this.defaultEditable;
    }

}
