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
package org.eclipse.nebula.widgets.nattable.dataset.pricing;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ColumnHeaders {
	ONE("ISIN", "isin", String.class),
	TWO("Bid", "bid", Double.class), 
	THREE("Ask", "ask", Double.class), 
	FOUR("Pricing Model", "pricingModel", String.class), 
	FIVE("Ask Yield", "askYield", Double.class),
	SIX("Bid Yield", "bidYield", Double.class),
	SEVEN("(p) Bid/Ask", "bidOverAsk", Double.class),
	EIGHT("Bid Ask Type", "bidAskType", String.class),
	NINE("Base Issue", "baseIssue", String.class),
	TEN("Closing Price", "closingPrice", Double.class),
	ELEVEN("Closing Yield", "closingYield", Double.class),
	TWELVE("Closing Spread", "closingSpread", Double.class),
	THIRTEEN("Price Change", "priceChange", Double.class),
	FOURTEEN("Yield Change", "yieldChange", Double.class),
	FIFTEEN("Spread Change", "spreadChange", Double.class),
	SIXTEEN("Alias", "alias", String.class),
	SEVENTEEN("Basis Point Value", "basisPointValue", Double.class),
	EIGHTEEN("Mod. Duration", "modDuration", Double.class),
	NINETEEN("Convexity", "convexity", Double.class),
	TWENTY("Native Trading Group", "nativeTradingGroup", String.class),
	TWENTYONE("Error Severity", "errorSeverity", Double.class),
	TWENTYTWO("Error Message", "errorMessage", String.class),
	TWENTYTHREE("Pricing Source", "pricingSource", String.class),
	TWENTYFOUR("Security Type", "securityType", String.class),
	;

	private String label;
	private String property;
	private final Class<?> type;

	ColumnHeaders(String label, String property, Class<?> type) {
		this.label = label;
		this.property = property;
		this.type = type;
	}
	
	public static String[] getLabels() {
		String labels[] = new String[ColumnHeaders.values().length];
		int colIndex = 0;
		for(ColumnHeaders header : ColumnHeaders.values()) {
			labels[colIndex++] = header.getLabel();
		}
		return labels;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public static String[] getProperties() {
		String properties[] = new String[ColumnHeaders.values().length];
		int colIndex = 0;
		for(ColumnHeaders header : ColumnHeaders.values()) {
			properties[colIndex++] = header.getProperty();
		}
		return properties;
	}
	
	public static Map<String, Integer> getPropertyMap() {
		Map<String, Integer> propertyMap = new LinkedHashMap<String, Integer>();
		String[] properties = getProperties();
		for(int index = 0; index < properties.length; index++) {
			propertyMap.put(properties[index], new Integer(index));
		}
		return propertyMap;
	}

	public static LinkedHashMap<String, Class<?>> getPropertyNamesToTypeMap() {
		LinkedHashMap<String, Class<?>> propertyNamesToTypeMap = new LinkedHashMap<String, Class<?>>();
		
		for (ColumnHeaders columnHeader : ColumnHeaders.values()) {
			propertyNamesToTypeMap.put(columnHeader.getProperty(), columnHeader.getType());
		}
		
		return propertyNamesToTypeMap;
	}
	
	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	public Class<?> getType() {
		return type;
	}
}
