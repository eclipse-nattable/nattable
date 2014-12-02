/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.theme;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;

/**
 * Theme extensions can be used to extend a already existing theme with
 * additional style configurations, like conditional styles or styles that are
 * dependent to other plugins (e.g. groupBy in GlazedLists extension).
 *
 * @author Dirk Fauth
 *
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
    public void registerStyles(IConfigRegistry configRegistry);

    /**
     * Unregister the style configurations that were registered by this
     * IThemeExtension.
     *
     * @param configRegistry
     *            The IConfigRegistry that is used by the NatTable instance to
     *            which the style configurations were applied to.
     */
    public void unregisterStyles(IConfigRegistry configRegistry);

}
