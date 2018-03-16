/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.hierarchical;

import java.util.Arrays;

/**
 * Wrapper class to support an object model of hierarchical classes in NatTable.
 * Via {@link HierarchicalHelper} a collection of nested classes can be
 * de-normalized into a simple collection that can then be used inside a
 * NatTable e.g. via HierarchicalTreeLayer.
 *
 * @see HierarchicalHelper
 *
 * @since 1.6
 */
public class HierarchicalWrapper {

    private final Object[] levelObjects;

    /**
     * Creates a new {@link HierarchicalWrapper} with the given number of
     * levels. The level objects need to be set afterwards via
     * {@link #setObject(int, Object)}.
     *
     * @param level
     *            The number of levels this {@link HierarchicalWrapper}
     *            supports.
     */
    public HierarchicalWrapper(int level) {
        this.levelObjects = new Object[level];
    }

    /**
     * Creates a new {@link HierarchicalWrapper} with the given model object
     * array.
     *
     * @param levelObjects
     *            The objects that should be wrapped, where each item in the
     *            array specifies a level object.
     */
    private HierarchicalWrapper(Object[] levelObjects) {
        this.levelObjects = levelObjects;
    }

    /**
     * Get the model object for the given level out of this wrapper.
     *
     * @param level
     *            The level for which the model object is requested.
     * @return The object for the given level.
     * @throws IllegalArgumentException
     *             if an object is requested for a deeper level than this
     *             wrapper supports.
     */
    public Object getObject(int level) {
        if (level >= this.levelObjects.length) {
            throw new IllegalArgumentException("Requested a deeper level than available"); //$NON-NLS-1$
        }
        return this.levelObjects[level];
    }

    /**
     * Set the given model object for the given level.
     *
     * @param level
     *            The level on which the object should be set.
     * @param object
     *            The object to set to the given level in this wrapper.
     * @throws IllegalArgumentException
     *             if it is tried to set an object to a level that is not
     *             supported by this wrapper.
     */
    public void setObject(int level, Object object) {
        if (level >= this.levelObjects.length) {
            throw new IllegalArgumentException("Level " + level + " is not supported by this instance"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.levelObjects[level] = object;
    }

    /**
     *
     * @return The number of levels provided by this wrapper.
     */
    public int getLevels() {
        return this.levelObjects.length;
    }

    @Override
    public HierarchicalWrapper clone() {
        return new HierarchicalWrapper(Arrays.copyOf(this.levelObjects, this.levelObjects.length));
    }
}
