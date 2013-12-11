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
package org.eclipse.nebula.widgets.nattable.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Customized combobox control that supports editing directly in the text field 
 * and selecting items from the dropdown.
 * 
 * <p>This control supports the ability for multi select in the dropdown of the combo
 * which is not available for the SWT Combo control. This feature was added with
 * Nebula NatTable 1.0.0
 * 
 * <p>
 * The following style bits are supported by this control.
 * @see SWT#BORDER (if a border should be added to the Text control)
 * @see SWT#READ_ONLY (default for Text control, if this is missing, the Text control can be edited)
 * @see SWT#CHECK (if the items in the combo should be showed with checkboxes)
 * @see SWT#MULTI (if multi selection is allowed)
 */
public class NatCombo extends Composite {

	/**
	 * Default String that is used to separate values in the String representation showed
	 * in the text control if multiselect is supported.
	 */
	public static final String DEFAULT_MULTI_SELECT_VALUE_SEPARATOR = ", "; //$NON-NLS-1$
	/**
	 * Default String that is used to prefix the generated String representation showed
	 * in the text control if multiselect is supported.
	 */
	public static final String DEFAULT_MULTI_SELECT_PREFIX = "["; //$NON-NLS-1$
	/**
	 * String that is used to suffix the generated String representation showed
	 * in the text control if multiselect is supported.
	 */
	public static final String DEFAULT_MULTI_SELECT_SUFFIX = "]"; //$NON-NLS-1$
	/**
	 * The default number of visible items on open the combo.
	 */
	public static final int DEFAULT_NUM_OF_VISIBLE_ITEMS = 5;

	/**
	 * The IStyle that is used for rendering the Text and the combo control.
	 * The important configurations used are horizontal alignment, background and 
	 * foreground color and font.
	 */
	protected final IStyle cellStyle;

	/**
	 * The maximum number of visible items of the combo.
	 * Setting this value to -1 will result in always showing all items at once.
	 */
	protected int maxVisibleItems;

	/**
	 * The items that are showed within the combo transformed to a java.util.List.
	 * Needed for indexed operations in the dropdown
	 */
	protected java.util.List<String> itemList;

	/**
	 * The text control of this NatCombo, allowing to enter values directly.
	 */
	protected Text text;

	/**
	 * The Shell containing the dropdown of this NatCombo
	 */
	protected Shell dropdownShell;

	/**
	 * The Table control used for the combo component of this NatCombo
	 */
	protected Table dropdownTable;

	/**
	 * The image that is shown at the right edge of the text control if the NatCombo
	 * is opened.
	 */
	protected Image iconImage;

	/**
	 * The style bits that where set on creation time. Needed in case the dropdown shell
	 * was disposed and needs to be created again.
	 */
	protected final int style;
	
	/**
	 * Flag that indicates whether this ComboBoxCellEditor supports free editing in the
	 * text control of the NatCombo or not. By default free editing is disabled.
	 */
	protected boolean freeEdit;
	
	/**
	 * Flag that indicates whether this NatCombo supports multiselect or not.
	 * By default multiselect is disabled.
	 */
	protected boolean multiselect;
	
	/**
	 * Flag that indicates whether checkboxes should be shown for the items in the dropdown.
	 */
	protected boolean useCheckbox;
	
	/**
	 * String that is used to separate values in the String representation showed
	 * in the text control if multiselect is supported.
	 */
	protected String multiselectValueSeparator = DEFAULT_MULTI_SELECT_VALUE_SEPARATOR;
	/**
	 * String that is used to prefix the generated String representation showed
	 * in the text control if multiselect is supported. Needed to visualize the 
	 * multiselection to the user.
	 */
	protected String multiselectTextPrefix = DEFAULT_MULTI_SELECT_PREFIX;
	/**
	 * String that is used to suffix the generated String representation showed
	 * in the text control if multiselect is supported. Needed to visualize the 
	 * multiselection to the user. 
	 */
	protected String multiselectTextSuffix = DEFAULT_MULTI_SELECT_SUFFIX;

