/*******************************************************************************
 * Copyright (c) 2015, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowRegularExpressionConverter;
import org.junit.Test;

public class FilterRowRegularExpressionConverterTest {

    @Test
    public void shouldConvertAsteriskFromDisplayToCanonical() {
        FilterRowRegularExpressionConverter converter = new FilterRowRegularExpressionConverter();
        assertEquals("(.*)", converter.displayToCanonicalValue("*"));
        assertEquals("(.*)abc", converter.displayToCanonicalValue("*abc"));
        assertEquals("abc(.*)", converter.displayToCanonicalValue("abc*"));
        assertEquals("ab(.*)g", converter.displayToCanonicalValue("ab*g"));
        assertEquals("(.*)abc(.*)", converter.displayToCanonicalValue("*abc*"));
        assertEquals("(.*)ab(.*)cd(.*)", converter.displayToCanonicalValue("*ab*cd*"));
    }

    @Test
    public void shouldConvertQuestionMarkFromDisplayToCanonical() {
        FilterRowRegularExpressionConverter converter = new FilterRowRegularExpressionConverter();
        assertEquals("(.?)", converter.displayToCanonicalValue("?"));
        assertEquals("(.?)abc", converter.displayToCanonicalValue("?abc"));
        assertEquals("abc(.?)", converter.displayToCanonicalValue("abc?"));
        assertEquals("ab(.?)g", converter.displayToCanonicalValue("ab?g"));
        assertEquals("(.?)abc(.?)", converter.displayToCanonicalValue("?abc?"));
        assertEquals("(.?)ab(.?)cd(.?)", converter.displayToCanonicalValue("?ab?cd?"));
    }

    @Test
    public void shouldConvertMixedFromDisplayToCanonical() {
        FilterRowRegularExpressionConverter converter = new FilterRowRegularExpressionConverter();
        assertEquals("(.*)abc(.?)", converter.displayToCanonicalValue("*abc?"));
        assertEquals("(.?)abc(.*)", converter.displayToCanonicalValue("?abc*"));
    }

    @Test
    public void shouldConvertAsteriskFromCanonicalToDisplay() {
        FilterRowRegularExpressionConverter converter = new FilterRowRegularExpressionConverter();
        assertEquals("*", converter.canonicalToDisplayValue("(.*)"));
        assertEquals("*abc", converter.canonicalToDisplayValue("(.*)abc"));
        assertEquals("abc*", converter.canonicalToDisplayValue("abc(.*)"));
        assertEquals("ab*g", converter.canonicalToDisplayValue("ab(.*)g"));
        assertEquals("*abc*", converter.canonicalToDisplayValue("(.*)abc(.*)"));
        assertEquals("*ab*cd*", converter.canonicalToDisplayValue("(.*)ab(.*)cd(.*)"));
    }

    @Test
    public void shouldConvertQuestionMarkFromCanonicalToDisplay() {
        FilterRowRegularExpressionConverter converter = new FilterRowRegularExpressionConverter();
        assertEquals("?", converter.canonicalToDisplayValue("(.?)"));
        assertEquals("?abc", converter.canonicalToDisplayValue("(.?)abc"));
        assertEquals("abc?", converter.canonicalToDisplayValue("abc(.?)"));
        assertEquals("ab?g", converter.canonicalToDisplayValue("ab(.?)g"));
        assertEquals("?abc?", converter.canonicalToDisplayValue("(.?)abc(.?)"));
        assertEquals("?ab?cd?", converter.canonicalToDisplayValue("(.?)ab(.?)cd(.?)"));
    }

    @Test
    public void shouldConvertMixedFromCanonicalToDisplay() {
        FilterRowRegularExpressionConverter converter = new FilterRowRegularExpressionConverter();
        assertEquals("*abc?", converter.canonicalToDisplayValue("(.*)abc(.?)"));
        assertEquals("?abc*", converter.canonicalToDisplayValue("(.?)abc(.*)"));
    }
}
