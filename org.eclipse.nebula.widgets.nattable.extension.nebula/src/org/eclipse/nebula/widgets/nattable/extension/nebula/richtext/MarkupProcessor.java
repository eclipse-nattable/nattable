/*****************************************************************************
 * Copyright (c) 2016 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

/**
 * Interface for specifying markup processing by using the
 * {@link MarkupDisplayConverter}
 *
 * @see MarkupDisplayConverter
 *
 * @since 1.1
 */
public interface MarkupProcessor {

    /**
     * Takes an input and applies the HTML markup.
     * 
     * @param input
     *            The input that should be processed for markup.
     * @return The input with additional markups.
     */
    String applyMarkup(String input);

    /**
     * Takes an input and removes possible markups that where applied by this
     * {@link MarkupProcessor}
     * 
     * @param input
     *            The input from which the markup should be removed.
     * @return The input value without markups.
     */
    String removeMarkup(String input);

}
