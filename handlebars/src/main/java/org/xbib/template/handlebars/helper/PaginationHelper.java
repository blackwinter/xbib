package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaginationHelper implements Helper<Object> {

    public static final Helper<Object> INSTANCE = new PaginationHelper();

    public static final String NAME = "pagination";

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        int currentPage = options.param(0, 1);
        int totalPages = options.param(1, 1);
        int groups = options.param(2, 10);
        int first = (((currentPage - 1) / groups)) * groups + 1;
        int last = (((currentPage - 1) / groups)) * groups + groups;
        int previous = last - groups;
        int next = last + 1;
        boolean hasPrevious = first > 1;
        boolean hasNext = totalPages > last;
        int displayedLastPage = totalPages < last ? totalPages : last;
        Map<String, Object> map = new HashMap<>();
        List<Map> pages = new ArrayList<>();
        for (int i = first; i <= displayedLastPage; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("page", Integer.toString(i));
            m.put("isCurrent", currentPage == i);
            pages.add(m);
        }
        map.put("hasPrevious", hasPrevious);
        map.put("previous", previous);
        map.put("pages", pages);
        map.put("hasNext", hasNext);
        map.put("next", next);
        return options.fn(map);
    }

}