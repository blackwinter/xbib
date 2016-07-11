package org.xbib.tools.feed.elasticsearch.natliz;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.rdf.memory.MemoryTriple;
import org.xbib.service.client.Clients;
import org.xbib.service.client.http.SimpleHttpClient;
import org.xbib.service.client.http.SimpleHttpRequest;
import org.xbib.service.client.http.SimpleHttpRequestBuilder;
import org.xbib.service.client.http.SimpleHttpResponse;
import org.xbib.service.client.invocation.RemoteInvokerFactory;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.IndexDefinition;
import org.xbib.util.URIBuilder;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xbib.common.xcontent.XContentService.jsonBuilder;
import static org.xbib.rdf.RdfContentFactory.ntripleBuilder;

public class NatLizCrawler extends Feeder {

    private final static Logger logger = LogManager.getLogger(NatLizCrawler.class);

    private final static RemoteInvokerFactory remoteInvokerFactory = RemoteInvokerFactory.DEFAULT;

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new NatLizCrawler().setPipeline(p);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(URI uri) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // the list of ISIL we want to traverse. If there is no such file, we would have to
        // use findInstitutions()
        try (InputStream in = getClass().getResourceAsStream("nlz-sigel-isil.json")) {
            List<Map<String, Object>> members = mapper.readValue(in, List.class);
            String cookie = login(uri.toString());
            if (cookie.isEmpty()) {
                throw new IOException("not authorized, no cookie found");
            }
            Set<Triple> triples = new TreeSet<>();
            Map<String, Object> licenses = new HashMap<>();
            for (Map<String, Object> member : members) {
                String memberid = (String) member.get("member");
                Object o = member.get("isil");
                if (!(o instanceof Collection)) {
                    o = Collections.singletonList(o);
                }
                Collection<Object> isils = (Collection<Object>) o;
                findLicenses(uri.toString(), cookie, memberid, isils, licenses, triples);
            }
            writeLicenses(licenses);
            writeTriples(triples);
        }
    }

    private String login(String httpUrl) throws Exception {
        StringBuilder cookie = new StringBuilder();
        SimpleHttpClient client = Clients.newClient(remoteInvokerFactory, httpUrl,
                SimpleHttpClient.class);
        SimpleHttpRequest request = SimpleHttpRequestBuilder
                .forPost("/anmeldung/inform_registration")
                .content("came_from=https%3A%2F%2Fwww.nationallizenzen.de%2Fanmeldung%2Finform_registration&js_enabled=0&cookies_enabled=&login_name=&pwd_empty=0&__ac_name="+settings.get("username")+"&__ac_password="+settings.get("password")+"&form.button.login=login&form.submitted=1", StandardCharsets.UTF_8)
                .header(HttpHeaderNames.ORIGIN, "https://www.nationallizenzen.de")
                .header(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaderNames.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header(HttpHeaderNames.CACHE_CONTROL, "max-age=0")
                .header(HttpHeaderNames.REFERER, "https://www.nationallizenzen.de/anmeldung/inform_registration")
                .header(HttpHeaderNames.CONNECTION, "keep-alive")
                .build();
        SimpleHttpResponse response = client.execute(request).get();
        logger.info("login: status code={} headers={}",
                response.status().codeAsText(), response.headers());

        cookie.append(response.headers().get("Set-Cookie"));
        return cookie.toString();
    }

    private Collection<String> findInstitutions(String httpUrl, String cookie) throws Exception {
        int n = 0;
        int count;
        Set<String> members = new LinkedHashSet<>();
        SimpleHttpClient client = Clients.newClient(remoteInvokerFactory, httpUrl,
                SimpleHttpClient.class);
        do {
            SimpleHttpRequest request = SimpleHttpRequestBuilder
                    .forGet("/Members/institutionen?b_start:int=" + Integer.toString(n) )
                    .header(HttpHeaderNames.COOKIE, "ZopeId=\"67065706A6-9xahwvd0\"; " + cookie)
                    .header(HttpHeaderNames.ACCEPT, "text/html, */*; q=0.01")
                    .header(HttpHeaderNames.CONNECTION, "keep-alive")
                    .build();
            SimpleHttpResponse response = client.execute(request).get();
            logger.info("status code={}", response.status().codeAsText());
            String content = new String(response.content(), StandardCharsets.UTF_8);
            logger.info("got content length = {}", content.length());
            Pattern pattern = Pattern.compile("<a href=\".*?Members/(WIB.*?)\"", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            count = 0;
            while (matcher.find()) {
                String s = matcher.group(1);
                logger.info("found {}", s);
                members.add(s);
                count++;
            }
            n += 50;
            logger.info("n={} count={} members={}", n, count, members.size());
        } while (count > 0);
        return members;
    }

    private void findSigel(String httpUrl, String cookie, String member) throws Exception {
        SimpleHttpClient client = Clients.newClient(remoteInvokerFactory, httpUrl,
                SimpleHttpClient.class);
        SimpleHttpRequest request = SimpleHttpRequestBuilder
                .forGet("/Members/" + member)
                .header(HttpHeaderNames.COOKIE, "ZopeId=\"67065706A6-9xahwvd0\"; " + cookie)
                .header(HttpHeaderNames.ACCEPT, "text/html, */*; q=0.01")
                .header(HttpHeaderNames.CONNECTION, "keep-alive")
                .build();
        SimpleHttpResponse response = client.execute(request).get();
        logger.info("status code={}", response.status().codeAsText());
        String content = new String(response.content(), StandardCharsets.UTF_8);
        /*Pattern pattern = Pattern.compile("parent\\-fieldname\\-title.*?>\\s*(.*?)\\s*<", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        String name = "";
        if (matcher.find()) {
            name = matcher.group(1);
        }
        pattern = Pattern.compile("parent\\-fieldname\\-sigel.*?>\\s*(.*?)\\s*<", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        matcher = pattern.matcher(content);
        String sigel = "";
        if (matcher.find()) {
            sigel = matcher.group(1);
        }*/
        //String s = String.format("{\"member\":\"%s\", \"name\":\"%s\", \"sigel\":\"%s\"}", member, name, sigel);
        //logger.info("{}", s);
        //writer.write(s);
        //writer.write("\n");
    }

    private void findLicenses(String httpUrl, String cookie, String member, Collection<Object> isils,
                              Map<String,Object> licenses, Set<Triple> triples) throws Exception {
        SimpleHttpClient client = Clients.newClient(remoteInvokerFactory, httpUrl,
                SimpleHttpClient.class);
        for (String license: new String[] {"NLLicence", "NLOptLicence"}) {
            String params = "base_url=nl3_inst_get_licences&ltype=" + license + "&state=authorized";
            SimpleHttpRequest request = SimpleHttpRequestBuilder
                    .forGet("/Members/" + member + "/nl3_inst_get_licences?" + params)
                    .header(HttpHeaderNames.COOKIE, "ZopeId=\"67065706A6-9xahwvd0\"; " + cookie)
                    .header(HttpHeaderNames.ACCEPT, "application/json, text/javascript, */*; q=0.01")
                    .header(HttpHeaderNames.CONNECTION, "keep-alive")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            SimpleHttpResponse response = client.execute(request).get();
            logger.info("status code={}", response.status().codeAsText());
            if (response.status().code() != 200) {
                continue;
            }
            String content = new String(response.content(), StandardCharsets.UTF_8);
            logger.info("{}: {} bytes", license, content.length());
            //writer.write(content.toString());
            //writer.write("\n");
            // it might not be JSON we receive
            ObjectMapper mapper = new ObjectMapper();
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> lics = mapper.readValue(content, List.class);
                for (Map<String, Object> lic : lics) {
                    // Name
                    String name = (String) lic.get("Title");
                    // ZDB-ISIL
                    String zdbisil = (String) lic.get("zseal");
                    if (zdbisil == null || zdbisil.isEmpty()) {
                        logger.info("ZDB-ISIL not found in {}", lic);
                        // fix missing ZDB-ISIL
                        if ("Walter de Gruyter Online-Zeitschriften (Opt-In, DFG-geförderte Allianz-Lizenz)".equals(name)) {
                            zdbisil = "ZDB-1-LLH";
                        } else {
                            continue;
                        }
                    }
                    // download metadata
                    String url = (String) lic.get("vmetadata");
                    if (settings.getAsBoolean("downloadmetadata", false) && url != null && !url.isEmpty()) {
                        downloadMetadataArchive(httpUrl, cookie, url, zdbisil + ".zip");
                    }
                    Map<String, Object> map = new HashMap<>();
                    String timestamp = convertFromDate((String) lic.get("modification_date"));
                    map.put("lastmodified", timestamp);
                    map.put("isil", isils);
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> list = licenses.containsKey(zdbisil) ?
                            (List<Map<String, Object>>) licenses.get(zdbisil) : new LinkedList<>();
                    list.add(map);
                    licenses.put(zdbisil, list);
                    // generate triples
                    Resource subject = new MemoryResource(IRI.create("http://xbib.info/isil/" + zdbisil));
                    triples.add(new MemoryTriple(subject, IRI.create("xbib:topic"), new MemoryLiteral(zdbisil)));
                    triples.add(new MemoryTriple(subject, IRI.create("xbib:name"), new MemoryLiteral(name)));
                    IRI predicate = IRI.create("xbib:member");
                    for (Object isil : isils) {
                        MemoryTriple triple = new MemoryTriple(subject, predicate, new MemoryLiteral(isil));
                        triples.add(triple);
                    }
                    subject = new MemoryResource(IRI.create("http://xbib.info/isil/" + zdbisil + "#" + timestamp));
                    triples.add(new MemoryTriple(subject, IRI.create("xbib:topic"), new MemoryLiteral(zdbisil)));
                    triples.add(new MemoryTriple(subject, IRI.create("xbib:timestamp"), new MemoryLiteral(timestamp)));
                    if (url != null && !url.isEmpty()) {
                        triples.add(new MemoryTriple(subject, IRI.create("xbib:metadata"), IRI.create(url)));
                    }
                    predicate = IRI.create("xbib:subscribe");
                    for (Object isil : isils) {
                        MemoryTriple triple = new MemoryTriple(subject, predicate, new MemoryLiteral(isil));
                        triples.add(triple);
                    }
                }
            } catch (Exception e) {
                logger.warn("can't parse JSON response for member {}", member);
            }
        }
    }

    private void downloadMetadataArchive(String httpUrl, String cookie, String url, String target) throws Exception {
        // already exists?
        Path path = Paths.get(target);
        if (path.toFile().exists()) {
            logger.info("already exists. skipping: {}", target);
            return;
        }
        // parse URL
        final URI uri = URI.create(url);
        Map<String,String> params = URIBuilder.parseQueryString(uri,StandardCharsets.UTF_8);
        String lname = params.get("lname");
        String puid = params.get("puid");
        String mid = params.get("mid");

        SimpleHttpClient client = Clients.newClient(remoteInvokerFactory, httpUrl,
                SimpleHttpClient.class);
        String paramstr = "lname=" + lname + "&puid=" + puid + "&mid=" + mid;
        SimpleHttpRequest request = SimpleHttpRequestBuilder
                .forGet(uri.getPath() + "?" + paramstr)
                .header(HttpHeaderNames.COOKIE, "ZopeId=\"67065706A6-9xahwvd0\"; " + cookie)
                .header(HttpHeaderNames.ACCEPT, "*/*; q=0.01")
                .header(HttpHeaderNames.CONNECTION, "keep-alive")
                .header("X-Requested-With", "XMLHttpRequest")
                .build();
        SimpleHttpResponse response = client.execute(request).get();
        logger.info("status code={}", response.status().codeAsText());
        try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            out.write(response.content());
        } finally {
            logger.info("done: download of {}, file length = {}", url, path.toFile().length());
        }
    }

    private void writeLicenses(Map<String,Object> licenses) throws IOException {
        // Java object
        if (settings.getAsBoolean("mapfile", true)) {
            Path path = Paths.get("nlz-licenses.map");
            try (Writer fileWriter = new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE),
                    StandardCharsets.UTF_8)) {
                fileWriter.write(licenses.toString());
            }
        }
        // JSON file
        if (settings.getAsBoolean("jsonfile", true)) {
            XContentBuilder builder = jsonBuilder();
            builder.map(licenses);
            Path path = Paths.get("nlz-licenses.json");
            try (Writer fileWriter = new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE),
                    StandardCharsets.UTF_8)) {
                fileWriter.write(builder.string());
            }
        }
        // ES
        IndexDefinition indexDefinition = indexDefinitionMap.get("nlz");
        if (indexDefinition != null) {
            for (Map.Entry<String,Object> entry : licenses.entrySet()) {
                String key = entry.getKey();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> value = (List<Map<String, Object>>)entry.getValue();
                XContentBuilder builder = jsonBuilder();
                List<String> isils = new LinkedList<>();
                for (Map<String,Object> map : value) {
                    isils.addAll((Collection<? extends String>) map.get("isil"));
                }
                builder.startObject().array("isil", isils).endObject();
                ingest.index(indexDefinition.getConcreteIndex(), indexDefinition.getType(), key, builder.string());
            }
            if (indexDefinition.getTimeWindow() != null) {
                logger.info("switching index {}", indexDefinition.getIndex());
                elasticsearchOutput.switchIndex(ingest, indexDefinition, Collections.singletonList(indexDefinition.getIndex()));
                logger.info("performing retention policy for index {}", indexDefinition.getIndex());
                elasticsearchOutput.retention(ingest, indexDefinition);
            }
        }
    }

    private void writeTriples(Set<Triple> triples) throws IOException {
        Path path = Paths.get("nlz-triples.nt");
        OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE);
        RdfContentBuilder builder = ntripleBuilder(out);
        for (Triple triple : triples) {
            builder.receive(triple);
        }
        out.close();
    }

    private String convertFromDate(String s) {
        int pos = s.indexOf(" ");
        String monat = pos > 0 ? s.substring(0, pos) : s;
        s = pos > 0 ? s.substring(pos+1) : s;
        pos = s.indexOf(", ");
        String day = pos > 0 ? s.substring(0, pos) : s;
        s = pos > 0 ? s.substring(pos+2) : s;
        String year = s;
        String month = null;
        switch (monat) {
            case "Jan":
                month = "01";
                break;
            case "Feb":
                month = "02";
                break;
            case "Mär":
            case "M\\u00e4r":
                month = "03";
                break;
            case "Apr":
                month = "04";
                break;
            case "Mai":
                month = "05";
                break;
            case "Jun":
                month = "06";
                break;
            case "Jul":
                month = "07";
                break;
            case "Aug":
                month = "08";
                break;
            case "Sep":
                month = "09";
                break;
            case "Okt":
                month = "10";
                break;
            case "Nov":
                month = "11";
                break;
            case "Dez":
                month = "12";
                break;
            default:
                break;
        }
        return year + "-" + month + "-" + day;
    }
}