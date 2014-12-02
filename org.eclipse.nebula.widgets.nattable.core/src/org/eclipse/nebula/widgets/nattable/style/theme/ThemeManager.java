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
 * The ThemeManager is used to register/unregister style configurations set by a
 * {@link ThemeConfiguration} at runtime.
 *
 * @author Dirk Fauth
 *
 */
public class ThemeManager {

    /**
     * The IConfigRegistry this ThemeManager is connected to.
     */
    private final IConfigRegistry configRegistry;

    /**
     * The current applied ThemeConfiguration.
     */
    private ThemeConfiguration currentTheme;

    /**
     * Creates a ThemeManager that is connected to the given IConfigRegistry.
     *
     * @param configRegistry
     *            The IConfigRegistry the ThemeManager should be connected to.
     * @throws IllegalArgumentException
     *             if the given IConfigRegistry is <code>null</code>.
     */
    public ThemeManager(IConfigRegistry configRegistry) {
        if (configRegistry == null) {
            throw new IllegalArgumentException("IConfigRegistry can not be null!"); //$NON-NLS-1$
        }
        this.configRegistry = configRegistry;
    }

    /**
     * Apply the given ThemeConfiguration to the IConfigRegistry this
     * ThemeManager is registered.
     *
     * @param configuration
     *            The ThemeConfiguration that should be applied to the
     *            IConfigRegistry this ThemeManager is registered to.
     */
    public void applyTheme(ThemeConfiguration configuration) {
        // remove current applied style configurations
        cleanThemeConfiguration();
        // register new style configurations
        configuration.configureRegistry(this.configRegistry);
        // remember the applied configuration to be able to unregister on
        // changes
        this.currentTheme = configuration;
    }

    /**
     * This method is used to unregister the style configurations that were
     * applied by the current set ThemeConfiguration. This is necessary to
     * ensure to operate on a clean style state, so there are no style
     * configurations still applied from a previous ThemeConfiguration.
     */
    protected void cleanThemeConfiguration() {
        // remove current applied style configurations
        if (this.currentTheme != null) {
            this.currentTheme
                    .unregisterThemeStyleConfigurations(this.configRegistry);
        }
    }
}
