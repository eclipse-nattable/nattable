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
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.nebula.widgets.nattable.widget.NatCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Specialisation of ComboBoxCellEditor that can only be created using an IComboBoxDataProvider.
 * Will show a multiselect combobox with checkboxes and uses the FilterNatCombo as underlying control.
 * 
 * @see FilterRowComboBoxDataProvider
 * 
 * @author Dirk Fauth
 *
 */
public class FilterRowComboBoxCellEditor extends ComboBoxCellEditor {

	/**
	 * Create a new {@link FilterRowComboBoxCellEditor} based on the given {@link IComboBoxDataProvider},
	 * showing the default number of items in the dropdown of the combo.
	 * @param dataProvider The {@link IComboBoxDataProvider} that is responsible for populating the 
	 * 			items to the dropdown box.
	 */
	public FilterRowComboBoxCellEditor(IComboBoxDataProvider dataProvider) {
		this(dataProvider, NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
	}

	/**
	 * Create a new {@link FilterRowComboBoxCellEditor} based on the given {@link IComboBoxDataProvider}.
	 * @param dataProvider The {@link IComboBoxDataProvider} that is responsible for populating the 
	 * 			items to the dropdown box. 
	 * @param maxVisibleItems The maximum number of items the drop down will show before introducing 
	 * 			a scroll bar.
	 */
	public FilterRowComboBoxCellEditor(IComboBoxDataProvider dataProvider, int maxVisibleItems) {
		super(dataProvider, maxVisibleItems);
		this.multiselect = true;
		this.useCheckbox = true;
	}

	@Override
	public NatCombo createEditorControl(Composite parent) {
		int style = SWT.READ_ONLY | SWT.MULTI | SWT.CHECK;
		final FilterNatCombo combo = this.iconImage == null ? 
				new FilterNatCombo(parent, this.cellStyle, this.maxVisibleItems, style)
				: new FilterNatCombo(parent, this.cellStyle, this.maxVisibleItems, style, this.iconImage);
			
		combo.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));

		combo.setMultiselectValueSeparator(this.multiselectValueSeparator);
		combo.setMultiselectTextBracket(this.multiselectTextPrefix, this.multiselectTextSuffix);
		
		addNatComboListener(combo);
		
		//additionally add the ICheckStateListener so on changing the value of the select all
		//item the change is also committed
		combo.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				commit(MoveDirectionEnum.NONE, (!multiselect && editMode == EditModeEnum.INLINE));
			}
		});
		
		return combo;
	}
}
