/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.gui;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.ConversionFailedException;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.edit.DialogEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigHelper;
import org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum;
import org.eclipse.nebula.widgets.nattable.edit.ICellEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a {@link ICellEditor} that is also a
 * {@link ICellEditDialog}. By creating a {@link ICellEditor} based on this
 * abstract implementation, you are able to create an editor that wraps a SWT or
 * JFace dialog. As SWT and JFace dialogs does not extend the same base classes,
 * the local instance for the wrapped dialog is of type object in here. In the
 * concrete implementation the
 * {@link AbstractDialogCellEditor#getDialogInstance()} should return the
 * concrete dialog type that is wrapped.
 * <p>
 * By using this implementation, the {@link CellEditDialogFactory} will return
 * the instance of this editor, after it was activated previously.
 */
public abstract class AbstractDialogCellEditor implements ICellEditor, ICellEditDialog {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDialogCellEditor.class);

    /**
     * The parent Composite, needed for the creation of the dialog.
     */
    protected Composite parent;
    /**
     * The {@link Dialog} that should be used as a cell editor.
     */
    protected Object dialog;
    /**
     * The cell whose editor should be activated.
     */
    protected ILayerCell layerCell;
    /**
     * The {@link ICellEditHandler} that will be used on commit.
     */
    protected DialogEditHandler editHandler = new DialogEditHandler();
    /**
     * The {@link IDisplayConverter} that should be used to convert the input
     * value to the canonical value and vice versa.
     */
    protected IDisplayConverter displayConverter;
    /**
     * The {@link IDataValidator} that should be used to validate the input
     * value prior committing.
     */
    protected IDataValidator dataValidator;
    /**
     * The error handler that will be used to show conversion errors.
     */
    protected IEditErrorHandler conversionEditErrorHandler;
    /**
     * The error handler that will be used to show validation errors.
     */
    protected IEditErrorHandler validationEditErrorHandler;
    /**
     * The {@link IConfigRegistry} containing the configuration of the current
     * NatTable instance. This is necessary because the editors in the current
     * architecture are not aware of the NatTable instance they are running in.
     */
    protected IConfigRegistry configRegistry;
    /**
     * Map that contains custom configurations for this {@link CellEditDialog}.
     * We do not use the {@link IDialogSettings} provided by JFace, because they
     * are used to store and load the settings in XML rather than overriding the
     * behaviour.
     */
    protected Map<String, Object> editDialogSettings;

    @Override
    public EditTypeEnum getEditType() {
        // by default the value selected in the wrapped dialog should simply be
        // set to the data model on commit.
        return EditTypeEnum.SET;
    }

    @Override
    public Object calculateValue(Object currentValue, Object processValue) {
        // by default the value selected in the wrapped dialog should simply be
        // set to the data model on commit.
        return processValue;
    }

    @Override
    public abstract int open();

    @Override
    public Control activateCell(Composite parent,
            Object originalCanonicalValue, EditModeEnum editMode,
            ICellEditHandler editHandler, ILayerCell cell,
            IConfigRegistry configRegistry) {

        this.parent = parent;
        this.layerCell = cell;
        this.configRegistry = configRegistry;

        final List<String> configLabels = cell.getConfigLabels();
        this.displayConverter = configRegistry.getConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                DisplayMode.EDIT,
                configLabels);
        this.dataValidator = configRegistry.getConfigAttribute(
                EditConfigAttributes.DATA_VALIDATOR,
                DisplayMode.EDIT,
                configLabels);

        this.conversionEditErrorHandler = EditConfigHelper.getEditErrorHandler(
                configRegistry,
                EditConfigAttributes.CONVERSION_ERROR_HANDLER,
                configLabels);
        this.validationEditErrorHandler = EditConfigHelper.getEditErrorHandler(
                configRegistry,
                EditConfigAttributes.VALIDATION_ERROR_HANDLER,
                configLabels);

        this.dialog = createDialogInstance();

        setCanonicalValue(originalCanonicalValue);

        // this method is only needed to initialize the dialog editor, there
        // will be no control to return
        return null;
    }

    /**
     * Will create the dialog instance that should be wrapped by this
     * {@link AbstractDialogCellEditor}. Note that you always need to create and
     * return a new instance because on commit or close the dialog will be
     * closed, which disposes the shell of the dialog. Therefore the instance
     * will not be usable after commit/close.
     *
     * @return The dialog instance that should be wrapped by this
     *         {@link AbstractDialogCellEditor}
     */
    public abstract Object createDialogInstance();

    /**
     * @return The current dialog instance that is wrapped by this
     *         {@link AbstractDialogCellEditor}
     */
    public abstract Object getDialogInstance();

    @Override
    public abstract Object getEditorValue();

    @Override
    public abstract void setEditorValue(Object value);

    @Override
    public Object getCanonicalValue() {
        return getCanonicalValue(this.conversionEditErrorHandler);
    }

    @Override
    public Object getCanonicalValue(IEditErrorHandler conversionErrorHandler) {
        Object canonicalValue;
        try {
            if (this.displayConverter != null) {
                // always do the conversion to check for valid entered data
                canonicalValue = this.displayConverter.displayToCanonicalValue(
                        this.layerCell, this.configRegistry, getEditorValue());
            } else {
                canonicalValue = getEditorValue();
            }

            // if the conversion succeeded, remove error rendering if exists
            conversionErrorHandler.removeError(this);
        } catch (ConversionFailedException e) {
            // conversion failed
            conversionErrorHandler.displayError(this, this.configRegistry, e);
            throw e;
        } catch (Exception e) {
            // conversion failed
            conversionErrorHandler.displayError(this, this.configRegistry, e);
            throw new ConversionFailedException(e.getMessage(), e);
        }
        return canonicalValue;
    }

    @Override
    public void setCanonicalValue(Object canonicalValue) {
        Object displayValue;
        if (this.displayConverter != null) {
            displayValue = this.displayConverter.canonicalToDisplayValue(
                    this.layerCell, this.configRegistry, canonicalValue);
        } else {
            displayValue = canonicalValue;
        }
        setEditorValue(displayValue);
    }

    @Override
    public boolean validateCanonicalValue(Object canonicalValue) {
        return validateCanonicalValue(canonicalValue,
                this.validationEditErrorHandler);
    }

    @Override
    public boolean validateCanonicalValue(Object canonicalValue,
            IEditErrorHandler validationErrorHandler) {
        // do the validation if a validator is registered
        if (this.dataValidator != null) {
            try {
                boolean validationResult = this.dataValidator.validate(
                        this.layerCell, this.configRegistry, canonicalValue);

                // if the validation succeeded, remove error rendering if exists
                if (validationResult) {
                    this.validationEditErrorHandler.removeError(this);
                } else {
                    throw new ValidationFailedException(
                            Messages.getString("AbstractCellEditor.validationFailure")); //$NON-NLS-1$
                }
                return validationResult;
            } catch (Exception e) {
                // validation failed
                this.validationEditErrorHandler.displayError(this, this.configRegistry, e);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean commit(MoveDirectionEnum direction) {
        return commit(direction, true);
    }

    @Override
    public boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit) {
        return commit(direction, closeAfterCommit, false);
    }

    @Override
    public boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit, boolean skipValidation) {
        if (this.editHandler != null && this.dialog != null && !isClosed()) {
            try {
                // always do the conversion
                Object canonicalValue = getCanonicalValue();
                if (skipValidation
                        || (!skipValidation && validateCanonicalValue(canonicalValue))) {
                    boolean committed = this.editHandler.commit(canonicalValue, direction);

                    if (committed && closeAfterCommit) {
                        close();
                    }

                    return committed;
                }
            } catch (ConversionFailedException e) {
                // do nothing as exceptions caused by conversion are handled
                // already we just need this catch block for stopping the
                // process
                // if conversion failed with an exception
            } catch (ValidationFailedException e) {
                // do nothing as exceptions caused by validation are handled
                // already we just need this catch block for stopping the
                // process
                // if validation failed with an exception
            } catch (Exception e) {
                // if another exception occured that wasn't thrown by us, it
                // should at least be logged without killing the whole
                // application
                LOG.error("Error on updating cell value: {}", e.getLocalizedMessage(), e); //$NON-NLS-1$
            }
        }
        return false;
    }

    @Override
    public Object getCommittedValue() {
        return this.editHandler.getCommittedValue();
    }

    @Override
    public abstract void close();

    @Override
    public abstract boolean isClosed();

    @Override
    public Control getEditorControl() {
        // as this editor wraps a dialog, there is no explicit editor control
        return null;
    }

    @Override
    public Control createEditorControl(Composite parent) {
        // as this editor wraps a dialog, there is no explicit editor control
        return null;
    }

    @Override
    public boolean openInline(IConfigRegistry configRegistry, List<String> configLabels) {
        return false;
    }

    @Override
    public boolean supportMultiEdit(IConfigRegistry configRegistry, List<String> configLabels) {
        return EditConfigHelper.supportMultiEdit(configRegistry, configLabels);
    }

    @Override
    public boolean openMultiEditDialog() {
        return true;
    }

    @Override
    public boolean openAdjacentEditor() {
        // as editing with a dialog should only result in committing the value
        // and
        // then set the selection to the edited value, it doesn't make sense to
        // open
        // the adjacent editor.
        return false;
    }

    @Override
    public boolean activateAtAnyPosition() {
        return true;
    }

    @Override
    public boolean activateOnTraversal(IConfigRegistry configRegistry, List<String> configLabels) {
        return EditConfigHelper.activateEditorOnTraversal(configRegistry, configLabels);
    }

    @Override
    public void addEditorControlListeners() {
        // there is no need for special editor control listeners here
    }

    @Override
    public void removeEditorControlListeners() {
        // there is no need for special editor control listeners here
    }

    @Override
    public Rectangle calculateControlBounds(Rectangle cellBounds) {
        return cellBounds;
    }

    @Override
    public void setDialogSettings(Map<String, Object> editDialogSettings) {
        this.editDialogSettings = editDialogSettings;
    }

    @Override
    public int getColumnIndex() {
        return this.layerCell.getColumnIndex();
    }

    @Override
    public int getRowIndex() {
        return this.layerCell.getRowIndex();
    }

    @Override
    public int getColumnPosition() {
        return this.layerCell.getColumnPosition();
    }

    @Override
    public int getRowPosition() {
        return this.layerCell.getRowPosition();
    }
}
