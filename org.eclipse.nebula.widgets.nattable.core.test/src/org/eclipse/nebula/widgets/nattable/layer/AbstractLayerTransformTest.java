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
package org.eclipse.nebula.widgets.nattable.layer;

import org.eclipse.nebula.widgets.nattable.test.LayerAssert;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AbstractLayerTransformTest {

    private ILayer testLayer;

    @BeforeEach
    public void setup() {
        String columnInfo = "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100";
        String rowInfo = "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40";

        String cellInfo = "A0 | <  | C0 | D0 \n" + "^  | <  | C1 | D1 \n"
                + "A2 | B2 | C2 | D2 \n" + "A3 | B3 | C3 | D3 \n";

        this.testLayer = new TestLayer(4, 4, columnInfo, rowInfo, cellInfo);
    }

    @Test
    public void testIdentityLayerTransform() {
        LayerAssert.assertLayerEquals(
                this.testLayer,
                new AbstractLayerTransform(this.testLayer) {
                });
    }

}
