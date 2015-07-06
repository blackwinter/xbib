
package org.xbib.web.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.xbib.common.settings.Settings;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


@Path("/router/v1")
public class RoutingService {

    private final static Logger logger = LogManager.getLogger(RoutingService.class.getName());

    @GET
    @Produces({"application/json;charset=UTF-8"})
    public Response get(@Context UriInfo uriInfo,
                        @QueryParam("index") String index,
                        @QueryParam("indextype") String indextype,
                          @QueryParam("from") Integer from,
                          @QueryParam("size") Integer size,
                          @QueryParam("base") String base,
                          @QueryParam("source") String source,
                          @QueryParam("id") String identifier,
                          @QueryParam("year") Integer year,
                          @QueryParam("baseGroup") String baseGroup,
                          @QueryParam("groupLimit") Integer groupLimit,
                          @QueryParam("group") List<String> group,
                          @QueryParam("excludegroup") List<String> excludegroup,
                          @QueryParam("carrier") List<String> carrier,
                          @QueryParam("excludecarrier") List<String> excludecarrier,
                          @QueryParam("institution") List<String> institution,
                          @QueryParam("excludeinstitution") List<String> excludeinstitution,
                          @QueryParam("type") List<String> type,
                          @QueryParam("excludetype") List<String> excludetype,
                          @QueryParam("mode") List<String> mode,
                          @QueryParam("excludemode") List<String> excludemode,
                          @QueryParam("distribution") List<String> dist,
                          @QueryParam("excludedistribution") List<String> excludedist,
                          @QueryParam("style") String style
                         ) throws IOException {

        DispatcherSettings dispatcherSettings= DispatcherSettings.Factory.getDispatcherSettings(baseGroup);

        DispatcherRequest dispatcherRequest = new DispatcherRequest()
                .setIndex(index)
                .setType(indextype)
                .setCompact(contains(style, "compact"))
                .setFrom(from)
                .setSize(size)
                .setBase(base)
                .setSource(source)
                .setIdentifier(identifier)
                .setYear(year)
                .setBaseGroup(baseGroup)
                .setGroupMap(contains(style, "region") ? null : dispatcherSettings.getServiceMap())
                .setGroupLimit(groupLimit)
                .setExpandGroups(true)
                .setCarrierFilter(carrier)
                .setExcludeCarrierFilter(excludecarrier)
                .setGroupFilter(group)
                .setExcludeGroupFilter(excludegroup)
                .setInstitutionFilter(institution)
                .setExcludeInstitutionFilter(excludeinstitution)
                .setTypeFilter(type)
                .setExcludeTypeFilter(excludetype)
                .setModeFilter(mode)
                .setExcludeModeFilter(excludemode)
                .setDistributionFilter(dist)
                .setExcludeDistributionFilter(excludedist);

        Client client = ApplicationContextListener.searchSupport.client();
        Dispatcher dispatcher = ApplicationContextListener.dispatcher;

        return Response.ok().entity(dispatcher.execute(client, dispatcherRequest)).build();
    }

