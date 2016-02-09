package no.finn.solr.integration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.hamcrest.CoreMatchers;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SolrTestServer {
    private SolrClient client;
    private String defaultContentField = "body";
    private String groupField = null;
    private String search;
    private QueryResponse response;
    private String uniqueKeyField = "id";
    private SolrQuery solrQuery = new SolrQuery();

    private void configureSysProperties() {
        File solrFolder = findSolrFolder();
        String solrHomeAlternative = solrFolder.getAbsolutePath();
        String solrDataDir = new File(solrFolder.getParentFile(), "data").getAbsolutePath();
        String solrHome = System.getProperty("solr.solr.home", solrHomeAlternative);
        System.out.println("Running from: " + solrHome);
        System.setProperty("solr.solr.home", solrHome);
        System.setProperty("solr.data.dir", System.getProperty("solr.data.dir", solrDataDir));
    }

    private File findRootOfTests(File folder) {
        if ("target".equals(folder.getName()) || "build".equals(folder.getName())) {
            return folder;
        }
        return findRootOfTests(folder.getParentFile());
    }

    private File findSolrXml(File folder) {
        Optional<File> solrXml = FileUtils.listFiles(folder, new String[]{"xml"}, true)
            .stream()
            .filter(x -> x.getName().equals("solr.xml"))
            .findFirst();
        assert solrXml.isPresent();
        return solrXml.get();
    }

    private File findSolrFolder() {
        ClassLoader loader = SolrTestServer.class.getClassLoader();
        URL root = loader.getResource(".");
        String path = root.getPath();
        File classRoot = new File(path);
        File solrXml = findSolrXml(findRootOfTests(classRoot));
        System.out.println(solrXml);
        return solrXml.getParentFile();
    }

    /**
     * Wires up a Solr Server
     */
    public SolrTestServer() {
        configureSysProperties();
        String solrHome = System.getProperty("solr.solr.home");
        CoreContainer coreContainer = new CoreContainer(solrHome);
        coreContainer.load();
        String coreName = getCore(coreContainer);
        client = new EmbeddedSolrServer(coreContainer, coreName);
    }

    private String getCore(CoreContainer coreContainer) {
        Collection<String> allCoreNames = coreContainer.getAllCoreNames();
        assert allCoreNames.size() > 0;
        return allCoreNames.toArray(new String[0])[0];
    }

    /**
     * Sets up a normal Http based client
     *
     * @param baseUrl IP or servername of server
     * @param port    which port does your Solr server run on
     * @param core    Which core are you using
     */
    public SolrTestServer(String baseUrl, String port, String core) {
        client = new HttpSolrClient(baseUrl + ":" + port + "/solr" + "/" + core);
    }

    private boolean isGrouped() {
        return StringUtils.isNotBlank(groupField);
    }

    public void shutdown() throws IOException {
        client.close();
    }

    /**
     * Sets a parameter
     *
     * @param parameter name of the parameter
     * @param values    values of the parameter
     * @return The server currently under test
     */
    public SolrTestServer withParam(String parameter, String... values) {
        solrQuery.set(parameter, values);
        return this;
    }

    /**
     * Resets the SolrQuery
     *
     * @return The server currently under test
     */
    public SolrTestServer withEmptyParams() {
        solrQuery = new SolrQuery();
        return this;
    }

    /**
     * Empties the index
     *
     * @return The server currently under test
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public SolrTestServer withEmptyIndex() throws IOException, SolrServerException {
        client.deleteByQuery("*:*");
        client.commit();
        return this;
    }

    /**
     * Sets a new default field to add content into
     *
     * @param defaultField name of the field
     * @return The server currently under test
     */
    public SolrTestServer withDefaultContentField(String defaultField) {
        this.defaultContentField = defaultField;
        return this;
    }

    /**
     * Sets a field to group on
     * This will make the query use the solr group parameters
     *
     * @param groupField name of the field
     * @return The server currently under test
     */
    public SolrTestServer withGrouping(String groupField) {
        this.groupField = groupField;
        return this;
    }

    /**
     * Sets up highlighting
     * It sets hl=true
     * hl.field = [highlightedField]
     * hl.alternateField = [alternateField]
     *
     * @param highlightedField which field to highlight
     * @param alternateField   if no content can be found in highlightedField, use this instead
     * @return The server currently under test
     */
    public SolrTestServer withHighlighting(String highlightedField, String alternateField) {
        return withParam("hl", "true")
            .withParam("hl.fl", highlightedField)
            .withParam("hl.alternateField", alternateField);

    }

    /**
     * Updates the fl parameter
     *
     * @param fields Which fields to request from Solr
     * @return The server currently under test
     */
    public SolrTestServer withReturnedFields(String... fields) {
        return withParam("fl", StringUtils.join(fields, ","));
    }

    //
    // Indexing / adding docs
    //

    /**
     * Adds a single document to the index
     *
     * @param doc - document to add
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public void addDocument(SolrInputDocument doc) throws IOException, SolrServerException {
        client.add(doc);
        client.commit();
    }

    /**
     * Gets document from a document builder
     *
     * @param docBuilder - build to get document from
     * @return id of the document added to the index
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public Long addDocument(SolrInputDocumentBuilder docBuilder) throws IOException, SolrServerException {
        addDocument(docBuilder.getDoc());
        return docBuilder.getDocId();
    }

    /**
     * Adds a single string to the default content field
     *
     * @param content content to add
     * @return id of the document
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public Long addDocumentWith(String content) throws IOException, SolrServerException {
        SolrInputDocumentBuilder docBuilder = new SolrInputDocumentBuilder(defaultContentField).with(content);
        return addDocument(docBuilder);
    }

    /**
     * Adds a single string to field specified
     *
     * @param fieldName name of the field to add content to
     * @param content   content to add
     * @return id of the document
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public Long addDocumentWithField(String fieldName, String content) throws IOException, SolrServerException {
        SolrInputDocumentBuilder docBuilder = new SolrInputDocumentBuilder(fieldName).with(content);
        return addDocument(docBuilder);

    }

    /**
     * Adds a single object value to the field specified
     *
     * @param fieldName name of the field to add content to
     * @param content   content to add
     * @return id of the document
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public Long addDocumentWithField(String fieldName, Object content) throws IOException, SolrServerException {
        SolrInputDocumentBuilder docBuilder = new SolrInputDocumentBuilder(fieldName).with(content);
        return addDocument(docBuilder);
    }

    /**
     * Adds docContents to the default content field
     *
     * @param docContent content(s) to add
     * @return id(s) of the document(s) added
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public Long[] addDocumentsWith(String... docContent) throws IOException, SolrServerException {
        List<Long> docIds = new ArrayList<>();
        for (String content : docContent) {
            docIds.add(addDocumentWith(content));
        }
        return docIds.toArray(new Long[docIds.size()]);
    }

    /**
     * Adds docContents to the specified field name
     *
     * @param fieldName   name of the field to add contents to
     * @param docContents content(s) to add
     * @return id(s) of the document(s) added
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public Long[] addDocumentsWithField(String fieldName, String... docContents) throws IOException, SolrServerException {
        List<Long> docIds = new ArrayList<>();
        for (String docContent : docContents) {
            docIds.add(addDocumentWithField(fieldName, docContent));
        }
        return docIds.toArray(new Long[docIds.size()]);
    }

    /**
     * Convenience method to add more than one document at the time from several document builders
     *
     * @param documentBuilders - builder(s) to add to the index
     * @return id(s) of the document(s) added
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public Long[] addDocuments(SolrInputDocumentBuilder... documentBuilders) throws IOException, SolrServerException {
        List<Long> docIds = new ArrayList<>();
        for (SolrInputDocumentBuilder documentBuilder : documentBuilders) {
            docIds.add(addDocument(documentBuilder));
        }
        return docIds.toArray(new Long[docIds.size()]);
    }


    /**
     * Perform an update call against the extract endpoint
     *
     * @param file        file to send as an update request
     * @param contentType MIME content-type
     * @param params      - further params to send
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public void updateStreamRequest(File file, String contentType, Map<String, String> params) throws IOException, SolrServerException {
        ContentStreamUpdateRequest updateRequest = new ContentStreamUpdateRequest("/update/extract");
        for (Map.Entry<String, String> e : params.entrySet()) {
            updateRequest.setParam(e.getKey(), e.getValue());
        }
        updateRequest.addFile(file, contentType);
        client.request(updateRequest);
    }

    /**
     * Performs a search and asserts exactly one hit.
     *
     * @param search search to perform
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public void performSearchAndAssertOneHit(String search) throws IOException, SolrServerException {
        performSearchAndAssertNoOfHits(search, 1);
    }


    /**
     * Performs a search and checks that ids are present in the result.
     * Guarantees that only the expectedIds are present by verifying hit length equal to expectedIds
     *
     * @param search      search to perform
     * @param expectedIds ids of the documents expected to be found by the search
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public void performSearchAndAssertHits(String search, Long... expectedIds) throws IOException, SolrServerException {
        dismaxSearch(search);
        verifyHits(expectedIds.length);
        assertDocumentsInResult(expectedIds);
    }

    /**
     * Performs a search and checks number of hits
     *
     * @param search      search to perform
     * @param resultCount number of documents expected to be found by the search
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public void performSearchAndAssertNoOfHits(String search, long resultCount) throws IOException, SolrServerException {
        dismaxSearch(search);
        verifyHits(resultCount);
    }

    /**
     * Performs a search and verifies that it does not find anything in the index
     *
     * @param search search to perform
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public void performSearchAndAssertNoHits(String search) throws IOException, SolrServerException {
        performSearchAndAssertNoOfHits(search, 0L);
    }

    /**
     * Performs a search
     *
     * @param searchQuery query to perform
     * @return The raw QueryResponse from the solr server
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public QueryResponse search(String searchQuery) throws IOException, SolrServerException {
        search = searchQuery;
        withParam("q", StringUtils.defaultIfBlank(searchQuery, ""));
        search();
        return response;
    }

    /**
     * Performs a query of the form [field]:[query]
     *
     * @param field name of the field to search in
     * @param value expected value of the field
     * @return The raw QueryResponse from the solr server
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public QueryResponse search(String field, String value) throws IOException, SolrServerException {
        search = field + ":" + value;
        withParam("q", StringUtils.defaultIfBlank(field + ":" + value, ""));
        search();
        return response;
    }

    /**
     * Performs a search with the parameters currently set
     *
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public void search() throws IOException, SolrServerException {
        if (StringUtils.isEmpty(solrQuery.get("q"))) {
            withParam("hl.q", "*:*");
        }
        response = client.query(solrQuery);
    }

    /**
     * Performs a search using the dismax query handler
     *
     * @param query search to perform
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public void dismaxSearch(String query) throws IOException, SolrServerException {
        withParam("qt", "dismax");
        search(query);
    }

    //
    // Verify/asserts
    //

    /**
     * Verifies that ids come in the order expected. Used to check that sorts are working as expected
     *
     * @param sequence
     */
    public void verifySequenceOfHits(Long... sequence) {
        assertThat(sequence.length <= response.getResults().getNumFound(), CoreMatchers.is(true));
        int i = 0;
        for (long id : sequence) {
            String assertMessage = "Document " + i + " should have docId: " + id;

            assertThat(assertMessage,
                Long.parseLong((String) response.getResults().get(i).getFirstValue(uniqueKeyField)),
                CoreMatchers.is(id));
            i++;
        }
    }

    /**
     * Verifiies that we've got exactly one hit
     */
    public void verifyOneHit() {
        verifyHits(1L);
    }

    /**
     * Verifies `hits` number of hits
     *
     * @param hits amount of expected hits
     */
    public void verifyHits(long hits) {
        long matches = isGrouped() ? response.getGroupResponse().getValues().get(0).getMatches() : response.getResults().getNumFound();
        assertThat("Search for \"" + search + "\" should get " + hits + " results, but got: " + matches, matches, is(hits));
    }

    /**
     * Verifies that ngroups response is of expected value
     *
     * @param groups amount of groups expected
     */
    public void verifyNoOfGroups(long groups) {
        if (isGrouped()) {
            int matchGroups = response.getGroupResponse().getValues().get(0).getNGroups();
            assertThat("Search for \"" + search + "\" should get " + groups + " groups, but got: " + matchGroups,
                (long) matchGroups,
                is(groups));
        }
    }

    /**
     * JUnit assert that the document ids can be found in the result
     *
     * @param docIds ids expected
     */
    public void assertDocumentsInResult(Long... docIds) {
        for (Long docId : docIds) {
            assertTrue("DocId: [" + docId + "] should be in the result set",
                isGrouped() ? docIdIsInGroupedResponse(docId) : docIdIsInList(docId, response.getResults()));
        }
    }

    /**
     * One of the groups returned from the search contains the id
     *
     * @param docId id expected
     * @return whether or not the docid was contained in any of groups
     */
    private boolean docIdIsInGroupedResponse(Long docId) {
        List<SolrDocument> docs = new ArrayList<>();
        for (Group group : response.getGroupResponse().getValues().get(0).getValues()) {
            SolrDocumentList list = group.getResult();
            for (SolrDocument doc : list) {
                docs.add(doc);
            }
        }
        return docIdIsInList(docId, docs);
    }

    private boolean docIdIsInList(Long docId, List<SolrDocument> docs) {
        for (SolrDocument doc : docs) {
            Object id = doc.getFirstValue(uniqueKeyField);
            if (id == null) {
                throw new NullPointerException(uniqueKeyField + " not found in doc. you should probably call solr.withReturnedFields" +
                    "(\"id\")" +
                    " before calling the tests, " +
                    "" + "or add \"id\" to the fl-parameter in solrconfig.xml");
            }
            if (id.equals(String.valueOf(docId))) {
                return true;
            }
        }
        return false;
    }

    // Highlighting/snippets

    /**
     * Checks that highlighting works as expected
     *
     * @param search                   query
     * @param startHighlightingElement the starting element to look for
     * @param endHighlightingElement   the closing element to look for
     * @param snippets                 the snippets returned from solr as highlights
     */
    public void assertHighlight(String search, String startHighlightingElement, String endHighlightingElement, List<String> snippets) {
        String pattern = String.format("%s%s%s", startHighlightingElement, search, endHighlightingElement);
        for (String snippet : snippets) {
            if (snippet.contains(search)) {
                assertTrue(snippet.contains(pattern));
            }
        }
    }

    private List<String> getSnippets(Map<String, Map<String, List<String>>> highlighting) {
        List<String> snippets = new ArrayList<>();
        for (Map<String, List<String>> doc : highlighting.values()) {
            for (List<String> highlightedSnippets : doc.values()) {
                snippets.addAll(highlightedSnippets);
            }
        }
        return snippets;
    }

    /**
     * @return Highlight snippets
     */
    public List<String> getSnippets() {
        return getSnippets(response.getHighlighting());
    }

    public void assertTeaser(String query, String teaser, List<String> snippets) {
        for (String snippet : snippets) {
            if (query == null || !snippet.contains(query)) {
                assertThat(snippet, is(teaser));
            }
        }
    }

    //
    // Faceting
    //

    /**
     * JUnit assert that a facet query has the expected hit count
     *
     * @param facetName name of the facet
     * @param hitCount  expected hit count
     */
    public void assertFacetQueryHasHitCount(String facetName, int hitCount) {
        assertThat(response.getFacetQuery().get(facetName), is(hitCount));
    }

    /**
     * Convenience method to run raw Solr commands rather than using the helper methods
     *
     * @return The raw SolrClient
     */
    public SolrClient getClient() {
        return this.client;
    }

}
