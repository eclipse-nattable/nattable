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
package org.eclipse.nebula.widgets.nattable.dataset.valuegenerator;

import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.generator.IValueGenerator;


public class UppercaseStringValueGenerator implements IValueGenerator {
	
	private char[] uppercase = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	private int maxLength;
	
	public UppercaseStringValueGenerator() {
		this(10);
	}
	
	public UppercaseStringValueGenerator(int maxLength) {
		this.maxLength = maxLength;
	}
	
	public Object newValue(Random random) {
		StringBuilder strBuilder = new StringBuilder();
		
		int wordLength = random.nextInt(maxLength);
		for (int i = 0; i < wordLength; i++) {
			strBuilder.append(getChar(uppercase, random));
		}
		
		return strBuilder.toString();
	}
	
	private char getChar(char[] chars, Random random) {
		return chars[random.nextInt(chars.length)];
	}
	
}
