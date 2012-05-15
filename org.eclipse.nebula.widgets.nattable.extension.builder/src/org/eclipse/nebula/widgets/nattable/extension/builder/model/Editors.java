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
package org.eclipse.nebula.widgets.nattable.extension.builder.model;

import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;

public class Editors {

	public static IEditor getCheckBoxEditor() {
		return new IEditor() {

			public IEditableRule getEditableRule() {
				return IEditableRule.ALWAYS_EDITABLE;
			}

			public IDataValidator getValidator() {
				return IDataValidator.ALWAYS_VALID;
			}

			public IEditor.Type getType() {
				return Type.CHECKBOX;
			}

			public ICellEditor getCellEditor() {
				return new CheckBoxCellEditor();
			}
		};
	}

	public static IEditor getComboboxEditor(final IComboBoxDataProvider dataProvider) {
		return new IEditor() {

			public IEditableRule getEditableRule() {
				return IEditableRule.ALWAYS_EDITABLE;
			}

			public IDataValidator getValidator() {
				return IDataValidator.ALWAYS_VALID;
			}

			public IEditor.Type getType() {
				return Type.COMBO;
			}

			public ICellEditor getCellEditor() {
				return new ComboBoxCellEditor(dataProvider);
			}
		};
	}

	public static IEditor getTextEditor() {
		return new IEditor() {

			public IEditableRule getEditableRule() {
				return IEditableRule.ALWAYS_EDITABLE;
			}

			public IDataValidator getValidator() {
				return IDataValidator.ALWAYS_VALID;
			}

			public IEditor.Type getType() {
				return Type.TEXT;
			}

			public ICellEditor getCellEditor() {
				return new TextCellEditor();
			}
		};
	}
}
