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
package org.eclipse.nebula.widgets.nattable.edit;

import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.IStyle;

public interface EditConfigAttributes {
	
	public static final ConfigAttribute<IEditableRule> CELL_EDITABLE_RULE = new ConfigAttribute<IEditableRule>();
	
	public static final ConfigAttribute<ICellEditor> CELL_EDITOR = new ConfigAttribute<ICellEditor>();

	public static final ConfigAttribute<IDataValidator> DATA_VALIDATOR = new ConfigAttribute<IDataValidator>();

	public static final ConfigAttribute<IEditErrorHandler> CONVERSION_ERROR_HANDLER = new ConfigAttribute<IEditErrorHandler>();
	public static final ConfigAttribute<IEditErrorHandler> VALIDATION_ERROR_HANDLER = new ConfigAttribute<IEditErrorHandler>();
	public static final ConfigAttribute<IStyle> VALIDATION_ERROR_STYLE = new ConfigAttribute<IStyle>();

}
