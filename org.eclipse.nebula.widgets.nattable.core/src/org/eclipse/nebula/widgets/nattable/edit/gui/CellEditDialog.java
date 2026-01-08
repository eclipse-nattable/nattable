/*******************************************************************************
 * Copyright (c) 2013, 2026 Dirk Fauth and others.
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

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.DialogEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum;
import org.eclipse.nebula.widgets.nattable.edit.ICellEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.InlineEditHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that supports editing of cells in NatTable. Is used for multi cell
 * editing and for dialog only editors.
 */
public class CellEditDialog extends Dialog implements ICellEditDialog {

    /**
     * The value that should be propagated to the editor control. Needed because
     * for multi cell editing or editor activation by letter/digit key will
     * result in a different value to populate for some editors than populating
     * the value out of the cell/ data model directly.
     */
    protected final Object originalCanonicalValue;

    /**
     * The cell editor that should be integrated and activated in this dialog.
     */
    protected final ICellEditor cellEditor;

    /**
     * The {@link ICellEditHandler} that should be used by the editor.
     */
    protected DialogEditHandler cellEditHandler = new DialogEditHandler();

    /**
     * The cell that should be edited. Needed because editor activation
     * retrieves the configuration for editing directly out of the cell.
     */
    protected final ILayerCell cell;

    /**
     * The {@link IConfigRegistry} containing the configuration of the current
     * NatTable instance the command should be executed for. This is necessary
     * because the edit controllers in the current architecture are not aware of
     * the instance they are running in and therefore it is needed for
     * activation of editors.
     */
    protected final IConfigRegistry configRegistry;

    /**
     * Map that contains custom configurations for this {@link CellEditDialog}.
     * We do not use the {@link IDialogSettings} provided by JFace, because they
     * are used to store and load the settings in XML rather than overriding the
     * behaviour.
     */
    protected Map<String, Object> editDialogSettings;

    /**
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level
     *            shell
     * @param originalCanonicalValue
     *            The value that should be propagated to the editor control.
     *            Needed because for multi cell editing or editor activation by
     *            letter/digit key will result in a different value to populate
     *            for some editors than populating the value out of the
     *            cell/data model directly.
     * @param cell
     *            The cell that should be edited. Needed because editor
     *            activation retrieves the configuration for editing directly
     *            out of the cell.
     * @param cellEditor
     *            The {@link ICellEditor} that will be used as editor control
     *            within the dialog.
     * @param configRegistry
     *            The {@link IConfigRegistry} containing the configuration of
     *            the current NatTable instance the command should be executed
     *            for. This is necessary because the edit controllers in the
     *            current architecture are not aware of the instance they are
     *            running in and therefore it is needed for activation of
     *            editors.
     */
    public CellEditDialog(Shell parentShell,
            Object originalCanonicalValue,
            ILayerCell cell,
            ICellEditor cellEditor,
            IConfigRegistry configRegistry) {

        super(parentShell);
        this.originalCanonicalValue = originalCanonicalValue;
        this.cell = cell;
        this.cellEditor = cellEditor;
        this.configRegistry = configRegistry;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        String shellTitle = Messages.getString("CellEditDialog.shellTitle"); //$NON-NLS-1$
        Image shellIcon = GUIHelper.getDisplayImage("editor"); //$NON-NLS-1$
        if (this.editDialogSettings != null) {
            if (this.editDialogSettings.containsKey(DIALOG_SHELL_TITLE)) {
                String settingsShellTitle = this.editDialogSettings.get(DIALOG_SHELL_TITLE).toString();
                shellTitle = settingsShellTitle;
            }
            Object settingsShellImage = this.editDialogSettings.get(DIALOG_SHELL_ICON);
            if (settingsShellImage instanceof Image) {
                shellIcon = (Image) settingsShellImage;
            }
        }
        newShell.setText(shellTitle);
        newShell.setImage(shellIcon);
    }

    @Override
    protected boolean isResizable() {
        return false;
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        if (this.editDialogSettings != null) {
            Object settingsLocation = this.editDialogSettings.get(DIALOG_SHELL_LOCATION);
            if (settingsLocation instanceof Point) {
                return (Point) settingsLocation;
            }
        }
        return super.getInitialLocation(initialSize);
    }

    @Override
    protected Point getInitialSize() {
        if (this.editDialogSettings != null) {
            Object settingsSize = this.editDialogSettings.get(DIALOG_SHELL_SIZE);
            if (settingsSize instanceof Point) {
                return (Point) settingsSize;
            }
        }
        return super.getInitialSize();
    }

