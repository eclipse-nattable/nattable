/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453898
 *     Ryan McHale <rpmc22@gmail.com> - Bug 484716
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.nebula.widgets.nattable.widget.NatCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * {@link ICellEditor} implementation to provide combo box editing behaviour.
 * Uses the {@link NatCombo} as editor control which provides free editing in
 * the text control part and multi selection in the dropdown part if configured.
 * <p>
 * You can create a ComboBoxCellEditor either by setting the items to show
 * statically by constructor, or by using an {@link IComboBoxDataProvider}. Last
 * one is a way to dynamically populate the items showed in a combobox in
 * NatTable. It is not possible to mix these two approaches!
 *
 */
public class ComboBoxCellEditor extends AbstractCellEditor {

    /**
     * The wrapped editor control.
     */
    private NatCombo combo;

    /**
     * The maximum number of items the drop down will show before introducing a
     * scroll bar.
     */
    protected int maxVisibleItems;

    /**
     * The list of canonical values that will be set as selectable items to the
     * combo. If this {@link ComboBoxCellEditor} is created for using such a
     * list, the selectable values will be static.
     * <p>
     * The values will be converted to the corresponding display values prior
     * filling the combo.
     * <p>
     * If this {@link ComboBoxCellEditor} is created for using a
     * {@link IComboBoxDataProvider} this list will be ignored.
     */
    private List<?> canonicalValues;

    /**
     * The {@link IComboBoxDataProvider} that is used to set the selectable
     * items to the combo. If this {@link ComboBoxCellEditor} is created using
     * such a data provider, the selectable value will be dynamic.
     * <p>
     * The values will be converted to the corresponding display values prior
     * filling the combo.
     * <p>
     * If a {@link IComboBoxDataProvider} is set, a possible set static list of
     * canonical values will be ignored.
     */
    private IComboBoxDataProvider dataProvider;

    /**
     * Flag that indicates whether a text box is displayed to filter the drop
     * down options
     *
     * @since 1.4
     */
    protected boolean showDropdownFilter = false;

    /**
     * Flag that indicates whether this ComboBoxCellEditor supports free editing
     * in the text control of the NatCombo or not. By default free editing is
     * disabled.
     */
    protected boolean freeEdit = false;

    /**
     * Flag that indicates whether this ComboBoxCellEditor supports multiple
     * selection or not. By default multiple selection is disabled.
     */
    protected boolean multiselect = false;

    /**
     * Flag that indicates whether this ComboBoxCellEditor shows checkboxes for
     * items in the dropdown or not.
     */
    protected boolean useCheckbox = false;

    /**
     * String that is used to separate values in the String representation
     * showed in the text control if multiselect is supported. <code>null</code>
     * to use the default String ", ".
     */
    protected String multiselectValueSeparator = null;

    /**
     * String that is used to prefix the generated String representation showed
     * in the text control if multiselect is supported. Needed to visualize the
     * multiselection to the user. If this value is <code>null</code> the
     * default String "[" is used.
     */
    protected String multiselectTextPrefix = null;

    /**
     * String that is used to suffix the generated String representation showed
     * in the text control if multiselect is supported. Needed to visualize the
     * multiselection to the user. If this value is <code>null</code> the
     * default String "]" is used.
     */
    protected String multiselectTextSuffix = null;

    /**
     * The image to use as overlay to the {@link Text} Control if the dropdown
     * is visible. It will indicate that the control is an open combo to the
     * user. If this value is <code>null</code> the default image specified in
     * NatCombo will be used.
     */
    protected Image iconImage;

    /**
     * The list of the canonical values that are currently shown in the opened
     * NatCombo. Needed in case of multi selection and dynamic changing content
     * via data provider.
     */
    private List<?> currentCanonicalValues;

