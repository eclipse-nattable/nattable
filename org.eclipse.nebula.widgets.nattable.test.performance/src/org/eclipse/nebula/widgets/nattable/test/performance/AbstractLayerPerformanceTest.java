/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.performance;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

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

    @Before
    public void setup() {
        this.layer = null;
        this.expectedTimeInMillis = DEFAULT_THRESHOLD;

        this.shell = new Shell();
        this.shell.setLayout(new FillLayout());
        this.shell.setSize(1800, 800);
        this.shell.setLocation(0, 0);

    }

    @After
    public void tearDown() {
        Assert.assertNotNull("Layer was not set", this.layer);

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
                Assert.assertTrue("Expected to take less than " + AbstractLayerPerformanceTest.this.expectedTimeInMillis
                        + " milliseconds but took " + actualTimeInMillis
                        + " milliseconds", actualTimeInMillis < AbstractLayerPerformanceTest.this.expectedTimeInMillis);
            }
        };

        getShell().setVisible(true);

        this.shell.dispose();
    }

}
