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
package org.eclipse.nebula.widgets.nattable.filterrow;

/**
 * Represents the result of parsing the filter text typed into the text box in
 * the filter row.
 */
public class ParseResult {

	/**
	 * Comparison tokens
	 */
	public enum MatchType {
		EQUALS("="), //$NON-NLS-1$
		GREATER(">"), //$NON-NLS-1$
		GREATER_THAN_EQUALS(">="), //$NON-NLS-1$
		LESS_THAN_EQUALS("<="), //$NON-NLS-1$
		LESSER("<"), //$NON-NLS-1$
		NONE(""); //$NON-NLS-1$

		private String symbol;

		private MatchType(String symbol) {
			this.symbol = symbol;
		}

		public String getSymbol() {
			return symbol;
		}

		public static MatchType parse(String symbol) {
			if ("=".equals(symbol)) { //$NON-NLS-1$
				return EQUALS;
			} else if (">".equals(symbol)) { //$NON-NLS-1$
				return GREATER;
			} else if ("<".equals(symbol)) { //$NON-NLS-1$
				return LESSER;
			} else if (">=".equals(symbol)) { //$NON-NLS-1$
				return GREATER_THAN_EQUALS;
			} else if ("<=".equals(symbol)) { //$NON-NLS-1$
				return LESS_THAN_EQUALS;
			}
			return NONE;
		}
	};

	private MatchType matchType = MatchType.NONE;
	private String valueToMatch;

	public ParseResult() {
	}

	public MatchType getMatchOperation() {
		return matchType;
	}

	public String getValueToMatch() {
		return valueToMatch;
	}

	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	public void setValueToMatch(String valueToMatch) {
		this.valueToMatch = valueToMatch;
	}

}
