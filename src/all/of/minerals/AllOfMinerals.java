package all.of.minerals;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

public class AllOfMinerals extends Mod {
    public AllOfMinerals() {
        Log.info("Loading All of Minerals.");
        Events.on(ClientLoadEvent.class, e -> clientInit());
        serverAndClientInit();
    }

    public void clientInit() {
        Log.info("Loading client part All of Minerals.");
        ConfigMenu.hookModsMenu();
        if (Core.settings.getBool("all-of-minerals-first-time-hint", true)) {
            ConfigMenu.showFirstTimeHintDialog();
            Core.settings.put("all-of-minerals-first-time-hint", false);
        }
    }

    public void serverAndClientInit() {
        Log.info("Loading server and client part All of Minerals.");
    }

    @Override
    public void loadContent() {

    }
}
