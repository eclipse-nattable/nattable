/*******************************************************************************
 * Copyright (c) 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.nebula.cdatetime;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * ICellEditor implementation that uses the Nebula {@link CDateTime} control for
 * editing. It supports objects of type Date and Calendar aswell.
 * <p>
 * Introduces the contract that the editor control value is of type Calendar.
 * Therefore the methods to deal with the canonical values need to be overriden
 * too, to avoid conversion of the canonical value to display value by using the
 * IDisplayConverter that is registered together with this editor.
 * 
 * @since 1.1
 */
public class CDateTimeCellEditor extends AbstractCellEditor {

    /**
     * The DateTime control which is the editor wrapped by this DateCellEditor.
     */
    private CDateTime dateTime;

    /**
     * Flag to configure whether the selection should move after a value was
     * committed after pressing enter.
     */
    private final boolean moveSelectionOnEnter;

    /**
     * The style bits that should be used to create the {@link CDateTime}
     * control of the editor.
     */
    private int style;

    /**
     * Flag to configure whether the editor should provide a {@link Calendar}
     * object on editing or a {@link Date}. By default a {@link Date} is
     * returned as {@link CDateTime} returns {@link Date} objects.
     */
    private boolean provideCalendar = false;

    /**
     * Creates the default DateCellEditor that does not move the selection on
     * committing the value by pressing enter.
     */
    public CDateTimeCellEditor() {
        this(false);
    }

    /**
     * Creates a DateCellEditor.
     *
     * @param moveSelectionOnEnter
     *            Flag to configure whether the selection should move after a
     *            value was committed after pressing enter.
     */
    public CDateTimeCellEditor(boolean moveSelectionOnEnter) {
        this(moveSelectionOnEnter, CDT.DROP_DOWN | CDT.DATE_SHORT | CDT.TIME_SHORT);
    }

    /**
     * Creates a DateCellEditor.
     *
     * @param moveSelectionOnEnter
     *            Flag to configure whether the selection should move after a
     *            value was committed after pressing enter.
     * @param style
     *            The style bits that should be used to create the
     *            {@link CDateTime} control of the editor.
     */
    public CDateTimeCellEditor(boolean moveSelectionOnEnter, int style) {
        this.moveSelectionOnEnter = moveSelectionOnEnter;
        this.style = style;
    }

    @Override
    public Object getEditorValue() {
        if (this.dateTime.getSelection() != null) {
            return this.dateTime.getSelection();
        }
        return null;
    }

    @Override
    public void setEditorValue(Object value) {
        if (value instanceof Calendar) {
            Calendar cal = (Calendar) value;
            this.dateTime.setSelection(cal.getTime());
        } else if (value instanceof Date) {
            this.dateTime.setSelection((Date) value);
        }
    }

    @Override
    public Object getCanonicalValue() {
        // there is no need for conversion because the CDateTime control
        // already returns a Date
        if (this.provideCalendar) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) getEditorValue());
            return cal;
        }
        return getEditorValue();
    }

    @Override
    public void setCanonicalValue(Object canonicalValue) {
        Date editorValue = null;
        if (canonicalValue instanceof Calendar) {
            editorValue = ((Calendar) canonicalValue).getTime();
        } else if (canonicalValue instanceof Date) {
            editorValue = (Date) canonicalValue;
        }

        if (editorValue != null) {
            setEditorValue(editorValue);
        }
    }

    @Override
    public CDateTime getEditorControl() {
        return this.dateTime;
    }

    @Override
    public CDateTime createEditorControl(final Composite parent) {
        final CDateTime dateControl = new CDateTime(parent, this.style) {
            @Override
            protected Shell getContentShell() {
                Shell shell = super.getContentShell();
                shell.addShellListener(new ShellAdapter() {

                    @Override
                    public void shellActivated(ShellEvent e) {
                        if (CDateTimeCellEditor.this.focusListener instanceof InlineFocusListener) {
                            ((InlineFocusListener) CDateTimeCellEditor.this.focusListener).handleFocusChanges = false;
                        }
                    }

                    @Override
                    public void shellClosed(ShellEvent e) {
                        if (CDateTimeCellEditor.this.focusListener instanceof InlineFocusListener) {
                            ((InlineFocusListener) CDateTimeCellEditor.this.focusListener).handleFocusChanges = true;
                        }
                    }
                });
                return shell;
            }

            @Override
            protected void addTextListener() {
                super.addTextListener();
                this.text.getControl().addTraverseListener(new TraverseListener() {

                    @Override
                    public void keyTraversed(TraverseEvent event) {
                        boolean committed = false;
                        if (event.keyCode == SWT.TAB && event.stateMask == SWT.MOD2) {
                            committed = commit(MoveDirectionEnum.LEFT);
                        } else if (event.keyCode == SWT.TAB && event.stateMask == 0) {
                            committed = commit(MoveDirectionEnum.RIGHT);
                        } else if (event.detail == SWT.TRAVERSE_ESCAPE) {
                            close();
                        }
                        if (!committed) {
                            event.doit = false;
                        }
                    }
                });
            }
        };

        // set style information configured in the associated cell style
        dateControl.setBackground(this.cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        dateControl.setForeground(this.cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
        // when trying to set a font in simple mode (embedded GUI) we will get a
        // StackOverflowError
        if ((this.style & CDT.SIMPLE) == 0) {
            dateControl.setFont(this.cellStyle.getAttributeValue(CellStyleAttributes.FONT));
        }

        dateControl.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                boolean commit = (event.stateMask == SWT.MOD3) ? false : true;
                MoveDirectionEnum move = MoveDirectionEnum.NONE;
                if (CDateTimeCellEditor.this.moveSelectionOnEnter
                        && CDateTimeCellEditor.this.editMode == EditModeEnum.INLINE) {
                    if (event.stateMask == 0) {
                        move = MoveDirectionEnum.DOWN;
                    } else if (event.stateMask == SWT.MOD2) {
                        move = MoveDirectionEnum.UP;
                    }
                }

                if (commit)
                    commit(move);

                if (CDateTimeCellEditor.this.editMode == EditModeEnum.DIALOG) {
                    parent.forceFocus();
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

    /**
     *
     * @param provideCalendar
     *            <code>true</code> if this editor should provide a
     *            {@link Calendar} object on editing, <code>false</code> if it
     *            should provide a {@link Date} object.
     */
    public void setProvideCalendar(boolean provideCalendar) {
        this.provideCalendar = provideCalendar;
    }
}
