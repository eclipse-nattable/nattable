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

import org.eclipse.nebula.widgets.nattable.dataset.generator.DataValueGenerator;
import org.eclipse.nebula.widgets.nattable.dataset.generator.GenerateDouble;
import org.eclipse.nebula.widgets.nattable.dataset.generator.GenerateListOfStrings;
import org.eclipse.nebula.widgets.nattable.dataset.pricing.valuegenerator.BidAskTypeValueGenerator;
import org.eclipse.nebula.widgets.nattable.dataset.pricing.valuegenerator.ErrorSeverityValueGenerator;
import org.eclipse.nebula.widgets.nattable.dataset.pricing.valuegenerator.IsinValueGenerator;
import org.eclipse.nebula.widgets.nattable.dataset.valuegenerator.SentenceValueGenerator;

public class PricingDataBean  {

	@DataValueGenerator(IsinValueGenerator.class)
	private String isin;

	// bid ask stuff
	@GenerateDouble(range = 10000)
	private double bid;
	@GenerateDouble(range = 500)
	private double bidYield;
	@GenerateDouble(range = 10000)
	private double ask;
	@GenerateDouble(range = 500)
	private double askYield;
	@GenerateDouble(floor = -10, range = 100)
	private double bidOverAsk;
	private double bidOverAskP;
	private double bidSpread;
	private double askSpread;
	@DataValueGenerator(BidAskTypeValueGenerator.class)
	private String bidAskType;

	// closing
	@GenerateDouble(range = 10000)
	private double closingPrice;
	@GenerateDouble(range = 500)
	private double closingYield;
	private double closingSpread;
	private double priceChange;
	@GenerateDouble(range = 500)
	private double yieldChange;
	private double spreadChange;

	// analytics?
	private double basisPointValue;
	private double modDuration;
	private double convexity;

	// trading group
	private String nativeTradingGroup;
	private double tgPosition;
	private double tgPL;
	private double tgClosingPL;
	private double tgCostOfInventory;
	private double tgAverageCost;
	private double tgUnrealizedPL;
	private double tgNetPL;

	// IDN
	private double idnBid;
	private double idnBidYield;
	private double idnBidSize;
	private double idnBidSpread;
	private double idnAskYield;

	// TD
	private double tdPosition;
	private double tdTradingPL;
	private double tdClosingPL;
	private double tdCostOfInventory;
	private double tdAvgCost;
	private double tdUnrealizedPL;
	private double tdNetPL;

	@GenerateListOfStrings(values = { "STATS", "CSSXML", "SPREADSHEET",
			"POCKET_CALCULATOR" })
	private String pricingSource;
	@DataValueGenerator(SentenceValueGenerator.class)
	private String comments;
	@GenerateListOfStrings(values = { "23M", "5YR", "OLDR3", "OLDTTT" }, nullLoadFactor = 24)
	private String alias;
	@GenerateListOfStrings(values = { "1MO", "2YR", "3YR", "30YR",
			"ESPEED 2YR", "OLD2YR", "S 0 08/15/18", "T 3 1/2 08/15/09",
			"T 3 1/2 12/15/09", "T 4 04/15/10", "T 4 3/4 02/15/37",
			"T 4 7/8 08/31/08" })
	private String baseIssue;
	@GenerateListOfStrings(values = { "ATT", "BTEC", "MANUAL_PRICE",
			"MANUAL_YIELD", "PPSST", "Y_SS" })
	private String pricingModel;
	@GenerateListOfStrings(values = { "B", "CPN" })
	private String securityType;

	// error
	@GenerateListOfStrings(nullLoadFactor = 19, values = {
			"Market price data not available", "Market price is not available",
			"Pricing model not supported",
			"Security cannot be priced; calculation errors encountered" })
	private String errorMessage;
	@DataValueGenerator(ErrorSeverityValueGenerator.class)
	private int errorSeverity;

	public PricingDataBean() {
	}

	public String getIsin() {
		return isin;
	}
	
