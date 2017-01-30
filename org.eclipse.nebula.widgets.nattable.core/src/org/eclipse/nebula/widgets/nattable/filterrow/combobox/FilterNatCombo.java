/*******************************************************************************
 * Copyright (c) 2013, 2017 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Ryan McHale <rpmc22@gmail.com> - Bug 484716
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Specialisation of NatCombo which doesn't populate the selected values to the
 * Text control. Instead the String representation of the selected values are
 * stored in a local member. Doing this the selection is only visible in the
 * dropdown via selection/check state of the contained items.
 * <p>
 * Usually this combo will be created with the SWT.CHECK style bit. This way the
 * selected items are visualized by showing checked checkboxes. Also adds a
 * <i>Select All</i> item for convenience that de-/selects all items on click.
 */
public class FilterNatCombo extends NatCombo {

    /**
     * The viewer that contains the select all item in the dropdown control.
     *
     * @since 1.4
     */
    protected CheckboxTableViewer selectAllItemViewer;
    /**
     * The local selection String storage.
     */
    private String filterText;

    /**
     * List of ICheckStateListener that should be added to the select all table
     * once it is created. Kept locally because the table creation is deferred
     * to the first access.
     */
    private List<ICheckStateListener> checkStateListener = new ArrayList<ICheckStateListener>();

    /**
     * Creates a new FilterNatCombo using the given IStyle for rendering,
     * showing the default number of items at once in the dropdown.
     *
     * @param parent
     *            A widget that will be the parent of this NatCombo
     * @param cellStyle
     *            Style configuration containing horizontal alignment, font,
     *            foreground and background color information.
     * @param style
     *            The style for the Text Control to construct. Uses this style
     *            adding internal styles via ConfigRegistry.
     */
    public FilterNatCombo(Composite parent, IStyle cellStyle, int style) {
        this(parent,
                cellStyle,
                DEFAULT_NUM_OF_VISIBLE_ITEMS,
                style,
                GUIHelper.getImage("down_2")); //$NON-NLS-1$
    }

