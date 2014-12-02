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
package org.eclipse.nebula.widgets.nattable.edit.gui;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum;
import org.eclipse.swt.graphics.Point;

/**
 * Interface for dialogs that can be used as editing dialogs in NatTable.
 *
 * @author Dirk Fauth
 *
 */
public interface ICellEditDialog {

    /**
     * Key to specify a custom shell title of the dialog. The value needs to be
     * a {@link java.lang.String}.
     */
    String DIALOG_SHELL_TITLE = "DIALOG_SHELL_TITLE"; //$NON-NLS-1$
    /**
     * Key to specify a custom shell icon of the dialog. The value needs to be
     * an {@link org.eclipse.swt.graphics.Image}.
     */
    String DIALOG_SHELL_ICON = "DIALOG_SHELL_ICON"; //$NON-NLS-1$
    /**
     * Key to specify the location where the dialog should be shown. Will be
     * interpreted by {@link CellEditDialog#getInitialLocation(Point)} The value
     * needs to be a {@link org.eclipse.swt.graphics.Point}.
     */
    String DIALOG_SHELL_LOCATION = "DIALOG_SHELL_LOCATION"; //$NON-NLS-1$
    /**
     * Key to specify the size of the dialog. Will be interpreted by
     * {@link CellEditDialog#getInitialSize()} The value needs to be a
     * {@link org.eclipse.swt.graphics.Point}.
     */
    String DIALOG_SHELL_SIZE = "DIALOG_SHELL_SIZE"; //$NON-NLS-1$
    /**
     * Key to specify whether the dialog should be resizable or not. Will not be
     * interpreted by {@link CellEditDialog#isResizable()} because it is called
     * on instantiating the dialog in the constructor of the super class. It
     * will modify the set shell style bits instead. The value needs to be a
     * {@link java.lang.Boolean}.
     */
    String DIALOG_SHELL_RESIZABLE = "DIALOG_SHELL_RESIZABLE"; //$NON-NLS-1$
    /**
     * Key to specify a custom message for the to be shown in the dialog. The
     * value needs to be a {@link java.lang.String}.
     */
    String DIALOG_MESSAGE = "DIALOG_MESSAGE"; //$NON-NLS-1$

    /**
     * @return The canonical value that was committed to the editor control.
     */
    Object getCommittedValue();

    /**
     * @return The edit type that has impact on how the set value will be
     *         updated to the data model. By default
     *         {@link org.eclipse.nebula.widgets.nattable.edit.EditTypeEnum#SET}
     *         is returned, which will simply set the committed value to the
     *         data model. Every other edit type will do some calculation based
     *         on the committed value and the current value in the data model.
     */
    EditTypeEnum getEditType();

    /**
     * In case {@link ICellEditDialog#getEditType()} returns an edit type for
     * processing values, this method should implemented to do that
     * transformation.
     *
     * @param currentValue
     *            The current value for the cell before data model update
     * @param processValue
     *            The value committed to the editor that should be used for
     *            calculation on the current value.
     * @return The value that should be used to update the data model.
     */
    Object calculateValue(Object currentValue, Object processValue);

    /**
     * Opens this dialog, creating it first if it has not yet been created.
     * <p>
     * Specified in here for convenience so we only need to check against this
     * interface for a dialog.
     * </p>
     *
     * @return the return code
     */
    int open();

    /**
     * Allows to customize the appearance of the dialog. This method will be
     * called by the framework at creation time of the dialog via the
     * {@link CellEditDialogFactory}.
     * <p>
     * The map containing the settings can be registered to the
     * {@link IConfigRegistry} for the key
     * {@link org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes#EDIT_DIALOG_SETTINGS}
     * . The keys that are valid for this map are specified below.
     *
     * @param editDialogSettings
     *            Map containing the settings to customize the edit dialog
     *            appearance.
     *
     * @see ICellEditDialog#DIALOG_SHELL_TITLE
     * @see ICellEditDialog#DIALOG_SHELL_ICON
     * @see ICellEditDialog#DIALOG_SHELL_LOCATION
     * @see ICellEditDialog#DIALOG_SHELL_SIZE
     * @see ICellEditDialog#DIALOG_SHELL_RESIZABLE
     * @see ICellEditDialog#DIALOG_MESSAGE
     * @see EditConfigAttributes#EDIT_DIALOG_SETTINGS
     */
    void setDialogSettings(Map<String, Object> editDialogSettings);
}
