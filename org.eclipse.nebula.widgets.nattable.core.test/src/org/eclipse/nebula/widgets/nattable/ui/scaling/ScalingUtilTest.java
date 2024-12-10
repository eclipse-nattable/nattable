/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.ui.scaling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ScalingUtilTest {

    @Test
    public void shouldGetZoomInDPIOs() {
        assertEquals(24, ScalingUtil.getNewZoomInDPI(12, false));
        assertEquals(48, ScalingUtil.getNewZoomInDPI(24, false));
        assertEquals(96, ScalingUtil.getNewZoomInDPI(48, false));
        assertEquals(120, ScalingUtil.getNewZoomInDPI(96, false));
        assertEquals(144, ScalingUtil.getNewZoomInDPI(120, false));
        assertEquals(192, ScalingUtil.getNewZoomInDPI(144, false));
        assertEquals(288, ScalingUtil.getNewZoomInDPI(192, false));
        assertEquals(288, ScalingUtil.getNewZoomInDPI(288, false));
    }

    @Test
    public void shouldGetZoomOutDPIOs() {
        assertEquals(192, ScalingUtil.getNewZoomOutDPI(288, false));
        assertEquals(144, ScalingUtil.getNewZoomOutDPI(192, false));
        assertEquals(120, ScalingUtil.getNewZoomOutDPI(144, false));
        assertEquals(96, ScalingUtil.getNewZoomOutDPI(120, false));
        assertEquals(48, ScalingUtil.getNewZoomOutDPI(96, false));
        assertEquals(24, ScalingUtil.getNewZoomOutDPI(48, false));
        assertEquals(12, ScalingUtil.getNewZoomOutDPI(24, false));
        assertEquals(12, ScalingUtil.getNewZoomOutDPI(12, false));
    }

    @Test
    public void shouldGetZoomInDPIPercentage() {
        assertEquals(13, ScalingUtil.getNewZoomInDPI(12, true));
        assertEquals(14, ScalingUtil.getNewZoomInDPI(13, true));
        assertEquals(15, ScalingUtil.getNewZoomInDPI(14, true));
        assertEquals(16, ScalingUtil.getNewZoomInDPI(15, true));
        assertEquals(17, ScalingUtil.getNewZoomInDPI(16, true));
        assertEquals(18, ScalingUtil.getNewZoomInDPI(17, true));
        assertEquals(20, ScalingUtil.getNewZoomInDPI(18, true));
        assertEquals(22, ScalingUtil.getNewZoomInDPI(20, true));
        assertEquals(24, ScalingUtil.getNewZoomInDPI(22, true));
        assertEquals(26, ScalingUtil.getNewZoomInDPI(24, true));
        assertEquals(28, ScalingUtil.getNewZoomInDPI(26, true));
        assertEquals(31, ScalingUtil.getNewZoomInDPI(28, true));
        assertEquals(34, ScalingUtil.getNewZoomInDPI(31, true));
        assertEquals(37, ScalingUtil.getNewZoomInDPI(34, true));
        assertEquals(41, ScalingUtil.getNewZoomInDPI(37, true));
        assertEquals(45, ScalingUtil.getNewZoomInDPI(41, true));
        assertEquals(50, ScalingUtil.getNewZoomInDPI(45, true));
        assertEquals(55, ScalingUtil.getNewZoomInDPI(50, true));
        assertEquals(61, ScalingUtil.getNewZoomInDPI(55, true));
        assertEquals(67, ScalingUtil.getNewZoomInDPI(61, true));
        assertEquals(74, ScalingUtil.getNewZoomInDPI(67, true));
        assertEquals(82, ScalingUtil.getNewZoomInDPI(74, true));
        // mathematically this would be different, but we want to get to a os
        // dpi value in such corner cases
        assertEquals(96, ScalingUtil.getNewZoomInDPI(82, true));
        assertEquals(106, ScalingUtil.getNewZoomInDPI(96, true));
        assertEquals(120, ScalingUtil.getNewZoomInDPI(106, true));
        assertEquals(133, ScalingUtil.getNewZoomInDPI(120, true));
        // mathematically this would be different, but we want to get to a os
        // dpi value in such corner cases
        assertEquals(144, ScalingUtil.getNewZoomInDPI(133, true));
        assertEquals(160, ScalingUtil.getNewZoomInDPI(144, true));
        assertEquals(177, ScalingUtil.getNewZoomInDPI(160, true));
        // mathematically this would be different, but we want to get to a os
        // dpi value in such corner cases
        assertEquals(192, ScalingUtil.getNewZoomInDPI(177, true));
        assertEquals(213, ScalingUtil.getNewZoomInDPI(192, true));
        assertEquals(236, ScalingUtil.getNewZoomInDPI(213, true));
        assertEquals(262, ScalingUtil.getNewZoomInDPI(236, true));
        assertEquals(288, ScalingUtil.getNewZoomInDPI(262, true));
        // mathematically this would be different, but we want to get to a os
        // dpi value in such corner cases
        assertEquals(288, ScalingUtil.getNewZoomInDPI(288, true));
    }

    @Test
    public void shouldGetZoomOutDPIPercentage() {
        assertEquals(259, ScalingUtil.getNewZoomOutDPI(288, true));
        assertEquals(233, ScalingUtil.getNewZoomOutDPI(259, true));
        assertEquals(209, ScalingUtil.getNewZoomOutDPI(233, true));
        assertEquals(192, ScalingUtil.getNewZoomOutDPI(209, true));
        assertEquals(172, ScalingUtil.getNewZoomOutDPI(192, true));
        assertEquals(154, ScalingUtil.getNewZoomOutDPI(172, true));
        assertEquals(144, ScalingUtil.getNewZoomOutDPI(154, true));
        assertEquals(129, ScalingUtil.getNewZoomOutDPI(144, true));
        assertEquals(120, ScalingUtil.getNewZoomOutDPI(129, true));
        assertEquals(108, ScalingUtil.getNewZoomOutDPI(120, true));
        assertEquals(96, ScalingUtil.getNewZoomOutDPI(108, true));
        assertEquals(86, ScalingUtil.getNewZoomOutDPI(96, true));
        assertEquals(77, ScalingUtil.getNewZoomOutDPI(86, true));
        assertEquals(69, ScalingUtil.getNewZoomOutDPI(77, true));
        assertEquals(43, ScalingUtil.getNewZoomOutDPI(48, true));
        assertEquals(38, ScalingUtil.getNewZoomOutDPI(43, true));
        assertEquals(34, ScalingUtil.getNewZoomOutDPI(38, true));
        assertEquals(30, ScalingUtil.getNewZoomOutDPI(34, true));
        assertEquals(27, ScalingUtil.getNewZoomOutDPI(30, true));
        assertEquals(24, ScalingUtil.getNewZoomOutDPI(27, true));
        assertEquals(21, ScalingUtil.getNewZoomOutDPI(24, true));
        assertEquals(18, ScalingUtil.getNewZoomOutDPI(21, true));
        assertEquals(16, ScalingUtil.getNewZoomOutDPI(18, true));
        assertEquals(14, ScalingUtil.getNewZoomOutDPI(16, true));
        assertEquals(12, ScalingUtil.getNewZoomOutDPI(14, true));
        assertEquals(12, ScalingUtil.getNewZoomOutDPI(12, true));
    }
}
