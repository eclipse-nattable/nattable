/*******************************************************************************
 * Copyright (c) 2013, 2022 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class CalculatedValueCacheTest {

    ICalculatedValueCache valueCache;

    ICalculator calculator = () -> {

        // simply add some delay
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Integer.valueOf(42);
    };

    @Test
    public void testCalculateInBackgroundWithCoordsKey() throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), true, true);

        Object result = this.valueCache.getCalculatedValue(0, 0, true,
                this.calculator);
        // as calculation is performed in background, the immediate return value
        // is null
        assertNull(result);
        // now wait so the background process is able to finish
        Thread.sleep(250);
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
    }

    @Test
    public void testCalculateInSameThreadWithCoordsKey() throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), true, true);

        Object result = this.valueCache.getCalculatedValue(0, 0, false,
                this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
    }

    @Test
    public void testCalculateInBackgroundWithCoordsKeyClearSmooth()
            throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), true, true);

        Object result = this.valueCache.getCalculatedValue(0, 0, true,
                this.calculator);
        // as calculation is performed in background, the immediate return value
        // is null
        assertNull(result);
        // now wait so the background process is able to finish
        Thread.sleep(250);
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));

        this.valueCache.clearCache();
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        // as the cache is configured for smooth updates, the value should be
        // still there
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
    }

    @Test
    public void testCalculateInBackgroundWithCoordsKeyClearNonSmooth()
            throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), true, true, false);

        Object result = this.valueCache.getCalculatedValue(0, 0, true,
                this.calculator);
        // as calculation is performed in background, the immediate return value
        // is null
        assertNull(result);
        // now wait so the background process is able to finish
        Thread.sleep(250);
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));

        this.valueCache.clearCache();
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        // as the cache is configured for non smooth updates, the value should
        // be null again
        assertNull(result);
    }

    @Test
    public void testCalculateInBackgroundWithCoordsKeyKillSmooth()
            throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), true, true);

        Object result = this.valueCache.getCalculatedValue(0, 0, true,
                this.calculator);
        // as calculation is performed in background, the immediate return value
        // is null
        assertNull(result);
        // now wait so the background process is able to finish
        Thread.sleep(250);
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));

        this.valueCache.killCache();
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        // killing the cache should have the same effect as clearing non smooth
        assertNull(result);
    }

    @Test
    public void testCalculateInBackgroundWithColumnKey() throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), true, false);

        Object result = this.valueCache.getCalculatedValue(0, 0, true,
                this.calculator);
        // as calculation is performed in background, the immediate return value
        // is null
        assertNull(result);
        // now wait so the background process is able to finish
        Thread.sleep(250);
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
    }

    @Test
    public void testCalculateInSameThreadWithColumnKey() throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), true, false);

        Object result = this.valueCache.getCalculatedValue(0, 0, false,
                this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
    }

    @Test
    public void testCalculateInBackgroundWithRowKey() throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), false, true);

        Object result = this.valueCache.getCalculatedValue(0, 0, true,
                this.calculator);
        // as calculation is performed in background, the immediate return value
        // is null
        assertNull(result);
        // now wait so the background process is able to finish
        Thread.sleep(250);
        result = this.valueCache.getCalculatedValue(0, 0, true, this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
    }

    @Test
    public void testCalculateInSameThreadWithRowKey() throws Exception {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), false, true);

        Object result = this.valueCache.getCalculatedValue(0, 0, false,
                this.calculator);
        assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
    }

    @Test
    public void testIllegalState() {
        this.valueCache = new CalculatedValueCache(new DataLayer(
                new DummyBodyDataProvider(10, 10)), false, false);
        assertThrows(IllegalStateException.class, () -> {
            this.valueCache.getCalculatedValue(0, 0, false, this.calculator);
        });
    }

    @AfterEach
    public void tearDown() {
        this.valueCache.dispose();
    }

    // TODO test cache clearing
    // TODO test cache kill
    // TODO test smooth updates
}
