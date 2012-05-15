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
package org.eclipse.nebula.widgets.nattable.edit.editor;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;


public interface IComboBoxDataProvider {

	/**
	 * List of values to used as a data source in a {@link ComboBoxCellEditor}.
	 * 	Note: these will be converted using the {@link IDisplayConverter} for display
	 */
	public List<?> getValues(int columnIndex, int rowIndex);
}
