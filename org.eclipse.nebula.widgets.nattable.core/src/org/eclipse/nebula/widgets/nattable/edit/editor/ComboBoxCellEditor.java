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

import java.util.ArrayList;
import java.util.List;


import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.nebula.widgets.nattable.widget.NatCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Renders an SWT combo box.
 *    Users can select from the drop down or enter their own values.
 */
public class ComboBoxCellEditor extends AbstractCellEditor {

	private NatCombo combo;

	private int maxVisibleItems = 10;
	
	private List<?> canonicalValues;
	private IComboBoxDataProvider dataProvider;
	
	private Object originalCanonicalValue;
	
	/**
	 * @see this{@link #ComboBoxCellEditor(List, int)}
	 */
	public ComboBoxCellEditor(List<?> canonicalValues){
		this(canonicalValues, NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
	}

	/**
	 * @param canonicalValues Array of items to be shown in the drop down box. These will be
	 * 	converted using the {@link IDisplayConverter} for display purposes
	 * @param maxVisibleItems the max items the drop down will show before introducing a scroll bar.
	 */
	public ComboBoxCellEditor(List<?> canonicalValues, int maxVisibleItems) {
		this.canonicalValues = canonicalValues;
		this.maxVisibleItems = maxVisibleItems;
	}

	/**
	 * @see this{@link #ComboBoxCellEditor(List, int)}
	 */
	public ComboBoxCellEditor(IComboBoxDataProvider dataProvider) {
		this(dataProvider, NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
	}

	/**
	 * @param canonicalValues Array of items to be shown in the drop down box. These will be
	 * 	converted using the {@link IDisplayConverter} for display purposes
	 * @param maxVisibleItems the max items the drop down will show before introducing a scroll bar.
	 */
	public ComboBoxCellEditor(IComboBoxDataProvider dataProvider, int maxVisibleItems) {
		this.dataProvider = dataProvider;
		this.maxVisibleItems = maxVisibleItems;
	}
	
	public NatCombo getCombo() {
		return combo;
	}

	@Override
	protected Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue) {
		this.originalCanonicalValue = originalCanonicalValue;

		combo = new NatCombo(parent, getCellStyle(), maxVisibleItems);
		
		combo.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));

		combo.setEditMode(editMode);
		
		combo.setItems(getDisplayValues());

		if (originalCanonicalValue != null) {
			combo.setSelection(new String[] { getDisplayValue() });
		}

		combo.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if ((event.keyCode == SWT.CR && event.stateMask == 0)
						|| (event.keyCode == SWT.KEYPAD_CR && event.stateMask == 0)) {
					commit(MoveDirectionEnum.NONE);
				} else if (event.keyCode == SWT.ESC && event.stateMask == 0){
					close();
				}
			}

		});

		combo.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent event) {
				if (event.keyCode == SWT.TAB && event.stateMask == SWT.SHIFT) {
					commit(MoveDirectionEnum.LEFT);
				} else if (event.keyCode == SWT.TAB && event.stateMask == 0) {
					commit(MoveDirectionEnum.RIGHT);
				}
			}

		});

		combo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (editMode == EditModeEnum.INLINE) {
					commit(MoveDirectionEnum.NONE);
				}
				else if (editMode == EditModeEnum.MULTI) {
					combo.hideDropdownControl();
				}
			}
		});

		if (editMode == EditModeEnum.INLINE) {
			combo.addShellListener(new ShellAdapter() {
				@Override
				public void shellClosed(ShellEvent e) {
						close();
				}
			});
		}
		
		if (editMode == EditModeEnum.MULTI) {
			combo.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					combo.hideDropdownControl();
				}
			});
		}
		
		return combo;
	}

	public Object getCanonicalValue() {
		int selectionIndex = combo.getSelectionIndex();

		//Item selected from list
		if (selectionIndex >= 0) {
			if (dataProvider != null) {
				return dataProvider.getValues(getColumnIndex(), getRowIndex()).get(selectionIndex);
			} else {
				return canonicalValues.get(selectionIndex);
			}
		} else {
			return originalCanonicalValue;
		}
	}

	public void select(int index){
		combo.select(index);
	}

	public void setCanonicalValue(Object value) {
		//No op - combo is not dynamic
	}

	@Override
	public void close() {
		super.close();

		if (combo != null && !combo.isDisposed()) {
			combo.dispose();
		}
	}

	private String getDisplayValue() {
		return (String) getDataTypeConverter().canonicalToDisplayValue(layerCell, configRegistry, originalCanonicalValue);
	}

	private String[] getDisplayValues() {
		List<String> displayValues = new ArrayList<String>();

		List<?> values;
		if (dataProvider != null) {
			values = dataProvider.getValues(getColumnIndex(), getRowIndex());
		} else {
			values = canonicalValues;
		}
		
		for (Object canonicalValue : values) {
			displayValues.add((String) getDataTypeConverter().canonicalToDisplayValue(layerCell, configRegistry, canonicalValue));
		}

		return displayValues.toArray(ArrayUtil.STRING_TYPE_ARRAY);
	}
}
