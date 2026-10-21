package io.github.onrcanogul.openbanking.hhsbank.common.pagination;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.InvalidPagingException;

public enum SortDirection {
    ASC, DESC;

    /** srlmYon: A = Azalan (descending), Y = Artan (ascending). */
    public static SortDirection fromOhvps(String code) {
        if ("A".equals(code)) return DESC;
        if ("Y".equals(code)) return ASC;
        throw new InvalidPagingException("srlmYon");
    }
}
