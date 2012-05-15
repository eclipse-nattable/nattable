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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class PricingDataFileLoader<T> {

	public List<T> loadDataFromFile() throws IOException {
		List<T> data = new ArrayList<T>();
		int i = 0;
		DelimitedFileReader reader = new DelimitedFileReader(new BufferedReader(new InputStreamReader(PricingDataFileLoader.class.getResourceAsStream("pricing_data.txt"))), '\t');
		if (reader.ready() && reader.markSupported()) {
			while (reader.read() > 0) {
				i++;
				parseTabDelimitedLine(reader.getTabbedLineRead(), data);
			}
		}
		return data;
	}

	@SuppressWarnings("unchecked")
	public void parseTabDelimitedLine(StringTokenizer tabs, List<T> data) {
		while (tabs.hasMoreElements()) {
			PricingDataBean bean = new PricingDataBean();
			bean.setIsin(extractStringFromToken(tabs.nextToken()));

			bean.setBid(extractDoubleFromToken(tabs.nextToken()));
			bean.setAsk(extractDoubleFromToken(tabs.nextToken()));
			bean.setBidYield(extractDoubleFromToken(tabs.nextToken()));
			bean.setAskYield(extractDoubleFromToken(tabs.nextToken()));
			bean.setBidSpread(extractDoubleFromToken(tabs.nextToken()));
			bean.setAskSpread(extractDoubleFromToken(tabs.nextToken()));
			bean.setBidOverAsk(extractDoubleFromToken(tabs.nextToken()));
			bean.setBidOverAskP(extractDoubleFromToken(tabs.nextToken()));
			bean.setBidAskType(extractStringFromToken(tabs.nextToken()));

			bean.setPricingModel(extractStringFromToken(tabs.nextToken()));
			bean.setBaseIssue(extractStringFromToken(tabs.nextToken()));

			/*
			 * bean.setClosingPrice(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setClosingYield(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setClosingSpread(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setPriceChange(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setYieldChange(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setSpreadChange(extractDoubleFromToken(tabs.nextToken()));
			 * 
			 * bean.setBasisPointValue(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setModDuration(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setConvexity(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setComments(extractStringFromToken(tabs.nextToken()));
			 * bean.setNativeTradingGroup(extractStringFromToken(tabs.nextToken()));
			 * 
			 * bean.setTgPosition(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTgPL(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTgClosingPL(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTgCostOfInventory(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTgAverageCost(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTgUnrealizedPL(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTgNetPL(extractDoubleFromToken(tabs.nextToken()));
			 */

			// new file does not contain these columns
			/*
			 * bean.setIdnAskYield(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setIdnBid(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setIdnBidSize(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setIdnBidSpread(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setIdnBidYield(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTdPosition(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTdTradingPL(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTdClosingPL(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTdCostOfInventory(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTdAvgCost(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTdUnrealizedPL(extractDoubleFromToken(tabs.nextToken()));
			 * bean.setTdNetPL(extractDoubleFromToken(tabs.nextToken()));
			 */

			bean.setAlias(extractStringFromToken(tabs.nextToken()));
			bean.setErrorMessage(extractStringFromToken(tabs.nextToken()));
			bean.setErrorSeverity(extractIntFromToken(tabs.nextToken()));
			bean.setPricingSource(extractStringFromToken(tabs.nextToken()));
			bean.setSecurityType(extractStringFromToken(tabs.nextToken()));
			bean.setComments(extractStringFromToken(tabs.nextToken()));
			data.add((T) bean);
		}
	}

	private int extractIntFromToken(String token) {
		System.out.println("int: " + token);
		return token.trim().equals("") || token.trim().equals("\t") ? 0 : Integer.parseInt(token);
	}

	private double extractDoubleFromToken(String token) {
		System.out.println("double: " + token);
		return token.trim().equals("") || token.trim().equals("\t") ? 0 : Double.parseDouble(token);
	}

	private String extractStringFromToken(String token) {
		System.out.println("string: " + token);
		return token == null || (token.trim().equals("") || token.trim().equals("\t")) ? null : token;
	}
}
