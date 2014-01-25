package org.xbib.elasticsearch;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.testng.annotations.Test;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class AliasTest {

    private static final ESLogger logger = ESLoggerFactory.getLogger(AliasTest.class.getName());

    @Test
    public void testAlias() {
        Node node = null;
        try {
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name", "test")
                    .put("gateway.type", "none")
                    .build();
            node = nodeBuilder().settings(settings).local(true).node();
            Client client = node.client();

            // create index
            CreateIndexRequest indexRequest = new CreateIndexRequest("test");
            client.admin().indices().create(indexRequest).actionGet();

            // put alias
            IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
            String[] indices = new String[]{"test"};
            String[] aliases = new String[]{"test_alias"};
            IndicesAliasesRequest.AliasActions aliasAction = new IndicesAliasesRequest.AliasActions(AliasAction.Type.ADD, indices, aliases);
            indicesAliasesRequest.addAliasAction(aliasAction);
            client.admin().indices().aliases(indicesAliasesRequest).actionGet();

            // get alias
            GetAliasesRequest getAliasesRequest = new GetAliasesRequest(Strings.EMPTY_ARRAY);
            long t0 = System.nanoTime();
            GetAliasesResponse getAliasesResponse = client.admin().indices().getAliases(getAliasesRequest).actionGet();
            long t1 = System.nanoTime() - t0;

            logger.info("{} time(ms) = {}", getAliasesResponse.getAliases(), t1 / 1000000);

        } catch (ClusterBlockException | NoNodeAvailableException | IndexMissingException e) {
            logger.warn(e.getMessage());
        } finally {
            if (node !=null){
                node.stop();
                node.close();
            }
        }
    }

}
