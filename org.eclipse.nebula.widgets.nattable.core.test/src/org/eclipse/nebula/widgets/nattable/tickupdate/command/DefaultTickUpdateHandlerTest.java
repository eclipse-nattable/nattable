/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tickupdate.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.tickupdate.ITickUpdateHandler;
import org.junit.Test;

public class DefaultTickUpdateHandlerTest {

    @Test
    public void shouldHandleNumbers() {
        assertTrue(ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.isApplicableFor(Byte.valueOf((byte) 5)));
        assertTrue(ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.isApplicableFor(Short.valueOf((short) 42)));
        assertTrue(ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.isApplicableFor(Integer.valueOf(1024)));
        assertTrue(ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.isApplicableFor(666666l));
        assertTrue(ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.isApplicableFor(3.14f));
        assertTrue(ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.isApplicableFor(13.10d));
    }

    @Test
    public void shouldNotHandleNonNumbers() {
        assertFalse(ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.isApplicableFor("Hello"));
        assertFalse(ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.isApplicableFor(PersonService.createAddress()));
    }

    @Test
    public void shouldIncreaseTypeCorrectly() {
        assertEquals(Byte.valueOf("6"), ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getIncrementedValue(Byte.valueOf("5"), 1));
        assertEquals(Short.valueOf("43"), ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getIncrementedValue(Short.valueOf("42"), 1));
        assertEquals(1026, ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getIncrementedValue(1024, 2));
        assertEquals(666676l, ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getIncrementedValue(666666l, 10));
        assertEquals(Float.valueOf("3.24"), ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getIncrementedValue(Float.valueOf("3.14"), 0.1));
        assertEquals(Double.valueOf("13.6"), ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getIncrementedValue(Double.valueOf("13.10"), 0.5));
    }

    @Test
    public void shouldDecreaseTypeCorrectly() {
        assertEquals(Byte.valueOf("4"), ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getDecrementedValue(Byte.valueOf("5"), 1));
        assertEquals(Short.valueOf("41"), ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getDecrementedValue(Short.valueOf("42"), 1));
        assertEquals(1022, ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getDecrementedValue(1024, 2));
        assertEquals(666656l, ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getDecrementedValue(666666l, 10));
        assertEquals(Float.valueOf("2.94"), ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getDecrementedValue(Float.valueOf("3.14"), 0.2));
        assertEquals(Double.valueOf("12.6"), ITickUpdateHandler.DEFAULT_TICK_UPDATE_HANDLER.getDecrementedValue(Double.valueOf("13.10"), 0.5));
    }
}
