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

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;

/**
 * Interface to specify a data provider for {@link ComboBoxCellEditor}s in
 * NatTable. Using such a data provider allows to specify dynamic content for
 * the combo box.
 */
public interface IComboBoxDataProvider {

    /**
     * Will determine the values for the cell at the specified position. It will
     * return a list with canonical values. The registered
     * {@link IDisplayConverter} will handle the conversion to display values
     * when the {@link ComboBoxCellEditor} is filled with the selectable items.
     * There is no need to convert within this method.
     *
     * @param columnIndex
     *            The column index of the cell whose {@link ComboBoxCellEditor}
     *            should be filled.
     * @param rowIndex
     *            The row index of the cell whose {@link ComboBoxCellEditor}
     *            should be filled.
     * @return List of values that should be used to fill the values of the
     *         {@link ComboBoxCellEditor}
     */
    public List<?> getValues(int columnIndex, int rowIndex);
}
