package agent.common.utils;

public class MetadataUtils {
    private static final String METADATA_FILE = ".metadata";

    public static boolean isMetadataFile(String path) {
        return path.endsWith(METADATA_FILE);
    }

    public static String getMetadataFile(String path) {
        return path + METADATA_FILE;
    }

}
