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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;


public abstract class AbstractOverrider implements IConfigLabelAccumulator {
	
	private Map<Serializable, List<String>> overrides = new HashMap<Serializable, List<String>>();

	public void removeOverride(Serializable key) {
		overrides.remove(key);
	}

	public void registerOverrides(Serializable key, String...configLabels) {
		List<String> existingOverrides = getOverrides(key);
		if(existingOverrides == null){
			registerOverrides(key, ArrayUtil.asList(configLabels));
		} else {
			for (String configLabel : configLabels) {
				if (!existingOverrides.contains(configLabel)) {
					existingOverrides.add(configLabel);
				}
			}
		}
	}
	
	public void registerOverridesOnTop(Serializable key, String...configLabels) {
		List<String> existingOverrides = getOverrides(key);
		if(existingOverrides == null){
			registerOverrides(key, ArrayUtil.asList(configLabels));
		} else {
			for (String configLabel : configLabels) {
				if (!existingOverrides.contains(configLabel)) {
					existingOverrides.add(0, configLabel);
				}
			}
		}
	}
	
	public void registerOverrides(Serializable key, List<String> configLabels) {
		overrides.put(key, configLabels);
	}

	public Map<Serializable, List<String>> getOverrides() {
		return overrides;
	}
	
	public List<String> getOverrides(Serializable key) {
		return overrides.get(key);
	}

	public void addOverrides(Map<Serializable, List<String>> overrides) {
		this.overrides.putAll(overrides);
	}
	
}