    /**
     * Create a new single selection {@link ComboBoxCellEditor} based on the
     * given list of items, showing the default number of items in the dropdown
     * of the combo.
     *
     * @param canonicalValues
     *            Array of items to be shown in the drop down box. These will be
     *            converted using the {@link IDisplayConverter} for display
     *            purposes
     */
    public ComboBoxCellEditor(List<?> canonicalValues) {
        this(canonicalValues, NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
    }

    /**
     * Create a new single selection {@link ComboBoxCellEditor} based on the
     * given list of items, showing the default number of items in the dropdown
     * of the combo.
     *
     * @param canonicalValues
     *            Array of items to be shown in the drop down box. These will be
     *            converted using the {@link IDisplayConverter} for display
     *            purposes
     *
     * @since 1.5
     */
    public ComboBoxCellEditor(Object... canonicalValues) {
        this(Arrays.asList(canonicalValues), NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
    }

    /**
     * Create a new single selection {@link ComboBoxCellEditor} based on the
     * given list of items.
     *
     * @param canonicalValues
     *            Array of items to be shown in the drop down box. These will be
     *            converted using the {@link IDisplayConverter} for display
     *            purposes
     * @param maxVisibleItems
     *            The maximum number of items the drop down will show before
     *            introducing a scroll bar.
     */
    public ComboBoxCellEditor(List<?> canonicalValues, int maxVisibleItems) {
        this.canonicalValues = canonicalValues;
        this.maxVisibleItems = maxVisibleItems;
    }

    /**
     * Create a new single selection {@link ComboBoxCellEditor} based on the
     * given {@link IComboBoxDataProvider}, showing the default number of items
     * in the dropdown of the combo.
     *
     * @param dataProvider
     *            The {@link IComboBoxDataProvider} that is responsible for
     *            populating the items to the dropdown box. This is the way to
     *            use a ComboBoxCellEditor with dynamic content.
     */
    public ComboBoxCellEditor(IComboBoxDataProvider dataProvider) {
        this(dataProvider, NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
    }

    /**
     * Create a new single selection {@link ComboBoxCellEditor} based on the
     * given {@link IComboBoxDataProvider}.
     *
     * @param dataProvider
     *            The {@link IComboBoxDataProvider} that is responsible for
     *            populating the items to the dropdown box. This is the way to
     *            use a ComboBoxCellEditor with dynamic content.
     * @param maxVisibleItems
     *            The maximum number of items the drop down will show before
     *            introducing a scroll bar.
     */
    public ComboBoxCellEditor(IComboBoxDataProvider dataProvider, int maxVisibleItems) {
        this.dataProvider = dataProvider;
        this.maxVisibleItems = maxVisibleItems;
    }

    @Override
    protected Control activateCell(Composite parent, final Object originalCanonicalValue) {
        this.combo = createEditorControl(parent);

        // filling and populating a multiselect combo could take some time for
        // huge data sets
        BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
            @Override
            public void run() {
                fillCombo();

                setCanonicalValue(originalCanonicalValue);
            }
        });

        // open the dropdown immediately after the Text control of the NatCombo
        // is positioned
        if (this.editMode == EditModeEnum.INLINE) {
            this.combo.addTextControlListener(new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    ComboBoxCellEditor.this.combo.showDropdownControl(originalCanonicalValue instanceof Character);
                    ComboBoxCellEditor.this.combo.removeTextControlListener(this);
                }

                @Override
                public void controlMoved(ControlEvent e) {
                    ComboBoxCellEditor.this.combo.showDropdownControl(originalCanonicalValue instanceof Character);
                    ComboBoxCellEditor.this.combo.removeTextControlListener(this);
                }
            });
        }

