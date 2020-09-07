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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;

public class BorderStyleFixture extends BorderStyle {

    public static int THICKNESS = 2;
    public static Color COLOR = GUIHelper.COLOR_GREEN;
    public static LineStyleEnum LINE_STYLE = LineStyleEnum.DASHDOT;

    public BorderStyleFixture() {
        super(2, COLOR, LINE_STYLE);
    }
}
