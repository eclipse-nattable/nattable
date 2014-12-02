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
package org.eclipse.nebula.widgets.nattable.data.validate;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public interface IDataValidator {

    /**
     *
     * @param columnIndex
     *            Index of the column being validated
     * @param rowIndex
     *            Index of the row being validated
     * @param newValue
     *            Value entered through the edit control text box, combo box
     *            etc. Note: In case of the {@link TextCellEditor} the text
     *            typed in by the user will be converted to the canonical value
     *            using the {@link IDisplayConverter} before it hits this method
     *
     * @see IDataProvider#getDataValue(int, int)
     *
     * @return true is newValue is valid. False otherwise.
     */
    public boolean validate(int columnIndex, int rowIndex, Object newValue);

    /**
     *
     * @param cell
     *            LayerCell which should be validated
     * @param configRegistry
     * @param newValue
     *            Value entered through the edit control text box, combo box
     *            etc. Note: In case of the {@link TextCellEditor} the text
     *            typed in by the user will be converted to the canonical value
     *            using the {@link IDisplayConverter} before it hits this method
     *
     * @see IDataProvider#getDataValue(int, int)
     *
     * @return true is newValue is valid. False otherwise.
     */
    public boolean validate(ILayerCell cell, IConfigRegistry configRegistry,
            Object newValue);

    public static final IDataValidator ALWAYS_VALID = new IDataValidator() {

        @Override
        public boolean validate(ILayerCell cell,
                IConfigRegistry configRegistry, Object newValue) {
            return true;
        }

        @Override
        public boolean validate(int columnIndex, int rowIndex, Object newValue) {
            return true;
        }

    };

    public static final IDataValidator NEVER_VALID = new IDataValidator() {

        @Override
        public boolean validate(ILayerCell cell,
                IConfigRegistry configRegistry, Object newValue) {
            return false;
        }

        @Override
        public boolean validate(int columnIndex, int rowIndex, Object newValue) {
            return false;
        }

    };
}
