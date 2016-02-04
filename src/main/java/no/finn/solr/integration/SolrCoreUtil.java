package no.finn.solr.integration;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

public class SolrCoreUtil {
    public static String getSolrCore(String solrHome, String solrCore) {
        return getProperty("solr.core", solrCore);
    }

    public static boolean coreExists(String solrHome, String coreName) {
        File coreHome = new File(solrHome, coreName);
        return coreHome.exists();
    }
    private static String getProperty(String propertyName, String fallbackCoreName) {
        String environmentVariable = propertyName.replaceAll("\\.", "_").toUpperCase(Locale.ROOT);
        return Optional.ofNullable(System.getenv(environmentVariable))
                .orElseGet(() -> System.getProperty(propertyName, fallbackCoreName));
    }
}
