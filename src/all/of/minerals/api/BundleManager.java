package all.of.minerals.api;

import java.util.HashMap;
import java.util.Map;

public class BundleManager {
    private static final Map<String, Bundle> listBundles = new HashMap<>();

    public static Bundle getBundle(String nameSpace) {
        final Bundle cachedBundle = listBundles.get(nameSpace);

        if (cachedBundle != null) {
            return cachedBundle;
        } else {
            final Bundle newBundle = new Bundle(nameSpace);
            listBundles.put(nameSpace, newBundle);
            return newBundle;
        }
    }

    public static boolean isBundle(String nameSpace) {
        return listBundles.containsKey(nameSpace);
    }
}
