/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.junit.Assert;
import org.junit.Test;

public class LetterOrDigitKeyEventMatcherTest {
	
	@Test
	public void testLetterOrDigitMatch() {
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('0'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('1'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('2'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('3'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('4'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('5'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('6'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('7'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('8'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('9'));
		
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('a'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('b'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('c'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('d'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('e'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('f'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('g'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('h'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('i'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('j'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('k'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('l'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('m'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('n'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('o'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('p'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('q'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('r'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('s'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('t'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('u'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('v'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('w'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('x'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('y'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('z'));
	}
	
	@Test
	public void testWhiteSpacesNotMatch() {
		Assert.assertFalse(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\t'));
		Assert.assertFalse(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\n'));
		Assert.assertFalse(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\r'));
	}
	
	@Test
	public void testSpecialCharactersMatch() {
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('.'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(':'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(','));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(';'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('-'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('_'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('#'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\''));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('+'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('*'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('~'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('!'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('?'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('§'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('$'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('%'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('&'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('/'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('('));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(')'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('['));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit(']'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('{'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('}'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('='));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('\\'));
		Assert.assertTrue(LetterOrDigitKeyEventMatcher.isLetterOrDigit('"'));
	}
}
