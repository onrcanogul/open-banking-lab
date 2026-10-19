package io.github.onrcanogul.openbanking.hhsbank.common.pagination;

import java.util.List;

public record PageResult<T> (List<T> content, long totalCount, int pageNumber, int pageSize) {
    public boolean hasNext() { return (long) pageNumber * pageSize < totalCount; }
}
