/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.theme;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;

/**
 * Theme extensions can be used to extend a already existing theme with
 * additional style configurations, like conditional styles or styles that are
 * dependent to other plugins (e.g. groupBy in GlazedLists extension).
 */
public interface IThemeExtension {

    /**
     * Register the style configurations that should be added by this
     * IThemeExtension.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configurations should be registered.
     */
    void registerStyles(IConfigRegistry configRegistry);

    /**
     * Unregister the style configurations that were registered by this
     * IThemeExtension.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configurations were applied to.
     */
    void unregisterStyles(IConfigRegistry configRegistry);

    /**
     * Method that is used to create the painter instances that should be
     * registered for styling. Needed to update painters in case zoom operations
     *
     * @since 2.0
     */
    default void createPainterInstances() {
        // by default does nothing
        // needs to be overridden to create the painter instances that need to
        // be updated on zoom operations
    }
}