	/**
	 * Flag that tells whether the NatCombo has focus or not.
	 * The flag is set by the FocusListenerWrapper that is set as focus listener
	 * on both, the Text control and the dropdown table control.
	 * This flag is necessary as the NatCombo has focus if either of both
	 * controls have focus.
	 */
	private boolean hasFocus = false;
	/**
	 * Flag to determine whether the focus lost runnable is currently active or not.
	 * Necessary in case the FocusListener is removing itself on focusLost().
	 * Quite unusual in normal cases, but for NatTable editing this appears because
	 * if the control looses focus it gets destroyed in AbstractCellEditor.close()
	 * Introducing and handling this flag ensures concurrency safety.
	 */
	private boolean focusLostRunnableActive = false;
	/**
	 * The list of FocusListener that contains the listeners that will be informed
	 * if the NatCombo control gains or looses focus. We keep our own list of
	 * listeners because the two controls that are combined in this control share
	 * the same focus.
	 */
	private List<FocusListener> focusListener = new ArrayList<FocusListener>();

	/**
	 * Creates a new NatCombo using the given IStyle for rendering, showing the default number
	 * of items at once in the dropdown. Creating the NatCombo with this constructor, there is
	 * no free edit and no multiple selection enabled.
	 * @param parent A widget that will be the parent of this NatCombo
	 * @param cellStyle Style configuration containing horizontal alignment, font, foreground and 
	 * 			background color information.
	 * @param style The style for the Text Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	public NatCombo(Composite parent, IStyle cellStyle, int style) {
		this(parent, cellStyle, DEFAULT_NUM_OF_VISIBLE_ITEMS, style, GUIHelper.getImage("down_2")); //$NON-NLS-1$
	}

	/**
	 * Creates a new NatCombo using the given IStyle for rendering, showing the given amount
	 * of items at once in the dropdown. Creating the NatCombo with this constructor, there is
	 * no free edit and no multiple selection enabled.
	 * @param parent A widget that will be the parent of this NatCombo
	 * @param cellStyle Style configuration containing horizontal alignment, font, foreground and 
	 * 			background color information.
	 * @param maxVisibleItems the max number of items the drop down will show before introducing a 
	 * 			scroll bar.
	 * @param style The style for the Text Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	public NatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, int style) {
		this(parent, cellStyle, maxVisibleItems, style, GUIHelper.getImage("down_2")); //$NON-NLS-1$
	}

	/**
	 * Creates a new NatCombo using the given IStyle for rendering, showing the given amount
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
	public NatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, int style, Image iconImage) {
		super(parent, SWT.NONE);

		this.cellStyle = cellStyle;
		this.maxVisibleItems = maxVisibleItems;
		this.iconImage = iconImage;

		this.style = style;
		
		this.freeEdit = (style & SWT.READ_ONLY) == 0;
		this.multiselect = (style & SWT.MULTI) != 0;
		this.useCheckbox = (style & SWT.CHECK) != 0;

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		createTextControl(style);
		createDropdownControl(style);
		
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dropdownShell.dispose();
				text.dispose();
			}
		});
	}
	
	/**
	 * Sets the given items to be the items shown in the dropdown of this NatCombo.
	 * @param items The array of items to set.
	 */
	public void setItems(String[] items) {
		if (items != null) {
			this.itemList = Arrays.asList(items);
			if (!dropdownTable.isDisposed()) {
				for (String item : items) {
					TableItem tableItem = new TableItem(dropdownTable, SWT.NONE);
					tableItem.setText(item);
				}
			}
		}
	}
	
