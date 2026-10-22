package all.of.minerals;

import all.of.minerals.api.Bundle;
import all.of.minerals.api.BundleManager;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class ConfigMenu {
    private final static Bundle bundle = BundleManager.getBundle("all-of-minerals");

    /**
     * Перехватывает открытие вкладки модов и запускает поиск плашки.
     * Вызывается один раз при инициализации мода.
     */
    public static void hookModsMenu() {
        if (Vars.ui == null || Vars.ui.mods == null) return;

        try {
            var loadedMod = Vars.mods.getMod(AllOfMinerals.class);
            if (loadedMod == null) return;

            final String targetName = loadedMod.meta.displayName != null ?
                    loadedMod.meta.displayName.toLowerCase().trim() : "all of minerals";

            Vars.ui.mods.shown(() -> {
                Vars.ui.mods.update(() -> {
                    scanAndInject(Vars.ui.mods, targetName);
                });
            });
        } catch (Exception e) {
            Log.err("[AllOfMinerals] Ошибка при установке хука меню модов", e);
        }
    }

    /**
     * Рекурсивно сканирует UI, находит плашку мода и аккуратно встраивает шестерёнку в правую таблицу.
     */
    private static void scanAndInject(Element element, String targetName) {
        if (element == null) return;

        if (element instanceof Label label && label.getText() != null) {
            String currentLabelText = label.getText().toString().toLowerCase().trim();

            if (currentLabelText.contains(targetName)) {
                Group current = label.parent;

                // Поднимаемся строго до корневой кнопки плашки мода
                while (current != null && !(current instanceof Button)) {
                    current = current.parent;
                }

                if (current instanceof Button modButton) {
                    // Возвращаем ванильный способ: берем самую правую финальную таблицу плашки
                    Table rightTable = findRightButtonsTable(modButton);
                    if (rightTable != null) {
                        // Защита от дублирования
                        if (rightTable.find("minerals-settings-btn") != null) return;

                        // Создаем кнопку-шестерёнку ровно в старом стиле
                        ImageButton settingsBtn = new ImageButton(Icon.settings, Styles.clearNonei);
                        settingsBtn.name = "minerals-settings-btn";
                        settingsBtn.changed(ConfigMenu::showConfigMenu);

                        // Добавляем в правую таблицу с ванильными отступами
                        rightTable.add(settingsBtn).size(50f).right().padRight(1.5f);

                        // Сдвигаем крестик удаления в самый край, как это было изначально
                        int lastIndex = rightTable.getChildren().size - 1;
                        Element removed = rightTable.getChildren().remove(lastIndex);
                        rightTable.getChildren().insert(rightTable.getChildren().size, removed);

                        rightTable.invalidateHierarchy();
                    }
                }
            }
        }

        if (element instanceof Group group) {
            var children = group.getChildren();
            for (int i = 0; i < children.size; i++) {
                scanAndInject(children.get(i), targetName);
            }
        }
    }

    /**
     * Возвращает оригинальный поиск правой таблицы кнопок плашки мода.
     */
    private static Table findRightButtonsTable(Group modButton) {
        if (modButton.getChildren().size > 0) {
            Element lastChild = modButton.getChildren().peek();
            if (lastChild instanceof Table table) {
                return table;
            }
        }
        return null;
    }

    public static void showConfigMenu() {
        BaseDialog dialog = new BaseDialog("All of Minerals Config");
        dialog.addCloseButton();
        dialog.cont.margin(20f);
        dialog.cont.add(new Label("Настройки мода находятся в разработке...")).pad(10f);
        dialog.show();
    }

    public static void showFirstTimeHintDialog() {
        BaseDialog dialog = new BaseDialog(bundle.get("settings-hint-title"));
        dialog.cont.margin(24f);

        dialog.cont.add(bundle.get("settings-hint-text")).width(450f).wrap().center().row();
        dialog.buttons.button(bundle.get("text-ok"), dialog::hide).size(200f, 60f).pad(10f);
        dialog.show();
    }
}
