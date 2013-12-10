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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.widget.NatCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Specialisation of NatCombo which doesn't populate the selected values to the Text control.
 * Instead the String representation of the selected values are stored in a local member.
 * Doing this the selection is only visible in the dropdown via selection/check state of the
 * contained items.
 * <p>
 * Usually this combo will be created with the SWT.CHECK style bit. This way the selected items
 * are visualized by showing checked checkboxes. Also adds a <i>Select All</i> item for convenience
 * that de-/selects all items on click.
 * 
 * @author Dirk Fauth
 *
 */
public class FilterNatCombo extends NatCombo {

	/**
	 * The viewer that contains the select all item in the dropdown control.
	 */
	private CheckboxTableViewer selectAllItemViewer;
	/**
	 * The local selection String storage.
	 */
	private String filterText;
	
	/**
	 * Creates a new FilterNatCombo using the given IStyle for rendering, showing the default number
	 * of items at once in the dropdown. 
	 * @param parent A widget that will be the parent of this NatCombo
	 * @param cellStyle Style configuration containing horizontal alignment, font, foreground and 
	 * 			background color information.
	 * @param style The style for the Text Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	public FilterNatCombo(Composite parent, IStyle cellStyle, int style) {
		this(parent, cellStyle, DEFAULT_NUM_OF_VISIBLE_ITEMS, style, GUIHelper.getImage("down_2")); //$NON-NLS-1$
	}

	/**
	 * Creates a new FilterNatCombo using the given IStyle for rendering, showing the given amount
	 * of items at once in the dropdown. 
	 * @param parent A widget that will be the parent of this NatCombo
	 * @param cellStyle Style configuration containing horizontal alignment, font, foreground and 
	 * 			background color information.
	 * @param maxVisibleItems the max number of items the drop down will show before introducing a 
	 * 			scroll bar.
	 * @param style The style for the Text Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	public FilterNatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, int style) {
		this(parent, cellStyle, maxVisibleItems, style, GUIHelper.getImage("down_2")); //$NON-NLS-1$
	}

	/**
	 * Creates a new FilterNatCombo using the given IStyle for rendering, showing the given amount
	 * of items at once in the dropdown. 
	 * @param parent A widget that will be the parent of this NatCombo
	 * @param cellStyle Style configuration containing horizontal alignment, font, foreground and 
	 * 			background color information.
	 * @param maxVisibleItems the max number of items the drop down will show before introducing a 
	 * 			scroll bar.
	 * @param style The style for the {@link Text} Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 * @param iconImage The image to use as overlay to the {@link Text} Control if the dropdown
	 * 			is visible. Using this image will indicate that the control is an open combo
	 * 			to the user.
	 */
	public FilterNatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, int style, Image iconImage) {
		super(parent, cellStyle, maxVisibleItems, style, iconImage);
	}
	
	@Override
	protected void calculateBounds() {
		if (dropdownShell != null && !dropdownShell.isDisposed()) {
			Point size = getSize();
			//calculate the height by multiplying the number of visible items with
			//the item height of items in the list and adding 2 to work around a
			//calculation error regarding the descent of the font metrics for the 
			//last shown item
			//Note: if there are no items to show in the combo, calculate with the item count of
			//		3 so an empty combo will open
			int listHeight = (getVisibleItemCount() > 0 ? getVisibleItemCount() : 3) * dropdownTable.getItemHeight() + 2;
			int listWidth = Math.max(dropdownTable.computeSize(SWT.DEFAULT, listHeight, true).x, size.x);

			int viewerHeight = selectAllItemViewer.getTable().getItemHeight();
			listWidth = Math.max(selectAllItemViewer.getTable().computeSize(SWT.DEFAULT, viewerHeight, true).x, listWidth);
			
			dropdownTable.setSize(listWidth, listHeight);
			selectAllItemViewer.getTable().setSize(listWidth, viewerHeight);
			
			Point textPosition = text.toDisplay(text.getLocation());
			
			dropdownShell.setBounds(
					textPosition.x, 
					textPosition.y + text.getBounds().height, 
					listWidth, 
					listHeight + viewerHeight);
		}
	}
	
	@Override
	protected void createDropdownControl(int style) {
		super.createDropdownControl(style);
		
		FormLayout layout = new FormLayout();
		layout.spacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		dropdownShell.setLayout(layout);
		
		int dropdownListStyle = style | SWT.V_SCROLL | HorizontalAlignmentEnum.getSWTStyle(cellStyle) | SWT.FULL_SELECTION;
		this.selectAllItemViewer = CheckboxTableViewer.newCheckList(this.dropdownShell, dropdownListStyle);
		
		FormData data = new FormData();
		data.top = new FormAttachment(dropdownShell, 0, SWT.TOP);
		data.left = new FormAttachment(dropdownShell, 0, SWT.LEFT);
		data.right = new FormAttachment(dropdownShell, 0, SWT.RIGHT);
		this.selectAllItemViewer.getTable().setLayoutData(data);
		
		data = new FormData();
		//need to set the top attachment like this because attaching it to the viewer does some wrong calculations
		data.top = new FormAttachment(dropdownShell, this.selectAllItemViewer.getTable().getItemHeight(), SWT.TOP);
		data.left = new FormAttachment(dropdownShell, 0, SWT.LEFT);
		data.right = new FormAttachment(dropdownShell, 0, SWT.RIGHT);
		dropdownTable.setLayoutData(data);
		
		this.selectAllItemViewer.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
			
			@Override
			public void dispose() { }
			
			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement) {
				return ((Collection<String>)inputElement).toArray(); 
			}
		});
		
		this.selectAllItemViewer.setLabelProvider(new ILabelProvider() {
			
			@Override
			public void removeListener(ILabelProviderListener listener) { }
			
			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			
			@Override
			public void dispose() { }
			
			@Override
			public void addListener(ILabelProviderListener listener) { }
			
			@Override
			public String getText(Object element) {
				return element.toString();
			}
			
			@Override
			public Image getImage(Object element) {
				return null;
			}
		});

		final String selectAllLabel = Messages.getString("FilterNatCombo.selectAll"); //$NON-NLS-1$
		List<String> input = new ArrayList<String>();
		input.add(selectAllLabel);
		this.selectAllItemViewer.setInput(input);

		this.selectAllItemViewer.getTable().setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		this.selectAllItemViewer.getTable().setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		this.selectAllItemViewer.getTable().setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));
		
		this.selectAllItemViewer.getTable().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				showDropdownControl();
			}
		});
		
		this.selectAllItemViewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				//if the select all item is clicked directly, the grayed state needs to be set to false
				selectAllItemViewer.setGrayed(selectAllLabel, false);
				
				if (event.getChecked()) {
					//select all
					dropdownTable.selectAll();
				}
				else {
					//deselect all
					dropdownTable.deselectAll();
				}
				
				//after selection is performed we need to ensure that selection and checkboxes are in sync
				for (TableItem tableItem : dropdownTable.getItems()) {
					tableItem.setChecked(dropdownTable.isSelected(itemList.indexOf(tableItem.getText())));
				}

				updateTextControl(!multiselect);
			}
		});
		
		//set an ICheckStateProvider that sets the checkbox state of the select all checkbox regarding
		//the selection of the items in the dropdown
		this.selectAllItemViewer.setCheckStateProvider(new ICheckStateProvider() {
			
			@Override
			public boolean isGrayed(Object element) {
				if (dropdownTable.getSelectionCount() == dropdownTable.getItemCount()) {
					return false;
				}
				return true;
			}
			
			@Override
			public boolean isChecked(Object element) {
				if (dropdownTable.getSelectionCount() == 0) {
					return false;
				}
				return true;
			}
		});
		
		//add a selection listener to the items that simply refreshes the select all checkbox
		this.dropdownTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAllItemViewer.refresh();
			}
		});
	}
	
	@Override
	protected void setDropdownSelection(String[] selection) {
		super.setDropdownSelection(selection);
		if (selectAllItemViewer != null)
			selectAllItemViewer.refresh();
	}
	
	/**
	 * Add an ICheckStateListener to the viewer of the dropdown that contains the
	 * select all item. Needed so the editor is able to commit after the click on the
	 * select all checkbox is performed.
	 * @param listener The listener to add to the select all item
	 */
	public void addCheckStateListener(ICheckStateListener listener) {
		this.selectAllItemViewer.addCheckStateListener(listener);
	}
	
	@Override
	protected void updateTextControl(boolean hideDropdown) {
		this.filterText = getTransformedTextForSelection();
		if (hideDropdown) {
			hideDropdownControl();
		}
	}
	
	@Override
	public int getSelectionIndex() {
		if (!this.dropdownTable.isDisposed()) {
			return this.dropdownTable.getSelectionIndex();
		} else if (this.filterText != null && this.filterText.length() > 0) {
			return this.itemList.indexOf(this.filterText);
		}
		return -1;
	}
	
	@Override
	public String[] getSelection() {
		String[] result = getTransformedSelection();
		if (result == null || (result.length == 0 && this.filterText != null)) {
			result = getTextAsArray();
		}
		return result;
	}
	
	@Override
	public void setSelection(String[] items) {
		String textValue = ""; //$NON-NLS-1$
		if (items != null) {
			if (!this.dropdownTable.isDisposed()) {
				setDropdownSelection(items);
				if (this.freeEdit && this.dropdownTable.getSelectionCount() == 0) {
					textValue = getTransformedText(items);
				} else {
					textValue = getTransformedTextForSelection();
				}
			} else {
				textValue = getTransformedText(items);
			}
		}
		this.filterText = textValue;
	}
	
	@Override
	public void select(int index) {
		if (!this.dropdownTable.isDisposed()) {
			this.dropdownTable.select(index);
			this.filterText = getTransformedTextForSelection();
		} else if (index >= 0) {
			this.filterText = this.itemList.get(index);
		}
	}
	
	@Override
	public void select(int[] indeces) {
		if (!this.dropdownTable.isDisposed()) {
			this.dropdownTable.select(indeces);
			this.filterText = getTransformedTextForSelection();
		} else {
			String[] selectedItems = new String[indeces.length];
			for (int i = 0; i < indeces.length; i++) {
				if (indeces[i] >= 0) {
					selectedItems[i] = this.itemList.get(indeces[i]);
				}
			}
			this.filterText = getTransformedText(selectedItems);
		}
	}
	
	@Override
	protected String getTransformedText(String[] values) {
		String result = ""; //$NON-NLS-1$
		if (this.multiselect) {
			for (int i = 0; i < values.length; i++) {
				String selection = values[i];
				result += selection;
				if ((i+1) < values.length) {
					result += this.multiselectValueSeparator;
				}
			}
			//if at least one value was selected, add the prefix and suffix
			//we check the values array instead of the result length because there can be also
			//an empty String be selected
			if (values.length > 0) {
				result = this.multiselectTextPrefix + result + this.multiselectTextSuffix;
			}
		}
		else if (values.length > 0) {
			result = values[0];
		}
		return result;
	}
	
	@Override
	protected String[] getTextAsArray() {
		if (this.filterText != null && this.filterText.length() > 0) {
			String transform = this.filterText;
			int prefixLength = this.multiselectTextPrefix.length();
			int suffixLength = this.multiselectTextSuffix.length();
			transform = transform.substring(prefixLength, transform.length()-suffixLength);
			return transform.split(this.multiselectValueSeparator);
		}
		return new String[] {};
	}
}