	public void setIsin(String isin) {
		this.isin = isin;
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getBidYield() {
		return bidYield;
	}

	public void setBidYield(double bidYield) {
		this.bidYield = bidYield;
	}

	public double getAsk() {
		return ask;
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public double getAskYield() {
		return askYield;
	}

	public void setAskYield(double askYield) {
		this.askYield = askYield;
	}

	public double getBidOverAsk() {
		return bidOverAsk;
	}

	public void setBidOverAsk(double bidOverAsk) {
		this.bidOverAsk = bidOverAsk;
	}

	public String getBidAskType() {
		return bidAskType;
	}

	public void setBidAskType(String bidkAskType) {
		this.bidAskType = bidkAskType;
	}

	public double getClosingPrice() {
		return closingPrice;
	}

	public void setClosingPrice(double closingPrice) {
		this.closingPrice = closingPrice;
	}

	public double getClosingYield() {
		return closingYield;
	}

	public void setClosingYield(double closingYield) {
		this.closingYield = closingYield;
	}

	public double getClosingSpread() {
		return closingSpread;
	}

	public void setClosingSpread(double closingSpread) {
		this.closingSpread = closingSpread;
	}

	public double getPriceChange() {
		return priceChange;
	}

	public void setPriceChange(double priceChange) {
		this.priceChange = priceChange;
	}

	public double getYieldChange() {
		return yieldChange;
	}

	public void setYieldChange(double yieldChange) {
		this.yieldChange = yieldChange;
	}

	public double getSpreadChange() {
		return spreadChange;
	}

	public void setSpreadChange(double spreadChange) {
		this.spreadChange = spreadChange;
	}

	public double getBasisPointValue() {
		return basisPointValue;
	}

	public void setBasisPointValue(double basisPointValue) {
		this.basisPointValue = basisPointValue;
	}

	public double getModDuration() {
		return modDuration;
	}

	public void setModDuration(double modDuration) {
		this.modDuration = modDuration;
	}

	public double getConvexity() {
		return convexity;
	}

	public void setConvexity(double convexity) {
		this.convexity = convexity;
	}

	public String getNativeTradingGroup() {
		return nativeTradingGroup;
	}

	public void setNativeTradingGroup(String nativeTradingGroup) {
		this.nativeTradingGroup = nativeTradingGroup;
	}

	public double getTgPosition() {
		return tgPosition;
	}

	public void setTgPosition(double tgPosition) {
		this.tgPosition = tgPosition;
	}

	public double getTgPL() {
		return tgPL;
	}

	public void setTgPL(double tgPL) {
		this.tgPL = tgPL;
	}

	public double getTgClosingPL() {
		return tgClosingPL;
	}

	public void setTgClosingPL(double tgClosingPL) {
		this.tgClosingPL = tgClosingPL;
	}

	public double getTgCostOfInventory() {
		return tgCostOfInventory;
	}

	public void setTgCostOfInventory(double tgCostOfInventory) {
		this.tgCostOfInventory = tgCostOfInventory;
	}

	public double getTgAverageCost() {
		return tgAverageCost;
	}

	public void setTgAverageCost(double tgAverageCost) {
		this.tgAverageCost = tgAverageCost;
	}

	public double getTgUnrealizedPL() {
		return tgUnrealizedPL;
	}

	public void setTgUnrealizedPL(double tgUnrealizedPL) {
		this.tgUnrealizedPL = tgUnrealizedPL;
	}

	public double getTgNetPL() {
		return tgNetPL;
	}

	public void setTgNetPL(double tgNetPL) {
		this.tgNetPL = tgNetPL;
	}

	public double getIdnBid() {
		return idnBid;
	}

	public void setIdnBid(double idnBid) {
		this.idnBid = idnBid;
	}

	public double getIdnBidYield() {
		return idnBidYield;
	}

	public void setIdnBidYield(double idnBidYield) {
		this.idnBidYield = idnBidYield;
	}

	public double getIdnBidSize() {
		return idnBidSize;
	}

	public void setIdnBidSize(double idnBidSize) {
		this.idnBidSize = idnBidSize;
	}

	public double getIdnBidSpread() {
		return idnBidSpread;
	}

	public void setIdnBidSpread(double idnBidSpread) {
		this.idnBidSpread = idnBidSpread;
	}

	public double getIdnAskYield() {
		return idnAskYield;
	}

	public void setIdnAskYield(double idnAskYield) {
		this.idnAskYield = idnAskYield;
	}

	public double getTdPosition() {
		return tdPosition;
	}

	public void setTdPosition(double tdPosition) {
		this.tdPosition = tdPosition;
	}

	public double getTdTradingPL() {
		return tdTradingPL;
	}

	public void setTdTradingPL(double tdTradingPL) {
		this.tdTradingPL = tdTradingPL;
	}

	public double getTdClosingPL() {
		return tdClosingPL;
	}

	public void setTdClosingPL(double tdClosingPL) {
		this.tdClosingPL = tdClosingPL;
	}

	public double getTdCostOfInventory() {
		return tdCostOfInventory;
	}

	public void setTdCostOfInventory(double tdCostOfInventory) {
		this.tdCostOfInventory = tdCostOfInventory;
	}

	public double getTdAvgCost() {
		return tdAvgCost;
	}

	public void setTdAvgCost(double tdAvgCost) {
		this.tdAvgCost = tdAvgCost;
	}

	public double getTdUnrealizedPL() {
		return tdUnrealizedPL;
	}

	public void setTdUnrealizedPL(double tdUnrealizedPL) {
		this.tdUnrealizedPL = tdUnrealizedPL;
	}

	public double getTdNetPL() {
		return tdNetPL;
	}

	public void setTdNetPL(double tdNetPL) {
		this.tdNetPL = tdNetPL;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getBaseIssue() {
		return baseIssue;
	}

	public void setBaseIssue(String baseIssue) {
		this.baseIssue = baseIssue;
	}

	public String getPricingModel() {
		return pricingModel;
	}

	public void setPricingModel(String pricingModel) {
		this.pricingModel = pricingModel;
	}

	public String getPricingSource() {
		return pricingSource;
	}

	public void setPricingSource(String pricingSource) {
		this.pricingSource = pricingSource;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getErrorSeverity() {
		return errorSeverity;
	}

	public void setErrorSeverity(int errorSeverity) {
		this.errorSeverity = errorSeverity;
	}

	public String getSecurityType() {
		return securityType;
	}

	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}

	public double getBidSpread() {
		return bidSpread;
	}

	public void setBidSpread(double bidSpread) {
		this.bidSpread = bidSpread;
	}

	public double getAskSpread() {
		return askSpread;
	}

	public void setAskSpread(double askSpread) {
		this.askSpread = askSpread;
	}

	public double getBidOverAskP() {
		return bidOverAskP;
	}

	public void setBidOverAskP(double bidOverAskP) {
		this.bidOverAskP = bidOverAskP;
	}

	public String serializeToString() {
		return getIsin() + "\t" + getPricingModel() + "\t" + getAsk() + "\t"
				+ getBid() + "\t" + getAskYield() + "\t" + getBidYield() + "\t"
				+ getBidAskType() + "\t" + getBidOverAskP() + "\t"
				+ getBaseIssue() + "\t" + getClosingPrice() + "\t"
				+ getClosingYield() + "\t" + getClosingSpread() + "\t"
				+ getPriceChange() + "\t" + getYieldChange() + "\t"
				+ getSpreadChange() + "\t" + getAlias() + "\t"
				+ getBasisPointValue() + "\t" + getModDuration() + "\t"
				+ getConvexity() + "\t" + getNativeTradingGroup() + "\t"
				+ getErrorSeverity() + "\t" + getErrorMessage() + "\t"
				+ getPricingSource() + "\t" + getSecurityType();
	}
	
	@Override
	public String toString() {
		return "PricingDataBean[Isin:" + getIsin() + "]";
	}
}
