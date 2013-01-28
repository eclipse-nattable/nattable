/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.edit.gui;

import org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum;

/**
 * Interface for dialogs that can be used as editing dialogs in NatTable.
 * 
 * @author Dirk Fauth
 *
 */
public interface ICellEditDialog {

	/**
	 * @return The canonical value that was committed to the editor control.
	 */
	Object getCommittedValue();
	
	/**
	 * @return The edit type that has impact on how the set value will be updated to 
	 * 			the data model. By default {@link EditTypeEnum#SET} is returned, which
	 * 			will simply set the committed value to the data model. Every other
	 * 			edit type will do some calculation based on the committed value and 
	 * 			the current value in the data model.
	 */
	EditTypeEnum getEditType();
	
	/**
	 * In case {@link ICellEditDialog#getEditType()} returns an edit type for processing
	 * values, this method should implemented to do that transformation.
	 * @param currentValue The current value for the cell before data model update
	 * @param processValue The value committed to the editor that should be used for
	 * 			calculation on the current value.
	 * @return The value that should be used to update the data model.
	 */
	Object calculateValue(Object currentValue, Object processValue);
	
	/**
	 * Opens this dialog, creating it first if it has not yet been created.
	 * <p>
	 * Specified in here for convenience so we only need to check against this
	 * interface for a dialog.
	 * </p>
	 * 
	 * @return the return code
	 */
	int open();

}
