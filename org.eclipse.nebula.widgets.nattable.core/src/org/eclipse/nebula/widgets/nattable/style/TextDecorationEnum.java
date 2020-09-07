/*******************************************************************************
 * Copyright (c) 2012, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

/**
 * Enumeration for text decorations that can be configured for cell styles.
 * <ul>
 * <li>NONE (default) - Render no decoration</li>
 * <li>UNDERLINE - Render the text underlined</li>
 * <li>STRIKETHROUGH - Render the text strike through</li>
 * <li>UNDERLINE_STRIKETHROUGH - Render the text underlined and strike
 * through</li>
 * </ul>
 */
public enum TextDecorationEnum {

    NONE, UNDERLINE, STRIKETHROUGH, UNDERLINE_STRIKETHROUGH;

}
