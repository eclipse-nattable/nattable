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
package org.eclipse.nebula.widgets.nattable.edit.editor;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.EditController;
import org.eclipse.nebula.widgets.nattable.edit.ICellEditHandler;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for implementing editors that can be used in a NatTable. Such an
 * editor is mainly a wrapper for a native SWT control, providing additional
 * functionality to support NatTable specific handling, e.g. conversion,
 * validation, model updates.
 *
 * Implementations are responsible for capturing new cell value during cell
 * edit.
 */
public interface ICellEditor {

    /**
     * This method will be called by the framework to activate this cell editor.
     * It initializes the the values needed for further processing of the editor
     * and will add listeners for general behavior of the editor control.
     *
     * @param parent
     *            The parent Composite, needed for the creation of the editor
     *            control.
     * @param originalCanonicalValue
     *            The value that should be put to the activated editor control.
     * @param editMode
     *            The {@link EditModeEnum} which is used to activate special
     *            behavior and styling. This is needed because activating an
     *            editor inline will have different behavior (e.g. moving the
     *            selection after commit) and styling than rendering the editor
     *            on a subdialog.
     * @param editHandler
     *            The {@link ICellEditHandler} that will be used on commit.
     * @param cell
     *            The cell whose corresponding editor should be activated.
     * @param configRegistry
     *            The {@link IConfigRegistry} containing the configuration of
     *            the current NatTable instance. This is necessary because the
     *            editors in the current architecture are not aware of the
     *            NatTable instance they are running in.
     * @return The SWT {@link Control} to be used for capturing the new cell
     *         value.
     */
    public Control activateCell(Composite parent,
            Object originalCanonicalValue, EditModeEnum editMode,
            ICellEditHandler editHandler, ILayerCell cell,
            IConfigRegistry configRegistry);

    /**
     * @return The column index of the cell to which this editor is attached.
     */
    int getColumnIndex();

    /**
     * @return The row index of the cell to which this editor is attached.
     */
    int getRowIndex();

    /**
     * @return The column position of the cell to which this editor is attached.
     */
    int getColumnPosition();

    /**
     * @return The row position of the cell to which this editor is attached.
     */
    int getRowPosition();

    /**
     * Returns the current value in this editor prior to conversion. For a text
     * editor that is used to edit integer values, this would mean it returns
     * the text value instead of the converted integer value. This method is
     * only intended to be used internally .
     *
     * @return The current value in this editor prior to conversion.
     */
    Object getEditorValue();

    /**
     * Sets the given value to editor control. This method is used to put the
     * display values to the wrapped editor.
     *
     * @param value
     *            The display value to set to the wrapped editor control.
     */
    void setEditorValue(Object value);

    /**
     * Converts the current value in this editor using the configured
     * {@link IDisplayConverter}. If there is no {@link IDisplayConverter}
     * registered for this editor, the value itself will be returned.
     *
     * @return The canonical value after converting the current value or the
     *         value itself if no {@link IDisplayConverter} is configured.
     * @throws RuntimeException
     *             for conversion failures. As the {@link IDisplayConverter}
     *             interface does not specify throwing checked Exceptions on
     *             converting data, only unchecked Exceptions can occur. This is
     *             needed to stop further commit processing if the conversion
     *             failed.
     * @see IDisplayConverter
     */
    Object getCanonicalValue();

    /**
     * Converts the current value in this editor using the configured
     * {@link IDisplayConverter}. If there is no {@link IDisplayConverter}
     * registered for this editor, the value itself will be returned. Will use
     * the specified {@link IEditErrorHandler} for handling conversion errors.
     *
     * @param conversionErrorHandler
     *            The error handler that will be activated in case of conversion
     *            errors.
     * @return The canonical value after converting the current value or the
     *         value itself if no {@link IDisplayConverter} is configured.
     * @throws RuntimeException
     *             for conversion failures. As the {@link IDisplayConverter}
     *             interface does not specify throwing checked Exceptions on
     *             converting data, only unchecked Exceptions can occur. This is
     *             needed to stop further commit processing if the conversion
     *             failed.
     * @see IDisplayConverter
     */
    Object getCanonicalValue(IEditErrorHandler conversionErrorHandler);

