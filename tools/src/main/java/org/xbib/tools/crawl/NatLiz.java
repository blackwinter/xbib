package org.xbib.tools.crawl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.io.Request;
import org.xbib.io.Session;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.HttpResponse;
import org.xbib.io.http.HttpResponseListener;
import org.xbib.io.http.netty.NettyHttpSession;
import org.xbib.iri.IRI;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.rdf.memory.MemoryTriple;
import org.xbib.tools.Converter;
import org.xbib.util.URIUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;
import static org.xbib.rdf.RdfContentFactory.ntripleBuilder;

public class NatLiz extends Converter {

    private final static Logger logger = LogManager.getLogger(NatLiz.class);

    @Override
    public String getName() {
        return "natliz-crawler";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return NatLiz::new;
    }

    @Override
    public void process(URI uri) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // the list of ISIL we want to traverse. If there is no such file, we would have to
        // use findInstitutions()
        InputStream in = getClass().getResourceAsStream("nlz-sigel-isil.json");
        List<Map<String,Object>> members = mapper.readValue(in, List.class);
        String cookie = login();
        if (cookie.isEmpty()) {
            throw new IOException("not authorized, no cookie found");
        }
        Set<Triple> triples = new TreeSet<>();
        Map<String, Object> licenses = new HashMap<String, Object>();
        for (Map<String,Object> member : members) {
            String memberid = (String) member.get("member");
            Object o = member.get("isil");
            if (!(o instanceof Collection)) {
                o = Collections.singletonList(o);
            }
            Collection<Object> isils = (Collection<Object>) o;
            findLicenses(cookie, memberid, isils, licenses, triples);
        }
        writeLicenses(licenses);
        writeTriples(triples);
    }

    private String login() throws Exception {
        StringBuilder cookie = new StringBuilder();
        final NettyHttpSession session = new NettyHttpSession();
        try {
            session.open(Session.Mode.CONTROL); // do not follow redirect
            HttpRequest request = session.newRequest()
                    .setMethod("POST")
                    .setURL(URI.create("https://www.nationallizenzen.de/anmeldung/inform_registration"))
                    .addHeader("Origin", "https://www.nationallizenzen.de")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .addHeader("Cache-Control", "max-age=0")
                    .addHeader("Referer", "https://www.nationallizenzen.de/anmeldung/inform_registration")
                    .addHeader("Connection", "keep-alive")
                    .setBody("came_from=https%3A%2F%2Fwww.nationallizenzen.de%2Fanmeldung%2Finform_registration&js_enabled=0&cookies_enabled=&login_name=&pwd_empty=0&__ac_name="+settings.get("username")+"&__ac_password="+settings.get("password")+"&form.button.login=login&form.submitted=1");
            HttpResponseListener listener = new HttpResponseListener() {
                @Override
                public void receivedResponse(HttpResponse response) throws IOException {
                    logger.info("login: status code={} headers={}",
                            response.getStatusCode(), response.getHeaders());
                    cookie.append(response.getHeaders().get("Set-Cookie").get(0));
                }

                @Override
                public void onConnect(Request request) throws IOException {

                }

                @Override
                public void onDisconnect(Request request) throws IOException {
                    session.close();
                }

                @Override
                public void onReceive(Request request, CharSequence message) throws IOException {
                }

                @Override
                public void onError(Request request, CharSequence errorMessage) throws IOException {
                    logger.error("{}", errorMessage);
                }
            };
            request.prepare().execute(listener).waitFor();
        } finally {
            session.close();
        }
        return cookie.toString();
    }

    private Collection<String> findInstitutions(String cookie) throws Exception {
        URI uri = URI.create("https://www.nationallizenzen.de/Members/institutionen");
        int n = 0;
        int count;
        Set<String> members = new LinkedHashSet<>();
        do {
            final NettyHttpSession session = new NettyHttpSession();
            try {
                session.open(Session.Mode.READ);
                HttpRequest request = session.newRequest()
                        .setMethod("GET")
                        .setURL(uri)
                        .addParameter("b_start:int", Integer.toString(n))
                        .addHeader("Cookie", "ZopeId=\"67065706A6-9xahwvd0\"; " + cookie)
                        .addHeader("Accept", "text/html, */*; q=0.01")
                        .addHeader("Connection", "keep-alive");
                StringBuilder content = new StringBuilder();
                HttpResponseListener listener = new HttpResponseListener() {
                    @Override
                    public void receivedResponse(HttpResponse response) throws IOException {
                        logger.info("status code={}", response.getStatusCode());
                    }

                    @Override
                    public void onConnect(Request request) throws IOException {

                    }

                    @Override
                    public void onDisconnect(Request request) throws IOException {
                        session.close();
                    }

                    @Override
                    public void onReceive(Request request, CharSequence message) throws IOException {
                        content.append(message);
                    }

                    @Override
                    public void onError(Request request, CharSequence errorMessage) throws IOException {
                        logger.error("{}", errorMessage);

                    }
                };
                request.prepare().execute(listener).waitFor();
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
                logger.info("n={} count={} members={} next={}", n, count, members.size(), uri);
            } finally {
                session.close();
            }
        } while (count > 0);
        return members;
    }

    private void findSigel(String cookie, String member) throws Exception {
        final NettyHttpSession session = new NettyHttpSession();
        try {
            session.open(Session.Mode.READ);
            HttpRequest request = session.newRequest()
                    .setMethod("GET")
                    .setURL(URI.create("https://www.nationallizenzen.de/Members/" + member))
                    .addHeader("Cookie", "ZopeId=\"67065706A6-9xahwvd0\"; " + cookie)
                    .addHeader("Accept", "text/html, */*; q=0.01")
                    .addHeader("Connection", "keep-alive");
            StringBuilder content = new StringBuilder();
            HttpResponseListener listener = new HttpResponseListener() {
                @Override
                public void receivedResponse(HttpResponse response) throws IOException {
                    logger.info("status code={}", response.getStatusCode());
                }

                @Override
                public void onConnect(Request request) throws IOException {

                }

                @Override
                public void onDisconnect(Request request) throws IOException {
                    session.close();
                }

                @Override
                public void onReceive(Request request, CharSequence message) throws IOException {
                    content.append(message);
                }

                @Override
                public void onError(Request request, CharSequence errorMessage) throws IOException {
                    logger.error("{}", errorMessage);
                }
            };
            request.prepare().execute(listener).waitFor();
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
        } finally {
            session.close();
        }
    }

    private void findLicenses(String cookie, String member, Collection<Object> isils,
                              Map<String,Object> licenses, Set<Triple> triples) throws Exception {
        final NettyHttpSession session = new NettyHttpSession();
        try {
            session.open(Session.Mode.READ);
            for (String license: new String[] {"NLLicence", "NLOptLicence"}) {
                HttpRequest request = session.newRequest()
                        .setMethod("GET")
                        .setURL(URI.create("https://www.nationallizenzen.de/Members/" + member + "/nl3_inst_get_licences"))
                        .addParameter("base_url", "nl3_inst_get_licences")
                        .addParameter("ltype", license)
                        .addParameter("state", "authorized")
                        .addHeader("Cookie", "ZopeId=\"67065706A6-9xahwvd0\"; " + cookie)
                        .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        .addHeader("Connection", "keep-alive");
                StringBuilder content = new StringBuilder();
                AtomicInteger status = new AtomicInteger();
                HttpResponseListener listener = new HttpResponseListener() {
                    @Override
                    public void receivedResponse(HttpResponse response) throws IOException {
                        logger.info("status code={}", response.getStatusCode());
                        status.set(response.getStatusCode());
                    }

                    @Override
                    public void onConnect(Request request) throws IOException {

                    }

                    @Override
                    public void onDisconnect(Request request) throws IOException {
                        session.close();
                    }

                    @Override
                    public void onReceive(Request request, CharSequence message) throws IOException {
                        content.append(message);
                    }

                    @Override
                    public void onError(Request request, CharSequence errorMessage) throws IOException {
                        logger.error("{}", errorMessage);
                    }
                };
                request.prepare().execute(listener).waitFor();
                if (status.get() == 200) {
                    logger.info("{}: {} bytes", license, content.toString().length());
                    //writer.write(content.toString());
                    //writer.write("\n");
                    // it might not be JSON we receive
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        List<Map<String, Object>> lics = mapper.readValue(content.toString(), List.class);
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
                                downloadMetadataArchive(cookie, url, zdbisil + ".zip");
                            }
                            Map<String, Object> map = new HashMap<>();
                            String timestamp = convertFromDate((String) lic.get("modification_date"));
                            map.put("lastmodified", timestamp);
                            map.put("isil", isils);
                            List<Map<String, Object>> list = licenses.containsKey(zdbisil) ?
                                    (List<Map<String, Object>>) licenses.get(zdbisil) : new LinkedList<>();
                            list.add(map);
                            licenses.put(zdbisil, list);
                            // generate triples
                            Resource subject = new MemoryResource().id(IRI.create("http://xbib.info/isil/" + zdbisil));
                            triples.add(new MemoryTriple(subject, IRI.create("xbib:topic"), new MemoryLiteral(zdbisil)));
                            triples.add(new MemoryTriple(subject, IRI.create("xbib:name"), new MemoryLiteral(name)));
                            IRI predicate = IRI.create("xbib:member");
                            for (Object isil : isils) {
                                MemoryTriple triple = new MemoryTriple(subject, predicate, new MemoryLiteral(isil));
                                triples.add(triple);
                            }
                            subject = new MemoryResource().id(IRI.create("http://xbib.info/isil/" + zdbisil + "#" + timestamp));
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
                } else {
                    logger.error("page returned status {}, skipped", status.get());
                }
            }
        } finally {
            session.close();
        }
    }

    private void downloadMetadataArchive(String cookie, String url, String target) throws Exception {
        // already exists?
        File file = new File(target);
        if (file.exists()) {
            logger.info("already exists. skipping: {}", target);
            return;
        }
        // parse URL
        final URI uri = URI.create(url);
        Map<String,String> params = URIUtil.parseQueryString(uri, Charset.forName("UTF-8"));
        String lname = params.get("lname");
        String puid = params.get("puid");
        String mid = params.get("mid");
        final FileOutputStream out = new FileOutputStream(target);
        final NettyHttpSession session = new NettyHttpSession();
        try {
            // 3 hour max download time before timeout (file size may be up to some GB)
            session.open(Session.Mode.READ, settings.getAsInt("timeout", 3 * 3600 * 1000));
            HttpRequest request = session.newRequest()
                    .setMethod("GET")
                    .setURL(URI.create("https://www.nationallizenzen.de" + uri.getPath()))
                    .addParameter("lname", lname)
                    .addParameter("puid", puid)
                    .addParameter("mid", mid)
                    .addHeader("Cookie", "ZopeId=\"67065706A6-9xahwvd0\"; " + cookie)
                    .addHeader("Accept", "*/*; q=0.01")
                    .addHeader("Connection", "keep-alive");
            logger.info("start: download from {} to {}", url, target);
            HttpResponseListener listener = new HttpResponseListener() {
                @Override
                public void receivedResponse(HttpResponse response) throws IOException {
                    logger.info("status code={} for url={}", response.getStatusCode(), uri);
                }

                @Override
                public void onConnect(Request request) throws IOException {
                }

                @Override
                public void onDisconnect(Request request) throws IOException {
                    session.close();
                }

                @Override
                public void onReceive(Request request, CharSequence message) throws IOException {
                    // not called, we write to out
                }

                @Override
                public void onError(Request request, CharSequence errorMessage) throws IOException {
                    logger.error("{}", errorMessage);
                }
            };
            request.prepare().setOutputStream(out).execute(listener).waitFor();
        } finally {
            session.close();
            out.close();
            file = new File(target);
            logger.info("done: download of {}, file length = {}", url, file.length());
        }
    }


    private void writeLicenses(Map<String,Object> licenses) throws IOException {
        // Java object
        File file = new File("nlz-licenses.map");
        FileWriter writer = new FileWriter(file);
        writer.write(licenses.toString());
        writer.close();
        // JSON
        XContentBuilder builder = jsonBuilder();
        builder.map(licenses);
        file = new File("nlz-licenses.json");
        writer = new FileWriter(file);
        writer.write(builder.string());
        writer.close();
    }

    private void writeTriples(Set<Triple> triples) throws IOException {
        File file = new File("nlz-triples.nt");
        OutputStream out = new FileOutputStream(file);
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
        }
        return year + "-" + month + "-" + day;
    }
}