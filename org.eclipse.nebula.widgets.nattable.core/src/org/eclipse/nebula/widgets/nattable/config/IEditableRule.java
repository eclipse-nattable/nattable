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

public interface IEditableRule {

    public boolean isEditable(ILayerCell cell, IConfigRegistry configRegistry);

    public boolean isEditable(int columnIndex, int rowIndex);

    public static final IEditableRule ALWAYS_EDITABLE = new IEditableRule() {

        @Override
        public boolean isEditable(ILayerCell cell,
                IConfigRegistry configRegistry) {
            return true;
        }

        @Override
        public boolean isEditable(int columnIndex, int rowIndex) {
            return true;
        }

    };

    public static final IEditableRule NEVER_EDITABLE = new IEditableRule() {

        @Override
        public boolean isEditable(ILayerCell cell,
                IConfigRegistry configRegistry) {
            return false;
        }

        @Override
        public boolean isEditable(int columnIndex, int rowIndex) {
            return false;
        }

    };

}
