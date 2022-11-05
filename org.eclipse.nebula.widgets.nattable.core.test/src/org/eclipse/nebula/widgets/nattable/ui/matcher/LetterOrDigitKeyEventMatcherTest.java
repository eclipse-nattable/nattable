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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LetterOrDigitKeyEventMatcherTest {

    @Test
    public void testLetterOrDigitMatch() {
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('0'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('1'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('2'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('3'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('4'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('5'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('6'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('7'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('8'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('9'));

        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('a'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('b'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('c'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('d'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('e'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('f'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('g'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('h'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('i'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('j'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('k'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('l'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('m'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('n'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('o'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('p'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('q'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('r'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('s'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('t'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('u'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('v'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('w'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('x'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('y'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('z'));
    }

    @Test
    public void testWhiteSpacesNotMatch() {
        assertFalse(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\t'));
        assertFalse(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\n'));
        assertFalse(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\r'));
    }

    @Test
    public void testSpecialCharactersMatch() {
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('.'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(':'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(','));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(';'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('-'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('_'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('#'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\''));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('+'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('*'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('~'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('!'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('?'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\u00A7'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('$'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('%'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('&'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('/'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('('));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(')'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('['));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(']'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('{'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('}'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('='));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\\'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('"'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('´'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('`'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('@'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('^'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('°'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('|'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('<'));
        assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('>'));
    }
}