	/**
	 * Creates the Text control of this NatCombo, adding styles, look&amp;feel and
	 * needed listeners for the control only.
	 * @param style The style for the Text Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	protected void createTextControl(int style) {
		int widgetStyle = style | HorizontalAlignmentEnum.getSWTStyle(cellStyle);
		text = new Text(this, widgetStyle);
		text.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		text.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		text.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		text.setLayoutData(gridData);

		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.ARROW_DOWN || event.keyCode == SWT.ARROW_UP) {
					showDropdownControl();

					int selectionIndex = dropdownTable.getSelectionIndex();
					selectionIndex += event.keyCode == SWT.ARROW_DOWN ? 1 : -1;
					if (selectionIndex < 0) {
						selectionIndex = 0;
					}

					dropdownTable.select(selectionIndex);
				}
				else if (!LetterOrDigitKeyEventMatcher.isLetterOrDigit(event.character)) {
					if (freeEdit) {
						//simply clear the selection in dropdownlist so the free value in text control
						//will be used
						if (!dropdownTable.isDisposed()) {
							dropdownTable.deselectAll();
						}
					} else {
						showDropdownControl();
					}
				}
			}
		});

		text.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseDown(MouseEvent e) {
				if (!freeEdit) {
					if (dropdownTable.isDisposed() || !dropdownTable.isVisible()) {
						showDropdownControl();
					} else {
						//if there is no free edit enabled, set the focus back to the
						//dropdownlist so it handles key strokes itself
						dropdownTable.forceFocus();
					}
				}
			}
		});
		
		text.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				calculateBounds();
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				calculateBounds();
			}
		});
		
		text.addFocusListener(new FocusListenerWrapper());

		final Canvas iconCanvas = new Canvas(this, SWT.NONE) {

			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Rectangle iconImageBounds = iconImage.getBounds();
				return new Point(iconImageBounds.width + 2, iconImageBounds.height + 2);
			}

		};

		gridData = new GridData(GridData.BEGINNING, SWT.FILL, false, true);
		iconCanvas.setLayoutData(gridData);

		iconCanvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent event) {
				GC gc = event.gc;

				Rectangle iconCanvasBounds = iconCanvas.getBounds();
				Rectangle iconImageBounds = iconImage.getBounds();
				int horizontalAlignmentPadding = CellStyleUtil.getHorizontalAlignmentPadding(HorizontalAlignmentEnum.CENTER, iconCanvasBounds, iconImageBounds.width);
				int verticalAlignmentPadding = CellStyleUtil.getVerticalAlignmentPadding(VerticalAlignmentEnum.MIDDLE, iconCanvasBounds, iconImageBounds.height);
				gc.drawImage(iconImage, horizontalAlignmentPadding, verticalAlignmentPadding);

				Color originalFg = gc.getForeground();
				gc.setForeground(GUIHelper.COLOR_WIDGET_BORDER);
				gc.drawRectangle(0, 0, iconCanvasBounds.width - 1, iconCanvasBounds.height - 1);
				gc.setForeground(originalFg);
			}

		});

		iconCanvas.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				if (dropdownShell != null && !dropdownShell.isDisposed()) {
					if (dropdownShell.isVisible()) {
						text.forceFocus();
						hideDropdownControl();
					}
					else {
						showDropdownControl();
					}
				}
			}
		});
	}
	
	/**
	 * Create the dropdown control of this NatCombo, adding styles, look&amp;feel and
	 * needed listeners for the control only.
	 * @param style The style for the Table Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	protected void createDropdownControl(int style) {
		dropdownShell = new Shell(getShell(), SWT.MODELESS);
		dropdownShell.setLayout(new FillLayout());

		int dropdownListStyle = style | SWT.V_SCROLL | HorizontalAlignmentEnum.getSWTStyle(cellStyle) | SWT.FULL_SELECTION;

		dropdownTable = new Table(dropdownShell, dropdownListStyle);
		dropdownTable.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		dropdownTable.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		dropdownTable.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));
		
		dropdownTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = e.detail != SWT.CHECK;
				TableItem item = (TableItem) e.item;

				//checkbox clicked, now sync the selection
				if (!selected) {
					if (!item.getChecked()) {
						dropdownTable.deselect(itemList.indexOf(item.getText()));
					}
					else {
						dropdownTable.select(itemList.indexOf(item.getText()));
					}
				}
				//item selected, now sync checkbox
				else if (useCheckbox) {
					//after selection is performed we need to ensure that selection and checkboxes are in sync
					for (TableItem tableItem : dropdownTable.getItems()) {
						tableItem.setChecked(dropdownTable.isSelected(itemList.indexOf(tableItem.getText())));
					}
				}

				updateTextControl(false);
			}
		});
		
		dropdownTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if ((event.keyCode == SWT.CR)
						|| (event.keyCode == SWT.KEYPAD_CR)) {
					updateTextControl(true);
				}
				else if (event.keyCode == SWT.F2 && freeEdit) {
					text.forceFocus();
					hideDropdownControl();
				}
			}
		});

		dropdownTable.addFocusListener(new FocusListenerWrapper());
		
		if (this.itemList != null) {
			setItems(this.itemList.toArray(new String[] {}));
		}
		setDropdownSelection(getTextAsArray());
	}
	
	/**
	 * This method will be called if an item of the dropdown control is selected
	 * via mouse click or pressing enter. It will populate the text control with
	 * the information gathered out of the selection in the dropdown control and 
	 * hide the dropdown if necessary.
	 * @param hideDropdown <code>true</code> if the dropdown should be hidden 
	 * 			after updating the text control
	 */
	protected void updateTextControl(boolean hideDropdown) {
		text.setText(getTransformedTextForSelection());
		if (hideDropdown) {
			hideDropdownControl();
		}
	}
	
