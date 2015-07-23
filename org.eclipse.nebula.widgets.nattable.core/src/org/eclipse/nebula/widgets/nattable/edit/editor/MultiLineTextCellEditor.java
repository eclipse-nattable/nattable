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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * A specialization of {@link TextCellEditor} that uses a multi line text editor
 * as editor control. To support multi line editing correctly, the behaviour to
 * commit on pressing the enter key is disabled.
 * <p>
 * A multi line editor usually needs some space. Therefore it might be a good
 * decision to set the configuration attribute
 * {@link EditConfigAttributes#OPEN_IN_DIALOG} to <code>true</code> for this
 * editor, so the editor always opens in a subdialog.
 * </p>
 * <p>
 * As some table layouts may support enough space for an inline cell editor,
 * this editor does not specify
 * {@link ICellEditor#openInline(org.eclipse.nebula.widgets.nattable.config.IConfigRegistry, java.util.List)}
 * to always return <code>false</code>.
 * </p>
 */
public class MultiLineTextCellEditor extends TextCellEditor {

    /**
     * Flag to configure whether the text control should enable automatic line
     * wrap behaviour or not. By default this editor will support automatic line
     * wrapping.
     */
    private boolean lineWrap = true;

    /**
     * Create a new multi line text editor that ensures to not commit the editor
     * value in case enter is typed. The text control will support automatic
     * line wrapping.
     */
    public MultiLineTextCellEditor() {
        this(true);
    }

    /**
     * Create a new multi line text editor that ensures to not commit the editor
     * value in case enter is typed.
     *
     * @param lineWrap
     *            Flag to configure whether the text control should enable
     *            automatic line wrap behaviour or not.
     */
    public MultiLineTextCellEditor(boolean lineWrap) {
        this.commitOnEnter = false;
        this.lineWrap = lineWrap;
    }

    @Override
    public Text createEditorControl(Composite parent) {
        boolean openInline = openInline(this.configRegistry, this.labelStack.getLabels());

        int style = HorizontalAlignmentEnum.getSWTStyle(this.cellStyle) | SWT.MULTI | SWT.BORDER;
        if (!openInline) {
            // if the editor control is opened in a dialog, we add scrolling as
            // the size of the control is dependent on the dialog size
            style = style | SWT.V_SCROLL;
        }
        if (this.lineWrap) {
            style = style | SWT.WRAP;
        } else if (!openInline) {
            // if the editor control is opened in a dialog, we add scrolling as
            // the size of the control is dependent on the dialog size
            style = style | SWT.H_SCROLL;
        }
        final Text textControl = super.createEditorControl(parent, style);

        if (!openInline) {
            // add the layout data directly so it will not be layouted by the
            // CellEditDialog
            GridDataFactory.fillDefaults().grab(true, true).hint(100, 50).applyTo(textControl);
        }

        // on inline editing there need to be a different handling of the return
        // key as the Text control is performing a new line on return, it is not
        // possible to commit a value by pressing enter. So for inline editing
        // we catch enter to perform the commit, while pressing Alt/Shift +
        // enter will add a new line
        if (openInline) {
            this.commitOnEnter = true;
            textControl.addKeyListener(new KeyListener() {

                @Override
                public void keyReleased(KeyEvent event) {
                    if (event.keyCode == SWT.CR
                            || event.keyCode == SWT.KEYPAD_CR) {
                        if (event.stateMask == SWT.MOD3) {
                            textControl.insert(textControl.getLineDelimiter());
                        }
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {}
            });
        }

        return textControl;
    }

    @Override
    public Rectangle calculateControlBounds(final Rectangle cellBounds) {
        Point size = getEditorControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        // add a listener that increases/decreases the size of the control if
        // the text is modified as the calculateControlBounds method is only
        // called in case of inline editing, this listener shouldn't hurt
        // anybody else
        getEditorControl().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                Point p = getEditorControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
                Point loc = getEditorControl().getLocation();
                getEditorControl().setBounds(
                        loc.x,
                        loc.y,
                        Math.max(p.x, cellBounds.width),
                        Math.max(p.y, cellBounds.height));
            }
        });

        return new Rectangle(
                cellBounds.x,
                cellBounds.y,
                Math.max(size.x, cellBounds.width),
                Math.max(size.y, cellBounds.height));
    }

    /**
     * @param lineWrap
     *            <code>true</code> if the text control should enable automatic
     *            line wrap behaviour, <code>false</code> if not
     */
    public void setLineWrap(boolean lineWrap) {
        this.lineWrap = lineWrap;
    }
}
