package io.github.onrcanogul.openbanking.hhsbank.common.pagination;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.InvalidPagingException;

public record PageQuery(int pageNumber, int pageSize, String sortField, SortDirection direction) {

    private static final int MAX_PAGE_SIZE = 100;

    public PageQuery {
        if (pageNumber < 1) throw new InvalidPagingException("syfNo");
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) throw new InvalidPagingException("syfKytSayi");
    }

}
