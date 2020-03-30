/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.engine.AbstractCSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
    private static AtomicBoolean listenerApplied = new AtomicBoolean(false);

    /**
     * Collection of virtual children that can be used as child selectors in CSS
     * that will be mapped to NatTable labels.
     */
    ArrayList<Node> virtualChildren = new ArrayList<>();

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

        natTable.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });

        // the special NatTable related update listener should only be
        // registered once, there is no need to add additional listeners for
        // additional NatTable instances
        if (listenerApplied.compareAndSet(false, true)) {
            new NatTableSkinListener(natTable.getDisplay(), engine);
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

    /**
     * @since 2.0
     */
    @Override
    public void dispose() {
        super.dispose();
        for (Node node : this.virtualChildren) {
            if (node instanceof NatTableWrapperElementAdapter) {
                ((NatTableWrapperElementAdapter) node).dispose();

                if (this.engine != null && this.engine instanceof AbstractCSSEngine) {
                    try {
                        Method method = AbstractCSSEngine.class.getDeclaredMethod("handleWidgetDisposed", Object.class);
                        if (method != null) {
                            method.setAccessible(true);
                            method.invoke(this.engine, ((NatTableWrapperElementAdapter) node).natTableWrapper);
                        }
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Add a listener for {@link SWT#Skin} to apply styles to a NatTable.
     * <p>
     * The listener added via CSSSWTApplyStylesListener does not apply styles
     * for the children. But as NatTable styling is done via virtual children
     * for the labels, it is important to apply the styles also to the children
     * on skinning.
     * </p>
     * <p>
     * Extracted to a separate class instead of an anonymous inner class, to
     * avoid memory leakage.
     * </p>
     */
    private static class NatTableSkinListener {

        CSSEngine engine;

        public NatTableSkinListener(Display display, final CSSEngine cssEngine) {
            this.engine = cssEngine;

            display.addListener(SWT.Skin, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    if (NatTableSkinListener.this.engine != null && event.widget instanceof NatTable) {
                        NatTableSkinListener.this.engine.applyStyles(event.widget, true);
                    }
                }
            });
        }

    }
}