    @POST
    @Produces({"application/json;charset=UTF-8"})
    @Consumes("application/json")
    public Response post(@Context UriInfo uriInfo,
                         @FormParam("index") String index,
                         @FormParam("indextype") String indextype,
                         @FormParam("from") Integer from,
                          @FormParam("size") Integer size,
                          @FormParam("base") String base,
                          @FormParam("source") String source,
                          @FormParam("id") String identifier,
                          @FormParam("year") Integer year,
                          @FormParam("baseGroup") String baseGroup,
                          @FormParam("groupLimit") Integer groupLimit,
                          @FormParam("carrier") List<String> carrier,
                          @FormParam("excludecarrier") List<String> excludecarrier,
                          @FormParam("group") List<String> group,
                          @FormParam("excludegroup") List<String> excludegroup,
                          @FormParam("institution") List<String> institution,
                          @FormParam("excludeinstitution") List<String> excludeinstitution,
                          @FormParam("type") List<String> type,
                          @FormParam("excludetype") List<String> excludetype,
                          @FormParam("mode") List<String> mode,
                          @FormParam("excludemode") List<String> excludemode,
                          @FormParam("distribution") List<String> dist,
                          @FormParam("excludedistribution") List<String> excludedist,
                          @FormParam("style") String style,
                          InputStream in
    ) throws IOException {

        if (in != null) {
            Settings settings = Settings.settingsBuilder().loadFrom(new InputStreamReader(in, "UTF-8")).build();
            logger.info("got settings from POST body {}", settings.getAsMap());
        }

        DispatcherSettings dispatcherSettings = DispatcherSettings.Factory.getDispatcherSettings(baseGroup);

        DispatcherRequest dispatcherRequest = new DispatcherRequest()
                .setIndex(index)
                .setType(indextype)
                .setCompact(contains(style, "compact"))
                .setFrom(from)
                .setSize(size)
                .setBase(base)
                .setBaseGroup(baseGroup)
                .setSource(source)
                .setIdentifier(identifier)
                .setYear(year)
                .setGroupMap(contains(style, "region") ? null : dispatcherSettings.getServiceMap())
                .setGroupLimit(groupLimit)
                .setExpandGroups(true)
                .setCarrierFilter(carrier)
                .setExcludeCarrierFilter(excludecarrier)
                .setGroupFilter(group)
                .setExcludeGroupFilter(excludegroup)
                .setInstitutionFilter(institution)
                .setExcludeInstitutionFilter(excludeinstitution)
                .setTypeFilter(type)
                .setExcludeTypeFilter(excludetype)
                .setModeFilter(mode)
                .setExcludeModeFilter(excludemode)
                .setDistributionFilter(dist)
                .setExcludeDistributionFilter(excludedist);

        Client client = ApplicationContextListener.searchSupport.client();
        Dispatcher dispatcher = ApplicationContextListener.dispatcher;

        return Response.ok().entity(dispatcher.execute(client, dispatcherRequest)).build();
    }

    @GET
    @Path("/{group}")
    @Produces("application/json;charset=UTF-8")
    public Response groupGet(@PathParam("group") String group,
                           @QueryParam("base") String base,
                           @QueryParam("source") String source,
                           @QueryParam("id") String identifier,
                           @QueryParam("year") Integer year,
                           @QueryParam("style") String style,
                           @QueryParam("carrier") List<String> carrier
    ) throws IOException {
        return execute(base, group, source, identifier, year, style, carrier);
    }

    @POST
    @Path("/{group}")
    @Produces("application/json;charset=UTF-8")
    public Response groupPost(@PathParam("group") String group,
            @FormParam("base") String base,
            @FormParam("source") String source,
            @FormParam("id") String identifier,
            @FormParam("year") Integer year,
            @FormParam("style") String style,
            @FormParam("carrier") List<String> carrier
    ) throws IOException {
        return execute(base, group, source, identifier, year, style, carrier);
    }

    private Response execute(String base, String group, String source, String identifier, Integer year, String style,
                             List<String> carrier) throws IOException {

        DispatcherSettings dispatcherSettings = DispatcherSettings.Factory.getDispatcherSettings(group);

        style = style != null ? style : "";

        DispatcherRequest dispatcherRequest = new DispatcherRequest()
                .setCompact(contains(style, "compact"))
                .setBase(base)
                .setBaseGroup(group != null ? group.toUpperCase() : null)
                .setGroupMap(contains(style, "region") ? null : dispatcherSettings.getServiceMap())
                .setGroupLimit(contains(style, "unlimited") ? 0 : 10)
                .setExpandGroups(false)
                .setInstitutionCarrierFilter(dispatcherSettings.getServiceRestrictions())
                .setSource(source)
                .setIdentifier(identifier)
                .setYear(year)
                .setInstitutionMarker("priority", dispatcherSettings.getPriority())
                .setTypeFilter(style.contains("alltypes") ? null : Arrays.asList("interlibrary"))
                .setModeFilter(style.contains("allmodes") ? null : Arrays.asList("copy", "copy-loan"))
                .setCarrierFilter(carrier);

        Client client = ApplicationContextListener.searchSupport.client();
        Dispatcher dispatcher = ApplicationContextListener.dispatcher;

        return Response.ok().entity(dispatcher.execute(client, dispatcherRequest)).build();

    }

    private boolean contains(String s, CharSequence value) {
        return s != null && s.contains(value);
    }

}
