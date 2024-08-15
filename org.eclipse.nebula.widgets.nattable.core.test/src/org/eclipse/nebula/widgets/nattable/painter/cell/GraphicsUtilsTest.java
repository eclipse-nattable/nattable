/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

public class GraphicsUtilsTest {

    @Test
    public void shouldGetBoundsExternalBorder() {
        Rectangle r = new Rectangle(10, 10, 20, 20);
        BorderStyle borderStyle = new BorderStyle(6, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID, BorderModeEnum.EXTERNAL);

        // first get the external bounds
        Rectangle result = GraphicsUtils.getResultingExternalBounds(r, borderStyle);

        // bounds = thickness - 1
        assertEquals(5, result.x);
        assertEquals(5, result.y);
        assertEquals(30, result.width);
        assertEquals(30, result.height);

        // now get the internal bounds from the external bounds
        Rectangle internal = GraphicsUtils.getInternalBounds(result, borderStyle);

        assertEquals(10, internal.x);
        assertEquals(10, internal.y);
        assertEquals(20, internal.width);
        assertEquals(20, internal.height);
    }

    @Test
    public void shouldGetBoundsInternalBorder() {
        Rectangle r = new Rectangle(10, 10, 20, 20);
        BorderStyle borderStyle = new BorderStyle(6, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID, BorderModeEnum.INTERNAL);

        // first get the external bounds
        Rectangle result = GraphicsUtils.getResultingExternalBounds(r, borderStyle);

        // bounds = thickness - 1
        assertEquals(10, result.x);
        assertEquals(10, result.y);
        assertEquals(20, result.width);
        assertEquals(20, result.height);

        // now get the internal bounds from the external bounds
        Rectangle internal = GraphicsUtils.getInternalBounds(result, borderStyle);

        assertEquals(10, internal.x);
        assertEquals(10, internal.y);
        assertEquals(20, internal.width);
        assertEquals(20, internal.height);
    }

    @Test
    public void shouldGetBoundsCenteredBorder() {
        Rectangle r = new Rectangle(10, 10, 20, 20);
        BorderStyle borderStyle = new BorderStyle(6, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID, BorderModeEnum.CENTERED);

        // first get the external bounds
        Rectangle result = GraphicsUtils.getResultingExternalBounds(r, borderStyle);

        // bounds = thickness - 1
        assertEquals(7, result.x);
        assertEquals(7, result.y);
        assertEquals(25, result.width);
        assertEquals(25, result.height);

        // now get the internal bounds from the external bounds
        Rectangle internal = GraphicsUtils.getInternalBounds(result, borderStyle);

        assertEquals(10, internal.x);
        assertEquals(10, internal.y);
        assertEquals(20, internal.width);
        assertEquals(20, internal.height);
    }

    @Test
    public void shouldGetBoundsExternalBorderNegativeThickness() {
        Rectangle r = new Rectangle(10, 10, 20, 20);
        BorderStyle borderStyle = new BorderStyle(-6, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID, BorderModeEnum.EXTERNAL);

        // first get the external bounds
        Rectangle result = GraphicsUtils.getResultingExternalBounds(r, borderStyle);

        // bounds = thickness - 1
        assertEquals(15, result.x);
        assertEquals(15, result.y);
        assertEquals(10, result.width);
        assertEquals(10, result.height);

        // now get the internal bounds from the external bounds
        Rectangle internal = GraphicsUtils.getInternalBounds(result, borderStyle);

        assertEquals(10, internal.x);
        assertEquals(10, internal.y);
        assertEquals(20, internal.width);
        assertEquals(20, internal.height);
    }

    @Test
    public void shouldGetBoundsInternalBorderNegativeThickness() {
        Rectangle r = new Rectangle(10, 10, 20, 20);
        BorderStyle borderStyle = new BorderStyle(-6, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID, BorderModeEnum.INTERNAL);

        // first get the external bounds
        Rectangle result = GraphicsUtils.getResultingExternalBounds(r, borderStyle);

        // bounds = thickness - 1
        assertEquals(10, result.x);
        assertEquals(10, result.y);
        assertEquals(20, result.width);
        assertEquals(20, result.height);

        // now get the internal bounds from the external bounds
        Rectangle internal = GraphicsUtils.getInternalBounds(result, borderStyle);

        assertEquals(10, internal.x);
        assertEquals(10, internal.y);
        assertEquals(20, internal.width);
        assertEquals(20, internal.height);
    }

    @Test
    public void shouldGetBoundsCenteredBorderNegativeThickness() {
        Rectangle r = new Rectangle(10, 10, 20, 20);
        BorderStyle borderStyle = new BorderStyle(-6, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID, BorderModeEnum.CENTERED);

        // first get the external bounds
        Rectangle result = GraphicsUtils.getResultingExternalBounds(r, borderStyle);

        // bounds = thickness - 1
        assertEquals(13, result.x);
        assertEquals(13, result.y);
        assertEquals(15, result.width);
        assertEquals(15, result.height);

        // now get the internal bounds from the external bounds
        Rectangle internal = GraphicsUtils.getInternalBounds(result, borderStyle);

        assertEquals(10, internal.x);
        assertEquals(10, internal.y);
        assertEquals(20, internal.width);
        assertEquals(20, internal.height);
    }
}