    /**
     * Creates a new FilterNatCombo using the given IStyle for rendering,
     * showing the given amount of items at once in the dropdown.
     *
     * @param parent
     *            A widget that will be the parent of this NatCombo
     * @param cellStyle
     *            Style configuration containing horizontal alignment, font,
     *            foreground and background color information.
     * @param maxVisibleItems
     *            the max number of items the drop down will show before
     *            introducing a scroll bar.
     * @param style
     *            The style for the Text Control to construct. Uses this style
     *            adding internal styles via ConfigRegistry.
     */
    public FilterNatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, int style) {
        this(parent,
                cellStyle,
                maxVisibleItems,
                style,
                GUIHelper.getImage("down_2")); //$NON-NLS-1$
    }

    /**
     * Creates a new FilterNatCombo using the given IStyle for rendering,
     * showing the given amount of items at once in the dropdown.
     *
     * @param parent
     *            A widget that will be the parent of this NatCombo
     * @param cellStyle
     *            Style configuration containing horizontal alignment, font,
     *            foreground and background color information.
     * @param maxVisibleItems
     *            the max number of items the drop down will show before
     *            introducing a scroll bar.
     * @param style
     *            The style for the Text Control to construct. Uses this style
     *            adding internal styles via ConfigRegistry.
     *
     * @param showDropdownFilter
     *            Flag indicating whether the filter of the dropdown control
     *            should be displayed
     *
     * @since 1.4
     */
    public FilterNatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, int style, boolean showDropdownFilter) {
        this(parent, cellStyle, maxVisibleItems, style, GUIHelper.getImage("down_2"), showDropdownFilter); //$NON-NLS-1$
    }

    /**
     * Creates a new FilterNatCombo using the given IStyle for rendering,
     * showing the given amount of items at once in the dropdown.
     *
     * @param parent
     *            A widget that will be the parent of this NatCombo
     * @param cellStyle
     *            Style configuration containing horizontal alignment, font,
     *            foreground and background color information.
     * @param maxVisibleItems
     *            the max number of items the drop down will show before
     *            introducing a scroll bar.
     * @param style
     *            The style for the {@link Text} Control to construct. Uses this
     *            style adding internal styles via ConfigRegistry.
     * @param iconImage
     *            The image to use as overlay to the {@link Text} Control if the
     *            dropdown is visible. Using this image will indicate that the
     *            control is an open combo to the user.
     */
    public FilterNatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, int style, Image iconImage) {
        this(parent, cellStyle, maxVisibleItems, style, iconImage, false);
    }

    /**
     * Creates a new FilterNatCombo using the given IStyle for rendering,
     * showing the given amount of items at once in the dropdown.
     *
     * @param parent
     *            A widget that will be the parent of this NatCombo
     * @param cellStyle
     *            Style configuration containing horizontal alignment, font,
     *            foreground and background color information.
     * @param maxVisibleItems
     *            the max number of items the drop down will show before
     *            introducing a scroll bar.
     * @param style
     *            The style for the {@link Text} Control to construct. Uses this
     *            style adding internal styles via ConfigRegistry.
     * @param iconImage
     *            The image to use as overlay to the {@link Text} Control if the
     *            dropdown is visible. Using this image will indicate that the
     *            control is an open combo to the user.
     *
     * @param showDropdownFilter
     *            Flag indicating whether the filter of the dropdown control
     *            should be displayed
     *
     * @since 1.4
     */
    public FilterNatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems, int style, Image iconImage, boolean showDropdownFilter) {
        super(parent, cellStyle, maxVisibleItems, style, iconImage, showDropdownFilter);
    }

    @Override
    protected void calculateBounds() {
        if (this.dropdownShell != null && !this.dropdownShell.isDisposed()) {
            Point size = getSize();

            int gridLineAdjustment = this.dropdownTable.getGridLineWidth() * 2;

            // calculate the height by multiplying the number of visible items
            // with the item height of items in the list and adding 2*grid line
            // width to work around a calculation error regarding the descent of
            // the font metrics for the last shown item
            int listHeight = getVisibleItemCount() * this.dropdownTable.getItemHeight() + gridLineAdjustment;

            // since introduced the TableColumn for real full row selection, we
            // call pack() to perform autoresize to ensure the width shows the
            // whole content
            this.dropdownTable.getColumn(0).pack();
            this.selectAllItemViewer.getTable().getColumn(0).pack();

            int listWidth = Math.max(
                    this.dropdownTable.computeSize(SWT.DEFAULT, listHeight, true).x,
                    size.x);

            int viewerHeight = this.selectAllItemViewer.getTable().getItemHeight();
            listWidth = Math.max(
                    this.selectAllItemViewer.getTable().computeSize(SWT.DEFAULT, viewerHeight, true).x,
                    listWidth);

            Point textPosition = this.text.toDisplay(this.text.getLocation());

            int filterTextBoxHeight = this.showDropdownFilter ? this.filterBox.computeSize(SWT.DEFAULT, SWT.DEFAULT).y : 0;
            this.dropdownShell.setBounds(
                    textPosition.x,
                    textPosition.y + this.text.getBounds().height,
                    listWidth + (this.dropdownTable.getGridLineWidth() * 2),
                    listHeight + viewerHeight + filterTextBoxHeight);

            // as we performed auto resize for the columns, we now need to
            // ensure again that the columns
            // span the whole table width in case they shrunk
            calculateColumnWidth();
        }
    }

    @Override
    protected void calculateColumnWidth() {
        super.calculateColumnWidth();

        this.selectAllItemViewer.getTable().getColumn(0).setWidth(
                this.dropdownTable.getColumn(0).getWidth());
    }

    @Override
    protected void createDropdownControl(int style) {
        super.createDropdownControl(style);

        int dropdownListStyle = style | SWT.NO_SCROLL
                | HorizontalAlignmentEnum.getSWTStyle(this.cellStyle)
                | SWT.FULL_SELECTION;
        this.selectAllItemViewer =
                CheckboxTableViewer.newCheckList(this.dropdownShell, dropdownListStyle);

        // add a column to be able to resize the item width in the dropdown
        new TableColumn(this.selectAllItemViewer.getTable(), SWT.NONE);
        this.selectAllItemViewer.getTable().addListener(SWT.Resize,
                new Listener() {
                    @Override
                    public void handleEvent(Event event) {
                        calculateColumnWidth();
                    }
                });

        FormData data = new FormData();
        if (this.showDropdownFilter) {
            data.top = new FormAttachment(this.filterBox, 0, SWT.BOTTOM);
        } else {
            data.top = new FormAttachment(this.dropdownShell, 0, SWT.TOP);
        }
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(100);
        this.selectAllItemViewer.getTable().setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(this.selectAllItemViewer.getControl(), 0, SWT.BOTTOM);
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(100);
        data.bottom = new FormAttachment(100);
        this.dropdownTable.setLayoutData(data);

        this.selectAllItemViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

            @Override
            public void dispose() {}

            @SuppressWarnings("unchecked")
            @Override
            public Object[] getElements(Object inputElement) {
                return ((Collection<String>) inputElement).toArray();
            }
        });

        this.selectAllItemViewer.setLabelProvider(new ILabelProvider() {

            @Override
            public void removeListener(ILabelProviderListener listener) {}

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            @Override
            public void dispose() {}

            @Override
            public void addListener(ILabelProviderListener listener) {}

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

        this.selectAllItemViewer.getTable().setBackground(
                this.cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        this.selectAllItemViewer.getTable().setForeground(
                this.cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
        this.selectAllItemViewer.getTable().setFont(
                this.cellStyle.getAttributeValue(CellStyleAttributes.FONT));

        this.selectAllItemViewer.getTable().addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        showDropdownControl();
                    }
                });

        this.selectAllItemViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                // if the select all item is clicked directly, the
                // grayed state needs to be set to false
                FilterNatCombo.this.selectAllItemViewer.setGrayed(selectAllLabel, false);

                if (event.getChecked()) {
                    // select all
                    FilterNatCombo.this.dropdownTable.selectAll();
                } else {
                    // deselect all
                    FilterNatCombo.this.dropdownTable.deselectAll();
                }

                // after selection is performed we need to ensure that
                // selection and checkboxes are in sync
                for (TableItem tableItem : FilterNatCombo.this.dropdownTable.getItems()) {
                    tableItem.setChecked(
                            FilterNatCombo.this.dropdownTable.isSelected(
                                    FilterNatCombo.this.itemList.indexOf(tableItem.getText())));
                }

                // sync the selectionStateMap based on the state of the select
                // all checkbox
                for (String item : FilterNatCombo.this.itemList) {
                    FilterNatCombo.this.selectionStateMap.put(item, event.getChecked());
                }

                updateTextControl(!FilterNatCombo.this.multiselect);
            }
        });

        for (ICheckStateListener l : this.checkStateListener) {
            this.selectAllItemViewer.addCheckStateListener(l);
        }

        // set an ICheckStateProvider that sets the checkbox state of the select
        // all checkbox regarding the selection of the items in the dropdown
        this.selectAllItemViewer.setCheckStateProvider(new ICheckStateProvider() {

            @Override
            public boolean isGrayed(Object element) {
                if (FilterNatCombo.this.dropdownTable.getSelectionCount() == FilterNatCombo.this.dropdownTable.getItemCount()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean isChecked(Object element) {
                if (FilterNatCombo.this.dropdownTable.getSelectionCount() == 0) {
                    return false;
                }
                return true;
            }
        });

        // add a selection listener to the items that simply refreshes the
        // select all checkbox
        this.dropdownTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FilterNatCombo.this.selectAllItemViewer.refresh();
            }
        });
    }

    @Override
    protected void setDropdownSelection(String[] selection) {
        super.setDropdownSelection(selection);
        if (this.selectAllItemViewer != null)
            this.selectAllItemViewer.refresh();
    }

    /**
     * Add an ICheckStateListener to the viewer of the dropdown that contains
     * the select all item. Needed so the editor is able to commit after the
     * click on the select all checkbox is performed.
     *
     * @param listener
     *            The listener to add to the select all item
     */
    public void addCheckStateListener(ICheckStateListener listener) {
        if (listener != null) {
            if (this.selectAllItemViewer != null) {
                this.selectAllItemViewer.addCheckStateListener(listener);
            }
            this.checkStateListener.add(listener);
        }
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
        if (!getDropdownTable().isDisposed()) {
            return getDropdownTable().getSelectionIndex();
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
            if (!getDropdownTable().isDisposed()) {
                setDropdownSelection(items);
                if (this.freeEdit
                        && getDropdownTable().getSelectionCount() == 0) {
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
        if (!getDropdownTable().isDisposed()) {
            getDropdownTable().select(index);
            this.filterText = getTransformedTextForSelection();
        } else if (index >= 0) {
            this.filterText = this.itemList.get(index);
        }
    }

    @Override
    public void select(int[] indeces) {
        if (!getDropdownTable().isDisposed()) {
            getDropdownTable().select(indeces);
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
                if ((i + 1) < values.length) {
                    result += this.multiselectValueSeparator;
                }
            }
            // if at least one value was selected, add the prefix and suffix
            // we check the values array instead of the result length because
            // there can be also an empty String be selected
            if (values.length > 0) {
                result = this.multiselectTextPrefix + result + this.multiselectTextSuffix;
            }
        } else if (values.length > 0) {
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
            transform = transform.substring(prefixLength, transform.length() - suffixLength);
            return transform.split(this.multiselectValueSeparator);
        }
        return new String[] {};
    }
}
