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
