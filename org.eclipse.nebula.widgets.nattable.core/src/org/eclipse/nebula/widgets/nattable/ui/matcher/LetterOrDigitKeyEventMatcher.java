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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.eclipse.swt.events.KeyEvent;

public class LetterOrDigitKeyEventMatcher implements IKeyEventMatcher {
	
	public static boolean isLetterOrDigit(char character) {
		return Character.isLetterOrDigit(character) || character == '.';
	}

	private int stateMask;
	
	public LetterOrDigitKeyEventMatcher() {
		this(0);
	}
	
	public LetterOrDigitKeyEventMatcher(int stateMask) {
		this.stateMask = stateMask;
	}

	public boolean matches(KeyEvent event) {
		return event.stateMask == this.stateMask && isLetterOrDigit(event.character);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LetterOrDigitKeyEventMatcher == false) {
			return false;
		}
		
		return true;
	}
	
	@Override
    public int hashCode() {
		return 317;
	}

}
