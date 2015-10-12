package org.xbib.tools.merge.serials.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newLinkedList;

public class Institution extends HashMap<String,Object> implements Comparable<Institution> {

    private final Map<String,Integer> groupPriorities;

    private String group;

    private Set<String> carrier;

    private List<Service> activeServices;

    private List<Service> otherServices;

    public Institution(Map<String,Object> map,
                       Map<String,Integer> groupPriorities,
                       Map<String,String> groupMap
    ) {
        super(map);
        makeServices(this);
        this.groupPriorities = groupPriorities;
        String s = firstService().getGroup();
        this.group = groupMap != null ? groupMap.get(s) : s;
        if (group == null) {
            group = "X";
        }
        put("group", group);
    }

    private void makeServices(Institution institution) {
        List<Service> services = newLinkedList();
        List<Service> other = newLinkedList();
        ((List<Map<String,Object>>)get("service")).stream()
                .forEach(map -> {
                    Service service = new Service(map, institution);
                    if (service.isActive()) {
                        services.add(service);
                    } else {
                        other.add(service);
                    }
                });
        putActiveServices(services);
        putOther(other);
    }

    public String getGroup() {
        return group;
    }

    public Institution setCarrier(Set<String> carrier) {
        this.carrier = carrier;
        return this;
    }

    public Set<String> getCarrier() {
        return carrier;
    }

    public boolean isCarrierAllowed(String carrier) {
        return this.carrier == null || this.carrier.contains(carrier);
    }

    public void putActiveServices(List<Service> services) {
        this.activeServices = services;
        put("service", services);
        put("servicecount", services.size());
    }

    public List<Service> getActiveServices() {
        return activeServices;
    }

    public void putOther(List<Service> services) {
        this.otherServices = services;
        put("other", services);
        put("othercount", services.size());
    }

    public List<Service> getOtherServices() {
        return otherServices;
    }

    public String getISIL() {
        return (String)get("isil");
    }

    public Integer getGroupPriority(String group) {
        return groupPriorities != null ?
                groupPriorities.containsKey(group) ? groupPriorities.get(group) : 0 : 0;
    }

    public Institution setMarker(String marker) {
        if (marker != null) {
            put(marker, true);
        }
        return this;
    }

    public boolean getMarker(String marker) {
        return containsKey(marker);
    }

    public Integer getPriority() {
        return containsKey("priority") ? 0 : 1;
    }
    @Override
    public int compareTo(Institution o) {
        return firstService().compareTo(o.firstService());
    }

    private Service firstService() {
         return !activeServices.isEmpty() ? activeServices.get(0) :
                !otherServices.isEmpty() ? otherServices.get(0) : null;
    }

}
