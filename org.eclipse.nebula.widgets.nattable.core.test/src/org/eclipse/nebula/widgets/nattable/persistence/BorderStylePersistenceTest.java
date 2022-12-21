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
 *     Loris Securo <lorissek@gmail.com> - Bug 499701
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.junit.jupiter.api.Test;

public class BorderStylePersistenceTest {

    @Test
    public void toStringCreatesAPersistableString() throws Exception {
        BorderStyle borderStyle = new BorderStyle();
        assertEquals("1|0,0,0|SOLID|CENTERED", borderStyle.toString());
    }

    @Test
    public void canRecreateInstanceFromAPersistedStringWithoutBorderMode() throws Exception {
        BorderStyle borderStyle = new BorderStyle("2|100,110,120|DOTTED");

        assertEquals(2, borderStyle.getThickness());
        assertEquals(100, borderStyle.getColor().getRed());
        assertEquals(110, borderStyle.getColor().getGreen());
        assertEquals(120, borderStyle.getColor().getBlue());
        assertEquals(LineStyleEnum.DOTTED, borderStyle.getLineStyle());
        assertEquals(BorderModeEnum.CENTERED, borderStyle.getBorderMode());
    }

    @Test
    public void canRecreateInstanceFromAPersistedString() throws Exception {
        BorderStyle borderStyle = new BorderStyle("2|100,110,120|DOTTED|INTERNAL");

        assertEquals(2, borderStyle.getThickness());
        assertEquals(100, borderStyle.getColor().getRed());
        assertEquals(110, borderStyle.getColor().getGreen());
        assertEquals(120, borderStyle.getColor().getBlue());
        assertEquals(LineStyleEnum.DOTTED, borderStyle.getLineStyle());
        assertEquals(BorderModeEnum.INTERNAL, borderStyle.getBorderMode());
    }
}
