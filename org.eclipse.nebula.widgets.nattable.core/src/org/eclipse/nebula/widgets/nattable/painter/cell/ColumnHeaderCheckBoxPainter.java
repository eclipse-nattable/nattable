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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public class ColumnHeaderCheckBoxPainter extends ImagePainter {
	
	private static final Log log = LogFactory.getLog(ColumnHeaderCheckBoxPainter.class);

	private final Image checkedImg;
	private final Image semicheckedImg;
	private final Image uncheckedImg;
	
	private final IUniqueIndexLayer columnDataLayer;

	public ColumnHeaderCheckBoxPainter(IUniqueIndexLayer columnDataLayer) {
		this(
				columnDataLayer,
				GUIHelper.getImage("checked"), //$NON-NLS-1$
				GUIHelper.getImage("semichecked"), //$NON-NLS-1$
				GUIHelper.getImage("unchecked") //$NON-NLS-1$
		);
	}

	public ColumnHeaderCheckBoxPainter(IUniqueIndexLayer columnLayer, Image checkedImg, Image semicheckedImage, Image uncheckedImg) {
		this.columnDataLayer = columnLayer;
		this.checkedImg = checkedImg;
		this.semicheckedImg = semicheckedImage;
		this.uncheckedImg = uncheckedImg;
	}

	public int getPreferredWidth(boolean checked) {
		return checked ? checkedImg.getBounds().width : uncheckedImg.getBounds().width;
	}

	public int getPreferredHeight(boolean checked) {
		return checked ? checkedImg.getBounds().height : uncheckedImg.getBounds().height;
	}

	public void paintIconImage(GC gc, Rectangle rectangle, int yOffset, boolean checked) {
		Image checkBoxImage = checked ? checkedImg : uncheckedImg;

		// Center image
		int x = rectangle.x + (rectangle.width / 2) - (checkBoxImage.getBounds().width/2);

		gc.drawImage(checkBoxImage, x, rectangle.y + yOffset);
	}

	@Override
	protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		int columnPosition = LayerUtil.convertColumnPosition(cell.getLayer(), cell.getColumnPosition(), columnDataLayer);
		
		int checkedCellsCount = getCheckedCellsCount(columnPosition, configRegistry);
		
		if (checkedCellsCount > 0) {
			if (checkedCellsCount == columnDataLayer.getRowCount()) {
				return checkedImg;
			} else {
				return semicheckedImg;
			}
		} else {
			return uncheckedImg;
		}
	}

	public int getCheckedCellsCount(int columnPosition, IConfigRegistry configRegistry) {
		int checkedCellsCount = 0;
		
		for (int rowPosition = 0; rowPosition < columnDataLayer.getRowCount(); rowPosition++) {
			ILayerCell columnCell = columnDataLayer.getCellByPosition(columnPosition, rowPosition);
			if (isChecked(columnCell, configRegistry)) {
				checkedCellsCount++;
			}
		}
		return checkedCellsCount;
	}

	protected boolean isChecked(ILayerCell cell, IConfigRegistry configRegistry) {
		return convertDataType(cell, configRegistry).booleanValue();
	}

	protected Boolean convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
		if (cell.getDataValue() instanceof Boolean) {
			return (Boolean) cell.getDataValue();
		}
		IDisplayConverter displayConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		Boolean convertedValue = null;
		if (displayConverter != null) {
			try {
				convertedValue = (Boolean) displayConverter.canonicalToDisplayValue(cell, configRegistry, cell.getDataValue());
			} catch (Exception e) {
				log.debug(e);
			}
		}
		if (convertedValue == null) {
			convertedValue = Boolean.FALSE;
		}
		return convertedValue;
	}
	
}
