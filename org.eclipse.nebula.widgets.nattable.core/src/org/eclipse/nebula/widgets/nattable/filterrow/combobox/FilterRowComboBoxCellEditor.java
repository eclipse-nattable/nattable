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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.data.convert.ConversionFailedException;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.nebula.widgets.nattable.widget.NatCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialisation of ComboBoxCellEditor that can only be created using an
 * IComboBoxDataProvider. Will show a multiselect combobox with checkboxes and
 * uses the FilterNatCombo as underlying control.
 *
 * @see FilterRowComboBoxDataProvider
 */
public class FilterRowComboBoxCellEditor extends ComboBoxCellEditor {

    private static final Logger LOG = LoggerFactory.getLogger(FilterRowComboBoxCellEditor.class);

    /**
     * This object remembers the current value in the editor. This is necessary
     * to avoid double commits on closing the editor again.
     * <p>
     * Per default editors commit their values prior to closing them. The commit
     * call is performed by various actions that get triggered when the editor
     * losses focus. As this editor is configured to commit the value on each
     * click of a checkbox, we avoid committing the value again on closing.
     */
    private Object currentCanonicalValue = null;

    /**
     * Flag to configure if on filtering the combobox content, a filter on the
     * content list should be applied based on the current visible items.
     * Default is <code>false</code>.
     *
     * @since 2.1
     */
    private boolean applyFilterOnDropdownFilter = false;
    /**
     * Flag to configure if the editor should be closed on pressing ENTER when
     * having focus in the combobox filter control. Default is
     * <code>false</code>.
     *
     * @since 2.1
     */
    private boolean closeOnEnterInDropdownFilter = false;

    /**
     * Collection of selected items that are currently not visible in the combo.
     * Only needed in case an IComboBoxDataProvider is configured that provided
     * only items that are currently visible in the table to support iterative
     * filtering. This list will be added to the canonical values on commit to
     * avoid that the items that are not shown in the combo get unselected in a
     * filter process.
     *
     * @since 2.2
     */
    private List<?> notVisibleSelected;

