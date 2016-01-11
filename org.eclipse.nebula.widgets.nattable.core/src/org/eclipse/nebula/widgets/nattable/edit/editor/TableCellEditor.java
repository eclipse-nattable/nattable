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
package org.eclipse.nebula.widgets.nattable.edit.editor;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.data.convert.ConversionFailedException;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.painter.cell.TableCellPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * ICellEditor that uses a JFace TableViewer as editor control. In combination
 * with the TableCellPainter it can be used to simulate a table within a cell to
 * support editing of collections or arrays of values of an entity. This
 * implementation is a workaround for the missing feature of nested tables in
 * NatTable 1.x architecture. It is limited to one column editing. If a more
 * complex nested table structure is needed, you need to extend or implement a
 * new table cell editor.
 * <p>
 * Note: This editor is different to other default editors in NatTable in
 * several facts:
 * <ul>
 * <li>The data value in the NatTable cell needs to be an array or a collection
 * of values</li>
 * <li><code>getCanonicalValue()</code> will directly update the underlying
 * NatTable data model</li>
 * <li>Committing the value in the NatTable framework code will simply replace
 * the list reference with itself</li>
 * <li>It does only support validation error styling for conversion and
 * validation errors aswell</li>
 * </ul>
 *
 * @see TableCellPainter
 */
public class TableCellEditor extends AbstractCellEditor {

    /**
     * The internal control that is used for editing a table.
     */
    private TableViewer viewer;
    /**
     * Flag to configure whether the selection should move after a value was
     * committed by pressing enter.
     */
    private final boolean moveSelectionOnEnter;
    /**
     * Flag to configure whether the adjacent editor on selection movements
     * should always open the cell for editing instead of moving into the
     * selection state of the table control.
     */
    private final boolean alwaysOpenEditor;
    /**
     * The height of the sub cells to use. Setting a value >= 0 will result in
     * using the specified fixed sub cell heights, a negative value will result
     * in using the OS default height based on the font.
     * <p>
     * Because of limitations in the native table control for some OS, it is not
     * possible to specify different row heights.
     */
    private int fixedSubCellHeight;
    /**
     * Internal focus listener to handle committing and closing of this editor
     * if the focus is lost out of the editor control AND the editing support
     * editor control.
     */
    private FocusListener focusListener = new InternalFocusListener();
    /**
     * Internal ColumnLabelProvider that is used to apply styling to the table
     * viewer aswell as for the editor controls in the table viewer.
     */
    private InternalLabelProvider labelProvider;
    /**
     * Internal ICellEditorValidator that checks if a value is valid based on
     * conversion and validation rules applied via NatTable IDisplayConverter
     * and IDataValidator
     */
    protected ICellEditorValidator cellEditorValidator = new ICellEditorValidator() {
        @Override
        public String isValid(Object value) {
            // add validator to check conversion and validation configured in
            // NatTable
            if (TableCellEditor.this.displayConverter != null) {
                Object cValue = null;
                try {
                    // check if the information can be converted to the correct
                    // type
                    cValue = TableCellEditor.this.displayConverter.displayToCanonicalValue(
                            TableCellEditor.this.layerCell, TableCellEditor.this.configRegistry, value);

                    if (!validateCanonicalValue(cValue)) {
                        return Messages
                                .getString("AbstractCellEditor.validationFailure"); //$NON-NLS-1$
                    }
                } catch (ConversionFailedException e) {
                    return e.getLocalizedMessage();
                } catch (ValidationFailedException e) {
                    return e.getLocalizedMessage();
                }
            }
            return null;
        }
    };

    /**
     * Creates a TableCellEditor with a default sub cell height of 20 (which is
     * the same as the default for the TableCellPainter), that moves the
     * selection on committing the value with enter and always opens the editor
     * of the cell that is currently selected.
     */
    public TableCellEditor() {
        this(20, true, true);
    }

