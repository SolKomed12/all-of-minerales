package all.of.minerals.api;

import all.of.minerals.AllOfMinerals;
import arc.Core;

public class Bundle {
    public final String nameSpace;
    public Bundle(String nameSpace) {
        this.nameSpace = nameSpace;
    }
    public String get(String key) {
        final String fullKey = nameSpace + "-" + key;
        if (Core.bundle.has(fullKey)) {
            return Core.bundle.get(fullKey);
        } else {
            return "???" + fullKey + "???";
        }
    }
    public String get(String key, Object... args) {
        final String fullKey = nameSpace + "-" + key;
        if (Core.bundle.has(fullKey)) {
            // Используем метод format вместо get для поддержки массивов аргументов
            return Core.bundle.format(fullKey, args);
        } else {
            return "???" + fullKey + "???";
        }
    }

}
