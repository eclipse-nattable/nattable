/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
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
