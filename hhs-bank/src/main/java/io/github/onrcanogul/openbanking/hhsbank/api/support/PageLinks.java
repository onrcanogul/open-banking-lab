package io.github.onrcanogul.openbanking.hhsbank.api.support;

import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the RFC 8288 Link header for paginated responses (spec 3.16): prev/next wherever applicable,
 * first and last always (last is optional in the spec; on an empty result first and last are the same page).
 */
public final class PageLinks {

    private PageLinks() {}

    public static String header(PageResult<?> page, UriComponentsBuilder requestUri) {
        long lastPage = Math.max(1, (page.totalCount() + page.pageSize() - 1) / page.pageSize());

        List<String> links = new ArrayList<>();
        links.add(link(requestUri, 1, "first"));
        if (page.pageNumber() > 1) {
            links.add(link(requestUri, Math.min(page.pageNumber() - 1, lastPage), "prev"));
        }
        if (page.hasNext()) {
            links.add(link(requestUri, page.pageNumber() + 1L, "next"));
        }
        links.add(link(requestUri, lastPage, "last"));
        return String.join(", ", links);
    }

    private static String link(UriComponentsBuilder requestUri, long pageNumber, String rel) {
        String url = requestUri.cloneBuilder().replaceQueryParam("syfNo", pageNumber).build().toUriString();
        return "<" + url + ">; rel=\"" + rel + "\"";
    }
}