        return this.combo;
    }

    /**
     * This implementation overrides the default implementation because we can
     * work on the list of canonical items in the combo directly. Only for
     * multiselect in combination with free editing, we need to convert here
     * ourself.
     */
    @Override
    public Object getCanonicalValue() {
        if (!this.multiselect) {
            // single selection handling
            int selectionIndex = this.combo.getSelectionIndex();

            // Item selected from list
            if (selectionIndex >= 0) {
                if (this.dataProvider != null) {
                    return this.dataProvider.getValues(getColumnIndex(), getRowIndex()).get(selectionIndex);
                } else {
                    return this.canonicalValues.get(selectionIndex);
                }
            } else {
                // if there is no selection in the dropdown, we need to check if
                // there is a free edit in the NatCombo control
                if (this.combo.getSelection().length > 0) {
                    return super.getCanonicalValue();
                }
            }
        } else {
            // multi selection handling
            int[] selectionIndices = this.combo.getSelectionIndices();

            // as we are in multiselection mode, we always return a Collection
            // and never null
            List<Object> result = new ArrayList<Object>();

            // Item selected from list
            if (selectionIndices.length > 0) {
                for (int i : selectionIndices) {
                    result.add(this.currentCanonicalValues.get(i));
                }
            } else {
                // if there is no selection in the dropdown, we need to check if
                // there is a free edit in the NatCombo control
                String[] comboSelection = this.combo.getSelection();
                if (comboSelection.length > 0) {
                    for (String selection : comboSelection) {
                        result.add(handleConversion(selection, this.conversionEditErrorHandler));
                    }
                }
            }

            // if nothing is selected and there is no free edit, we return an
            // empty Collection
            return result;
        }

        return null;
    }

    /**
     * This implementation overrides the default implementation because of the
     * special handling for comboboxes. It can handle multi selection and needs
     * to transfer the converted values into a String array so the values in the
     * combobox can be selected.
     *
     * @param canonicalValue
     *            The canonical value to be set to the wrapped editor control.
     */
    @Override
    public void setCanonicalValue(Object canonicalValue) {
        if (canonicalValue != null) {
            String[] editorValues = null;
            if (canonicalValue instanceof List<?>) {
                List<?> temp = (List<?>) canonicalValue;
                String[] result = new String[temp.size()];
                for (int i = 0; i < temp.size(); i++) {
                    result[i] = (String) this.displayConverter.canonicalToDisplayValue(
                            this.layerCell, this.configRegistry, temp.get(i));
                }
                editorValues = result;
            } else {
                // in case the SELECT_ALL value is set for selecting all values
                // in the combo we don't need a conversion and use the value
                if (EditConstants.SELECT_ALL_ITEMS_VALUE.equals(canonicalValue)) {
                    editorValues = new String[] { canonicalValue.toString() };
                } else {
                    editorValues = new String[] {
                            (String) this.displayConverter.canonicalToDisplayValue(
                                    this.layerCell,
                                    this.configRegistry,
                                    canonicalValue) };
                }
            }
            setEditorValue(editorValues);
        }
    }

    /**
     * Will set the items selectable in the combo dependent on the configuration
     * of this {@link ComboBoxCellEditor}. As the combo is only able to handle
     * Strings in the combo itself, and this editor works directly on the
     * canonical values, the values are converted in here too.
     */
    private void fillCombo() {
        List<String> displayValues = new ArrayList<String>();

        if (this.dataProvider != null) {
            this.currentCanonicalValues = this.dataProvider.getValues(getColumnIndex(), getRowIndex());
        } else {
            this.currentCanonicalValues = this.canonicalValues;
        }

        for (Object canonicalValue : this.currentCanonicalValues) {
            Object displayValue = this.displayConverter.canonicalToDisplayValue(
                    this.layerCell, this.configRegistry, canonicalValue);
            displayValues.add(displayValue != null ? displayValue.toString() : ""); //$NON-NLS-1$
        }

        this.combo.setItems(displayValues.toArray(ArrayUtil.STRING_TYPE_ARRAY));
    }

    @Override
    public void close() {
        super.close();
        this.currentCanonicalValues = null;
    }

    @Override
    public Object getEditorValue() {
        if (!this.multiselect) {
            return this.combo.getSelection()[0];
        }
        return this.combo.getSelection();
    }

    @Override
    public void setEditorValue(Object value) {
        this.combo.setSelection((String[]) value);
    }

    @Override
    public NatCombo getEditorControl() {
        return this.combo;
    }

    @Override
    public NatCombo createEditorControl(Composite parent) {
        int style = SWT.NONE;
        if (!this.freeEdit) {
            style |= SWT.READ_ONLY;
        }
        if (this.multiselect) {
            style |= SWT.MULTI;
        }
        if (this.useCheckbox) {
            style |= SWT.CHECK;
        }
        final NatCombo combo = (this.iconImage == null)
                ? new NatCombo(parent, this.cellStyle, this.maxVisibleItems, style, this.showDropdownFilter)
                : new NatCombo(parent, this.cellStyle, this.maxVisibleItems, style, this.iconImage, this.showDropdownFilter);

        combo.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));

        if (this.multiselect) {
            combo.setMultiselectValueSeparator(this.multiselectValueSeparator);
            combo.setMultiselectTextBracket(this.multiselectTextPrefix, this.multiselectTextSuffix);
        }

        addNatComboListener(combo);
        return combo;
    }

    /**
     * Registers special listeners to the {@link NatCombo} regarding the
     * {@link EditModeEnum}, that are needed to commit/close or change the
     * visibility state of the {@link NatCombo} dependent on UI interactions.
     *
     * @param combo
     *            The {@link NatCombo} to add the listeners to.
     */
    protected void addNatComboListener(final NatCombo combo) {
        combo.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                if ((event.keyCode == SWT.CR)
                        || (event.keyCode == SWT.KEYPAD_CR)) {
                    commit(MoveDirectionEnum.NONE, ComboBoxCellEditor.this.editMode == EditModeEnum.INLINE);
                } else if (event.keyCode == SWT.ESC) {
                    if (ComboBoxCellEditor.this.editMode == EditModeEnum.INLINE) {
                        close();
                    } else {
                        combo.hideDropdownControl();
                    }
                }
            }

        });

        combo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                commit(MoveDirectionEnum.NONE,
                        (!ComboBoxCellEditor.this.multiselect && ComboBoxCellEditor.this.editMode == EditModeEnum.INLINE));
                if (!ComboBoxCellEditor.this.multiselect && ComboBoxCellEditor.this.editMode == EditModeEnum.DIALOG) {
                    // hide the dropdown after a value was selected in the combo
                    // in a dialog
                    combo.hideDropdownControl();
                }
            }
        });

        if (this.editMode == EditModeEnum.INLINE) {
            combo.addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    close();
                }
            });
        }

        if (this.editMode == EditModeEnum.DIALOG) {
            combo.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    combo.hideDropdownControl();
                }
            });
        }
    }

    /**
     * Selects the item at the given zero-relative index in the receiver's list.
     * If the item at the index was already selected, it remains selected.
     * Indices that are out of range are ignored.
     *
     * @param index
     *            the index of the item to select
     *
     * @see org.eclipse.swt.widgets.List#select(int)
     */
    public void select(int index) {
        this.combo.select(index);
    }

    /**
     * Selects the items at the given zero-relative indices in the receiver. The
     * current selection is not cleared before the new items are selected.
     * <p>
     * If the item at a given index is not selected, it is selected. If the item
     * at a given index was already selected, it remains selected. Indices that
     * are out of range and duplicate indices are ignored. If the receiver is
     * single-select and multiple indices are specified, then all indices are
     * ignored.
     *
     * @param indices
     *            the array of indices for the items to select
     *
     * @see org.eclipse.swt.widgets.List#select(int[])
     */
    public void select(int[] indices) {
        this.combo.select(indices);
    }

    /**
     * @param multiselectValueSeparator
     *            String that should be used to separate values in the String
     *            representation showed in the text control if multiselect is
     *            supported. <code>null</code> to use the default value
     *            separator.
     * @see NatCombo#DEFAULT_MULTI_SELECT_VALUE_SEPARATOR
     */
    public void setMultiselectValueSeparator(String multiselectValueSeparator) {
        this.multiselectValueSeparator = multiselectValueSeparator;
    }

    /**
     * Set the prefix and suffix that will parenthesize the text that is created
     * out of the selected values if this NatCombo supports multiselection.
     *
     * @param multiselectTextPrefix
     *            String that should be used to prefix the generated String
     *            representation showed in the text control if multiselect is
     *            supported. <code>null</code> to use the default prefix.
     * @param multiselectTextSuffix
     *            String that should be used to suffix the generated String
     *            representation showed in the text control if multiselect is
     *            supported. <code>null</code> to use the default suffix.
     * @see NatCombo#DEFAULT_MULTI_SELECT_PREFIX
     * @see NatCombo#DEFAULT_MULTI_SELECT_SUFFIX
     */
    public void setMultiselectTextBracket(String multiselectTextPrefix, String multiselectTextSuffix) {
        this.multiselectTextPrefix = multiselectTextPrefix;
        this.multiselectTextSuffix = multiselectTextSuffix;
    }

    /**
     * @return The image that is used as overlay to the {@link Text} Control if
     *         the dropdown is visible. It will indicate that the control is an
     *         open combo to the user. If this value is <code>null</code> the
     *         default image specified in NatCombo will be used.
     */
    public Image getIconImage() {
        return this.iconImage;
    }

    /**
     * @param iconImage
     *            The image to use as overlay to the {@link Text} Control if the
     *            dropdown is visible. It will indicate that the control is an
     *            open combo to the user. If this value is <code>null</code> the
     *            default image specified in NatCombo will be used.
     */
    public void setIconImage(Image iconImage) {
        this.iconImage = iconImage;
    }

    /**
     * @return <code>true</code> if this ComboBoxCellEditor supports free
     *         editing in the text control of the NatCombo or not. By default
     *         free editing is disabled.
     */
    public boolean isFreeEdit() {
        return this.freeEdit;
    }

    /**
     * @param freeEdit
     *            <code>true</code> to indicate that this ComboBoxCellEditor
     *            supports free editing in the text control of the NatCombo,
     *            <code>false</code> if not.
     */
    public void setFreeEdit(boolean freeEdit) {
        this.freeEdit = freeEdit;
    }

    /**
     * @return <code>true</code> if this ComboBoxCellEditor supports multiple
     *         selection or not. By default multiple selection is disabled.
     */
    public boolean isMultiselect() {
        return this.multiselect;
    }

    /**
     * @param multiselect
     *            <code>true</code> to indicate that this ComboBoxCellEditor
     *            supports multiple selection, <code>false</code> if not.
     */
    public void setMultiselect(boolean multiselect) {
        this.multiselect = multiselect;
    }

    /**
     * @return <code>true</code> if this ComboBoxCellEditor shows checkboxes for
     *         items in the dropdown. By default there are not checkboxes shown.
     */
    public boolean isUseCheckbox() {
        return this.useCheckbox;
    }

    /**
     * @param useCheckbox
     *            <code>true</code> if this ComboBoxCellEditor should show
     *            checkboxes for items in the dropdown, <code>false</code> if
     *            not.
     */
    public void setUseCheckbox(boolean useCheckbox) {
        this.useCheckbox = useCheckbox;
    }

    /**
     * @return <code>true</code> if this ComboBoxCellEditor should show the text
     *         control for filtering items in the dropdown. By default the
     *         filter is not shown.
     *
     * @since 1.4
     */
    public boolean isShowDropdownFilter() {
        return this.showDropdownFilter;
    }

    /**
     * @param showDropdownFilter
     *            <code>true</code> if this ComboBoxCellEditor should show the
     *            text control for filtering items in the dropdown,
     *            <code>false</code> if not.
     *
     * @since 1.4
     */
    public void setShowDropdownFilter(boolean showDropdownFilter) {
        this.showDropdownFilter = showDropdownFilter;
    }
}
