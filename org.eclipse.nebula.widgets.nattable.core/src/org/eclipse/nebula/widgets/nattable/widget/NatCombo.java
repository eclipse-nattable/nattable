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
package org.eclipse.nebula.widgets.nattable.widget;

import java.util.Arrays;

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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Customized combobox control that supports editing directly in the text field 
 * and selecting items from the dropdown.
 * 
 * <p>This control supports the ability for multi select in the dropdown of the combo
 * which is not available for the SWT Combo control. This feature was added with
 * Nebula NatTable 1.0.0
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
	private final IStyle cellStyle;

	/**
	 * The maximum number of visible items of the combo.
	 * Setting this value to -1 will result in always showing all items at once.
	 */
	private int maxVisibleItems;

	/**
	 * The items that are showed within the combo.
	 */
	private String[] items;

	/**
	 * The items that are showed within the combo transformed to a java.util.List.
	 * Needed for indexed operations in the dropdown
	 */
	private java.util.List<String> itemList;

	/**
	 * The text control of this NatCombo, allowing to enter values directly.
	 */
	private Text text;

	/**
	 * The Shell containing the dropdown of this NatCombo
	 */
	private Shell dropdownShell;

	/**
	 * The List control used for the combo component of this NatCombo
	 */
	private List dropdownList;

	/**
	 * The image that is shown at the right edge of the text control if the NatCombo
	 * is opened.
	 */
	private Image iconImage;

	/**
	 * Flag that indicates whether this ComboBoxCellEditor supports free editing in the
	 * text control of the NatCombo or not. By default free editing is disabled.
	 */
	private boolean freeEdit;
	
	/**
	 * Flag that indicates whether this NatCombo supports multiselect or not.
	 * By default multiselect is disabled.
	 */
	private boolean multiselect;
	
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
		this(parent, cellStyle, DEFAULT_NUM_OF_VISIBLE_ITEMS, style);
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
		this(parent, cellStyle, maxVisibleItems, false, style);
	}

	/**
	 * Creates a new NatCombo using the given IStyle for rendering, showing the given amount
	 * of items at once in the dropdown. Creating the NatCombo with this constructor, there is
	 * no multiple selection enabled.
	 * @param parent A widget that will be the parent of this NatCombo
	 * @param cellStyle Style configuration containing horizontal alignment, font, foreground and 
	 * 			background color information.
	 * @param maxVisibleItems the max number of items the drop down will show before introducing a 
	 * 			scroll bar.
	 * @param freeEdit whether this NatCombo supports free editing in the text control or not.
	 * @param style The style for the Text Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	public NatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, boolean freeEdit, int style) {
		this(parent, cellStyle, maxVisibleItems, freeEdit, false, style);
	}

	/**
	 * Creates a new NatCombo using the given IStyle for rendering, showing the given amount
	 * of items at once in the dropdown. 
	 * @param parent A widget that will be the parent of this NatCombo
	 * @param cellStyle Style configuration containing horizontal alignment, font, foreground and 
	 * 			background color information.
	 * @param maxVisibleItems the max number of items the drop down will show before introducing a 
	 * 			scroll bar.
	 * @param freeEdit whether this NatCombo supports free editing in the text control or not.
	 * @param multiselect whether this NatCombo should support multiselect or not.
	 * @param style The style for the {@link Text} Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	public NatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, boolean freeEdit, 
			boolean multiselect, int style) {
		this(parent, cellStyle, maxVisibleItems, freeEdit, multiselect, style, 
				GUIHelper.getImage("down_2")); //$NON-NLS-1$;
	}
	

	/**
	 * Creates a new NatCombo using the given IStyle for rendering, showing the given amount
	 * of items at once in the dropdown. 
	 * @param parent A widget that will be the parent of this NatCombo
	 * @param cellStyle Style configuration containing horizontal alignment, font, foreground and 
	 * 			background color information.
	 * @param maxVisibleItems the max number of items the drop down will show before introducing a 
	 * 			scroll bar.
	 * @param freeEdit whether this NatCombo supports free editing in the text control or not.
	 * @param multiselect whether this NatCombo should support multiselect or not.
	 * @param style The style for the {@link Text} Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 * @param iconImage The image to use as overlay to the {@link Text} Control if the dropdown
	 * 			is visible. Using this image will indicate that the control is an open combo
	 * 			to the user.
	 */
	public NatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, boolean freeEdit, 
			boolean multiselect, int style, Image iconImage) {
		super(parent, SWT.NONE);

		this.cellStyle = cellStyle;
		this.maxVisibleItems = maxVisibleItems;
		this.freeEdit = freeEdit;
		this.multiselect = multiselect;
		this.iconImage = iconImage;

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		createTextControl(style);
		createDropdownControl();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		dropdownShell.dispose();
		text.dispose();
	}
	
	/**
	 * Sets the given items to be the items shown in the dropdown of this NatCombo.
	 * @param items The array of items to set.
	 */
	public void setItems(String[] items) {
		if (items != null) {
			this.items = items;
			this.itemList = Arrays.asList(this.items);
			if (!dropdownList.isDisposed() && items != null && items.length > 0) {
				dropdownList.setItems(items);
			}
		}
	}

	/**
	 * Creates the Text control of this NatCombo, adding styles, look&feel and
	 * needed listeners for the control only.
	 * @param style The style for the Text Control to construct. Uses this style
	 * 			adding internal styles via ConfigRegistry.
	 */
	protected void createTextControl(int style) {
		int widgetStyle = style | (freeEdit ? 
				HorizontalAlignmentEnum.getSWTStyle(cellStyle) : 
					HorizontalAlignmentEnum.getSWTStyle(cellStyle) | SWT.READ_ONLY);
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

					int selectionIndex = dropdownList.getSelectionIndex();
					selectionIndex += event.keyCode == SWT.ARROW_DOWN ? 1 : -1;
					if (selectionIndex < 0) {
						selectionIndex = 0;
					}

					dropdownList.select(selectionIndex);
				}
				else if (!LetterOrDigitKeyEventMatcher.isLetterOrDigit(event.character)) {
					if (freeEdit) {
						//simply clear the selection in dropdownlist so the free value in text control
						//will be used
						if (!dropdownList.isDisposed()) {
							dropdownList.deselectAll();
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
					if (dropdownList.isDisposed() || !dropdownList.isVisible()) {
						showDropdownControl();
					} else {
						//if there is no free edit enabled, set the focus back to the
						//dropdownlist so it handles key strokes itself
						dropdownList.forceFocus();
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
				showDropdownControl();
			}

		});
	}
	
	/**
	 * Create the dropdown control of this NatCombo, adding styles, look&feel and
	 * needed listeners for the control only.
	 */
	protected void createDropdownControl() {
		dropdownShell = new Shell(getShell(), SWT.MODELESS);
		dropdownShell.setLayout(new FillLayout());

		int dropdownListStyle = SWT.V_SCROLL | HorizontalAlignmentEnum.getSWTStyle(cellStyle);
		if (this.multiselect) {
			dropdownListStyle = dropdownListStyle | SWT.MULTI;
		}
		dropdownList = new List(dropdownShell, dropdownListStyle);
		dropdownList.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		dropdownList.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		dropdownList.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));

		dropdownList.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				//for multiselect the highlighting feature will result in selecting all
				//items because of the SWT.MULTI style
				//to avoid this we need to ensure that items are deselected that are not
				//selected because of user interaction
				if (multiselect) {
					java.util.List<String> currentUserSelection = Arrays.asList(getTextAsArray());
					
					String[] dropdownSelection = dropdownList.getSelection();
					
					for (String ds : dropdownSelection) {
						if (!currentUserSelection.contains(ds)) {
							dropdownList.deselect(itemList.indexOf(ds));
						}
					}
				}

				int index = getItemIndexByMousePosition(e.y);
				if (index >= 0) {
					dropdownList.select(index);
				}
			}
		});
		
		dropdownList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (multiselect) {
					//if a user tries in multiselect mode to perform a multiselect
					//by holding the Ctrl key, because of the preselection on mouseMove
					//and the SWT.MULTI style, a deselection is performed
					//this is why we need to correct that behaviour in here
					if (e.stateMask == SWT.CTRL) {
						int index = getItemIndexByMousePosition(e.y);
						if (index >= 0) {
							java.util.List<String> currentUserSelection = Arrays.asList(getTextAsArray());
							
							if (currentUserSelection.contains(dropdownList.getItems()[index])) {
								dropdownList.deselect(index);
							}
							else {
								dropdownList.select(index);
							}
						}
					}
				}
				
				updateTextControl(!multiselect);
			}
		});
		
		dropdownList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if ((event.keyCode == SWT.CR)
						|| (event.keyCode == SWT.KEYPAD_CR)) {
					updateTextControl(true);
				}
			}
		});
		
		dropdownList.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusLost(FocusEvent e) {
				hideDropdownControl();
				if (freeEdit) {
					text.forceFocus();
				}
			}
		});
		
		setItems(items);
		dropdownList.setSelection(getTextAsArray());
	}

	/**
	 * This method will be called if an item of the dropdown control is selected
	 * via mouse click or pressing enter. It will populate the text control with
	 * the information gathered out of the selection in the dropdown control and 
	 * hide the dropdown if necessary.
	 * @param hideDropdown <code>true</code> if the dropdown should be hidden 
	 * 			after updating the text control
	 */
	private void updateTextControl(boolean hideDropdown) {
		text.setText(getTransformedTextForSelection());
		if (hideDropdown) {
			hideDropdownControl();
		}
	}
	
	/**
	 * Calculates the index of the item at given mouse y position.
	 * @param mouseY The y coordinate of the current mouse position
	 * @return The index of the item within the list of combo box items
	 * 			or -1 if there are no visible items or there is no
	 * 			visible item selected
	 */
	private int getItemIndexByMousePosition(int mouseY) {
		if (getVisibleItemCount() == 0) {
			return -1;
		}
		int itemHeight = dropdownShell.getSize().y / getVisibleItemCount();

		//operate on the visible items
		int resultIndex = -1;
		int topIndex = dropdownList.getTopIndex();
		if (mouseY <= itemHeight) {
			resultIndex = topIndex;
		}
		else {
			resultIndex = topIndex + mouseY / itemHeight;
		}
		
		return resultIndex;
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
			createDropdownControl();
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
	private int getVisibleItemCount() {
		int itemCount = dropdownList.getItemCount();
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
	private void calculateBounds() {
		if (dropdownShell != null && !dropdownShell.isDisposed()) {
			Point size = getSize();
			//calculate the height by multiplying the number of visible items with
			//the item height of items in the list and adding 2 to work around a
			//calculation error regarding the descent of the font metrics for the 
			//last shown item
			//Note: if there are no items to show in the combo, calculate with the item count of
			//		3 so an empty combo will open
			int listHeight = (getVisibleItemCount() > 0 ? getVisibleItemCount() : 3) * dropdownList.getItemHeight() + 2;
			int listWidth = dropdownList.computeSize(SWT.DEFAULT, listHeight).x;
			if (listWidth < size.x) {
				listWidth = size.x;
			}
			dropdownList.setSize(listWidth, listHeight);
			
			Point textPosition = text.toDisplay(text.getLocation());
			
			dropdownShell.setBounds(
					textPosition.x, 
					textPosition.y + text.getBounds().height, 
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
	 * 
	 * @see List#getSelectionIndex()
	 */
	public int getSelectionIndex() {
		if (!this.dropdownList.isDisposed()) {
			return this.dropdownList.getSelectionIndex();
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
	 * 
	 * @see List#getSelectionIndices()
	 */
	public int[] getSelectionIndices() {
		if (!this.dropdownList.isDisposed()) {
			return this.dropdownList.getSelectionIndices();
		} else {
			java.util.List<String> itemList = Arrays.asList(this.items);
			String[] selectedItems = getTextAsArray();
			int[] result = new int[selectedItems.length];
			for (int i = 0; i < selectedItems.length; i++) {
				result[i] = itemList.indexOf(selectedItems[i]);
			}
			return result;
		}
	}

	/**
	 * Returns the number of selected items contained in the receiver.
	 *
	 * @return the number of selected items
	 * 
	 * @see List#getSelectionCount()
	 */
	public int getSelectionCount() {
		if (!this.dropdownList.isDisposed()) {
			return this.dropdownList.getSelectionCount();
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
	 * 
	 * @see List#getSelection()
	 */
	public String[] getSelection() {
		String[] result = null;
		if (!this.dropdownList.isDisposed()) {
			result = this.dropdownList.getSelection();
			if (result.length == 0 && this.text.getText().length() > 0) {
				result = getTextAsArray();
			}
		} else {
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
	 * @param indices the indices of the items to select
	 * 
	 * @see List#setSelection(int[])
	 */
	public void setSelection(String[] items) {
		String textValue = ""; //$NON-NLS-1$
		if (items != null) {
			if (!this.dropdownList.isDisposed()) {
				this.dropdownList.setSelection(items);
				if (this.freeEdit && this.dropdownList.getSelectionCount() == 0) {
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
	 * 
	 * @see List#select(int)
	 */
	public void select(int index) {
		if (!this.dropdownList.isDisposed()) {
			this.dropdownList.select(index);
			this.text.setText(getTransformedTextForSelection());
		} else if (index >= 0) {
			this.text.setText(this.items[index]);
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
	 * 
	 * @see List#select(int[])
	 */
	public void select(int[] indeces) {
		if (!this.dropdownList.isDisposed()) {
			this.dropdownList.select(indeces);
			this.text.setText(getTransformedTextForSelection());
		} else {
			String[] selectedItems = new String[indeces.length];
			for (int i = 0; i < indeces.length; i++) {
				if (indeces[i] >= 0) {
					selectedItems[i] = this.items[indeces[i]];
				}
			}
			this.text.setText(getTransformedText(selectedItems));
		}
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		this.text.addKeyListener(listener);
		this.dropdownList.addKeyListener(listener);
	}

	@Override
	public void addTraverseListener(TraverseListener listener) {
		this.text.addTraverseListener(listener);
		this.dropdownList.addTraverseListener(listener);
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		//only add the mouse listener to the dropdown, as clicking in the text control
		//should not trigger anything else than it is handled by the text control itself.
		this.dropdownList.addMouseListener(listener);
	}

	@Override
	public void notifyListeners(int eventType, Event event) {
		this.dropdownList.notifyListeners(eventType, event);
	}
	
	public void addShellListener(ShellListener listener) {
		this.dropdownShell.addShellListener(listener);
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
				return transform.split(this.multiselectValueSeparator);
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
		if (!this.dropdownList.isDisposed()) {
			result = getTransformedText(this.dropdownList.getSelection());
		}
		return result;
	}
	
	/**
	 * Transforms the given array of Strings to a text representatino that
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
			for (String selection : values) {
				if (result.length() > 0 && selection.length() > 0) {
					result += this.multiselectValueSeparator;
				}
				result += selection;
			}
			if (result.length() > 0) {
				result = this.multiselectTextPrefix + result + this.multiselectTextSuffix;
			}
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

}
