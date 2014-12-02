/*******************************************************************************
 * Copyright (c) 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.persistence.gui.PersistenceDialog;

/**
 * Helper class for dealing with persistence of NatTable states.
 *
 * @author Dirk Fauth
 *
 */
public class PersistenceHelper {

    /**
     * Deletes the keys for a state that is identified by given prefix out of
     * the given properties.
     *
     * @param prefix
     *            The prefix for the keys the state consists of. Can be
     *            interpreted as state configuration name.
     * @param properties
     *            The properties containing the state configuration.
     */
    public static void deleteState(String prefix, Properties properties) {
        if (properties != null) {
            // build the key prefix to search for
            // always add the dot as states without a prefix are stored with a
            // leading dot
            // and for named states it might be possible that there are some
            // starting with
            // the same prefix, so the dot clarifies the prefix
            String keyPrefix = prefix + IPersistable.DOT;

            // collect the keys to remove
            List<Object> keysToRemove = new ArrayList<Object>();
            for (Object key : properties.keySet()) {
                if (key.toString().startsWith(keyPrefix)) {
                    keysToRemove.add(key);
                }
            }

            // remove the keys
            for (Object toRemove : keysToRemove) {
                properties.remove(toRemove);
            }
        }
    }

    /**
     * As one Properties instance can contain several stored states of a
     * NatTable instance, this method can be used to retrieve the names of the
     * containing states. In terms of NatTable states, you may also call the
     * names prefixes.
     *
     * @param properties
     *            The Properties to retrieve the containing states of
     * @return Collection of all state prefixes that are contained in the given
     *         properties.
     */
    public static Collection<String> getAvailableStates(Properties properties) {
        Set<String> stateNames = new HashSet<String>();
        if (properties != null && !properties.isEmpty()) {
            for (Object key : properties.keySet()) {
                String keyString = key.toString();
                if (!PersistenceDialog.ACTIVE_VIEW_CONFIGURATION_KEY
                        .equals(keyString))
                    stateNames.add(keyString.split("\\.")[0]); //$NON-NLS-1$
            }
        }
        return stateNames;
    }
}
