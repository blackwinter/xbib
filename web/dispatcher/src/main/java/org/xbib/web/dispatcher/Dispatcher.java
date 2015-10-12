package org.xbib.web.dispatcher;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import org.xbib.common.xcontent.XContentHelper;
import org.xbib.tools.merge.serials.entities.Institution;
import org.xbib.tools.merge.serials.entities.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Dispatcher algorithm for routing requests to libraries. Finds the right order of supplying libraries for a request.
 */
public class Dispatcher {

    private final static Logger logger = LogManager.getLogger(Dispatcher.class.getName());

    public Map<String,Object> execute(Client client, DispatcherRequest request) throws IOException {
        if (client == null) {
            throw new IOException("no client found");
        }
        if (request == null) {
            throw new IOException("no request found");
        }
        if (request.getIdentifier() == null) {
            return newHashMap();
        }
        if (request.getYear() == null) {
            String id = (request.getSource() != null ? request.getSource() + "." : "" ) + request.getIdentifier();
            SearchRequestBuilder searchRequest = client.prepareSearch()
                    .setIndices(request.getIndex())
                    .setTypes(request.getType())
                    .setQuery(termQuery("@id", id));
            if (request.getFrom() != null && request.getFrom() >= 0) {
                searchRequest.setFrom(request.getFrom());
            }
            if (request.getSize() != null && request.getSize() >= 0) {
                searchRequest.setSize(request.getSize());
            }
            logger.info("index {}/{} executing search {}", request.getIndex(), request.getType(), searchRequest);
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            Map<String, Object> result = newHashMap();
            result.put("found", searchResponse.getHits().getTotalHits());
            List<Map<String, Object>> results = newLinkedList();
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                Map<String, Object> m = newHashMap();
                Map<String,Object> source = hit.getSource();
                Map<String,Object> filtered = filter(request, toInstitutions(request, source));
                m.putAll(filtered);
                m.put("year", source.get("date"));
                if (!request.isCompact()) {
                    m.put("@id", source.get("@id"));
                    m.put("links", source.get("links"));
                }
                results.add(m);
            }
            result.put("results", results);
            return result;
        }
        String id = (request.getSource() != null ? request.getSource() + "." : "" ) + request.getIdentifier() + "." + request.getYear();
        GetRequestBuilder getRequest = client.prepareGet()
                .setIndex(request.getIndex())
                .setType(request.getType())
                .setId(id);
        logger.info("index {}/{} executing get request {}", request.getIndex(), request.getType(), id);
        GetResponse getResponse = getRequest.execute().actionGet();
        Map<String,Object> result = newHashMap();
        result.put("found", getResponse.isExists() ? 1 : 0);
        if (getResponse.isExists()) {
            Map<String,Object> source = getResponse.getSourceAsMap();
            Map<String, Object> filtered = filter(request, toInstitutions(request, source));
            result.putAll(filtered);
            if (!request.isCompact()) {
                result.put("@id", source.get("@id"));
                result.put("date", source.get("date"));
                result.put("links", source.get("links"));
            }
        } else {
            logger.warn("no document exists");
        }
        return result;
    }

    public Map<String,Object> execute(DispatcherRequest request, String json) throws IOException {
        Map<String,Object> result = newHashMap();
        Map<String,Object> source = XContentHelper.convertToMap(json);
        logger.info("source={}", source);
        Map<String, Object> filtered = filter(request, toInstitutions(request, source));
        result.putAll(filtered);
        if (!request.isCompact()) {
            result.put("@id", source.get("@id"));
            result.put("date", source.get("date"));
            result.put("links", source.get("links"));
        }
        return result;
    }

    private List<Institution> toInstitutions(DispatcherRequest request, Map<String,Object> source) throws IOException {
        final List<Institution> list = newLinkedList();
        ((List<Map<String, Object>>) source.get("institution")).stream()
                .forEach(new Consumer<Map<String, Object>>() {
                    @Override
                    public void accept(Map<String, Object> map) {
                        Institution institution = new Institution(map, request.getGroupOrder(), request.getGroupMap());
                        if (request.getInstituionCarrierFilter() != null && request.getInstituionCarrierFilter().containsKey(institution.getISIL())) {
                            institution.setCarrier(request.getInstituionCarrierFilter().get(institution.getISIL()));
                        }
                        list.add(institution);
                    }
                });

        return list;
    }

    private Map<String, Object> filter(DispatcherRequest request, List<Institution> institutions) {
        institutions.stream()
                .forEach(new Consumer<Institution>() {
                    @Override
                    public void accept(Institution institution) {
                        List<Service> filteredServices = institution.getActiveServices().stream()
                                .filter(service -> includeCarrier(request, institution, service))
                                .filter(service -> excludeCarrier(request, institution, service))
                                .filter(service -> includeGroup(request, service))
                                .filter(service -> excludeGroup(request, service))
                                .filter(service -> includeInstitution(request, service))
                                .filter(service -> excludeInstitution(request, service))
                                .filter(service -> includeType(request, service))
                                .filter(service -> excludeType(request, service))
                                .filter(service -> includeMode(request, service))
                                .filter(service -> excludeMode(request, service))
                                .filter(service -> includeDistribution(request, service))
                                .filter(service -> excludeDistribution(request, service))
                                .sorted()
                                .collect(toList());
                        institution.putActiveServices(filteredServices);
                        // difference set
                        Set<Service> diff = newHashSet(institution.getActiveServices());
                        diff.removeAll(filteredServices);
                        List<Service> other = institution.getOtherServices();
                        if (!diff.isEmpty()) {
                            other.addAll(diff);
                        }
                        if (!other.isEmpty()) {
                            institution.putOther(other);
                        }
                    }
                });
        // a bit of random here, we will sort later also.
        Collections.shuffle(institutions);

        if (request.isCompact()) {
            // compact display, only institution codes
            List<String> head = newLinkedList();
            List<String> tail = newLinkedList();
            List<String> other = newLinkedList();
            boolean hasBase;
            if (isEmpty(request.getBaseGroup())) {
                // no division into groups
                List<String> list = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .sorted()
                        .map(Institution::getISIL)
                        .collect(toList());
                hasBase = newHashSet(list).contains(request.getBase());

                // extract only priority insts
                Map<Boolean, List<String>> priorities = institutions.stream()
                        .collect(groupingBy(i -> i.getMarker("priority"),
                                mapping(Institution::getISIL, Collectors.toList())));
                if (priorities.containsKey(true)) {
                    head = priorities.remove(true);
                } else if (priorities.containsKey(false)) {
                    splitList(priorities.remove(false), request.getGroupLimit(), head, tail);
                }

                // append other institution services
                List<String> otherList = institutions.stream()
                        .filter(inst -> !inst.getOtherServices().isEmpty())
                        .sorted()
                        .map(Institution::getISIL)
                        .collect(toList());
                if (!otherList.isEmpty()) {
                    other.addAll(otherList);
                }
            } else {
                // divide into groups when baseGroup is given
                Map<String, List<String>> groups = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .filter(inst -> !inst.getGroup().equals("X"))
                        .sorted()
                        .collect(groupingBy(Institution::getGroup,
                                mapping(Institution::getISIL, Collectors.toList())));
                String group = request.getGroupMap() != null ?
                        request.getGroupMap().get(request.getBaseGroup()) : request.getBaseGroup();
                List<String> list = groups.containsKey(group) ? groups.remove(group) : newLinkedList();
                hasBase = newHashSet(list).contains(request.getBase());

                // extract only priority insts
                Map<Boolean, List<String>> priorities = institutions.stream()
                        .collect(groupingBy(i -> i.getMarker("priority"),
                                mapping(Institution::getISIL, Collectors.toList())));
                if (priorities.containsKey(true)) {
                    head = priorities.remove(true);
                } else if (priorities.containsKey(false)) {
                    splitList(priorities.remove(false), request.getGroupLimit(), head, tail);
                }

                List<String> last = newLinkedList(groups.keySet());
                Collections.shuffle(last);
                splitList(last, request.getGroupLimit(), head, other);
                groups = institutions.stream()
                        .filter(inst -> !inst.getOtherServices().isEmpty())
                        .filter(inst -> !inst.getGroup().equals("X"))
                        .sorted()
                        .collect(groupingBy(Institution::getGroup,
                                mapping(Institution::getISIL, Collectors.toList())));
                if (!groups.keySet().isEmpty()) {
                    other.addAll(groups.keySet());
                }
            }
            Map<String,Object> result = newHashMap();
            if (!head.isEmpty()) {
                result.put("head", head);
            }
            if (!tail.isEmpty()) {
                result.put("tail", tail);
            }
            if (!other.isEmpty()) {
                result.put("other", other);
            }
            result.put("hasbase", hasBase);
            return result;
        } else {
            // no compact display
            List<Institution> head = newLinkedList();
            List<Institution> tail = newLinkedList();
            List<Institution> other = newLinkedList();
            boolean hasBase;
            Map<String, Object> result = newHashMap();
            if (isEmpty(request.getBaseGroup())) {
                List<Institution> list = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .map(i -> i.setMarker(getMarker(request, i)))
                        .sorted()
                        .collect(toList());
                hasBase = list.stream().anyMatch(i -> i.getISIL().equals(request.getBase()));

                Map<Boolean, List<Institution>> priorities = list.stream()
                        .collect(groupingBy(i -> i.getMarker("priority")));
                if (priorities.containsKey(true)) {
                    head = priorities.remove(true);
                    //tail = priorities.remove(false).stream().sorted().collect(toList());
                } else if (priorities.containsKey(false)) {
                    splitList(priorities.remove(false), request.getGroupLimit(), head, tail);
                }
                List<Institution> otherInst = institutions.stream()
                        .filter(inst -> !inst.getOtherServices().isEmpty())
                        .filter(inst -> !inst.getGroup().equals("X"))
                        .map(i -> i.setMarker(getMarker(request, i)))
                        .sorted()
                        .collect(toList());
                if (!otherInst.isEmpty()) {
                    other.addAll(otherInst);
                }
            } else {
                // divide into groups, build first/last pair
                Map<String, List<Institution>> groups = institutions.stream()
                        .filter(inst -> !inst.getActiveServices().isEmpty())
                        .map(inst -> inst.setMarker(getMarker(request, inst)))
                        .sorted()
                        .collect(groupingBy(Institution::getGroup));
                // remove unknown institution groups
                groups.remove("X");
                String group = request.getGroupMap() != null ?
                        request.getGroupMap().get(request.getBaseGroup()) : request.getBaseGroup();
                // find our group (the base group)
                List<Institution> list = groups.containsKey(group) ? groups.remove(group) : newLinkedList();
                // find our institution (the base institution)
                hasBase = list.stream().anyMatch(inst -> inst.getISIL().equals(request.getBase()));

                // priority/non-priority insts
                Map<Boolean, List<Institution>> priorities = list.stream()
                        .collect(groupingBy(i -> i.getMarker("priority")));
                if (priorities.containsKey(true)) {
                    head = priorities.remove(true);
                } else if (priorities.containsKey(false)) {
                    splitList(priorities.remove(false), request.getGroupLimit(), head, tail);
                }

                // append other services
                Map<String, List<Institution>> groupsother = institutions.stream()
                        .filter(inst -> !inst.getOtherServices().isEmpty())
                        .filter(inst -> !inst.getGroup().equals("X"))
                        .map(inst -> inst.setMarker(getMarker(request, inst)))
                        .sorted()
                        .collect(groupingBy(Institution::getGroup));
                // random order of groups
                List<String> grouplist = Lists.newLinkedList(groups.keySet());
                Collections.shuffle(grouplist);
                list = groupsother.containsKey(group) ? groupsother.remove(group) : newLinkedList();
                if (!list.isEmpty()) {
                    other.addAll(list);
                }
                if (request.isExpandGroups()) {
                    // append all the group as institutions to head
                    for (String l : grouplist) {
                        head.addAll(groups.get(l));
                    }
                    for (String l : groupsother.keySet()) {
                        other.addAll(groupsother.get(l));
                    }
                } else {
                    // collapse: only group names instead of whole group institutions
                    List<String> last = newLinkedList();
                    List<String> lastother = newLinkedList();
                    splitList(grouplist, request.getGroupLimit(), last, lastother);
                    if (!groupsother.isEmpty()) {
                        lastother.addAll(groupsother.keySet());
                    }
                    if (!last.isEmpty()) {
                        result.put("group", last);
                        result.put("groupcount", last.size());
                    }
                    if (!lastother.isEmpty()) {
                        result.put("groupother", lastother);
                        result.put("groupothercount", lastother.size());
                    }
                }
            }
            if (!head.isEmpty()) {
                result.put("head", head);
                result.put("headcount", head.size());
            }
            if (!tail.isEmpty()) {
                result.put("tail", tail);
                result.put("tailcount", tail.size());
            }
            if (!other.isEmpty()) {
                result.put("other", other);
                result.put("othercount", other.size());
            }
            result.put("hasbase", hasBase);
            return result;
        }
    }

    private boolean includeCarrier(DispatcherRequest request, Institution institution, Service service) {
        String carriertype = (String)service.get("carriertype");
        return institution.isCarrierAllowed(carriertype) &&
                (request.getCarrierFilter() == null ||
                    (service.containsKey("carriertype") &&
                            request.getCarrierFilter().contains(carriertype)));
    }

    private boolean excludeCarrier(DispatcherRequest request, Institution institution, Service service) {
        String carriertype = (String)service.get("carriertype");
        return institution.isCarrierAllowed(carriertype) &&
                (request.getExcludeCarrierFilter() == null ||
                    (service.containsKey("carriertype") &&
                        ! request.getExcludeCarrierFilter().contains(carriertype)));
    }

    private boolean includeGroup(DispatcherRequest request, Service service) {
        String organization = (String)service.get("organization");
        return request.getGroupFilter() == null ||
                (service.containsKey("organization") && request.getGroupFilter().contains(organization));
    }

    private boolean excludeGroup(DispatcherRequest request, Service service) {
        String organization = (String)service.get("organization");
        return request.getExcludeGroupFilter() == null ||
                (service.containsKey("organization") && ! request.getExcludeGroupFilter().contains(organization));
    }

    private boolean includeInstitution(DispatcherRequest request, Service service) {
        String isil = (String)service.get("isil");
        return request.getInstitutionFilter() == null ||
                (service.containsKey("isil") && request.getInstitutionFilter().contains(isil));
    }

    private boolean excludeInstitution(DispatcherRequest request, Service service) {
        String isil = (String)service.get("isil");
        return request.getExcludeInstitutionFilter() == null ||
                (service.containsKey("isil") && !request.getExcludeInstitutionFilter().contains(isil));
    }

    private boolean includeType(DispatcherRequest request, Service service) {
        String type = (String)service.get("type");
        return request.getTypeFilter() == null ||
                (service.containsKey("type") && request.getTypeFilter().contains(type));
    }

    private boolean excludeType(DispatcherRequest request, Service service) {
        String type = (String)service.get("type");
        return request.getExcludeTypeFilter() == null ||
                (service.containsKey("type") && !request.getExcludeTypeFilter() .contains(type));
    }

    private boolean includeMode(DispatcherRequest request, Service service) {
        String mode = (String)service.get("mode");
        return request.getModeFilter() == null ||
                (service.containsKey("mode") && request.getModeFilter().contains(mode));
    }

    private boolean excludeMode(DispatcherRequest request, Service service) {
        String mode = (String)service.get("mode");
        return request.getExcludeModeFilter() == null ||
                (service.containsKey("mode") && !request.getExcludeModeFilter().contains(mode));
    }

    private boolean includeDistribution(DispatcherRequest request, Service service) {
        return checkValueInSet(service.get("distribution"), request.getDistributionFilter() );
    }

    private boolean excludeDistribution(DispatcherRequest request, Service service) {
        return checkValueNotInSet(service.get("distribution"), request.getExcludeDistributionFilter());
    }

    private String getMarker(DispatcherRequest request, Institution institution) {
        return request.getInstitutionMarker() != null ?
                request.getInstitutionMarker().get(institution.getISIL()) : null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private void splitList(List list, Integer split, List a, List b) {
        int pos = split != null && split > 0 && split < list.size() ? split : list.size();
        a.addAll(list.subList(0, pos));
        b.addAll(list.subList(pos, list.size()));
    }

    private boolean checkValueInSet(Object o, Set<String> set) {
        if (o == null || set == null) {
            return true;
        }
        if (!(o instanceof Collection)) {
            o = Collections.singleton(o);
        }
        for (Object value : (Collection)o) {
            if (set.contains(value.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkValueNotInSet(Object o, Set<String> set) {
        if (o == null || set == null) {
            return true;
        }
        if (!(o instanceof Collection)) {
            o = Collections.singleton(o);
        }
        boolean b = true;
        for (Object value : (Collection)o) {
            b = b && !set.contains(value.toString());
        }
        return b;
    }
}
