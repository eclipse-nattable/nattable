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
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.LinkedList;
import java.util.List;

public class LabelStack {

    /**
     * List implementation saves the overhead of popping labels off in the
     * {@link #getLabels()} method
     */
    private final List<String> labels = new LinkedList<String>();

    public LabelStack(String... labelNames) {
        for (String label : labelNames) {
            if (label != null) {
                this.labels.add(label);
            }
        }
    }

    /**
     * Adds a label to the bottom of the label stack.
     *
     * @param label
     */
    public void addLabel(String label) {
        if (!hasLabel(label)) {
            this.labels.add(label);
        }
    }

    /**
     * Adds a label to the top of the label stack.
     *
     * @param label
     */
    public void addLabelOnTop(String label) {
        if (!hasLabel(label)) {
            this.labels.add(0, label);
        }
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public boolean hasLabel(String label) {
        return this.labels.contains(label);
    }

    public boolean removeLabel(String label) {
        return this.labels.remove(label);
    }

    @Override
    public String toString() {
        return this.labels.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof LabelStack)) {
            return false;
        }

        LabelStack that = (LabelStack) obj;

        return this.labels.equals(that.labels);
    }

    @Override
    public int hashCode() {
        return this.labels.hashCode();
    }

}
