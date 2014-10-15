/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit;

import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.edit.gui.ICellEditDialog;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.IStyle;

/**
 * The configuration attributes for configuring editing behavior.
 */
public interface EditConfigAttributes {

    /**
     * The configuration attribute for the {@link IEditableRule} that is used to
     * determine whether a cell is editable or not.
     */
    public static final ConfigAttribute<IEditableRule> CELL_EDITABLE_RULE = new ConfigAttribute<IEditableRule>();

    /**
     * The configuration attribute for the {@link ICellEditor} that should be
     * used for editing a cell value.
     */
    public static final ConfigAttribute<ICellEditor> CELL_EDITOR = new ConfigAttribute<ICellEditor>();

    /**
     * The configuration attribute to register the {@link IDataValidator} that
     * should be used for validation if a value is committed by an editor. Note
     * that the IDisplayConverter is not part of the EditConfigAttributes as it
     * might also be used without editing, e.g. converting a date for correct
     * display.
     */
    public static final ConfigAttribute<IDataValidator> DATA_VALIDATOR = new ConfigAttribute<IDataValidator>();

    /**
     * The configuration attribute for the {@link IEditErrorHandler} that should
     * be used for conversion failure handling.
     */
    public static final ConfigAttribute<IEditErrorHandler> CONVERSION_ERROR_HANDLER = new ConfigAttribute<IEditErrorHandler>();

    /**
     * The configuration attribute for the {@link IEditErrorHandler} that should
     * be used for validation failure handling.
     */
    public static final ConfigAttribute<IEditErrorHandler> VALIDATION_ERROR_HANDLER = new ConfigAttribute<IEditErrorHandler>();

    /**
     * The configuration attribute for the style that should be used on just in
     * time conversion error rendering within an editor control.
     * <p>
     * Note: Currently this configuration attribute is only evaluated by the
     * TextCellEditor in combination with the RenderErrorHandling for just in
     * time conversion error rendering.
     */
    public static final ConfigAttribute<IStyle> CONVERSION_ERROR_STYLE = new ConfigAttribute<IStyle>();

    /**
     * The configuration attribute for the style that should be used on just in
     * time validation error rendering within an editor control.
     * <p>
     * Note: Currently this configuration attribute is only evaluated by the
     * TextCellEditor in combination with the RenderErrorHandling for just in
     * time validation error rendering.
     */
    public static final ConfigAttribute<IStyle> VALIDATION_ERROR_STYLE = new ConfigAttribute<IStyle>();

    /**
     * The configuration attribute to specify if cell editors should be opened
     * inline or in a dialog. If this configuration is not set, the editors will
     * open inline, unless the editors themself specify different behavior.
     *
     * @see ICellEditor#openInline(IConfigRegistry configRegistry, List
     *      configLabels)
     */
    public static final ConfigAttribute<Boolean> OPEN_IN_DIALOG = new ConfigAttribute<Boolean>();

    /**
     * The configuration attribute to specify behavior after committing a value
     * inline. If the value for this attribute is set to <code>true</code> and
     * the cell selection is specified to move after a successful commit
     * operation, the editor for the selected cell will be activated
     * immediately. If there is no value for this configuration attribute
     * registered, the default value used is <code>false</code> as this is the
     * default behavior prior to this.
     */
    public static final ConfigAttribute<Boolean> OPEN_ADJACENT_EDITOR = new ConfigAttribute<Boolean>();

    /**
     * The configuration attribute to specify whether an editor should be
     * activated if the activation was triggered by traversal, where traversal
     * also includes selection and activation after commit. If there is no value
     * for this configuration attribute registered, the default value used is
     * <code>true</code> as this is the default behavior prior to this.
     *
     * @see EditConfigAttributes#OPEN_ADJACENT_EDITOR
     */
    public static final ConfigAttribute<Boolean> ACTIVATE_EDITOR_ON_TRAVERSAL = new ConfigAttribute<Boolean>();

    /**
     * The configuration attribute to specify whether an editor supports multi
     * edit behavior or not. If this attribute is set to <code>true</code>, on
     * selecting and pressing F2 on several cells that are editable, having the
     * same editor type and converter registered, a multi edit dialog will open.
     * You should consider setting the value for this attribute to
     * <code>false</code> e.g. if the update operation is complex or you use
     * conditional validation, where a value is validated against another value
     * in the data model.
     */
    public static final ConfigAttribute<Boolean> SUPPORT_MULTI_EDIT = new ConfigAttribute<Boolean>();

    /**
     * The configuration attribute to specify custom dialog settings for edit
     * dialogs. Registering a map with matching key-value pairs for this
     * attribute allows to modify the appearance of edit dialogs. To see which
     * key-value-pairs are interpreted have a look at the constants configured
     * in {@link ICellEditDialog}.
     *
     * @see ICellEditDialog#DIALOG_SHELL_TITLE
     * @see ICellEditDialog#DIALOG_SHELL_ICON
     * @see ICellEditDialog#DIALOG_SHELL_LOCATION
     * @see ICellEditDialog#DIALOG_SHELL_SIZE
     * @see ICellEditDialog#DIALOG_SHELL_RESIZABLE
     * @see ICellEditDialog#DIALOG_MESSAGE
     */
    public static final ConfigAttribute<Map<String, Object>> EDIT_DIALOG_SETTINGS = new ConfigAttribute<Map<String, Object>>();
}
