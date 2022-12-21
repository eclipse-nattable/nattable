/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DpiConverterTest {

    @Test
    public void shouldNotScaleWithDPI96() {
        IDpiConverter converter = new NoScalingDpiConverter();

        assertEquals(100, converter.convertPixelToDpi(100));
        assertEquals(100, converter.convertDpiToPixel(100));

        assertEquals(1.0f, converter.getCurrentDpiFactor(), 0.01f);
    }

    @Test
    public void shouldScaleWithDPI120() {
        // use dpi of 120 which will result in a dpi factor of 1.25
        IDpiConverter converter = new FixedScalingDpiConverter(120);

        assertEquals(125, converter.convertPixelToDpi(100));
        assertEquals(100, converter.convertDpiToPixel(125));

        assertEquals(1.25f, converter.getCurrentDpiFactor(), 0.01f);

        assertEquals(663, converter.convertPixelToDpi(530));
        assertEquals(530, converter.convertDpiToPixel(663));
        assertEquals(1163, converter.convertPixelToDpi(930));
        assertEquals(930, converter.convertDpiToPixel(1163));
    }

    @Test
    public void shouldScaleWithDPI144() {
        // use dpi of 144 which will result in a dpi factor of 1.5
        IDpiConverter converter = new FixedScalingDpiConverter(144);

        assertEquals(1.5f, converter.getCurrentDpiFactor(), 0.01f);

        assertEquals(150, converter.convertPixelToDpi(100));
        assertEquals(100, converter.convertDpiToPixel(150));

        assertEquals(795, converter.convertPixelToDpi(530));
        assertEquals(530, converter.convertDpiToPixel(795));
        assertEquals(1395, converter.convertPixelToDpi(930));
        assertEquals(930, converter.convertDpiToPixel(1395));
    }
}
