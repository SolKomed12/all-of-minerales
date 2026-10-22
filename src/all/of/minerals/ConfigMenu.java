package all.of.minerals;

import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.mod.Mods.LoadedMod;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class ConfigMenu {

    /**
     * Перехватывает открытие вкладки модов и ищет плашку по точному displayName нашего мода.
     * Вызывается один раз при инициализации мода.
     */
    public static void hookModsMenu() {
        if (Vars.ui == null || Vars.ui.mods == null) return;

        // Получаем метаданные мода напрямую через его класс, чтобы узнать точное имя в UI
        LoadedMod mod = Vars.mods.getMod(AllOfMinerals.class);
        if (mod == null) return;

        final String modName = mod.meta.displayName;

        // Караулим открытие меню модов для вставки кнопки настроек
        Vars.ui.mods.shown(() -> {
            Table mainTable = Vars.ui.mods.cont;
            if (mainTable == null) return;

            mainTable.update(() -> injectSettingsButton(mainTable, modName));
        });
    }

    /**
     * Сканирует интерфейс, ориентируясь на точное имя мода, полученное из его метаданных.
     */
    private static void injectSettingsButton(Element element, String modName) {
        if (element == null) return;

        // Ищем текстовую метку, которая один в один совпадает с именем нашего мода
        if (element instanceof Label label && label.getText() != null) {
            if (modName.equals(label.getText().toString())) {
                Group current = label.parent;

                // Поднимаемся до главного контейнера плашки мода
                while (current != null && !(current instanceof Button)) {
                    current = current.parent;
                }

                if (current instanceof Button modButton) {
                    Table rightButtonsTable = findRightButtonsTable(modButton);
                    if (rightButtonsTable != null) {
                        if (rightButtonsTable.find("minerales-settings-btn") != null) return;

                        // Создаем кнопку-шестерёнку
                        ImageButton settingsBtn = new ImageButton(Icon.settings, Styles.clearNonei);
                        settingsBtn.name = "minerales-settings-btn";
                        settingsBtn.changed(ConfigMenu::showConfigMenu);

                        // Аккуратно встраиваем её в правый угол
                        rightButtonsTable.add(settingsBtn).size(50f).right().padRight(1.5f);

                        // Сдвигаем крестик удаления в самый край
                        int lastIndex = rightButtonsTable.getChildren().size - 1;
                        Element removed = rightButtonsTable.getChildren().remove(lastIndex);
                        rightButtonsTable.getChildren().insert(rightButtonsTable.getChildren().size, removed);

                        rightButtonsTable.invalidateHierarchy();
                    }
                }
            }
        }

        // Рекурсивно спускаемся глубже по элементам UI
        if (element instanceof Group group) {
            for (int i = 0; i < group.getChildren().size; i++) {
                injectSettingsButton(group.getChildren().get(i), modName);
            }
        }
    }

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
}