    /**
     * Special resize listener that gets added to the parent control at editor
     * control creation to fix the positioning of editor control and dropdown
     * shell.
     * <p>
     * If the NatTable is scrolled to the very right position, and a filter is
     * applied that causes to hide the vertical scrollbar, the rendering gets
     * into an inconsistent state. The columns are moved to the right to fill
     * the space of the hidden scrollbar, but the editor and the shell stay at
     * the current position. This results in an inconsistent rendering state,
     * which is fixed with this listener.
     * </p>
     *
     * @since 2.2
     */
    private Listener resizeListener = new Listener() {

        @Override
        public void handleEvent(Event event) {
            ILayer layer = FilterRowComboBoxCellEditor.this.layerCell.getLayer();
            ILayerCell cell =
                    layer.getCellByPosition(
                            getColumnPosition(),
                            getRowPosition());
            if (!cell.getBounds().equals(FilterRowComboBoxCellEditor.this.layerCell.getBounds())) {
                Rectangle cellBounds = cell.getBounds();
                Rectangle editorBounds = layer.getLayerPainter().adjustCellBounds(
                        getColumnPosition(),
                        getRowPosition(),
                        new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height));

                final NatCombo editorControl = getEditorControl();

                editorBounds = calculateControlBounds(editorBounds);

                // TODO introduce more generic way to identify the border width
                // the currently fixed border width of 1 is handled
                // because of the fixed border width possibly applied via
                // NatTableBorderOverlayPainter
                if (editorBounds.x == 0) {
                    editorBounds.x += 1;
                    editorBounds.width -= 1;
                }

                if (editorControl != null && !editorControl.isDisposed()) {
                    editorControl.setBounds(editorBounds);
                    editorControl.showDropdownControl();
                }
            }
        }
    };

    /**
     * Create a new {@link FilterRowComboBoxCellEditor} based on the given
     * {@link IComboBoxDataProvider}, showing the default number of items in the
     * dropdown of the combo.
     *
     * @param dataProvider
     *            The {@link IComboBoxDataProvider} that is responsible for
     *            populating the items to the dropdown box.
     */
    public FilterRowComboBoxCellEditor(IComboBoxDataProvider dataProvider) {
        this(dataProvider, NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
    }

    /**
     * Create a new {@link FilterRowComboBoxCellEditor} based on the given
     * {@link IComboBoxDataProvider}.
     *
     * @param dataProvider
     *            The {@link IComboBoxDataProvider} that is responsible for
     *            populating the items to the dropdown box.
     * @param maxVisibleItems
     *            The maximum number of items the drop down will show before
     *            introducing a scroll bar.
     */
    public FilterRowComboBoxCellEditor(IComboBoxDataProvider dataProvider, int maxVisibleItems) {
        super(dataProvider, maxVisibleItems);
        this.multiselect = true;
        this.useCheckbox = true;
    }

    @Override
    public NatCombo createEditorControl(Composite parent) {
        int style = SWT.READ_ONLY | SWT.MULTI | SWT.CHECK;
        final FilterNatCombo combo = this.iconImage == null
                ? new FilterNatCombo(parent, this.cellStyle, this.maxVisibleItems, style, this.showDropdownFilter)
                : new FilterNatCombo(parent, this.cellStyle, this.maxVisibleItems, style, this.iconImage, this.showDropdownFilter);

        if (this.freeEdit) {
            combo.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_IBEAM));
        } else {
            combo.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_ARROW));
        }

        combo.setMultiselectValueSeparator(this.multiselectValueSeparator);
        combo.setMultiselectTextBracket(this.multiselectTextPrefix, this.multiselectTextSuffix);

        addNatComboListener(combo);

        // additionally add the ICheckStateListener so on changing the value of
        // the select all item the change is also committed
        combo.addCheckStateListener(event -> {
            if (event.getChecked()) {
                setEditorValue(new String[] { EditConstants.SELECT_ALL_ITEMS_VALUE });
            }
            commit(MoveDirectionEnum.NONE,
                    (!FilterRowComboBoxCellEditor.this.multiselect
                            && FilterRowComboBoxCellEditor.this.editMode == EditModeEnum.INLINE));
        });

        if (this.applyFilterOnDropdownFilter) {
            combo.setFilterModifyAction(() -> {
                commit(MoveDirectionEnum.NONE,
                        (!FilterRowComboBoxCellEditor.this.multiselect
                                && FilterRowComboBoxCellEditor.this.editMode == EditModeEnum.INLINE));
            });
        }

        if (this.closeOnEnterInDropdownFilter) {
            combo.setDropdownFilterKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent event) {
                    if (event.keyCode == SWT.CR
                            || event.keyCode == SWT.KEYPAD_CR
                            || event.keyCode == SWT.ESC) {
                        close();
                    }
                }
            });
        }

        parent.addListener(SWT.Resize, this.resizeListener);

        return combo;
    }

    @Override
    public void close() {
        super.close();
        // remove the special resize listener on the parent on close
        this.parent.removeListener(SWT.Resize, this.resizeListener);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setCanonicalValue(Object canonicalValue) {
        this.currentCanonicalValue = canonicalValue;

        if (getComboBoxDataProvider() != null
                && getComboBoxDataProvider() instanceof FilterRowComboBoxDataProvider
                && ((FilterRowComboBoxDataProvider) getComboBoxDataProvider()).getFilterCollection() != null) {

            // calculate the diff between the currently visible items and all
            // available items
            List<?> allValues = ((FilterRowComboBoxDataProvider) getComboBoxDataProvider()).getAllValues(getColumnIndex());
            List<?> visibleValues = getComboBoxDataProvider().getValues(getColumnIndex(), getRowIndex());
            List<?> diffValues = new ArrayList<>(allValues);
            diffValues.removeAll(visibleValues);

            // ensure that items that are not selected don't get added, to avoid
            // that they get selected on filtering
            if (canonicalValue instanceof Collection) {
                Collection cValues = (Collection) canonicalValue;
                for (Iterator<?> it = diffValues.iterator(); it.hasNext();) {
                    Object object = it.next();
                    if (!cValues.contains(object)) {
                        it.remove();
                    }
                }
            }

            // store the real values instead of the placeholder here
            if (EditConstants.SELECT_ALL_ITEMS_VALUE.equals(canonicalValue)) {
                this.currentCanonicalValue = allValues;
            }

            // convert the not visible but selected items so they can be simply
            // added in getCanonicalValue() in the commit process
            this.notVisibleSelected = diffValues.stream()
                    .map(v -> handleConversion(v, this.conversionEditErrorHandler))
                    .collect(Collectors.toList());
        }

        super.setCanonicalValue(canonicalValue);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getCanonicalValue() {
        // add the currently not visible selected canonical values to avoid that
        // the filter changes
        Object canonicalValue = super.getCanonicalValue();
        if (canonicalValue instanceof Collection && this.notVisibleSelected != null) {
            ((Collection) canonicalValue).addAll(this.notVisibleSelected);
        }
        return canonicalValue;
    }

    @Override
    public boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit) {
        // If the editor should be closed after commit, we first need to ensure
        // if the value has changed since the last commit.
        // This needs to be done because in this filter combo box, every
        // selection immediately causes a commit, which results in applying a
        // filter. If the combo box is now closed because of losing the focus,
        // the value gets committed again, which again results in filtering,
        // which will lead to exceptions because the states are not synchronous
        // anymore.

        // avoid commit if combo filter is active
        boolean dropdownFilterActive = false;
        NatCombo editorControl = getEditorControl();
        if (editorControl != null
                && editorControl instanceof FilterNatCombo
                && ((FilterNatCombo) editorControl).isFilterActive()) {
            dropdownFilterActive = true;
        }

        if (dropdownFilterActive) {
            if (closeAfterCommit) {
                close();
            }
            return true;
        }

        if (!isClosed()) {
            try {
                Object canonicalValue = getCanonicalValue();
                if (!canonicalValuesEquals(canonicalValue)) {
                    if (super.commit(direction, closeAfterCommit)) {
                        this.currentCanonicalValue = canonicalValue;

                        // we also have to update the initial selection in case
                        // it is set
                        if (!dropdownFilterActive
                                && ((FilterNatCombo) editorControl).initialSelection != null) {
                            ((FilterNatCombo) editorControl).initialSelection = editorControl.getSelection();
                        }

                        return true;
                    }
                } else {
                    // the values are the same so it is not necessary to commit
                    // again
                    if (closeAfterCommit) {
                        close();
                    }
                    return true;
                }
            } catch (ConversionFailedException e) {
                // do nothing as exceptions caused by conversion are handled
                // already. we just need this catch block for stopping the
                // process if conversion failed with an exception
            } catch (Exception e) {
                // if another exception occured that wasn't thrown by us, it
                // should at least be logged without killing the whole
                // application
                LOG.error("Error on updating cell value: {}", e.getLocalizedMessage(), e); //$NON-NLS-1$
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private boolean canonicalValuesEquals(Object canonicalValue) {
        if (canonicalValue != null && this.currentCanonicalValue == null) {
            return false;
        }

        if (canonicalValue == null && this.currentCanonicalValue != null) {
            return false;
        }

        if (canonicalValue != null && this.currentCanonicalValue != null) {
            if (canonicalValue instanceof Collection && !(this.currentCanonicalValue instanceof Collection)) {
                return false;
            }

            if (!(canonicalValue instanceof Collection) && this.currentCanonicalValue instanceof Collection) {
                return false;
            }

            if (canonicalValue instanceof Collection && this.currentCanonicalValue instanceof Collection) {
                return ObjectUtils.collectionsEqual(
                        (Collection) canonicalValue,
                        (Collection) this.currentCanonicalValue);
            } else {
                return canonicalValue.equals(this.currentCanonicalValue);
            }
        }

        // should only happen if canonicalValue and currentCanonicalValue are
        // null
        return true;
    }

    /**
     * This method will activate the usage of the dropdown filter via setting
     * {@link #setShowDropdownFilter(boolean)} to <code>true</code>.
     * Additionally it is possible to configure behavior like whether a filter
     * should be applied to the content on filtering the dropdown or if the
     * editor should be closed on pressing ENTER when having the focus in the
     * dropdown filter control.
     *
     * @param applyFilter
     *            <code>true</code> if on filtering the combobox content, a
     *            filter on the list should be applied based on the current
     *            visible items, <code>false</code> if only the dropdown content
     *            should be filtered without applying a filter (default).
     * @param closeOnEnter
     *            <code>true</code> if the editor should be closed on pressing
     *            ENTER when having focus in the combobox filter control,
     *            <code>false</code> if nothing should happen (default).
     *
     * @see #setShowDropdownFilter(boolean)
     *
     * @since 2.1
     */
    public void configureDropdownFilter(boolean applyFilter, boolean closeOnEnter) {
        setShowDropdownFilter(true);
        this.applyFilterOnDropdownFilter = applyFilter;
        this.closeOnEnterInDropdownFilter = closeOnEnter;
    }

    /**
     *
     * @return <code>true</code> if on filtering the combobox content, a filter
     *         on the list is applied based on the current visible items,
     *         <code>false</code> if only the dropdown content is filtered
     *         without applying a filter (default).
     *
     * @see #configureDropdownFilter(boolean, boolean)
     * @since 2.2
     */
    protected boolean isApplyFilterOnDropdownFilter() {
        return this.applyFilterOnDropdownFilter;
    }

    /**
     *
     * @return <code>true</code> if the editor is closed on pressing ENTER when
     *         having focus in the combobox filter control, <code>false</code>
     *         if nothing happens (default).
     *
     * @see #configureDropdownFilter(boolean, boolean)
     * @since 2.2
     */
    protected boolean isCloseOnEnterInDropdownFilter() {
        return this.closeOnEnterInDropdownFilter;
    }
}