    /**
     * Sets the given canonical value to the wrapped editor control. Prior to
     * setting the value it needs to be converted to the display value, using
     * the configured {@link IDisplayConverter}.
     *
     * @param canonicalValue
     *            The canonical value to be set to the wrapped editor control.
     */
    void setCanonicalValue(Object canonicalValue);

    /**
     * Validates the given value using the configured {@link IDataValidator}.
     * This method should be called with the value converted before by using
     * {@link ICellEditor#getCanonicalValue()}.
     *
     * @param canonicalValue
     *            The canonical value to validate.
     * @return <code>true</code> if the current value in this editor is valid or
     *         no {@link IDataValidator} is registered, <code>false</code> if
     *         the value is not valid.
     */
    boolean validateCanonicalValue(Object canonicalValue);

    /**
     * Validates the current value in this editor using the configured
     * {@link IDataValidator}. Validates the given value using the configured
     * {@link IDataValidator}. This method should be called with the value
     * converted before by using {@link ICellEditor#getCanonicalValue()}. Will
     * use the specified {@link IEditErrorHandler} for handling validation
     * errors.
     *
     * @param canonicalValue
     *            The canonical value to validate.
     * @param validationErrorHandler
     *            The error handler that will be activated in case of validation
     *            errors.
     * @return <code>true</code> if the current value in this editor is valid or
     *         no {@link IDataValidator} is registered, <code>false</code> if
     *         the value is not valid.
     */
    public boolean validateCanonicalValue(Object canonicalValue,
            IEditErrorHandler validationErrorHandler);

    /**
     * Commits the current value of this editor. Will first try to convert and
     * validate the current value, and if that succeeds and the value can be
     * committed to the data model, the editor will be closed afterwards.
     *
     * @param direction
     *            The direction the selection within the NatTable should move
     *            after commit has finished.
     * @return <code>true</code> if the commit operation succeeded,
     *         <code>false</code> if the current value could not be committed. A
     *         value might not be committed for example if the conversion or the
     *         validation failed.
     */
    boolean commit(MoveDirectionEnum direction);

