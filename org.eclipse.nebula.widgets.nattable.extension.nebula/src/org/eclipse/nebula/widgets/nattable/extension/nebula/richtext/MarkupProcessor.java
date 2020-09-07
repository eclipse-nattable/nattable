/*****************************************************************************
 * Copyright (c) 2016, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
