package no.finn.solr.integration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchComponent;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SolrTestServer {
    private final File solrHome;
    private final Path dataDir;
    private final CoreContainer coreContainer;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<SolrCore> core;
    private final SolrClient client;
    private String defaultContentField = "body";
    private String groupField = null;
    private String search;
    private String uniqueKeyField = "id";
    private SolrQuery solrQuery = new SolrQuery();

    /**
     * Wires up a Solr Server
     */
    public SolrTestServer() {
        this.solrHome = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        this.dataDir = solrHome.toPath().resolve("data");
        File solrFolder = findSolrFolder();

        System.out.println();
        System.out.println("CREATING NEW SOLR TEST SERVER");
        System.out.println("SOLR FOLDER: " + solrFolder);
        System.out.println("SOLRHOME: " + this.solrHome);

        try {
            copyFilesTo(solrHome, solrFolder);
        } catch (IOException e) {
            System.out.println("IOException while copying to temporary folder");
        }
        System.setProperty("solr.solr.home", solrHome.getAbsolutePath());
        System.setProperty("solr.data.dir", System.getProperty("solr.data.dir", dataDir.toAbsolutePath().toString()));
        Path solrPath = solrHome.toPath().toAbsolutePath();

        this.coreContainer = CoreContainer.createAndLoad(solrPath);
        assertNoLoadErrors(coreContainer);
        core = getCore(coreContainer);
        client = core.map(EmbeddedSolrServer::new).orElse(null);
    }

    private File findRootOfTests(File folder) {
        if ("target".equals(folder.getName()) || "build".equals(folder.getName()) || "out".equals(folder.getName())) {
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

        File solrXml = ofNullable(root)
            .map(URL::getPath)
            .map(File::new)
            .map(this::findRootOfTests)
            .map(this::findSolrXml)
            .orElseThrow(() -> new IllegalStateException("Could not find Solr Home folder when looking from " + root));
        return solrXml.getParentFile();
    }


    private void assertNoLoadErrors(CoreContainer coreContainer) {
        Map<String, CoreContainer.CoreLoadFailure> coreInitFailures = coreContainer.getCoreInitFailures();
        for (CoreContainer.CoreLoadFailure coreLoadFailure : coreInitFailures.values()) {
            System.out.println("Error in loading core: " + coreLoadFailure.cd.getCollectionName());
            System.out.println("Instancedir set to: " + coreLoadFailure.cd.getInstanceDir());
            System.out.println("Error message: " + coreLoadFailure.exception.getMessage());
            System.out.println("Cause: " + coreLoadFailure.exception.getCause());
            if (coreLoadFailure.exception.getCause().getCause() != null) {
                System.out.println("Cause of Cause: " + coreLoadFailure.exception.getCause().getCause());
            }
        }
        assert coreInitFailures.size() == 0;
    }

    private void copyFilesTo(File solrHome, File solrFolder) throws IOException {
        FileUtils.copyDirectory(solrFolder, solrHome);
    }

    private Optional<SolrCore> getCore(CoreContainer coreContainer) {
        return coreContainer.getAllCoreNames()
                            .stream()
                            .findFirst()
                            .map(coreContainer::getCore);
    }

    private boolean isGrouped() {
        return StringUtils.isNotBlank(groupField);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void shutdown() throws IOException {
        client.close();
        core.filter(c -> !c.isClosed()).ifPresent(SolrCore::close);
        core.filter(c -> !c.isClosed()).ifPresent(SolrCore::close);
        ofNullable(coreContainer).filter(CoreContainer::isShutDown).ifPresent(CoreContainer::shutdown);
        solrHome.delete();
    }

    public Optional<SearchComponent> getSearchComponent(String componentName) {
        return core.map(c -> c.getSearchComponent(componentName));
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
        return docIds.toArray(new Long[0]);
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
        return docIds.toArray(new Long[0]);
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
        return docIds.toArray(new Long[0]);
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
        QueryResponse response = dismaxSearch(search);
        verifyHits(response, expectedIds.length);
        assertDocumentsInResult(response, expectedIds);
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
        verifyHits(dismaxSearch(search), resultCount);
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
        return search();
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
        return search();
    }

    /**
     * Performs a search with the parameters currently set
     *
     * @return The Solr QueryResponse
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public QueryResponse search() throws IOException, SolrServerException {
        if (StringUtils.isEmpty(solrQuery.get("q"))) {
            withParam("hl.q", "*:*");
        }
        return client.query(solrQuery);
    }

    /**
     * Performs a search using the dismax query handler
     *
     * @param query search to perform
     * @return The Solr QueryResponse
     * @throws IOException         if there is a communication error with the server
     * @throws SolrServerException if there is an error on the server
     */
    public QueryResponse dismaxSearch(String query) throws IOException, SolrServerException {
        withParam("qt", "dismax");
        return search(query);
    }

    //
    // Verify/asserts
    //

    /**
     * Verifies that ids come in the order expected. Used to check that sorts are working as expected
     *
     * @param response the result of the search
     * @param sequence the ids in the correct sequence
     */
    public void verifySequenceOfHits(QueryResponse response, Long... sequence) {
        assertEquals(sequence.length,
                     response.getResults().getNumFound(),
                     "getNumFound: " + response.getResults().getNumFound() + " not same length as expexted: " + sequence.length);
        int i = 0;
        for (long id : sequence) {
            String assertMessage = "Document " + i + " should have docId: " + id;

            assertEquals(id, Long.parseLong((String) response.getResults().get(i).getFirstValue(uniqueKeyField)), assertMessage);
            i++;
        }
    }

    /**
     * Verifiies that we've got exactly one hit
     *
     * @param response the result of the search
     */
    public void verifyOneHit(QueryResponse response) {
        verifyHits(response, 1L);
    }

    /**
     * Verifies `hits` number of hits
     *
     * @param response the result of the search
     * @param hits     amount of expected hits
     */
    public void verifyHits(QueryResponse response, long hits) {
        long matches = isGrouped() ? response.getGroupResponse().getValues().get(0).getMatches() : response.getResults().getNumFound();
        assertEquals(hits, matches, "Search for \"" + search + "\" should get " + hits + " results, but got: " + matches);
    }

    /**
     * Verifies that ngroups response is of expected value
     *
     * @param response the result of the search
     * @param groups   amount of groups expected
     */
    public void verifyNoOfGroups(QueryResponse response, long groups) {
        if (isGrouped()) {
            int matchGroups = response.getGroupResponse().getValues().get(0).getNGroups();
            assertEquals(groups, matchGroups, "Search for \"" + search + "\" should get " + groups + " groups, but got: " + matchGroups);
        }
    }

    /**
     * JUnit assert that the document ids can be found in the result
     *
     * @param response QueryResponse to check
     * @param docIds   ids expected
     */
    public void assertDocumentsInResult(QueryResponse response, Long... docIds) {
        for (Long docId : docIds) {
            assertTrue(isGrouped() ? docIdIsInGroupedResponse(response, docId) : docIdIsInList(docId, response.getResults()),
                       "DocId: [" + docId + "] should be in the result set");
        }
    }

    /**
     * One of the groups returned from the search contains the id
     *
     * @param response Query response to check
     * @param docId    id expected
     * @return whether or not the docid was contained in any of groups
     */
    private boolean docIdIsInGroupedResponse(QueryResponse response, Long docId) {
        List<SolrDocument> docs = new ArrayList<>();
        for (Group group : response.getGroupResponse().getValues().get(0).getValues()) {
            SolrDocumentList list = group.getResult();
            docs.addAll(list);
        }
        return docIdIsInList(docId, docs);
    }

    private boolean docIdIsInList(Long docId, List<SolrDocument> docs) {
        for (SolrDocument doc : docs) {
            Object id = doc.getFirstValue(uniqueKeyField);
            if (id == null) {
                throw new NullPointerException(uniqueKeyField + " not found in doc. you should probably call solr.withReturnedFields" +
                                                   "(\"" + uniqueKeyField + "\")" +
                                                   " before calling the tests, " +
                                                   "" + "or add \"+" + uniqueKeyField + "\" to the fl-parameter in solrconfig.xml");
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
    public void assertHighlight(String search,
                                String startHighlightingElement,
                                String endHighlightingElement,
                                List<String> snippets) {
        String pattern = String.format("%s%s%s", startHighlightingElement, search, endHighlightingElement);
        for (String snippet : snippets) {
            if (snippet.contains(search)) {
                assertTrue(snippet.contains(pattern), "missing " + pattern + " in: '" + snippet + "'");
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
     * @param response QueryResponse to fetch highlight snippets from
     * @return Highlight snippets
     */
    public List<String> getSnippets(QueryResponse response) {
        return getSnippets(response.getHighlighting());
    }

    public void assertTeaser(String query, String teaser, List<String> snippets) {
        for (String snippet : snippets) {
            if (query == null || !snippet.contains(query)) {
                assertEquals(teaser, snippet, "teaser: " + teaser + " != snippet: " + snippet);
            }
        }
    }

    //
    // Faceting
    //

    /**
     * JUnit assert that a facet query has the expected hit count
     *
     * @param response  where to locate the facet
     * @param facetName name of the facet
     * @param hitCount  expected hit count
     */
    public void assertFacetQueryHasHitCount(QueryResponse response, String facetName, int hitCount) {
        final int facetCount = response.getFacetQuery().get(facetName);
        assertEquals(hitCount, facetCount, "facetCount: " + facetCount + " != expected hitcount: " + hitCount);
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
