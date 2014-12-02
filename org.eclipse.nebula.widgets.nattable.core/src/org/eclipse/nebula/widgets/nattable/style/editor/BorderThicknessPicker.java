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

import static org.eclipse.swt.SWT.NONE;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Control to select the thickness of a border.
 */
public class BorderThicknessPicker extends Composite {

    private Combo combo;

    public BorderThicknessPicker(Composite parent) {
        super(parent, NONE);
        setLayout(new RowLayout());

        this.combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        this.combo.setItems(new String[] {
                Messages.getString("BorderThicknessPicker.thin"), //$NON-NLS-1$
                Messages.getString("BorderThicknessPicker.thick"), //$NON-NLS-1$
                Messages.getString("BorderThicknessPicker.veryThick") }); //$NON-NLS-1$
        this.combo.select(0);
    }

    @Override
    public void setEnabled(boolean b) {
        this.combo.setEnabled(b);
    }

    public int getSelectedThickness() {
        int idx = this.combo.getSelectionIndex();
        if (idx == 0)
            return 1;
        else if (idx == 1)
            return 3;
        else if (idx == 2)
            return 6;
        else
            throw new IllegalStateException("never happen"); //$NON-NLS-1$
    }

    public void setSelectedThickness(int thickness) {
        if (thickness < 0)
            throw new IllegalArgumentException("negative number"); //$NON-NLS-1$
        int idx = 0;
        if (thickness < 3)
            idx = 0;
        else if (thickness < 6)
            idx = 1;
        else if (thickness > 6)
            idx = 2;
        this.combo.select(idx);
    }
}
