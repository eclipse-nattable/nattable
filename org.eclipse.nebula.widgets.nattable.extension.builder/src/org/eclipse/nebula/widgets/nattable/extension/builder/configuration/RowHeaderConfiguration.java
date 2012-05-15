/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.builder.configuration;

import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableStyle;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;


public class RowHeaderConfiguration extends DefaultRowHeaderLayerConfiguration {

	private final TableStyle tableStyle;

	public RowHeaderConfiguration(TableStyle tableStyle) {
		super();
		this.tableStyle = tableStyle;
		addStyling();
	}

	private void addStyling() {
		DefaultRowHeaderStyleConfiguration styleConfig = new DefaultRowHeaderStyleConfiguration();
		styleConfig.bgColor = tableStyle.rowHeaderBGColor;
		styleConfig.fgColor = tableStyle.rowHeaderFGColor;
		styleConfig.font = tableStyle.rowHeaderFont;
		addConfiguration(styleConfig);
	}

	@Override
	protected void addRowHeaderStyleConfig() {}
}
