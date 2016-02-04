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
        System.out.println("Num potential lines that could fail: " + numMatches);

        final long numFailed = Files.lines(path).filter(ValidateConfig::shouldEndWithClosingStr).filter(ValidateConfig::doesNotEndInStr).count();
        System.out.println("Num failed: " + numFailed);
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