    @Override
    protected void okPressed() {
        // if the editor could not be committed, we should not proceed with
        // closing the editor, as the entered value is not valid in terms of
        // conversion/validation
        if (this.cellEditor.commit(MoveDirectionEnum.NONE, true)) {
            super.okPressed();
        }
    }

    @Override
    protected void cancelPressed() {
        this.cellEditor.close();
        super.cancelPressed();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        GridLayout panelLayout = new GridLayout(1, true);
        panelLayout.marginWidth = 8;
        panel.setLayout(panelLayout);

        // add a custom message if there is one configured in the edit dialog
        // settings
        if (this.editDialogSettings != null && this.editDialogSettings.containsKey(DIALOG_MESSAGE)) {
            String customMessage = this.editDialogSettings.get(DIALOG_MESSAGE).toString();
            Label customMessageLabel = new Label(panel, SWT.NONE);
            customMessageLabel.setText(customMessage);
            GridDataFactory.fillDefaults()
                    .grab(true, false)
                    .applyTo(customMessageLabel);
        }

        // If the dialog should commit the editor on edit like in the inline
        // mode, we need to wrap the existing cell edit handler to commit the
        // editor value in the data model directly and commit it in the dialog
        // to be consistent.
        ICellEditHandler eh = this.cellEditHandler;
        if (this.editDialogSettings != null && (boolean) this.editDialogSettings.getOrDefault(DIALOG_COMMIT_EDITOR_ON_EDIT, Boolean.FALSE)) {
            eh = new InlineEditHandler(
                    this.cell.getLayer(),
                    this.cell.getColumnPosition(),
                    this.cell.getRowPosition()) {
                @Override
                public boolean commit(Object canonicalValue, MoveDirectionEnum direction) {
                    CellEditDialog.this.cellEditHandler.commit(canonicalValue, direction);
                    return super.commit(canonicalValue, direction);
                }
            };
        }

        // activate the new editor
        this.cellEditor.activateCell(panel,
                this.originalCanonicalValue,
                EditModeEnum.DIALOG,
                eh,
                this.cell,
                this.configRegistry);

        Control editorControl = this.cellEditor.getEditorControl();

        // propagate the ESC event from the editor to the dialog
        editorControl.addKeyListener(getEscKeyListener());

        // If the editor control already has no layout data set already, apply
        // a default one.
        // This check ensures that a custom layout data that was specified while
        // creating the editor control in the ICellEditor is not overridden.
        if (editorControl.getLayoutData() == null) {
            GridDataFactory.fillDefaults()
                    .grab(true, false)
                    .applyTo(editorControl);
        }

        return panel;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (this.editDialogSettings != null && (boolean) this.editDialogSettings.getOrDefault(DIALOG_COMMIT_EDITOR_ON_EDIT, Boolean.FALSE)) {
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, true);
        } else {
            super.createButtonsForButtonBar(parent);
        }
    }

    @Override
    public Object getCommittedValue() {
        return this.cellEditHandler.getCommittedValue();
    }

    @Override
    public EditTypeEnum getEditType() {
        return EditTypeEnum.SET;
    }

    /**
     * {@inheritDoc}
     *
     * @return This implementation will always return processValue, as there is
     *         no processing specified in this {@link ICellEditDialog}
     *         implementation and therefore the value that was committed to the
     *         editor will be updated to the data model.
     */
    @Override
    public Object calculateValue(Object currentValue, Object processValue) {
        return processValue;
    }

    /**
     * @return KeyListener that intercepts the ESC key to cancel editing, close
     *         the editor and close the dialog.
     */
    protected KeyListener getEscKeyListener() {
        return new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.ESC) {
                    cancelPressed();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ESC) {
                    cancelPressed();
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will check if the given map contains a value for
     * {@link ICellEditDialog#DIALOG_SHELL_RESIZABLE}. If there is a value found
     * for that configuration, the Shell style will be recalculated based on the
     * specified value. The style bits are calculated the same way like in the
     * {@link Dialog} constructor. This is performed in here because setting the
     * Shell style bits after the creation of the Shell would have no effect.
     */
    @Override
    public void setDialogSettings(Map<String, Object> editDialogSettings) {
        this.editDialogSettings = editDialogSettings;

        // if the edit dialog settings contain a configuration for resizable
        // behaviour
        // we need to override the shellStyle bits before the shell is created
        // otherwise this configuration wouldn't have any effect.
        if (this.editDialogSettings != null) {
            Object settingsResizable = this.editDialogSettings.get(DIALOG_SHELL_RESIZABLE);
            if (settingsResizable instanceof Boolean) {
                if ((Boolean) settingsResizable) {
                    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.MAX | SWT.RESIZE | getDefaultOrientation());
                } else {
                    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | getDefaultOrientation());
                }
            }
        }

    }
}
