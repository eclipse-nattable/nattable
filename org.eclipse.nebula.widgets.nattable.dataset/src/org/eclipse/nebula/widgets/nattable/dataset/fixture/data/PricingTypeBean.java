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
package org.eclipse.nebula.widgets.nattable.dataset.fixture.data;

/**
 * Bean representing the pricing type. Used as the canonical data source for the
 * combo box - used to test the canonical to display conversion
 */
public class PricingTypeBean implements Comparable<PricingTypeBean> {
    public String type;

    public PricingTypeBean(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    @Override
    public int compareTo(PricingTypeBean o) {
        return this.toString().compareTo(o.toString());
    }

}
