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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

public class DisabledCheckboxPainter extends CheckBoxPainter {

    public DisabledCheckboxPainter() {
        super(
                GUIHelper.getImage("checked_disabled"), GUIHelper.getImage("unchecked_disabled")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public DisabledCheckboxPainter(Image checkedImg, Image uncheckedImg) {
        super(checkedImg, uncheckedImg);
    }
}
