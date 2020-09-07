/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.e4.css;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.utils.ClassUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public class NatTableWrapperElementAdapter extends ElementAdapter {

    public final NatTableElementAdapter parent;
    public final NatTableWrapper natTableWrapper;

    public NatTableWrapperElementAdapter(NatTableWrapper natTableWrapper, CSSEngine engine, NatTableElementAdapter parent) {
        super(natTableWrapper, engine);

        this.natTableWrapper = natTableWrapper;
        this.parent = parent;

        addStaticPseudoInstance("normal");
        addStaticPseudoInstance("select");
        addStaticPseudoInstance("edit");
        addStaticPseudoInstance("hover");
        addStaticPseudoInstance("select-hover");
    }

    @Override
    public Node getParentNode() {
        return this.parent;
    }

    @Override
    public NodeList getChildNodes() {
        return null;
    }

    @Override
    public String getCSSClass() {
        return this.natTableWrapper.getLabel();
    }

    @Override
    public String getNamespaceURI() {
        return ClassUtils.getPackageName(NatTableWrapper.class);
    }

    @Override
    public String getCSSId() {
        return this.natTableWrapper.getLabel();
    }

    @Override
    public String getCSSStyle() {
        return null;
    }

    @Override
    public String getLocalName() {
        return ClassUtils.getSimpleName(NatTableWrapper.class);
    }

    @Override
    public String getAttribute(String arg0) {
        return "";
    }

    /**
     * @since 2.0
     */
    @Override
    public void dispose() {
        super.dispose();
        if (this.natTableWrapper != null) {
            this.natTableWrapper.dispose();
        }
    }
}
