/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Modification to implement Collection
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

/**
 * The <code>LabelStack</code> is a list of labels attached to a
 * <code>ILayerCell</code>. With the label stack mechanism it is possible to
 * configure special rendering or behavior, dependent on labels included in the
 * stack.
 *
 * <p>
 * A LabelStack can not contain duplicate labels. So you might want to think of
 * the LabelStack as an ordered java.util.Set
 *
 * <p>
 * To add labels to the LabelStack, usually a {@link IConfigLabelAccumulator} is
 * used.
 */
public class LabelStack implements Collection<String> {

    /**
     * Internal list instance to store the labels.
     */
    private final List<String> labels = new LinkedList<String>();

    /**
     * Create a new LabelStack which is initialized with the specified
     * collection of labels. Duplicate labels are not allowed and will be
     * filtered on initialization automatically.
     *
     * @param labels
     *            The labels which should be used to initialize the new
     *            LabelStack
     */
    public LabelStack(String... labels) {
        for (String label : labels) {
            if (label != null) {
                addLabel(label);
            }
        }
    }

    /**
     * Adds a label at the bottom of this LabelStack. The label is only added if
     * it is not present yet.
     *
     * @param label
     *            The label to add to this LabelStack.
     * @return <code>true</code> if the label was added to the LabelStack,
     *         <code>false</code> if the label was not added, e.g. because it is
     *         already contained in this LabelStack
     */
    public boolean addLabel(String label) {
        if (!hasLabel(label)) {
            return this.labels.add(label);
        }
        return false;
    }

    /**
     * Adds a label to the top of this LabelStack. If the specified label is
     * already included in this LabelStack, it is moved to the top.
     *
     * @param label
     *            The label to add to this LabelStack
     */
    public void addLabelOnTop(String label) {
        if (hasLabel(label)) {
            removeLabel(label);
        }
        this.labels.add(0, label);
    }

    /**
     *
     * @return The list of labels included in this LabelStack
     * @deprecated LabelStack is itself a Collection of Strings
     */
    @Deprecated
    public List<String> getLabels() {
        return this.labels;
    }

    /**
     * Checks if this LabelStack contains the specified label.
     *
     * @param label
     *            The label whose presence need to be tested.
     * @return <code>true</code> if this LabelStack contains the specified
     *         label.
     */
    public boolean hasLabel(String label) {
        return this.labels.contains(label);
    }

    /**
     * Removes the specified label from this LabelStack.
     *
     * @param label
     *            The label to remove.
     * @return <code>true</code> if this LabelStack contained the specified
     *         label.
     */
    public boolean removeLabel(String label) {
        return this.labels.remove(label);
    }

    @Override
    public String toString() {
        return this.labels.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof LabelStack))
            return false;

        return this.labels.equals(((LabelStack) obj).labels);
    }

    @Override
    public int hashCode() {
        return this.labels.hashCode();
    }

    @Override
    public int size() {
        return this.labels.size();
    }

    @Override
    public boolean isEmpty() {
        return this.labels.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.labels.contains(o);
    }

    @Override
    public Iterator<String> iterator() {
        return this.labels.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.labels.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.labels.toArray(a);
    }

    /**
     * Adds a label at the bottom of this LabelStack. The label is only added if
     * it is not present yet.
     *
     * @param e
     *            The label to add to this LabelStack.
     * @return <code>true</code> if the label was added to the LabelStack,
     *         <code>false</code> if the label was not added, e.g. because it is
     *         already contained in this LabelStack
     */
    @Override
    public boolean add(String e) {
        return this.addLabel(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.labels.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.labels.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        return this.labels.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.labels.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.labels.retainAll(c);
    }

    @Override
    public void clear() {
        this.labels.clear();
    }

}
