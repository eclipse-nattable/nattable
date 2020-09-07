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
package org.eclipse.nebula.widgets.nattable.style;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.junit.Test;

public class CellStyleProxyTest {

    private static final String TEST_CONFIG_LABEL1 = "testConfigLabel1";

    @Test
    public void proxyShouldRetreiveConfigAttributeUsingTheDisplayModeOrdering()
            throws Exception {
        ConfigRegistry configRegistry = new ConfigRegistry();

        Style testCellStyle1 = new Style();
        testCellStyle1.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                HorizontalAlignmentEnum.RIGHT);

        Style testCellStyle2 = new Style();
        testCellStyle2.setAttributeValue(
                CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                HorizontalAlignmentEnum.CENTER);
        testCellStyle2.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                VerticalAlignmentEnum.MIDDLE);

        // NORMAL mode has an horizontal align attribute registered
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                testCellStyle1, DisplayMode.NORMAL, TEST_CONFIG_LABEL1);

        // SELECT mode has a 'default' horizontal align attribute registered
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                testCellStyle2, DisplayMode.SELECT);

        // The 'default' from SELECT gets picked up
        StyleProxy cellStyleProxy = new CellStyleProxy(configRegistry,
                DisplayMode.SELECT, Arrays.asList(TEST_CONFIG_LABEL1));
        HorizontalAlignmentEnum alignmentFromProxy = cellStyleProxy
                .getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);

        assertEquals(HorizontalAlignmentEnum.CENTER, alignmentFromProxy);
    }
}
