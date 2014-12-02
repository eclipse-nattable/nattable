/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.config;

import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;

/**
 * Added by the {@link ColumnReorderLayer}
 */
public class DefaultColumnReorderLayerConfiguration extends
        AggregateConfiguration {

    public DefaultColumnReorderLayerConfiguration() {
        addColumnReorderUIBindings();
    }

    protected void addColumnReorderUIBindings() {
        addConfiguration(new DefaultColumnReorderBindings());
    }

}
