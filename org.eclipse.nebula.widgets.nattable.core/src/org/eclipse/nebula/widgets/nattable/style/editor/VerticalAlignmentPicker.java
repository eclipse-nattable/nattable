/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.editor;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Component that lets the user select an alignment.
 */
public class VerticalAlignmentPicker extends Composite {

    private final Combo combo;

    public VerticalAlignmentPicker(Composite parent,
            VerticalAlignmentEnum alignment) {
        super(parent, SWT.NONE);
        setLayout(new RowLayout());

        this.combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        this.combo.setItems(new String[] {
                Messages.getString("VerticalAlignmentPicker.top"), Messages.getString("VerticalAlignmentPicker.middle"), Messages.getString("VerticalAlignmentPicker.bottom") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        update(alignment);
    }

    private void update(VerticalAlignmentEnum alignment) {
        if (alignment.equals(VerticalAlignmentEnum.TOP))
            this.combo.select(0);
        else if (alignment.equals(VerticalAlignmentEnum.MIDDLE))
            this.combo.select(1);
        else if (alignment.equals(VerticalAlignmentEnum.BOTTOM))
            this.combo.select(2);
        else
            throw new IllegalArgumentException("bad alignment: " + alignment); //$NON-NLS-1$
    }

    public VerticalAlignmentEnum getSelectedAlignment() {
        int idx = this.combo.getSelectionIndex();
        if (idx == 0)
            return VerticalAlignmentEnum.TOP;
        else if (idx == 1)
            return VerticalAlignmentEnum.MIDDLE;
        else if (idx == 2)
            return VerticalAlignmentEnum.BOTTOM;
        else
            throw new IllegalStateException("shouldn't happen"); //$NON-NLS-1$
    }

    public void setSelectedAlignment(VerticalAlignmentEnum verticalAlignment) {
        if (verticalAlignment == null)
            throw new IllegalArgumentException("null"); //$NON-NLS-1$
        update(verticalAlignment);
    }
}
