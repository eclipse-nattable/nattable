/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.convert;

import org.junit.Assert;
import org.junit.Test;

public class DefaultCharacterDisplayConverterTest {

    private DefaultCharacterDisplayConverter characterConverter = new DefaultCharacterDisplayConverter();

    @Test
    public void testNonNullDataToDisplay() {
        Assert.assertEquals("a",
                this.characterConverter.canonicalToDisplayValue('a'));
        Assert.assertEquals("1",
                this.characterConverter.canonicalToDisplayValue('1'));
    }

    @Test
    public void testNullDataToDisplay() {
        Assert.assertEquals("",
                this.characterConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        Assert.assertEquals('a',
                this.characterConverter.displayToCanonicalValue("a"));
        Assert.assertEquals('1',
                this.characterConverter.displayToCanonicalValue("1"));
    }

    @Test
    public void testNullDisplayToData() {
        Assert.assertEquals(null,
                this.characterConverter.displayToCanonicalValue(""));
    }

    @Test(expected = ConversionFailedException.class)
    public void testConversionException() {
        this.characterConverter.displayToCanonicalValue("abc");
    }

}
