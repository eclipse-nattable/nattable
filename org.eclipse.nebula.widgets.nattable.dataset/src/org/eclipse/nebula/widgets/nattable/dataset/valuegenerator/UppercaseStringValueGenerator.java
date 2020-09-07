/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.dataset.valuegenerator;

import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.generator.IValueGenerator;

public class UppercaseStringValueGenerator implements IValueGenerator {

    private char[] uppercase = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z' };

    private int maxLength;

    public UppercaseStringValueGenerator() {
        this(10);
    }

    public UppercaseStringValueGenerator(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public Object newValue(Random random) {
        StringBuilder strBuilder = new StringBuilder();

        int wordLength = random.nextInt(this.maxLength);
        for (int i = 0; i < wordLength; i++) {
            strBuilder.append(getChar(this.uppercase, random));
        }

        return strBuilder.toString();
    }

    private char getChar(char[] chars, Random random) {
        return chars[random.nextInt(chars.length)];
    }

}
