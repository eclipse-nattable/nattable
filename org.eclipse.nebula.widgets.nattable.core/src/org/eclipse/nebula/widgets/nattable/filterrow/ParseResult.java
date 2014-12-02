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
        EQUAL("="), //$NON-NLS-1$
        NOT_EQUAL("<>"), //$NON-NLS-1$
        GREATER_THAN(">"), //$NON-NLS-1$
        GREATER_THAN_OR_EQUAL(">="), //$NON-NLS-1$
        LESS_THAN_OR_EQUAL("<="), //$NON-NLS-1$
        LESS_THAN("<"), //$NON-NLS-1$
        NONE(""); //$NON-NLS-1$

        private String symbol;

        private MatchType(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return this.symbol;
        }

        public static MatchType parse(String symbol) {
            if ("=".equals(symbol)) { //$NON-NLS-1$
                return EQUAL;
            } else if ("<>".equals(symbol)) { //$NON-NLS-1$
                return NOT_EQUAL;
            } else if (">".equals(symbol)) { //$NON-NLS-1$
                return GREATER_THAN;
            } else if ("<".equals(symbol)) { //$NON-NLS-1$
                return LESS_THAN;
            } else if (">=".equals(symbol)) { //$NON-NLS-1$
                return GREATER_THAN_OR_EQUAL;
            } else if ("<=".equals(symbol)) { //$NON-NLS-1$
                return LESS_THAN_OR_EQUAL;
            }
            return NONE;
        }
    };

    private MatchType matchType = MatchType.NONE;
    private String valueToMatch;

    public ParseResult() {}

    public MatchType getMatchOperation() {
        return this.matchType;
    }

    public String getValueToMatch() {
        return this.valueToMatch;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public void setValueToMatch(String valueToMatch) {
        this.valueToMatch = valueToMatch;
    }

}
