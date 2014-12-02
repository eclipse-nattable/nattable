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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/**
 * {@link IKeyEventMatcher} implementation that will check if a pressed key is a
 * letter or digit key, in combination with the configured state mask for the
 * keyboard modifier.
 * <p>
 * Since 1.0.0 this matcher has evolved to match more than only letter or digit
 * keys. It will now also check for several special characters that are able to
 * be populated to an editor like e.g. the question mark. The following regular
 * expression will be used by this matcher:
 *
 * <b>[\\.:,;\\-_#\'+*~!?§$%&amp;/()\\[\\]\\{\\}=\\\\\"]</b>
 */
public class LetterOrDigitKeyEventMatcher implements IKeyEventMatcher {

    /**
     * The state of the keyboard modifier keys at the time the event was
     * generated, as defined by the key code constants in class <code>SWT</code>
     * .
     */
    private int stateMask;

    /**
     * Will create a new key event matcher that accepts no keyboard modifiers on
     * typing a key.
     */
    public LetterOrDigitKeyEventMatcher() {
        this(SWT.NONE);
    }

    /**
     * Will create a new key event matcher that accepts only the given keyboard
     * modifiers on typing a key.
     *
     * @param stateMask
     *            The state of the keyboard modifier keys at the time the event
     *            was generated, as defined by the key code constants in class
     *            <code>SWT</code>.
     */
    public LetterOrDigitKeyEventMatcher(int stateMask) {
        this.stateMask = stateMask;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.nebula.widgets.nattable.ui.matcher.IKeyEventMatcher#matches
     * (org.eclipse.swt.events.KeyEvent)
     */
    @Override
    public boolean matches(KeyEvent event) {
        return event.stateMask == this.stateMask
                && isLetterOrDigit(event.character);
    }

    /**
     * Will check if the given character is a letter or digit character, and
     * moreover will check special characters that can be typed that will cause
     * a character to be printed.
     * <p>
     * This method is intended to be used to determine whether a keypress is
     * able to open an editor, populating the representing character of the key
     * to the editor.
     *
     * @param character
     *            The character to check if it is a letter, digit or specified
     *            special character.
     * @return <code>true</code> if the character is an acceptable character,
     *         <code>false</code> if not.
     */
    public static boolean isLetterOrDigit(char character) {
        return Character.isLetterOrDigit(character)
                || Character
                        .valueOf(character)
                        .toString()
                        .matches("[\\.:,;\\-_#\'+*~!?§$%&/()\\[\\]\\{\\}=\\\\\"]"); //$NON-NLS-1$
    }

}
