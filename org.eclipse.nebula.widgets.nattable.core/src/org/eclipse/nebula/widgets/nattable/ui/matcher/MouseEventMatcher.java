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
package org.eclipse.nebula.widgets.nattable.ui.matcher;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.swt.events.MouseEvent;

public class MouseEventMatcher implements IMouseEventMatcher {

	public static final int LEFT_BUTTON = 1;
	public static final int RIGHT_BUTTON = 3;

	private final int stateMask;
	private final String regionName;
	private final int button;

	public MouseEventMatcher() {
		this(0, null, 0);
	}

	public MouseEventMatcher(String eventRegionName) {
		this(0, eventRegionName, 0);
	}

	public MouseEventMatcher(String eventRegion, int button) {
		this(0, eventRegion, button);
	}

	public MouseEventMatcher(int stateMask, String eventRegion) {
		this(stateMask, eventRegion, 0);
	}
	
	/**
	 * Constructor
	 * @param stateMask @see "org.eclipse.swt.events.MouseEvent.stateMask"
	 * @param eventRegion {@linkplain org.eclipse.nebula.widgets.nattable.grid.GridRegion}
	 * @param button @see org.eclipse.swt.events.MouseEvent#button
	 *  	{@link MouseEventMatcher#LEFT_BUTTON}, {@link MouseEventMatcher#RIGHT_BUTTON}
	 *  	can be used for convenience
	 */
	public MouseEventMatcher(int stateMask, String eventRegion, int button) {
		this.stateMask = stateMask;
		this.regionName = eventRegion;
		this.button = button;
	}

	public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
		if (regionLabels == null) {
			return false;
		}

		boolean stateMaskMatches;
		if (stateMask != 0) {
			stateMaskMatches = (event.stateMask == stateMask) ? true : false;
		} else {
			stateMaskMatches = event.stateMask == 0;
		}

		boolean eventRegionMatches;
		if (this.regionName != null) {
			eventRegionMatches = regionLabels.hasLabel(regionName);
		} else {
			eventRegionMatches = true;
		}

		boolean buttonMatches = button != 0 ? (button == event.button) : true;

		return stateMaskMatches && eventRegionMatches && buttonMatches;
	}

	@Override
    public boolean equals(Object obj) {
		if (obj instanceof MouseEventMatcher == false) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		MouseEventMatcher rhs = (MouseEventMatcher) obj;

		return new EqualsBuilder()
			.append(stateMask, rhs.stateMask)
			.append(regionName, rhs.regionName)
			.append(button, rhs.button)
			.isEquals();
	}

	@Override
    public int hashCode() {
		return new HashCodeBuilder(43, 21)
			.append(stateMask)
			.append(regionName)
			.append(button)
			.toHashCode();
	}

	public int getStateMask() {
		return stateMask;
	}

	public String getEventRegion() {
		return regionName;
	}

	public int getButton() {
		return button;
	}


	public static MouseEventMatcher columnHeaderLeftClick(int mask) {
	    return new MouseEventMatcher(mask, GridRegion.COLUMN_HEADER, LEFT_BUTTON);
	}

	public static MouseEventMatcher columnHeaderRightClick(int mask) {
		return new MouseEventMatcher(mask, GridRegion.COLUMN_HEADER, RIGHT_BUTTON);
	}

	public static MouseEventMatcher rowHeaderLeftClick(int mask) {
		return new MouseEventMatcher(mask, GridRegion.ROW_HEADER, LEFT_BUTTON);
	}
	
	public static MouseEventMatcher rowHeaderRightClick(int mask) {
		return new MouseEventMatcher(mask, GridRegion.ROW_HEADER, RIGHT_BUTTON);
	}

	public static MouseEventMatcher bodyLeftClick(int mask) {
		return new MouseEventMatcher(mask, GridRegion.BODY, LEFT_BUTTON);
	}
	
	public static MouseEventMatcher bodyRightClick(int mask) {
		return new MouseEventMatcher(mask, GridRegion.BODY, RIGHT_BUTTON);
	}

	public static MouseEventMatcher columnGroupHeaderLeftClick(int mask) {
	    return new MouseEventMatcher(mask, GridRegion.COLUMN_GROUP_HEADER, LEFT_BUTTON);
	}
	
	public static MouseEventMatcher columnGroupHeaderRightClick(int mask) {
	    return new MouseEventMatcher(mask, GridRegion.COLUMN_GROUP_HEADER, RIGHT_BUTTON);
	}
	
	public static MouseEventMatcher rowGroupHeaderLeftClick(int mask) {
	    return new MouseEventMatcher(mask, GridRegion.ROW_GROUP_HEADER, LEFT_BUTTON);
	}

	public static MouseEventMatcher rowGroupHeaderRightClick(int mask) {
	    return new MouseEventMatcher(mask, GridRegion.ROW_GROUP_HEADER, RIGHT_BUTTON);
	}
}

