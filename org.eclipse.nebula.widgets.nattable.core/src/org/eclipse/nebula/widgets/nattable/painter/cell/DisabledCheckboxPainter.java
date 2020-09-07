/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

public class DisabledCheckboxPainter extends CheckBoxPainter {

    public DisabledCheckboxPainter() {
        super(GUIHelper.getImage("checked_disabled"), GUIHelper.getImage("unchecked_disabled")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public DisabledCheckboxPainter(Image checkedImg, Image uncheckedImg) {
        super(checkedImg, uncheckedImg);
    }
}
