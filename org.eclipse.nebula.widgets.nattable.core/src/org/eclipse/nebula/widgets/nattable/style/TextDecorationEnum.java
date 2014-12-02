/*******************************************************************************
 * Copyright (c) Oct 15, 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

/**
 * Enumeration for text decorations that can be configured for cell styles.
 * <ul>
 * <li>NONE (default) - Render no decoration</li>
 * <li>UNDERLINE - Render the text underlined</li>
 * <li>STRIKETHROUGH - Render the text strike through</li>
 * <li>UNDERLINE_STRIKETHROUGH - Render the text underlined and strike through</li>
 * </ul>
 *
 * @author Dirk Fauth
 */
public enum TextDecorationEnum {

    NONE, UNDERLINE, STRIKETHROUGH, UNDERLINE_STRIKETHROUGH;

}