    /**
     * Creates a TableCellEditor with the given configurations.
     *
     * @param fixedSubCellHeight
     *            The height of the sub cells to use. Setting a value &gt;= 0
     *            will result in using the specified fixed sub cell heights, a
     *            negative value will result in using the OS default height
     *            based on the font. Note that because of limitations in the
     *            native table control for some OS, it is not possible to
     *            specify different row heights.
     * @param moveSelectionOnEnter
     *            configure whether the selection should move after a value was
     *            committed by pressing enter.
     * @param alwaysOpenEditor
     *            configure whether the adjacent editor on selection movements
     *            should always open the cell for editing instead of moving into
     *            the selection state of the table control.
     */
    public TableCellEditor(int fixedSubCellHeight,
            boolean moveSelectionOnEnter, boolean alwaysOpenEditor) {
        this.setFixedSubCellHeight(fixedSubCellHeight);
        this.moveSelectionOnEnter = moveSelectionOnEnter;
        this.alwaysOpenEditor = alwaysOpenEditor;
    }

    @Override
    public Object getEditorValue() {
        return this.viewer.getInput();
    }

    @Override
    public void setEditorValue(Object value) {
        if (value != null && value.getClass().isArray()) {
            this.viewer.setInput(value);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getCanonicalValue() {
        Object[] editorValues = getDataAsArray(getEditorValue());
        // perform conversion
        Object[] dataValues = new Object[editorValues.length];
        Object canonicalValue;
        for (int i = 0; i < editorValues.length; i++) {
            Object value = editorValues[i];
            if (this.displayConverter != null) {
                canonicalValue = this.displayConverter.displayToCanonicalValue(
                        this.layerCell, this.configRegistry, value);
            } else {
                canonicalValue = value;
            }
            dataValues[i] = canonicalValue;
        }

        if (this.layerCell.getDataValue().getClass().isArray()) {
            Object[] cellDataArray = (Object[]) this.layerCell.getDataValue();
            for (int i = 0; i < cellDataArray.length; i++) {
                cellDataArray[i] = dataValues[i];
            }
        } else if (this.layerCell.getDataValue() instanceof Collection) {
            // we don't create new collections, we operate on the existing
            // this is because we don't know the exact collection implementation
            // that we would need to create for type safety and performing an
            // instanceof check for every possible collection implementation
            // would be to complicated and could never be complete
            Collection cellDataCollection = (Collection) this.layerCell
                    .getDataValue();
            cellDataCollection.clear();
            cellDataCollection.addAll(Arrays.asList(dataValues));
        }
        return this.layerCell.getDataValue();
    }

    @Override
    public void setCanonicalValue(Object canonicalValue) {
        Object[] values = getDataAsArray(canonicalValue);
        if (values != null) {
            // transform the array of canonical data values to an array of
            // display values
            ValueWrapper[] editorValues = new ValueWrapper[values.length];
            Object displayValue;
            for (int i = 0; i < values.length; i++) {
                Object value = values[i];
                if (this.displayConverter != null) {
                    displayValue = this.displayConverter
                            .canonicalToDisplayValue(this.layerCell,
                                    this.configRegistry, value);
                } else {
                    displayValue = value;
                }
                editorValues[i] = new ValueWrapper("" + displayValue); //$NON-NLS-1$
            }
            setEditorValue(editorValues);
        }
    }

    @Override
    public Table getEditorControl() {
        return this.viewer.getTable();
    }

    @Override
    public Table createEditorControl(Composite parent) {
        // need to directly set the member variable because a TableViewer is not
        // a Control
        // and therefore we can not return the TableViewer here
        this.viewer = new TableViewer(parent, SWT.FULL_SELECTION);
        this.viewer.setContentProvider(ArrayContentProvider.getInstance());

        // this column is simply added because of the restriction that the first
        // column
        // in a table is always left aligned
        TableViewerColumn emptyColumn = new TableViewerColumn(this.viewer, SWT.NONE);
        emptyColumn.getColumn().setWidth(0);
        emptyColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ""; //$NON-NLS-1$
            }
        });

