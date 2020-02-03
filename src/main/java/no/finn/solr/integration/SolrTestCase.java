package no.finn.solr.integration;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class SolrTestCase {
    protected static SolrTestServer solr;

    /**
     * Wires up an embedded solr server
     * @throws IOException if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    @BeforeAll
    public static void setupSolr() throws IOException, SolrServerException {
        solr = new SolrTestServer().withEmptyIndex();
    }

    /**
     * Stops the Solr server
     * @throws IOException if there is a communication error with the server
     */
    @AfterAll
    public static void teardownSolr() throws IOException {
        if (solr != null) {
            solr.shutdown();
        }
    }

    /**
     * Removes all documents from the solr index
     * @throws IOException if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    @BeforeEach
    public void delete_all_docs() throws IOException, SolrServerException {
        solr = solr.withEmptyIndex();
    }

    /**
     * @return the embedded solr server
     */
    public static SolrTestServer getSolr() {
        return solr;
    }
}
