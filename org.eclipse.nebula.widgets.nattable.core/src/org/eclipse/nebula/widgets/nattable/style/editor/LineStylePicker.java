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
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Component to select a {@link LineStyleEnum}.
 */
public class LineStylePicker extends Composite {

    private Combo combo;

    public LineStylePicker(Composite parent) {
        super(parent, NONE);
        setLayout(new RowLayout());

        this.combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        this.combo
                .setItems(new String[] {
                        Messages.getString("LineStylePicker.solid"), Messages.getString("LineStylePicker.dashed"), Messages.getString("LineStylePicker.dotted"), Messages.getString("LineStylePicker.dashdot"), Messages.getString("LineStylePicker.dashdotdot") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        this.combo.select(0);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.combo.setEnabled(enabled);
    }

    public void setSelectedLineStyle(LineStyleEnum lineStyle) {
        int index = 0;
        if (lineStyle.equals(LineStyleEnum.SOLID))
            index = 0;
        else if (lineStyle.equals(LineStyleEnum.DASHED))
            index = 1;
        else if (lineStyle.equals(LineStyleEnum.DOTTED))
            index = 2;
        else if (lineStyle.equals(LineStyleEnum.DASHDOT))
            index = 3;
        else if (lineStyle.equals(LineStyleEnum.DASHDOTDOT))
            index = 4;
        this.combo.select(index);
    }

    public LineStyleEnum getSelectedLineStyle() {
        int index = this.combo.getSelectionIndex();
        if (index == 0)
            return LineStyleEnum.SOLID;
        else if (index == 1)
            return LineStyleEnum.DASHED;
        else if (index == 2)
            return LineStyleEnum.DOTTED;
        else if (index == 3)
            return LineStyleEnum.DASHDOT;
        else if (index == 4)
            return LineStyleEnum.DASHDOTDOT;
        else
            throw new IllegalStateException("never happen"); //$NON-NLS-1$
    }

}
