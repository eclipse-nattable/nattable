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
package org.eclipse.nebula.widgets.nattable.style;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.nebula.widgets.nattable.persistence.ColorPersistor;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * This class defines the visual attributes of a Border.
 */
public class BorderStyle {
  
    private int thickness = 1;
    private Color color = GUIHelper.COLOR_BLACK;
    private LineStyleEnum lineStyle = LineStyleEnum.SOLID;
    
    public enum LineStyleEnum {
        SOLID, DASHED, DOTTED, DASHDOT, DASHDOTDOT;
        
        public static int toSWT(LineStyleEnum line) {
            if (line == null) throw new IllegalArgumentException("null"); //$NON-NLS-1$
            if (line.equals(SOLID)) return SWT.LINE_SOLID;
            else if (line.equals(DASHED)) return SWT.LINE_DASH;
            else if (line.equals(DOTTED)) return SWT.LINE_DOT;
            else if (line.equals(DASHDOT)) return SWT.LINE_DASHDOT;
            else if (line.equals(DASHDOTDOT)) return SWT.LINE_DASHDOTDOT;
            else return SWT.LINE_SOLID;
        }
    }
    
    public BorderStyle() {}
    
    public BorderStyle(int thickness, Color color, LineStyleEnum lineStyle) {
        this.thickness = thickness;
        this.color = color;
        this.lineStyle = lineStyle;
    }
    
    /**
     * Reconstruct this instance from the persisted String.
     * @see BorderStyle#toString()
     */
    public BorderStyle(String string) {
    	String[] tokens = string.split("\\|"); //$NON-NLS-1$
    	
    	this.thickness = Integer.parseInt(tokens[0]);
    	this.color = ColorPersistor.asColor(tokens[1]);
    	this.lineStyle = LineStyleEnum.valueOf(tokens[2]);
    }

    public int getThickness() {
        return thickness;
    }

    public Color getColor() {
        return color;
    }

    public LineStyleEnum getLineStyle() {
        return lineStyle;
    }
    
    public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setLineStyle(LineStyleEnum lineStyle) {
		this.lineStyle = lineStyle;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BorderStyle == false) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		BorderStyle that = (BorderStyle) obj;
		
		return new EqualsBuilder()
			.append(this.thickness, that.thickness)
			.append(this.color, that.color)
			.append(this.lineStyle.name(), that.lineStyle.name())
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(87, 19)
			.append(thickness)
			.append(color)
			.append(lineStyle.name())
			.toHashCode();
	}

	/**
	 * @return a human readable representation of the border style.
	 *    This is suitable for constructing an equivalent instance using the BorderStyle(String) constructor  
	 */
    @Override
    public String toString() {
        return thickness + "|" +  //$NON-NLS-1$
        	ColorPersistor.asString(color) + "|" + //$NON-NLS-1$
        	lineStyle;
    }
}