	/**
	 * Shows the dropdown of this NatCombo.
	 * Will always calculate the size of the dropdown regarding
	 * the current size of the Text control.
	 */
	public void showDropdownControl() {
		showDropdownControl(false);
	}
	
	/**
	 * Shows the dropdown of this NatCombo.
	 * Will always calculate the size of the dropdown regarding
	 * the current size of the Text control.
	 * @param focusOnText <code>true</code> if the focus should be set
	 * 			to the text control instead of the dropdown after
	 * 			opening the dropdown.
	 */
	public void showDropdownControl(boolean focusOnText) {
		if (dropdownShell.isDisposed()) {
			createDropdownControl(this.style);
		}
		calculateBounds();
		dropdownShell.open();
		if (focusOnText) {
			this.text.forceFocus();
			this.text.setSelection(this.text.getText().length());
		}
	}

	/**
	 * Hide the dropdown of this NatCombo.
	 */
	public void hideDropdownControl() { 	
        if (!dropdownShell.isDisposed()) {
            dropdownShell.setVisible(false);
        } 	 
	}

	/**
	 * Calculates the number of items that should be showed in the dropdown at once.
	 * It is needed to calculate the height of the dropdown.
	 * If maxVisibleItems is configured -1, this method always returns the number
	 * of items in the list. Otherwise if will return the configured maximum number
	 * of items to be visible at once or less if there are less than the configured
	 * maximum.
	 * @return the number of items that should be showed in the dropdown at once.
	 */
	protected int getVisibleItemCount() {
		int itemCount = dropdownTable.getItemCount();
		if (itemCount > 0) {
			//if maxVisibleItems == -1 show all items at once
			//otherwise use the minimum for item count or max visible item configuration
			int visibleItemCount = itemCount;
			if (this.maxVisibleItems > 0) {
				visibleItemCount = Math.min(itemCount, maxVisibleItems);
			}
			itemCount = visibleItemCount;
		}
		return itemCount;
	}
	
	/**
	 * Calculates the size and location of the Shell that represents the dropdown control
	 * of this NatCombo. Size and location will be calculated dependent the position and
	 * size of the corresponding Text control and the information showed in the dropdown.
	 */
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
			int listWidth = dropdownTable.computeSize(SWT.DEFAULT, listHeight, true).x;
			if (listWidth < size.x) {
				listWidth = size.x;
			}
			dropdownTable.setSize(listWidth, listHeight);
			
			Point textPosition = text.toDisplay(text.getLocation());
			
			//by default the dropdown shell will be created below the cell in the table
			int dropdownShellStartingY = textPosition.y + text.getBounds().height;
			int shellBottomY = textPosition.y + text.getBounds().height + listHeight;
			//if the bottom of the drowdown is below the display, render it above the cell
			if (shellBottomY > Display.getCurrent().getBounds().height) {
				dropdownShellStartingY = textPosition.y - listHeight;
			}
			
