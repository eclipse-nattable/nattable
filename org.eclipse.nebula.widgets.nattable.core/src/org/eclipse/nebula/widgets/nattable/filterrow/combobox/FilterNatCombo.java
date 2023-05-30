/*******************************************************************************
 * Copyright (c) 2013, 2023 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Ryan McHale <rpmc22@gmail.com> - Bug 484716
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
     * The viewer that contains the 'select all' item in the dropdown control.
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
    private List<ICheckStateListener> checkStateListener = new ArrayList<>();

    /**
     * The action that is executed after the dropdown content filter was
     * applied, e.g. applying the filter on the content based on the current
     * visible items in the dropdown.
     *
     * @since 2.1
     */
    private Runnable filterModifyAction;

    /**
     * Flag to determine whether the content of the dropdown is filtered or not.
     *
     * @since 2.1
     */
    boolean filterActive = false;

    /**
     * The base style that is used to create the dropdown. Needed to use the
     * same style for creating the additional viewer ('select all' and 'add to
     * filter').
     *
     * @since 2.1
     */
    private int baseStyle;

    /**
     * The viewer that contains the 'add to filter' item in the dropdown
     * control.
     *
     * @since 2.1
     */
    private CheckboxTableViewer addToFilterItemViewer;

    /**
     * The initial selection that was set when the FilterNatCombo was opened.
     * Needed to restore the state on deactivating the 'add to filter' checkbox.
     *
     * @since 2.1
     */
    String[] initialSelection;

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

            int selectAllViewerHeight = this.selectAllItemViewer.getTable().getItemHeight();
            listWidth = Math.max(
                    this.selectAllItemViewer.getTable().computeSize(SWT.DEFAULT, selectAllViewerHeight, true).x,
                    listWidth);

            // calculate width with the 'add to filter' viewer
            int addViewerHeight = 0;
            if (this.addToFilterItemViewer != null && this.addToFilterItemViewer.getTable().isVisible()) {
                this.addToFilterItemViewer.getTable().getColumn(0).pack();

                addViewerHeight = this.addToFilterItemViewer.getTable().getItemHeight();
                listWidth = Math.max(
                        this.addToFilterItemViewer.getTable().computeSize(SWT.DEFAULT, addViewerHeight, true).x,
                        listWidth);
            }

            Point textPosition = this.text.toDisplay(this.text.getLocation());

            int filterTextBoxHeight = this.showDropdownFilter ? this.filterBox.computeSize(SWT.DEFAULT, SWT.DEFAULT).y : 0;
            this.dropdownShell.setBounds(
                    textPosition.x,
                    textPosition.y + this.text.getBounds().height,
                    listWidth + (this.dropdownTable.getGridLineWidth() * 2),
                    listHeight + selectAllViewerHeight + addViewerHeight + filterTextBoxHeight);

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

        if (this.addToFilterItemViewer != null) {
            this.addToFilterItemViewer.getTable().getColumn(0).setWidth(
                    this.dropdownTable.getColumn(0).getWidth());
        }
    }

    @Override
    protected void createDropdownControl(int style) {
        super.createDropdownControl(style);

        this.baseStyle = style;

        int dropdownListStyle = style | SWT.NO_SCROLL
                | HorizontalAlignmentEnum.getSWTStyle(this.cellStyle)
                | SWT.FULL_SELECTION;
        this.selectAllItemViewer =
                CheckboxTableViewer.newCheckList(this.dropdownShell, dropdownListStyle);

        // add a column to be able to resize the item width in the dropdown
        new TableColumn(this.selectAllItemViewer.getTable(), SWT.NONE);
        this.selectAllItemViewer.getTable().addListener(SWT.Resize, event -> calculateColumnWidth());

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

        this.selectAllItemViewer.setContentProvider(ArrayContentProvider.getInstance());

        this.selectAllItemViewer.setLabelProvider(new LabelProvider() {

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }
        });

        final String selectAllLabel = Messages.getString("FilterNatCombo.selectAll"); //$NON-NLS-1$
        List<String> input = new ArrayList<>();
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

        this.selectAllItemViewer.addCheckStateListener(event -> {
            boolean performCheck = event.getChecked();
            boolean grayed = getSelectionCount() < FilterNatCombo.this.itemList.size();
            // if not all items are checked, but we want to uncheck all via the
            // grayed select all checkbox, we need to double check the state of
            // all visible items. This is necessary because one could filter
            // dropdown where already an item is unselected and tries then to
            // select all visibles only.
            if (!performCheck && grayed) {
                boolean allVisibleUnchecked = true;
                for (TableItem tableItem : FilterNatCombo.this.dropdownTable.getItems()) {
                    if (tableItem.getChecked()) {
                        allVisibleUnchecked = false;
                    }
                }
                if (allVisibleUnchecked) {
                    performCheck = true;
                }
            }

            // after selection is performed we need to ensure that
            // selection and checkboxes are in sync
            for (TableItem tableItem : FilterNatCombo.this.dropdownTable.getItems()) {
                tableItem.setChecked(performCheck);
            }

            if (this.addToFilterItemViewer != null) {
                // we reset the "add to filter" without resetting the
                // selectionStateMap
                updateAddToFilterVisibility(null);
            }

            // sync the selectionStateMap based on the state of the select
            // all checkbox
            for (TableItem tableItem : FilterNatCombo.this.dropdownTable.getItems()) {
                FilterNatCombo.this.selectionStateMap.put(tableItem.getText(), performCheck);
            }

            updateTextControl(!FilterNatCombo.this.multiselect);
            // also refresh the selectAllItemViewer to show a potential grayed
            // checked state in case of an active filter
            this.selectAllItemViewer.refresh();
        });

        for (ICheckStateListener l : this.checkStateListener) {
            this.selectAllItemViewer.addCheckStateListener(l);
        }

        // set an ICheckStateProvider that sets the checkbox state of the select
        // all checkbox regarding the selection of the items in the dropdown
        this.selectAllItemViewer.setCheckStateProvider(new ICheckStateProvider() {

            @Override
            public boolean isGrayed(Object element) {
                for (TableItem tableItem : FilterNatCombo.this.dropdownTable.getItems()) {
                    if (!tableItem.getChecked()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean isChecked(Object element) {
                for (TableItem tableItem : FilterNatCombo.this.dropdownTable.getItems()) {
                    if (tableItem.getChecked()) {
                        return true;
                    }
                }
                return false;
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
        if (this.selectAllItemViewer != null) {
            this.selectAllItemViewer.refresh();
        }
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

        // if a filterModifyAction is registered, not all items are selected
        // (which means there is already a filter active on opening the
        // dropdown) and the "add to filter" viewer is not created yet, remember
        // the initial selection and create the "add to filter" viewer
        if (this.filterModifyAction != null
                && this.selectAllItemViewer != null
                && this.selectionStateMap.entrySet().stream().anyMatch(entry -> !entry.getValue())
                && this.addToFilterItemViewer == null) {

            // remember the initial selection to be able to restore it when
            // the dropdown filter is cleared
            this.initialSelection = getSelection();

            // create the "add to filter" entry
            createAddToFilterItemViewer();
        }
    }

    /**
     * Creates and adds the "add to filter" entry to the dropdown. Initially
     * hidden and becomes visible once a dropdown filter is entered.
     */
    private void createAddToFilterItemViewer() {
        int dropdownListStyle = this.baseStyle | SWT.NO_SCROLL
                | HorizontalAlignmentEnum.getSWTStyle(this.cellStyle)
                | SWT.FULL_SELECTION;
        this.addToFilterItemViewer =
                CheckboxTableViewer.newCheckList(this.dropdownShell, dropdownListStyle);

        // add a column to be able to resize the item width in the dropdown
        new TableColumn(this.addToFilterItemViewer.getTable(), SWT.NONE);
        this.addToFilterItemViewer.getTable().addListener(SWT.Resize, event -> calculateColumnWidth());

        FormData data = new FormData();
        data.top = new FormAttachment(this.selectAllItemViewer.getControl(), 0, SWT.BOTTOM);
        data.left = new FormAttachment(0);
        data.right = new FormAttachment(100);
        this.addToFilterItemViewer.getTable().setLayoutData(data);

        this.addToFilterItemViewer.setContentProvider(ArrayContentProvider.getInstance());

        this.addToFilterItemViewer.setLabelProvider(new LabelProvider() {

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }
        });

        final String addAllLabel = Messages.getString("FilterNatCombo.addToFilter"); //$NON-NLS-1$
        List<String> input = new ArrayList<>();
        input.add(addAllLabel);

        this.addToFilterItemViewer.setInput(input);

        this.addToFilterItemViewer.getTable().setBackground(
                this.cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        this.addToFilterItemViewer.getTable().setForeground(
                this.cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
        this.addToFilterItemViewer.getTable().setFont(
                this.cellStyle.getAttributeValue(CellStyleAttributes.FONT));

        this.addToFilterItemViewer.getTable().setVisible(this.filterActive);

        this.addToFilterItemViewer.getTable().addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        showDropdownControl();
                    }
                });

        this.addToFilterItemViewer.addCheckStateListener(event -> {
            boolean performCheck = event.getChecked();

            if (performCheck) {
                // add the current selection to the existing filter
                TableItem[] items = FilterNatCombo.this.dropdownTableViewer.getTable().getItems();
                for (int i = 0; i < items.length; i++) {
                    TableItem item = items[i];
                    this.selectionStateMap.put(item.getText(), item.getChecked());
                }
            } else {
                // set back to previous state
                for (Map.Entry<String, Boolean> entry : FilterNatCombo.this.selectionStateMap.entrySet()) {
                    entry.setValue(Boolean.FALSE);
                }
                for (String sel : this.initialSelection) {
                    this.selectionStateMap.put(sel, Boolean.TRUE);
                }
            }

            if (this.filterModifyAction != null) {
                this.filterActive = false;
                String[] tmp = this.initialSelection;
                this.filterModifyAction.run();
                this.initialSelection = tmp;
                this.filterActive = true;
            }

            // also refresh the selectAllItemViewer to show a potential grayed
            // checked state in case of an active filter
            this.selectAllItemViewer.refresh();
        });

        // add an additional selection listener to show/hide the "add to filter"
        // item based on whether the state of all now visible items is the same
        // as in the initial selection
        this.dropdownTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selected = e.detail != SWT.CHECK;

                if (!selected) {
                    // handle only checkbox interactions
                    TableItem clickedItem = (TableItem) e.item;
                    updateAddToFilterVisibility(clickedItem);
                }
            }
        });
    }

    private void updateAddToFilterVisibility(TableItem clickedItem) {
        if (this.initialSelection != null && this.filterActive) {
            boolean selectionChanged = false;

            for (TableItem item : getDropdownTable().getItems()) {
                if ((item.getChecked() && !Arrays.stream(this.initialSelection).anyMatch(s -> s.equals(item.getText())))
                        || (!item.getChecked() && Arrays.stream(this.initialSelection).anyMatch(s -> s.equals(item.getText())))) {
                    // check if the shown check state differs from
                    // the initial selection
                    selectionChanged = true;
                }
            }

            // only update the visibility of the "add to filter" viewer if the
            // change does not lead to the initial state
            if (!(this.addToFilterItemViewer.getCheckedElements().length > 0 && !selectionChanged)) {
                this.addToFilterItemViewer.getTable().setVisible(selectionChanged);

                ((FormData) this.dropdownTable.getLayoutData()).top =
                        new FormAttachment(
                                selectionChanged
                                        ? this.addToFilterItemViewer.getControl()
                                        : this.selectAllItemViewer.getControl(),
                                0,
                                SWT.BOTTOM);
            }

            resetAddToFilter(clickedItem);

            calculateBounds();
            this.selectAllItemViewer.refresh();
        }
    }

    private void resetAddToFilter(TableItem clickedItem) {
        // uncheck the "add to filter" item if it was checked in a
        // previous attempt and set the initial selection to the current one
        if (this.addToFilterItemViewer.getCheckedElements().length > 0) {
            this.addToFilterItemViewer.setAllChecked(false);

            // if a single item was checked/unchecked in the "add to filter"
            // active state, we need to reset the selectionStateMap to create a
            // new correct initial state
            if (clickedItem != null) {
                if (!clickedItem.getChecked()) {
                    this.selectionStateMap.put(clickedItem.getText(), Boolean.TRUE);
                } else {
                    this.selectionStateMap.put(clickedItem.getText(), Boolean.FALSE);
                }
            }

            this.initialSelection = getSelection();
        }
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

    /**
     * Activate the {@link FilterDropDownFilterModifyListener} to perform an
     * update on the selection on the dropdown based on the current visible
     * items. Adds the given {@link Runnable} as action that should be executed
     * after the dropdown content filter was applied, e.g. applying the filter
     * on the content based on the current visible items in the dropdown.
     * <p>
     * <b>Note:</b> Only has an effect if <code>showDropdownFilter</code> is set
     * to <code>true</code>.
     *
     * @param action
     *            The action that should be executed after the dropdown content
     *            filter was applied, e.g. applying the filter on the content
     *            based on the current visible items in the dropdown.
     *
     * @since 2.1
     */
    public void setFilterModifyAction(Runnable action) {
        this.filterModifyAction = action;
        setDropdownFilterModifyListener(new FilterDropDownFilterModifyListener());
    }

    /**
     * Specialization of the {@link DropDownFilterModifyListener} that updates
     * the selection of the dropdown content to what is currently visible. All
     * not visible items are deselected for the next filter operation.
     * Additionally it executes the {@link FilterNatCombo#filterModifyAction} it
     * one is set (e.g. for triggering a commit to apply the filter).
     *
     * @since 2.1
     */
    public class FilterDropDownFilterModifyListener extends DropDownFilterModifyListener {

        @Override
        protected void setSelection() {

            String[] selection = null;
            if (FilterNatCombo.this.filterActive) {
                TableItem[] items = FilterNatCombo.this.dropdownTableViewer.getTable().getItems();
                selection = new String[items.length];
                for (int i = 0; i < items.length; i++) {
                    TableItem item = items[i];
                    selection[i] = item.getText();
                }
            } else {
                // if no dropdown filter is active, e.g. on clearing the
                // dropdown filter, we restore the initial selection
                selection = getSelection();
            }

            if (FilterNatCombo.this.initialSelection != null) {
                FilterNatCombo.this.initialSelection = getSelection();

                // update layout to make the "add to filter" item visible
                // or invisible if filter is cleared
                FilterNatCombo.this.filterActive = !FilterNatCombo.this.filterBox.getText().isEmpty();
                boolean showAddToFilter = FilterNatCombo.this.filterActive;

                // only mark the visible items checked
                if (FilterNatCombo.this.filterActive) {
                    boolean allAlreadySelected = true;
                    for (TableItem item : getDropdownTable().getItems()) {
                        item.setChecked(true);
                        allAlreadySelected = Arrays.stream(FilterNatCombo.this.initialSelection).anyMatch(s -> s.equals(item.getText()));
                    }

                    showAddToFilter = !allAlreadySelected;
                } else {
                    setDropdownSelection(getSelection());
                }

                FilterNatCombo.this.addToFilterItemViewer.getTable().setVisible(showAddToFilter);

                ((FormData) FilterNatCombo.this.dropdownTable.getLayoutData()).top =
                        new FormAttachment(
                                showAddToFilter
                                        ? FilterNatCombo.this.addToFilterItemViewer.getControl()
                                        : FilterNatCombo.this.selectAllItemViewer.getControl(),
                                0,
                                SWT.BOTTOM);

                // we reset the "add to filter" without resetting the
                // selectionStateMap
                resetAddToFilter(null);

                if (FilterNatCombo.this.selectAllItemViewer != null) {
                    FilterNatCombo.this.selectAllItemViewer.refresh();
                }
            } else {
                // first clear the selection
                getDropdownTable().deselectAll();
                for (Map.Entry<String, Boolean> entry : FilterNatCombo.this.selectionStateMap.entrySet()) {
                    entry.setValue(Boolean.FALSE);
                }

                // then update the selection based on what is currently visible
                // in the dropdown
                setDropdownSelection(selection);

                // update the value in the underlying text control
                updateTextControl(false);

                // if the natcombo was opened with an initial filter (not all
                // items selected) we do not directly apply
                if (FilterNatCombo.this.filterModifyAction != null) {
                    FilterNatCombo.this.filterModifyAction.run();
                }
            }
        }
    }
}
