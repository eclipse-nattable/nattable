/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
