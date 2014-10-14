/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.config;

import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;

/**
 * Added by the
 * {@link org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer}
 */
public class DefaultRowReorderLayerConfiguration extends AggregateConfiguration {

    public DefaultRowReorderLayerConfiguration() {
        addRowReorderUIBindings();
    }

    protected void addRowReorderUIBindings() {
        addConfiguration(new DefaultRowReorderBindings());
    }

}
