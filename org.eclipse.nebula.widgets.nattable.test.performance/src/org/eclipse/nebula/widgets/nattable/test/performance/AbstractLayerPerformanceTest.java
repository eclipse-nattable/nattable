/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.test.performance;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractLayerPerformanceTest {

    public static final long DEFAULT_THRESHOLD = 100;

    private Shell shell;

    private long expectedTimeInMillis;

    protected ILayer layer;

    public Shell getShell() {
        return this.shell;
    }

    public long getExpectedTimeInMillis() {
        return this.expectedTimeInMillis;
    }

    public void setExpectedTimeInMillis(long expectedTimeInMillis) {
        this.expectedTimeInMillis = expectedTimeInMillis;
    }

    @BeforeEach
    public void setup() {
        this.layer = null;
        this.expectedTimeInMillis = DEFAULT_THRESHOLD;

        this.shell = new Shell();
        this.shell.setLayout(new FillLayout());
        this.shell.setSize(1800, 800);
        this.shell.setLocation(0, 0);

    }

    @AfterEach
    public void tearDown() {
        assertNotNull(this.layer, "Layer was not set");

        new NatTable(getShell(), this.layer) {
            @Override
            public void paintControl(PaintEvent event) {
                // Start!
                long startTimeInMillis = System.currentTimeMillis();
                super.paintControl(event);
                // Stop!
                long stopTimeInMillis = System.currentTimeMillis();
                long actualTimeInMillis = stopTimeInMillis - startTimeInMillis;

                System.out.println("duration = " + actualTimeInMillis + " milliseconds");
                assertTrue(actualTimeInMillis < AbstractLayerPerformanceTest.this.expectedTimeInMillis,
                        "Expected to take less than " + AbstractLayerPerformanceTest.this.expectedTimeInMillis
                                + " milliseconds but took " + actualTimeInMillis
                                + " milliseconds");
            }
        };

        getShell().setVisible(true);

        this.shell.dispose();
    }

}
