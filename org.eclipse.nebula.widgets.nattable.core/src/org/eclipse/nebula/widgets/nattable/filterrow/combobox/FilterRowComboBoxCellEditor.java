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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.nebula.widgets.nattable.data.convert.ConversionFailedException;
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

	private static final Log log = LogFactory.getLog(FilterRowComboBoxCellEditor.class);
	
	/**
	 * This object remembers the current value in the editor.
	 * This is necessary to avoid double commits on closing the editor again.
	 * <p>
	 * Per default editors commit their values prior to closing them. The commit
	 * call is performed by various actions that get triggered when the editor
	 * losses focus. As this editor is configured to commit the value on each
	 * click of a checkbox, we avoid committing the value again on closing.
	 */
	private Object currentCanonicalValue = null;
	
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
	
	@Override
	public void setCanonicalValue(Object canonicalValue) {
		this.currentCanonicalValue = canonicalValue;
		super.setCanonicalValue(canonicalValue);
	}
	
	@Override
	public boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit) {
		//If the editor should be closed after commit, we first need to ensure if the value
		//has changed since the last commit
		//This needs to be done because in this filter combo box, every selection immediately
		//causes a commit, which results in applying a filter. If the combo box is now closed
		//because of losing the focus, the value gets committed again, which again results
		//in filtering, which will lead to exceptions because the states are not synchronous
		//anymore.
		if (!isClosed()) {
			try {
				//always do the conversion
				Object canonicalValue = getCanonicalValue(); 
				if ((canonicalValue != null && currentCanonicalValue == null)
						|| (canonicalValue == null && currentCanonicalValue != null)
						|| (canonicalValue != null && currentCanonicalValue != null 
									&& !canonicalValue.equals(currentCanonicalValue))) {
					if (super.commit(direction, closeAfterCommit)) {
						this.currentCanonicalValue = canonicalValue;
						return true;
					}
				}
				else {
					//the values are the same so it is not necessary to commit again
					if (closeAfterCommit) {
						close();
					}
					return true;
				}
			}
			catch (ConversionFailedException e) {
				//do nothing as exceptions caused by conversion are handled already
				//we just need this catch block for stopping the process if conversion 
				//failed with an exception
			}
			catch (Exception e) {
				//if another exception occured that wasn't thrown by us, it should at least
				//be logged without killing the whole application
				log.error("Error on updating cell value: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
			}
		}
		return false;
	}
	
}
