/*******************************************************************************
 * Copyright (c) 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.fixtures;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.PricingTypeBean;

public class PricingTypeBeanDisplayConverter extends DisplayConverter {

    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        if (canonicalValue == null) {
            return null;
        } else {
            return canonicalValue.toString().equals("MN") ? "Manual" : "Automatic";
        }
    }

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        return displayValue.toString().equals("Manual") ? new PricingTypeBean("MN") : new PricingTypeBean("AT");
    }

}
