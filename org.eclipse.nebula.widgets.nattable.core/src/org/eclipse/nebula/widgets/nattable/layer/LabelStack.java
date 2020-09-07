/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Collection of labels applied to a cell. Used for conditional styling and
 * behavior.
 */
public class LabelStack extends LinkedList<String> {

    private static final long serialVersionUID = -2942954228158176792L;

    /**
     * Creates a {@link LabelStack} initialized with the given labels.
     *
     * @param labels
     *            The labels that should be initially added to the created
     *            {@link LabelStack}.
     */
    public LabelStack(String... labels) {
        super(Arrays.asList(labels));
    }

    /**
     * Creates a {@link LabelStack} initialized with the given labels.
     *
     * @param labels
     *            The labels that should be initially added to the created
     *            {@link LabelStack}.
     * @since 2.0
     */
    public LabelStack(Collection<String> labels) {
        super(labels);
    }

    /**
     * Adds a label to the bottom of the label stack.
     *
     * @param label
     *            The label to add.
     * @return <code>true</code> if the label was added, <code>false</code> if
     *         adding failed.
     * @since 2.0
     */
    public boolean addLabel(String label) {
        if (!hasLabel(label)) {
            return add(label);
        }
        return false;
    }

    /**
     * Adds a label to the top of the label stack. If the label is already in
     * the label stack it is moved to the top.
     *
     * @param label
     *            The label to add.
     */
    public void addLabelOnTop(String label) {
        if (hasLabel(label)) {
            removeLabel(label);
        }
        addFirst(label);
    }

    /**
     *
     * @return The label stack collection.
     * @deprecated As {@link LabelStack} is itself a collection, the usage of
     *             this method is not needed anymore.
     */
    @Deprecated
    public List<String> getLabels() {
        return this;
    }

    /**
     * Check if the given label is on the label stack.
     *
     * @param label
     *            The label to test.
     * @return <code>true</code> if the label stack contains the given label,
     *         <code>false</code> if not.
     */
    public boolean hasLabel(String label) {
        return contains(label);
    }

    /**
     * Removes the given label from the label stack.
     *
     * @param label
     *            The label to remove.
     * @return <code>true</code> if the label was removed, <code>false</code> if
     *         not.
     */
    public boolean removeLabel(String label) {
        return remove(label);
    }

}
