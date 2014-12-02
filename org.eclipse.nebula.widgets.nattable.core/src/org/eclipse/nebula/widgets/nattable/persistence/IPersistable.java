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
package org.eclipse.nebula.widgets.nattable.persistence;

import java.util.Properties;

/**
 * Instances implementing this interface can save and load their state from a
 * properties file. The state is therefore a collection of key value pairs.
 */
public interface IPersistable {

    /**
     * Separator used for properties. Example:
     * .BODY.columnWidth.resizableByDefault
     */
    public static final String DOT = "."; //$NON-NLS-1$

    /**
     * Separator used for values. Example: 0,1,2,3,4
     */
    public static final String VALUE_SEPARATOR = ","; //$NON-NLS-1$

    /**
     * Saves the state to the given Properties using the specified prefix. Note:
     * The prefix must be prepended to the property key to support multiple
     * states within one Properties instance.
     *
     * @param prefix
     *            The prefix to use for the state keys. Is also used as the
     *            state configuration name.
     * @param properties
     *            The Properties instance to save the state to.
     */
    public void saveState(String prefix, Properties properties);

    /**
     * Restore the state out of the given Properties identified by the specified
     * prefix. Note: The prefix must be prepended to the property key to support
     * multiple states within one Properties instance.
     *
     * @param prefix
     *            The prefix to use for the state keys. Is also used as the
     *            state configuration name.
     * @param properties
     *            The Properties instance to load the state from.
     */
    public void loadState(String prefix, Properties properties);

}
