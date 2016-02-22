package no.finn.solr.integration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.getProperty;
import static org.junit.Assert.fail;

/**
 * SOLR does not parse XML (like it probably should).  It parses text line by line.  One consequency of this is that if the
 * shards and masterUrl-lines end in a lineshift, the last host in these lists will be used as a host, including the trailing
 * lineshift.  SOLR will fail to start in such cases.  To validate you config for this - create the follwing test.
 * <pre>
 * &commat;Test
 * public void validateSolrConfig() throws URISyntaxException, IOException {
 *    ValidateConfig.validateSolrConfig();
 * }
 * </pre>
 */
public class ValidateConfig {
    private static final String FILE = "/conf/solrconfig.xml";

    public static void validateSolrConfig(String solrCore) throws URISyntaxException, IOException {
        final String solrHome = getProperty("solr.solr.home", getSolrHome());
        final String solrConfigPath = solrHome + "/" + solrCore + FILE;
        System.out.println("Validating " + solrConfigPath);
        final Path path = Paths.get(solrConfigPath);
        long numLines = Files.lines(path).count();
        System.out.println("Num lines: " + numLines);
        if (numLines == 0) {
            fail("Not able to read " + solrConfigPath);
        }

        long numMatches = Files.lines(path).filter(ValidateConfig::shouldEndWithClosingStr).count();
        System.out.println("Num lines that should be closed with </str>: " + numMatches);

        final long numFailed = Files.lines(path)
                                    .filter(ValidateConfig::shouldEndWithClosingStr)
                                    .filter(ValidateConfig::doesNotEndInStr)
                                    .count();
        System.out.println("Num lines that do not end with </str> but should: " + numFailed);

        if (numFailed > 0L) {
            final List<String> collect = Files.lines(path)
                                              .filter(ValidateConfig::shouldEndWithClosingStr)
                                              .filter(ValidateConfig::doesNotEndInStr)
                                              .map(l -> l.trim() + "\n")
                                              .collect(Collectors.toList());
            fail("Failed for " + numFailed + " lines: " + collect);
        }

    }

    private static String getSolrHome() {
        ClassLoader loader = ValidateConfig.class.getClassLoader();
        URL root = loader.getResource(".");
        //noinspection ConstantConditions
        String rootPath = root.getPath().replaceAll("/test-classes", "");
        return rootPath + "solr";
    }

    private static boolean shouldEndWithClosingStr(String str) {
        return str.trim().startsWith("<str name=\"shards\"") || str.trim().startsWith("<str name=\"masterUrl\"");
    }

    private static boolean doesNotEndInStr(String str) {
        return !str.trim().endsWith("</str>");
    }

}
