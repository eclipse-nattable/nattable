/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.e4.css;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public class NatTableElementAdapter extends WidgetElement {

    /**
     * Static flag to ensure that the update listener is only applied once to
     * the display.
     */
    private static boolean listenerApplied = false;

    /**
     * Collection of virtual children that can be used as child selectors in CSS
     * that will be mapped to NatTable labels.
     */
    List<Node> virtualChildren = new ArrayList<>();

    public NatTableElementAdapter(final NatTable natTable, final CSSEngine engine) {
        super(natTable, engine);

        addStaticPseudoInstance("normal");
        addStaticPseudoInstance("select");
        addStaticPseudoInstance("edit");
        addStaticPseudoInstance("hover");
        addStaticPseudoInstance("select-hover");

        // add virtual children for all labels that can be applied in the given
        // NatTable instance
        for (String label : natTable.getProvidedLabels()) {
            addVirtualChild(label);
        }

        if (!listenerApplied) {
            // The listener added via CSSSWTApplyStylesListener does not apply
            // styles for the children. But as NatTable styling is done via
            // virtual children for the labels, it is important to apply the
            // styles also to the children on skinning.
            natTable.getDisplay().addListener(SWT.Skin, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    if (engine != null && event.widget instanceof NatTable) {
                        engine.applyStyles(event.widget, true);
                    }
                }
            });

            // the special NatTable related update listener is registered, so we
            // won't add additional listeners for additional NatTable instances
            listenerApplied = true;
        }
    }

    @Override
    public Node getParentNode() {
        Control control = getControl();
        Composite parent = control.getParent();
        if (parent != null) {
            Element element = getElement(parent);
            return element;
        }
        return null;
    }

    protected NatTable getControl() {
        return (NatTable) getNativeWidget();
    }

    @Override
    public NodeList getChildNodes() {
        // only need to return a non-null value
        // strange implementation
        return this;
    }

    @Override
    public int getLength() {
        return this.virtualChildren.size();
    }

    @Override
    public Node item(int index) {
        return this.virtualChildren.get(index);
    }

    /**
     * Add a virtual child to the {@link NatTableElementAdapter}. This way the
     * given label can be used as child selector in the CSS file.
     *
     * @param label
     *            The label that should be usable as child selector.
     */
    public void addVirtualChild(String label) {
        this.virtualChildren.add(
                new NatTableWrapperElementAdapter(
                        new NatTableWrapper(getControl(), label), this.engine, this));
    }
}
