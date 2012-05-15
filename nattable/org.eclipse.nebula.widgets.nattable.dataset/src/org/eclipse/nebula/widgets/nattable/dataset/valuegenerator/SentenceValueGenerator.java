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


public class SentenceValueGenerator implements IValueGenerator {
	
	private char[] uppercase = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	
	private char[] lowercase = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	
	private char[] midPunctuation = new char[] { ' ', ',', '-' };
	
	private char[] endPunctuation = new char[] { '.', '!' };

	private int minLength;
	
	private int maxLength;
	
	public SentenceValueGenerator() {
		this(5, 20);
	}
	
	public SentenceValueGenerator(int minLength, int maxLength) {
		this.minLength = minLength;
		this.maxLength = maxLength;
	}
	
	public Object newValue(Random random) {
		if (random.nextBoolean()) {
			return "";
		}
		
		StringBuilder strBuilder = new StringBuilder();
		
		int remainingLength = minLength + random.nextInt(maxLength - minLength);
		
		boolean endsWithPunctuation = random.nextBoolean();
		
		if (endsWithPunctuation) {
			remainingLength--;
		}
		
		String word = getWord(remainingLength, random);
		strBuilder.append(word);
		remainingLength -= word.length();
		
		while (remainingLength > 1) {
			strBuilder.append(getChar(midPunctuation, random));
			remainingLength--;
			
			word = getWord(remainingLength, random);
			strBuilder.append(word);
			remainingLength -= word.length();
		}
		
		if (endsWithPunctuation) {
			strBuilder.append(getChar(endPunctuation, random));
		}
		
		return strBuilder.toString();
	}
	
	private String getWord(int maxWordLength, Random random) {
		StringBuilder wordBuilder = new StringBuilder();
		
		int wordLength = random.nextInt(maxWordLength);
		for (int i = 0; i < wordLength; i++) {
			wordBuilder.append(getChar(random.nextBoolean() ? uppercase : lowercase, random));
		}
		
		return wordBuilder.toString();
	}
	
	private char getChar(char[] chars, Random random) {
		return chars[random.nextInt(chars.length)];
	}
	
}
