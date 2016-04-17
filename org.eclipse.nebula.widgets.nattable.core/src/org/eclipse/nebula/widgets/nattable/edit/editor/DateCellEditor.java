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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;

/**
 * ICellEditor implementation that uses a DateTime control for editing. It
 * supports objects of type Date and Calendar aswell.
 * <p>
 * Introduces the contract that the editor control value is of type Calendar.
 * Therefore the methods to deal with the canonical values need to be overriden
 * too, to avoid conversion of the canonical value to display value by using the
 * IDisplayConverter that is registered together with this editor.
 * <p>
 * Note: This is an example implementation for a Date editor. As the SWT
 * DateTime control has some serious issues like it is not nullable, has issues
 * on setting the focus programmatically and it is not possible to open the
 * dropdown programmatically, we suggest to rather use some Nebula widget or a
 * custom widget for date editing.
 * </p>
 */
public class DateCellEditor extends AbstractCellEditor {

    /**
     * The DateTime control which is the editor wrapped by this DateCellEditor.
     */
    private DateTime dateTime;

    /**
     * Flag to configure whether the selection should move after a value was
     * committed after pressing enter.
     */
    private final boolean moveSelectionOnEnter;

    /**
     * Creates the default DateCellEditor that does not move the selection on
     * committing the value by pressing enter.
     */
    public DateCellEditor() {
        this(false);
    }

    /**
     * Creates a DateCellEditor.
     *
     * @param moveSelectionOnEnter
     *            Flag to configure whether the selection should move after a
     *            value was committed after pressing enter.
     */
    public DateCellEditor(boolean moveSelectionOnEnter) {
        this.moveSelectionOnEnter = moveSelectionOnEnter;
    }

    @Override
    public Object getEditorValue() {
        Calendar cal = Calendar.getInstance();
        cal.set(this.dateTime.getYear(), this.dateTime.getMonth(), this.dateTime.getDay());
        return cal;
    }

    @Override
    public void setEditorValue(Object value) {
        // in setCanonicalValue() we ensure that the value is of type Calendar
        // but an additional check to ensure type safety doesn't hurt
        if (value instanceof Calendar) {
            Calendar cal = (Calendar) value;
            this.dateTime.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
        }
    }

    @Override
    public Object getCanonicalValue() {
        if (this.layerCell.getDataValue() instanceof Calendar) {
            return getEditorValue();
        } else if (this.layerCell.getDataValue() instanceof Date) {
            return ((Calendar) getEditorValue()).getTime();
        }
        return null;
    }

    @Override
    public void setCanonicalValue(Object canonicalValue) {
        Calendar editorValue = null;
        if (canonicalValue instanceof Calendar) {
            editorValue = (Calendar) canonicalValue;
        } else if (canonicalValue instanceof Date) {
            editorValue = Calendar.getInstance();
            editorValue.setTime((Date) canonicalValue);
        }

        if (editorValue != null) {
            setEditorValue(editorValue);
        }
    }

    @Override
    public DateTime getEditorControl() {
        return this.dateTime;
    }

    @Override
    public DateTime createEditorControl(final Composite parent) {
        final DateTime dateControl = new DateTime(parent, SWT.DATE | SWT.DROP_DOWN);

        // set style information configured in the associated cell style
        dateControl.setBackground(this.cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        dateControl.setForeground(this.cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
        dateControl.setFont(this.cellStyle.getAttributeValue(CellStyleAttributes.FONT));

        // add a key listener that will commit or close the editor for special
        // key strokes
        dateControl.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {

                    boolean commit = (event.stateMask == SWT.MOD3) ? false : true;
                    MoveDirectionEnum move = MoveDirectionEnum.NONE;
                    if (DateCellEditor.this.moveSelectionOnEnter
                            && DateCellEditor.this.editMode == EditModeEnum.INLINE) {
                        if (event.stateMask == 0) {
                            move = MoveDirectionEnum.DOWN;
                        } else if (event.stateMask == SWT.MOD2) {
                            move = MoveDirectionEnum.UP;
                        }
                    }

                    if (commit)
                        commit(move);

                    if (DateCellEditor.this.editMode == EditModeEnum.DIALOG) {
                        parent.forceFocus();
                    }
                } else if (event.keyCode == SWT.ESC && event.stateMask == 0) {
                    close();
                }
            }
        });

        return dateControl;
    }

    @Override
    protected Control activateCell(Composite parent, Object originalCanonicalValue) {
        this.dateTime = createEditorControl(parent);
        setCanonicalValue(originalCanonicalValue);

        // this is necessary so the control gets the focus
        // but this also causing some issues as focusing the DateTime control
        // programmatically does some strange things with showing the editable
        // data also it seems to be not possible to open the dropdown
        // programmatically
        this.dateTime.forceFocus();

        return this.dateTime;
    }

}