    /**
     * Commits the current value of this editor. Will first try to convert the
     * current value. Then it is checked if the validation should be executed
     * which can be specified via parameter. If that succeeds and the value can
     * be committed to the data model, the editor will be closed afterwards.
     *
     * @param direction
     *            The direction the selection within the NatTable should move
     *            after commit has finished.
     * @param closeAfterCommit
     *            flag to tell whether this editor needs to closed after the
     *            commit or if it should stay open.
     * @return <code>true</code> if the commit operation succeeded,
     *         <code>false</code> if the current value could not be committed. A
     *         value might not be committed for example if the conversion or the
     *         validation failed.
     */
    boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit);

    /**
     * Commits the current value of this editor.
     *
     * @param direction
     *            The direction the selection within the NatTable should move
     *            after commit has finished.
     * @param closeAfterCommit
     *            flag to tell whether this editor needs to closed after the
     *            commit or if it should stay open.
     * @param skipValidation
     *            Flag to specify whether the current value in this editor
     *            should be validated or not.
     * @return <code>true</code> if the commit operation succeeded,
     *         <code>false</code> if the current value could not be committed. A
     *         value might not be committed for example if the conversion or the
     *         validation failed.
     */
    boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit,
            boolean skipValidation);

    /**
     * Close/dispose the contained {@link Control}
     */
    void close();

    /**
     * @return <code>true</code> if this editor has been closed already,
     *         <code>false</code> if it is still open
     */
    boolean isClosed();

    /**
     * @return The editor control that is wrapped by this ICellEditor.
     */
    Control getEditorControl();

    /**
     * Creates the editor control that is wrapped by this ICellEditor. Will use
     * the style configurations in ConfigRegistry for styling the control.
     *
     * @param parent
     *            The Composite that will be the parent of the new editor
     *            control. Can not be <code>null</code>
     * @return The created editor control that is wrapped by this ICellEditor.
     */
    Control createEditorControl(Composite parent);

    /**
     * Determines whether the editor should be opened inline or using a dialog.
     * By default it will check this by configuration attribute
     * {@link EditConfigAttributes#OPEN_IN_DIALOG}. If there is no configuration
     * found for this, <code>true</code> will be returned for backwards
     * compatibility.
     * <p>
     * If this method returns <code>true</code>, the editor will be opened
     * inline (default).
     * </p>
     * <p>
     * There might be editors that are only able to be opened in a dialog. These
     * implementations need to override this method to always return
     * <code>false</code>, so the editor never gets opened inline.
     * </p>
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the configuration for
     *            inline/dialog editing out of. Needed here because the instance
     *            {@link IConfigRegistry} might not be set on calling this
     *            method.
     * @param configLabels
     *            The labels out of the LabelStack of the cell whose editor
     *            should be activated. Needed here because this method needs to
     *            be called prior to activation to determine where to activate
     *            it.
     * @return <code>true</code> if the editor should opened inline,
     *         <code>false</code> if not.
     * @see EditConfigAttributes#OPEN_IN_DIALOG
     */
    boolean openInline(IConfigRegistry configRegistry, List<String> configLabels);

    /**
     * Determines whether this editor supports multi edit behavior or not. If
     * this method returns <code>true</code>, on selecting and pressing F2 on
     * several cells that are editable, having the same editor type and
     * converter registered, a multi edit dialog will open. By default this
     * method will return <code>true</code>. You can change this behavior by
     * setting the configuration attribute
     * {@link EditConfigAttributes#SUPPORT_MULTI_EDIT}.
     * <p>
     * You should consider returning <code>false</code> e.g. if the update
     * operation is complex or you use conditional validation, where a value is
     * validated against another value in the data model.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the configuration for
     *            multi edit support out of. Needed here because the instance
     *            {@link IConfigRegistry} might not be set on calling this
     *            method.
     * @param configLabels
     *            The labels out of the LabelStack of the cell whose editor
     *            should be activated. Needed here because this method needs to
     *            be called prior to activation to determine where to activate
     *            it.
     * @return <code>true</code> if this editor will open in a subdialog for
     *         multi editing, <code>false</code> if the multi editing of this
     *         kind of cell editor is not supported.
     * @see EditConfigAttributes#SUPPORT_MULTI_EDIT
     */
    boolean supportMultiEdit(IConfigRegistry configRegistry, List<String> configLabels);

    /**
     * This is a very special configuration to tell whether an ICellEditor
     * should open a multi edit dialog for multi editing or not. Usually for
     * multi editing there should be always a multi edit dialog be opened. There
     * are only special cases where this doesn't make sense. The only types of
     * ICellEditors that shouldn't open multi edit dialogs are editors that
     * change their values directly and there is no interactively editor control
     * opened, e.g. checkboxes.
     *
     * @return <code>true</code> if for multi editing a multi edit dialog should
     *         be opened, <code>false</code> if the multi editing should be
     *         performed directly without opening a multi edit dialog. Note:
     *         <code>true</code> is the default value and changing it to
     *         <code>false</code> for a custom editor might cause issues if not
     *         dealed correctly.
     */
    boolean openMultiEditDialog();

    /**
     * Determines behavior after committing the value of this editor in
     * combination with selection movement. If this method return
     * <code>true</code> and the selection is moved after committing, the editor
     * for the newly selected cell will be activated immediately. If this method
     * returns <code>false</code> or the selection is not moved after commit, no
     * action should be executed.
     * <p>
     * The behavior previous to this configuration was to not open the adjacent
     * editor. So if there is no configuration registered for this,
     * <code>false</code> will be returned by default.
     * </p>
     * <p>
     * Note: It only makes sense to call this method if the editor is already
     * activated. Calling this method on an editor that has not been activated
     * already will lead to exceptions.
     * </p>
     *
     * @return <code>true</code> if the adjacent editor should be opened if the
     *         selection moves after commit, <code>false</code> if not.
     * @see EditConfigAttributes#OPEN_ADJACENT_EDITOR
     */
    boolean openAdjacentEditor();

    /**
     * This method is intended to be used by {@link IMouseEventMatcher}
     * implementations that need to check for the editor and the click position
     * to determine whether an editor should be activated or not. By default
     * this method will return <code>true</code>. Special implementations that
     * need a different behavior need to return <code>false</code> instead. E.g.
     * checkbox editors should only be activated in case the icon that
     * represents the checkbox is clicked.
     *
     * @return <code>true</code> if this {@link ICellEditor} should be activated
     *         by clicking at any position in the corresponding cell,
     *         <code>false</code> if there need to be a special position
     *         clicked.
     */
    boolean activateAtAnyPosition();

    /**
     * This method is asked on tab traversal whether this {@link ICellEditor}
     * should be automatically activated or not. This is necessary to avoid
     * automatically changing the value of a checkbox or opening a dialog editor
     * on traversal.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the configuration out
     *            of. Needed here because the instance {@link IConfigRegistry}
     *            might not be set on calling this method.
     * @param configLabels
     *            The labels out of the LabelStack of the cell whose editor
     *            should be activated.
     * @return <code>true</code> if this {@link ICellEditor} should be activated
     *         in case of tab traversal, <code>false</code> if not.
     * @see EditConfigAttributes#ACTIVATE_EDITOR_ON_TRAVERSAL
     */
    boolean activateOnTraversal(IConfigRegistry configRegistry, List<String> configLabels);

    /**
     * This method is intended to add listeners to the wrapped editor control to
     * add context related behavior. For example, in {@link EditModeEnum#INLINE}
     * by default this should add a FocusListener that commits the current value
     * if the editor control loses focus.
     * <p>
     * This method was introduced mainly because of two issues:
     * <ol>
     * <li>On Mac OS calling setBounds() on a Control will cause losing focus.
     * So listeners need to be added after this method is called by the
     * EditController, otherwise on activating the editor it will be closed
     * immediately after the correct size is calculated.</li>
     * <li>The main concept for cell editor activation is, that the editor
     * control is disposed on closing the editor. This way everytime the cell
     * editor is activated, a new editor control will be created. If an editor
     * is implemented that needs to keep the editor control after closing the
     * editor, it needs to be ensured that the listeners are removed again.
     * Otherwise the listeners would be added again everytime the editor is
     * activated.</li>
     * </ol>
     * This method will be called automatically by
     * {@link EditController#editCell(ILayerCell, Composite, Object, IConfigRegistry)}.
     */
    void addEditorControlListeners();

    /**
     * This method is intended to remove listeners from the wrapped editor
     * control that was added by {@link ICellEditor#addEditorControlListeners()}
     * before to add context related behavior.
     * <p>
     * This method was introduced to add the possibility to create an
     * {@link ICellEditor} whose wrapped editor control should not be disposed
     * on closing the editor.
     * </p>
     * <p>
     * The main concept for cell editor activation is, that the editor control
     * is disposed on closing the editor. This way everytime the cell editor is
     * activated, a new editor control will be created. If an editor is
     * implemented that needs to keep the editor control after closing the
     * editor, it needs to be ensured that the listeners are removed again.
     * Otherwise the listeners would be added again everytime the editor is
     * activated.
     * </p>
     * This method needs to be called on {@link ICellEditor#close()}. There is
     * no automatical call by the framework if you are not using the abstract
     * implementation of {@link ICellEditor}.
     */
    void removeEditorControlListeners();

    /**
     * This method is used to calculate the bounds of the edit control when
     * opened inline. By default it should return the given cell bounds to match
     * the cell structure in NatTable. For several cases it might be useful to
     * return the preferred size to show all content rather than trimming the
     * control to the cell size.
     * <p>
     * Note: By changing the bounds you should ensure to only modify width and
     * height attributes and not x and y coordinate, otherwise the editor
     * control will show up somewhere else and not in place of the cell that is
     * edited.
     *
     * @param cellBounds
     *            The bounds of the cell for which the editor is opened.
     * @return The bounds of the editor control that should be applied. By
     *         default the cell bounds for several cases bigger.
     */
    Rectangle calculateControlBounds(Rectangle cellBounds);
}