			dropdownShell.setBounds(
					textPosition.x, 
					dropdownShellStartingY, 
					listWidth, 
					listHeight);
		}
	}

	/**
	 * Returns the zero-relative index of the item which is currently
	 * selected in the receiver, or -1 if no item is selected.
	 * <p>
	 * Note that this only returns useful results if this NatCombo supports
	 * single selection or only one item is selected.
	 * 
	 * @return the index of the selected item or -1
	 */
	public int getSelectionIndex() {
		if (!this.dropdownTable.isDisposed()) {
			return this.dropdownTable.getSelectionIndex();
		} else if (!this.text.isDisposed()) {
			return this.itemList.indexOf(this.text.getText());
		}
		return -1;
	}

	/**
	 * Returns the zero-relative indices of the items which are currently
	 * selected in the receiver.  The order of the indices is unspecified.
	 * The array is empty if no items are selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver
	 * to maintain its selection, so modifying the array will
	 * not affect the receiver. 
	 * </p>
	 * @return the array of indices of the selected items
	 */
	public int[] getSelectionIndices() {
		if (!this.dropdownTable.isDisposed()) {
			return this.dropdownTable.getSelectionIndices();
		} else {
			String[] selectedItems = getTextAsArray();
			int[] result = new int[selectedItems.length];
			for (int i = 0; i < selectedItems.length; i++) {
				result[i] = this.itemList.indexOf(selectedItems[i]);
			}
			return result;
		}
	}

	/**
	 * Returns the number of selected items contained in the receiver.
	 *
	 * @return the number of selected items
	 */
	public int getSelectionCount() {
		if (!this.dropdownTable.isDisposed()) {
			return this.dropdownTable.getSelectionCount();
		} else {
			return getTextAsArray().length;
		}
	}
	
	/**
	 * Returns an array of <code>String</code>s that are currently
	 * selected in the receiver.  The order of the items is unspecified.
	 * An empty array indicates that no items are selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver
	 * to maintain its selection, so modifying the array will
	 * not affect the receiver. 
	 * </p>
	 * @return an array representing the selection
	 */
	public String[] getSelection() {
		String[] result = getTransformedSelection();
		if (result == null || (result.length == 0 && this.text.getText().length() > 0)) { 
			result = getTextAsArray();
		}
		return result;
	}

	/**
	 * Selects the items at the given zero-relative indices in the receiver.
	 * The current selection is cleared before the new items are selected.
	 * <p>
	 * Indices that are out of range and duplicate indices are ignored.
	 * If the receiver is single-select and multiple indices are specified,
	 * then all indices are ignored.
	 * <p>
	 * The text control of this NatCombo will also be updated with the new
	 * selected values.
	 *
	 * @param items the items to select
	 */
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
		this.text.setText(textValue);
	}
	
	/**
	 * Selects the item at the given zero-relative index in the receiver's 
	 * list.  If the item at the index was already selected, it remains
	 * selected. Indices that are out of range are ignored.
	 *
	 * @param index the index of the item to select
	 */
	public void select(int index) {
		if (!this.dropdownTable.isDisposed()) {
			this.dropdownTable.select(index);
			this.text.setText(getTransformedTextForSelection());
		} else if (index >= 0) {
			this.text.setText(this.itemList.get(index));
		}
	}

	/**
	 * Selects the items at the given zero-relative indices in the receiver.
	 * The current selection is not cleared before the new items are selected.
	 * <p>
	 * If the item at a given index is not selected, it is selected.
	 * If the item at a given index was already selected, it remains selected.
	 * Indices that are out of range and duplicate indices are ignored.
	 * If the receiver is single-select and multiple indices are specified,
	 * then all indices are ignored.
	 *
	 * @param indices the array of indices for the items to select
	 */
	public void select(int[] indeces) {
		if (!this.dropdownTable.isDisposed()) {
			this.dropdownTable.select(indeces);
			this.text.setText(getTransformedTextForSelection());
		} else {
			String[] selectedItems = new String[indeces.length];
			for (int i = 0; i < indeces.length; i++) {
				if (indeces[i] >= 0) {
					selectedItems[i] = this.itemList.get(indeces[i]);
				}
			}
			this.text.setText(getTransformedText(selectedItems));
		}
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		if (this.text != null && !this.text.isDisposed())
			this.text.addKeyListener(listener);
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.addKeyListener(listener);
	}

	@Override
	public void removeKeyListener(KeyListener listener) {
		if (this.text != null && !this.text.isDisposed())
			this.text.removeKeyListener(listener);
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.removeKeyListener(listener);
	}

	@Override
	public void addTraverseListener(TraverseListener listener) {
		if (this.text != null && !this.text.isDisposed())
			this.text.addTraverseListener(listener);
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.addTraverseListener(listener);
	}

	@Override
	public void removeTraverseListener(TraverseListener listener) {
		if (this.text != null && !this.text.isDisposed())
			this.text.removeTraverseListener(listener);
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.removeTraverseListener(listener);
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		//only add the mouse listener to the dropdown, as clicking in the text control
		//should not trigger anything else than it is handled by the text control itself.
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.addMouseListener(listener);
	}

	@Override
	public void removeMouseListener(MouseListener listener) {
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.removeMouseListener(listener);
	}
	
	@Override
	public void notifyListeners(int eventType, Event event) {
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.notifyListeners(eventType, event);
	}
	
	public void addSelectionListener(SelectionListener listener) {
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.addSelectionListener(listener);
	}
	
	public void removeSelectionListener(SelectionListener listener) {
		if (this.dropdownTable != null && !this.dropdownTable.isDisposed())
			this.dropdownTable.removeSelectionListener(listener);
	}
	
	public void addShellListener(ShellListener listener) {
		if (this.dropdownShell != null && !this.dropdownShell.isDisposed())
			this.dropdownShell.addShellListener(listener);
	}
	
	public void removeShellListener(ShellListener listener) {
		if (this.dropdownShell != null && !this.dropdownShell.isDisposed())
			this.dropdownShell.removeShellListener(listener);
	}
	
	public void addTextControlListener(ControlListener listener) {
		if (this.text != null && !this.text.isDisposed())
			this.text.addControlListener(listener);
	}
	
	public void removeTextControlListener(ControlListener listener) {
		if (this.text != null && !this.text.isDisposed())
			this.text.removeControlListener(listener);
	}
	
	@Override
	public boolean isFocusControl() {
		return hasFocus;
	}
	
	@Override
	public boolean forceFocus() {
		return text.forceFocus();
	}
	
	@Override
	public void addFocusListener(FocusListener listener) {
		this.focusListener.add(listener);
	}
	
	@Override
	public void removeFocusListener(final FocusListener listener) {
		//The FocusListenerWrapper is executing the focusLost event
		//in a separate thread with 100ms delay to ensure that the NatComboe
		//lost focus. This is necessary because the NatCombo is a combination
		//of a text field and a table as dropdown which do not share the
		//same focus by default.
		//To avoid concurrent modifications, in case the focus lost runnable
		//is active, the removal of the focus listener is processed in a
		//new thread after the focus lost runnable is done.
		if (focusLostRunnableActive) {
			try {
				new Thread() {
					@Override
					public void run() {
						focusListener.remove(listener);
					};
				}.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else {
			focusListener.remove(listener);
		}
	}
	
	/**
	 * Transforms the selection in the Table control dropdown into a String[].
	 * Doing this is necessary to provide a SWT List like interface regarding 
	 * selections for the NatCombo.
	 * @return Array containing all selected TableItem text attributes
	 */
	protected String[] getTransformedSelection() {
		String[] selection = null;
		if (!this.dropdownTable.isDisposed()) {
			TableItem[] selectedItems = this.dropdownTable.getSelection();
			selection = new String[selectedItems.length];
			for (int i = 0; i < selectedItems.length; i++) {
				selection[i] = selectedItems[i].getText();
			}
		}
		return selection;
	}
	
	/**
	 * Transforms the given String array whose contents represents selected items
	 * to a selection that can be handled by the underlying Table control in the
	 * dropdown.
	 * 
	 * @param selection The Strings that represent the selected items
	 */
	protected void setDropdownSelection(String[] selection) {
		if (selection.length > 0) {
			java.util.List<String> selectionList = Arrays.asList(selection); 
			java.util.List<TableItem> selectedItems = new ArrayList<TableItem>();
			for (TableItem item : this.dropdownTable.getItems()) {
				if (selectionList.contains(item.getText())) {
					selectedItems.add(item);
					if (useCheckbox) {
						item.setChecked(true);
					}
				}
			}
			this.dropdownTable.setSelection(selectedItems.toArray(new TableItem[] {}));
		}
	}
	
	/**
	 * Will transform the text for the Text control of this NatCombo to an
	 * array of Strings. This is necessary for the multiselect feature.
	 * 
	 * <p>Note that by default the multiselect String is specified to show with
	 * enclosing [] brackets and values separated by ", ". If you need to change
	 * this you need to set the corresponding values in this NatCombo.
	 * 
	 * @return The text for the Text control of this NatCombo converted to
	 * 			an array of Strings.
	 */
	protected String[] getTextAsArray() {
		if (!this.text.isDisposed()) {
			String transform = this.text.getText();
			if (transform.length() > 0) {
				if (this.multiselect) {
					//for multiselect the String is defined by default in format [a, b, c]
					//the prefix and suffix for multiselect String representation need to 
					//be removed
					//in free edit mode we need to check if the format is used
					int prefixLength = this.multiselectTextPrefix.length();
					int suffixLength = this.multiselectTextSuffix.length();
					if (this.freeEdit) {
						if (!transform.startsWith(multiselectTextPrefix)) {
							prefixLength = 0;
						}
						if (!transform.endsWith(multiselectTextSuffix)) {
							suffixLength = 0;
						}
					}
					transform = transform.substring(prefixLength, transform.length()-suffixLength);
				}
				//if the transform value length is still > 0, then try to split
				if (transform.length() > 0) {
					return transform.split(this.multiselectValueSeparator);
				}
			}
		}
		return new String[] {};
	}
	
	/**
	 * Transforms the selection of the dropdown to a text representation that can
	 * be added to the text control of this combo.
	 * 
	 * <p>Note that by default the multiselect String is specified to show with
	 * enclosing [] brackets and values separated by ", ". If you need to change
	 * this you need to set the corresponding values in this NatCombo.
	 * 
	 * @return String representation for the selection within the combo.
	 */
	protected String getTransformedTextForSelection() {
		String result = ""; //$NON-NLS-1$
		String[] selection = getTransformedSelection();
		if (selection != null) {
			result = getTransformedText(selection);
		}
		return result;
	}
	
	/**
	 * Transforms the given array of Strings to a text representation that
	 * can be added to the text control of this combo.
	 * <p>
	 * If this NatCombo is only configured to support single selection, than
	 * only the first value in the array will be processed. Otherwise the
	 * result will be processed by concatenating the values.
	 * <p>
	 * Note that by default the multiselect String is specified to show with
	 * enclosing [] brackets and values separated by ", ". If you need to change
	 * this you need to set the corresponding values in this NatCombo.
	 * 
	 * @param values The values to build the text representation from.
	 * @return String representation for the selection within the combo.
	 */
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
			result = this.multiselectTextPrefix + result + this.multiselectTextSuffix;
		}
		else if (values.length > 0) {
			result = values[0];
		}
		return result;
	}

	/**
	 * @param multiselectValueSeparator String that should be used to separate values in the 
	 * 			String representation showed in the text control if multiselect is supported.
	 * 			<code>null</code> to use the default value separator.
	 * @see NatCombo#DEFAULT_MULTI_SELECT_VALUE_SEPARATOR
	 */
	public void setMultiselectValueSeparator(String multiselectValueSeparator) {
		if (multiselectValueSeparator == null) {
			this.multiselectValueSeparator = DEFAULT_MULTI_SELECT_VALUE_SEPARATOR;
		} else {
			this.multiselectValueSeparator = multiselectValueSeparator;
		}
	}

	/**
	 * Set the prefix and suffix that will parenthesize the text that is created out of
	 * the selected values if this NatCombo supports multiselection.
	 * @param multiselectTextPrefix String that should be used to prefix the generated String 
	 * 			representation showed in the text control if multiselect is supported.
	 * 			<code>null</code> to use the default prefix.
	 * @param multiselectTextSuffix String that should be used to suffix the generated String 
	 * 			representation showed in the text control if multiselect is supported.
	 * 			<code>null</code> to use the default suffix.
	 * @see NatCombo#DEFAULT_MULTI_SELECT_PREFIX
	 * @see NatCombo#DEFAULT_MULTI_SELECT_SUFFIX
	 */
	public void setMultiselectTextBracket(String multiselectTextPrefix, String multiselectTextSuffix) {
		if (multiselectTextPrefix == null) {
			this.multiselectTextPrefix = DEFAULT_MULTI_SELECT_PREFIX;
		}
		else {
			this.multiselectTextPrefix = multiselectTextPrefix;
		}
		
		if (multiselectTextSuffix == null) {
			this.multiselectTextSuffix = DEFAULT_MULTI_SELECT_SUFFIX;
		}
		else {
			this.multiselectTextSuffix = multiselectTextSuffix;
		}
	}

	
	/**
	 * FocusListener that is used to ensure that the Text control and the dropdown
	 * table control are sharing the same focus. If either of both controls looses
	 * focus, the local focus flag is set to false and a delayed background thread 
	 * for focus lost is started. If the other control gains focus, the local focus
	 * flag is set to true which skips the execution of the delayed background thread.
	 * This means the NatCombo hasn't lost focus.
	 *  
	 * @author Dirk Fauth
	 *
	 */
	class FocusListenerWrapper implements FocusListener {
		
		@Override
		public void focusLost(final FocusEvent e) {
			hasFocus = false;
			Display.getCurrent().timerExec(100, new Runnable() {
				@Override
				public void run() {
					if (!hasFocus) {
						focusLostRunnableActive = true;
						for (FocusListener f : focusListener) {
							f.focusLost(e);
						}
						focusLostRunnableActive = false;
					}
				}
			});
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			hasFocus = true;
			for (FocusListener f : focusListener) {
				f.focusGained(e);
			}
		}
	}

}