        TableViewerColumn singleColumn = new TableViewerColumn(this.viewer, SWT.NONE);
        singleColumn.getColumn().setAlignment(
                HorizontalAlignmentEnum.getSWTStyle(this.cellStyle));
        singleColumn.setLabelProvider(this.labelProvider);

        singleColumn.setEditingSupport(getEditingSupport());

        // set style information configured in the associated cell style
        final Table tableControl = this.viewer.getTable();
        tableControl.setBackground(this.cellStyle
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));

        tableControl.setLinesVisible(true);

        tableControl.addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // +1 because of the grid lines
                event.height = TableCellEditor.this.fixedSubCellHeight + 1;
            }
        });

        // add a key listener that will close the editor on pressing ESC
        tableControl.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.keyCode == SWT.ESC && event.stateMask == 0) {
                    close();
                }
                if (event.keyCode == SWT.F2) {
                    Object element = ((StructuredSelection) TableCellEditor.this.viewer
                            .getSelection()).getFirstElement();
                    if (element == null) {
                        tableControl.setSelection(tableControl.getTopIndex());
                        element = ((StructuredSelection) TableCellEditor.this.viewer.getSelection())
                                .getFirstElement();
                    }
                    if (element != null)
                        TableCellEditor.this.viewer.editElement(element, 1);
                }
            }
        });

        tableControl.addListener(SWT.Resize, new Listener() {

            @Override
            public void handleEvent(Event event) {
                // it is the second column, because the first column is not
                // visible as we want to
                // support alignment in the cells
                tableControl.getColumn(1).setWidth(
                        tableControl.getClientArea().width);

                // set focus on the table viewer and select the cell where the
                // mouse is positioned
                // this can only be done when the control is resized, as the
                // bounds are set in
                // the EditController dependent on the rendered cell
                Point mouseLoc = Display.getCurrent().getCursorLocation();
                Point tablePos = tableControl.toDisplay(0, 0);
                int relativeX = mouseLoc.x - tablePos.x;
                int relativeY = mouseLoc.y - tablePos.y;
                TableItem item = tableControl.getItem(new Point(relativeX,
                        relativeY));
                if (item != null)
                    tableControl.setSelection(item);
                else
                    tableControl.setSelection(tableControl.getTopIndex());

                // directly enable editing of the selected item
                if (tableControl.getItemCount() > 0)
                    TableCellEditor.this.viewer.editElement(((StructuredSelection) TableCellEditor.this.viewer
                            .getSelection()).getFirstElement(), 1);
            }
        });

        return tableControl;
    }

    @Override
    protected Control activateCell(Composite parent,
            Object originalCanonicalValue) {
        this.labelProvider = new InternalLabelProvider();

        createEditorControl(parent);

        setCanonicalValue(originalCanonicalValue);

        getEditorControl().forceFocus();

        return getEditorControl();
    }

    @Override
    public void close() {
        // this check is added to ensure that an open cell editor is also
        // committed if
        // the user clicks in another cell after editing within the table cell
        // editor
        // otherwise the framework performs a commit and close BEFORE the cell
        // editor
        // of the table viewer commits the value
        if (!this.viewer.isCellEditorActive()) {
            super.close();
        }
    }

    /**
     * Checks if the given data object is of type Collection or Array. Will
     * return the Collection or Array as Object[] or <code>null</code> if the
     * data object is not a Collection or Array.
     *
     * @param cellData
     *            The cellData that should be checked for its type.
     * @return The Object[] representation of the data object if it is of type
     *         Collection or Array, or <code>null</code> if the data object is
     *         not a Collection or Array.
     */
    protected Object[] getDataAsArray(Object cellData) {
        Object[] cellDataArray = null;
        if (cellData != null) {
            if (cellData.getClass().isArray()) {
                cellDataArray = (Object[]) cellData;
            } else if (cellData instanceof Collection) {
                Collection<?> cellDataCollection = (Collection<?>) cellData;
                cellDataArray = cellDataCollection.toArray();
            }
        }
        return cellDataArray;
    }

    /**
     * Note that because of limitations to native tables of the OS, it is not
     * possible to specify different row heights.
     *
     * @return The height of the sub cells to use. A value &gt;= 0 results in
     *         using the specified fixed sub cell heights, a negative value
     *         results in using the OS default height based on the font.
     */
    public int getFixedSubCellHeight() {
        return this.fixedSubCellHeight;
    }

    /**
     * Setting a value &gt;= 0 will result in using a fixed height of the sub
     * cells. Setting the value to a negative number will result in using the OS
     * default height based on the font.
     * <p>
     * Note that because of limitations to native tables of the OS, it is not
     * possible to specify different row heights.
     *
     * @param fixedSubCellHeight
     *            The height of the sub cells to use.
     */
    public void setFixedSubCellHeight(int fixedSubCellHeight) {
        this.fixedSubCellHeight = fixedSubCellHeight;
    }

    /**
     * @return The EditingSupport to use to make the TableViewer editable.
     */
    protected EditingSupport getEditingSupport() {
        return new TableCellEditingSupport();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: We need to override this to not register the FocusListener. This is
     * necessary because on editing the TableViewer, a Text control will be
     * created that gains focus. This would mean to close the editor and break
     * the whole use case.
     */
    @Override
    public void addEditorControlListeners() {
        Control editorControl = getEditorControl();
        if (editorControl != null && !editorControl.isDisposed()
                && this.editMode == EditModeEnum.INLINE) {
            editorControl.addTraverseListener(this.traverseListener);
            editorControl.addFocusListener(this.focusListener);
        }
    }

    @Override
    public void removeEditorControlListeners() {
        Control editorControl = getEditorControl();
        if (editorControl != null && !editorControl.isDisposed()) {
            editorControl.removeTraverseListener(this.traverseListener);
            editorControl.removeFocusListener(this.focusListener);
        }
    }

    /**
     * This class is needed to make editing work correctly within the
     * TableViewer. If we only work with the values themselves and the
     * collection contains the same values like for example the same Strings,
     * calling editElement() will always jump to the first element in the table
     * with that value instead of the selected one. With this wrapper we ensure
     * that the selected value is edited because we do not override equals() and
     * hashCode()
     */
    protected class ValueWrapper {
        private Object value;
        private boolean valid = true;

        protected ValueWrapper(Object value) {
            this.setValue(value);
        }

        public Object getValue() {
            return this.value;
        }

        public void setValue(Object value) {
            this.value = value;
            this.valid = TableCellEditor.this.cellEditorValidator.isValid(value) == null;
        }

        public boolean isValid() {
            return this.valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        @Override
        public String toString() {
            return this.value != null ? this.value.toString() : ""; //$NON-NLS-1$
        }
    }

    /**
     * EditingSupport to make the TableViewer control of this cell editor
     * editable. Will use a org.eclipse.jface.viewers.TextCellEditor for
     * editing.
     */
    protected class TableCellEditingSupport extends EditingSupport {

        private CellEditor editor;

        public TableCellEditingSupport() {
            super(TableCellEditor.this.viewer);
            this.editor = new org.eclipse.jface.viewers.TextCellEditor(
                    TableCellEditor.this.viewer.getTable());
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            this.editor = new org.eclipse.jface.viewers.TextCellEditor(
                    TableCellEditor.this.viewer.getTable());

            this.editor.setValidator(TableCellEditor.this.cellEditorValidator);

            this.editor.addListener(new ICellEditorListener() {

                @Override
                public void editorValueChanged(boolean oldValidState,
                        boolean newValidState) {
                    ((ValueWrapper) element).setValid(TableCellEditingSupport.this.editor.isValueValid());
                    TableCellEditor.this.labelProvider.applyCellStyle(TableCellEditingSupport.this.editor.getControl(), element);
                }

                @Override
                public void cancelEditor() {
                    close();
                }

                @Override
                public void applyEditorValue() {}
            });

            this.editor.getControl().addTraverseListener(new TraverseListener() {
                @Override
                public void keyTraversed(TraverseEvent event) {
                    if (event.keyCode == SWT.TAB) {
                        TableCellEditingSupport.this.setValue(element,
                                ((Text) TableCellEditingSupport.this.editor.getControl()).getText());

                        boolean committed = false;
                        if (event.stateMask == SWT.MOD2) {
                            committed = commit(MoveDirectionEnum.LEFT);
                        } else if (event.stateMask == 0) {
                            committed = commit(MoveDirectionEnum.RIGHT);
                        }
                        if (!committed) {
                            event.doit = false;
                        }
                    }
                }
            });

            this.editor.getControl().addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent event) {
                    if (event.keyCode == SWT.CR
                            || event.keyCode == SWT.KEYPAD_CR) {
                        TableCellEditingSupport.this.setValue(element,
                                ((Text) TableCellEditingSupport.this.editor.getControl()).getText());

                        if (TableCellEditingSupport.this.editor.isValueValid()) {
                            int selectionIndex = TableCellEditor.this.viewer.getTable()
                                    .getSelectionIndex();

                            // if move selection and not last item -> move the
                            // selection one down
                            if (TableCellEditor.this.moveSelectionOnEnter
                                    && selectionIndex + 1 < TableCellEditor.this.viewer.getTable().getItemCount()) {
                                selectionIndex++;
                                TableCellEditor.this.viewer.getTable().setSelection(selectionIndex);
                                if (TableCellEditor.this.alwaysOpenEditor) {
                                    TableCellEditor.this.viewer.editElement(
                                            ((StructuredSelection) TableCellEditor.this.viewer.getSelection()).getFirstElement(), 1);
                                }
                            } else {
                                commit(MoveDirectionEnum.NONE);
                            }
                        } else {
                            TableCellEditor.this.viewer.editElement(((StructuredSelection) TableCellEditor.this.viewer.getSelection()).getFirstElement(), 1);
                        }
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        TableCellEditingSupport.this.setValue(element,
                                ((Text) TableCellEditingSupport.this.editor.getControl()).getText());

                        int selectionIndex = TableCellEditor.this.viewer.getTable()
                                .getSelectionIndex();
                        if (selectionIndex + 1 < TableCellEditor.this.viewer.getTable()
                                .getItemCount()) {
                            selectionIndex++;
                        }
                        TableCellEditor.this.viewer.getTable().setSelection(selectionIndex);
                        if (TableCellEditor.this.alwaysOpenEditor) {
                            TableCellEditor.this.viewer.editElement(((StructuredSelection) TableCellEditor.this.viewer
                                    .getSelection()).getFirstElement(), 1);
                        }
                    } else if (event.keyCode == SWT.ARROW_UP) {
                        TableCellEditingSupport.this.setValue(element,
                                ((Text) TableCellEditingSupport.this.editor.getControl()).getText());

                        int selectionIndex = TableCellEditor.this.viewer.getTable()
                                .getSelectionIndex();
                        if (selectionIndex > 0) {
                            selectionIndex--;
                        }
                        TableCellEditor.this.viewer.getTable().setSelection(selectionIndex);
                        if (TableCellEditor.this.alwaysOpenEditor) {
                            TableCellEditor.this.viewer.editElement(((StructuredSelection) TableCellEditor.this.viewer
                                    .getSelection()).getFirstElement(), 1);
                        }
                    }
                }
            });

            this.editor.getControl().addFocusListener(TableCellEditor.this.focusListener);

            TableCellEditor.this.labelProvider.applyCellStyle(this.editor.getControl(), element);

            return this.editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            return ((ValueWrapper) element).getValue();
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (this.editor.isValueValid()) {
                ((ValueWrapper) element).setValue(value);
                ((ValueWrapper) element).setValid(true);
            } else {
                ((ValueWrapper) element).setValid(false);
            }
            TableCellEditor.this.viewer.refresh();
        }
    }

    /**
     * ColumnLabelProvider that determines the styles to use in the internal
     * editor control of the table viewer based on the registered styles in the
     * ConfigRegistry for default style, conversion error style and validation
     * error style.
     * <p>
     * If no explicit styles for conversion and validation errors are
     * configured, the foreground is simply rendered in red.
     */
    protected class InternalLabelProvider extends ColumnLabelProvider {

        private IStyle normalStyle;
        private IStyle conversionErrorStyle;
        private IStyle validationErrorStyle;

        public InternalLabelProvider() {
            this.normalStyle = TableCellEditor.this.cellStyle;
            this.conversionErrorStyle = TableCellEditor.this.configRegistry.getConfigAttribute(
                    EditConfigAttributes.CONVERSION_ERROR_STYLE,
                    DisplayMode.EDIT, TableCellEditor.this.labelStack.getLabels());

            if (this.conversionErrorStyle == null) {
                this.conversionErrorStyle = new Style();
                this.conversionErrorStyle.setAttributeValue(
                        CellStyleAttributes.FOREGROUND_COLOR,
                        GUIHelper.COLOR_RED);
            }

            this.validationErrorStyle = TableCellEditor.this.configRegistry.getConfigAttribute(
                    EditConfigAttributes.VALIDATION_ERROR_STYLE,
                    DisplayMode.EDIT, TableCellEditor.this.labelStack.getLabels());

            if (this.validationErrorStyle == null) {
                this.validationErrorStyle = new Style();
                this.validationErrorStyle.setAttributeValue(
                        CellStyleAttributes.FOREGROUND_COLOR,
                        GUIHelper.COLOR_RED);
            }
        }

        /**
         * Returns the IStyle based on the state of the given element
         *
         * @param element
         *            The element for which the style should be searched
         * @return The IStyle for the current state of the given element
         */
        public IStyle getActiveCellStyle(Object element) {
            if (!((ValueWrapper) element).isValid()) {
                return this.validationErrorStyle;
            }

            return this.normalStyle;
        }

        /**
         * Applies style attributes to the internal cell editor of the table
         * viewer based on the state of the value.
         *
         * @param editorControl
         *            The internal editor control of the table viewer to set the
         *            style to
         * @param element
         *            The element that is shown in that cell
         */
        public void applyCellStyle(Control editorControl, Object element) {
            Color foreground = getForeground(element);
            if (foreground != null) {
                editorControl.setForeground(foreground);
            }
            Color background = getBackground(element);
            if (background != null) {
                editorControl.setBackground(getBackground(element));
            }
            Font font = getFont(element);
            if (font != null) {
                editorControl.setFont(font);
            }
        }

        @Override
        public Color getForeground(Object element) {
            return getActiveCellStyle(element).getAttributeValue(
                    CellStyleAttributes.FOREGROUND_COLOR);
        }

        @Override
        public Color getBackground(Object element) {
            return getActiveCellStyle(element).getAttributeValue(
                    CellStyleAttributes.BACKGROUND_COLOR);
        }

        @Override
        public Font getFont(Object element) {
            return getActiveCellStyle(element).getAttributeValue(
                    CellStyleAttributes.FONT);
        }

    }

    /**
     * FocusListener that will fire the focus lost event only if the Text editor
     * control of the editing support and the table viewer does not have the
     * focus. This is because for NatTable use cases they share a focus. So
     * closing the TableCellEditor should only happen if both loose focus. Using
     * the default focus handling, opening an editor in the table via editing
     * support would cause a focus lost event on the table, which immediately
     * closes the editor again.
     */
    protected class InternalFocusListener implements FocusListener {

        boolean hasFocus = false;

        @Override
        public void focusLost(final FocusEvent e) {
            this.hasFocus = false;
            Display.getCurrent().timerExec(100, new Runnable() {
                @Override
                public void run() {
                    if (!InternalFocusListener.this.hasFocus) {
                        if (!commit(MoveDirectionEnum.NONE, true)) {
                            if (e.widget instanceof Control
                                    && !e.widget.isDisposed()) {
                                ((Control) e.widget).forceFocus();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void focusGained(FocusEvent e) {
            this.hasFocus = true;
        }
    }
}
