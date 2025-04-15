/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable;

import java.lang.reflect.InvocationTargetException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator that will call the RAPInitializer if available. Needed in an
 * activator to ensure that the RAPInitializer is called before any NatTable
 * class is loaded.
 *
 * @since 2.6
 */
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) {
        try {
            Class<?> forName = getClass().getClassLoader().loadClass("org.eclipse.nebula.widgets.nattable.RAPInitializer"); //$NON-NLS-1$
            if (forName != null) {
                forName.getMethod("initialize").invoke(null); //$NON-NLS-1$
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // do nothing, if not running in RAP there is no initializer
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
