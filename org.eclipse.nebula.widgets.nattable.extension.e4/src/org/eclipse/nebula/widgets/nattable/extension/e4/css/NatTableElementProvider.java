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

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.w3c.dom.Element;

@SuppressWarnings("restriction")
public class NatTableElementProvider implements IElementProvider {

    @Override
    public Element getElement(Object element, CSSEngine engine) {
        if (element instanceof NatTable) {
            NatTable natTable = (NatTable) element;
            return new NatTableElementAdapter(natTable, engine);
        } else if (element instanceof NatTableWrapper) {
            NatTableWrapper natTableWrapper = (NatTableWrapper) element;
            return new NatTableWrapperElementAdapter(
                    natTableWrapper,
                    engine,
                    new NatTableElementAdapter(natTableWrapper.getNatTable(), engine));
        }
        return null;
    }

}
