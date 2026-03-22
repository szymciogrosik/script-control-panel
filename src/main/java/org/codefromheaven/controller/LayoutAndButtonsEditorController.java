package org.codefromheaven.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.dto.ConfigType;
import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.DirectoryDTO;
import org.codefromheaven.dto.data.LayoutAndButtonsDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SubSectionDTO;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.LayoutService;
import org.codefromheaven.service.settings.SettingsServiceBase;
import org.codefromheaven.service.style.StyleService;

import org.codefromheaven.service.LoadFromJsonService;
import org.codefromheaven.service.settings.LayoutOrderService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import static javafx.beans.binding.Bindings.*;

public class LayoutAndButtonsEditorController {

    private static final String APP_LOCATION = "APP_LOCATION";

    private final MainWindowController.ContentLoader contentLoader;
    private final MainWindowController.ResizeWindow resizeWindow;
    private final AnimalService animalService;
    private final StyleService styleService;
    private LayoutAndButtonsDTO currentDto;
    private Runnable refreshLayoutForm;

    private final ObservableList<MutableDirectory> directories = FXCollections.observableArrayList();
    private final ObservableList<MutableSection> sections = FXCollections.observableArrayList();

    public LayoutAndButtonsEditorController(MainWindowController.ContentLoader contentLoader,
            MainWindowController.ResizeWindow resizeWindow) {
        this.contentLoader = contentLoader;
        this.resizeWindow = resizeWindow;
        this.animalService = SpringContext.getBean(AnimalService.class);
        this.styleService = SpringContext.getBean(StyleService.class);
    }

    public void setupPage() {
        currentDto = SpringContext.getBean(LayoutService.class).load();
        initMutables();

        Stage stage = new Stage();
        stage.getIcons().add(animalService.getCurrentAnimalImage());
        stage.setTitle("Edit Layout and Buttons");

        ToggleButton dirTabBtn = new ToggleButton("Variables (Directories)");
        dirTabBtn.getStyleClass().add("button-default");

        ToggleButton layoutTabBtn = new ToggleButton("Layout & Buttons");
        layoutTabBtn.getStyleClass().add("button-default");

        ToggleGroup tabGroup = new ToggleGroup();
        dirTabBtn.setToggleGroup(tabGroup);
        layoutTabBtn.setToggleGroup(tabGroup);

        // Visual cue for which tab is selected (100% opacity for active, 60% for inactive)
        dirTabBtn.opacityProperty().bind(when(dirTabBtn.selectedProperty()).then(1.0).otherwise(0.6));
        layoutTabBtn.opacityProperty().bind(when(layoutTabBtn.selectedProperty()).then(1.0).otherwise(0.6));

        HBox tabBar = new HBox(10, dirTabBtn, layoutTabBtn);

        VBox dirContent = createDirectoriesEditor(stage);
        VBox layoutContent = createLayoutEditor();

        StackPane contentPane = new StackPane(dirContent);

        dirTabBtn.setSelected(true);

        dirTabBtn.setOnAction(e -> {
            if (!dirTabBtn.isSelected()) dirTabBtn.setSelected(true); // Prevent deselecting
            contentPane.getChildren().setAll(dirContent);
        });

        layoutTabBtn.setOnAction(e -> {
            if (!layoutTabBtn.isSelected()) layoutTabBtn.setSelected(true); // Prevent deselecting
            if (refreshLayoutForm != null) {
                refreshLayoutForm.run();
            }
            contentPane.getChildren().setAll(layoutContent);
        });

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("button-default");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        saveButton.setOnAction(e -> saveAndApply(stage));

        HBox bottomBar = new HBox(saveButton);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));

        VBox mainContent = new VBox(tabBar, contentPane);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        VBox root = new VBox(10, mainContent, bottomBar);
        root.setPadding(new Insets(10));
        root.getStyleClass().addAll("primary-page", "background-primary");
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(styleService.getCurrentStyleUrl());

        stage.setScene(scene);
        stage.show();
    }

    private void initMutables() {
        if (currentDto == null) {
            currentDto = new LayoutAndButtonsDTO(new ArrayList<>(), new ArrayList<>());
        }

        directories.clear();
        boolean hasAppLoc = false;
        if (currentDto.directories() != null) {
            for (DirectoryDTO d : currentDto.directories()) {
                directories.add(new MutableDirectory(d));
                if (APP_LOCATION.equals(d.name())) hasAppLoc = true;
            }
        }
        if (!hasAppLoc) {
            directories.add(new MutableDirectory(new DirectoryDTO(APP_LOCATION, ".", "Application Root Directory")));
        }
        directories.sort((d1, d2) -> {
            boolean isApp1 = APP_LOCATION.equals(d1.name.get());
            boolean isApp2 = APP_LOCATION.equals(d2.name.get());
            if (isApp1 && !isApp2) return -1;
            if (!isApp1 && isApp2) return 1;
            return d1.name.get().toLowerCase().compareTo(d2.name.get().toLowerCase());
        });

        sections.clear();
        if (currentDto.layout() != null) {
            for (SectionDTO s : currentDto.layout()) {
                sections.add(new MutableSection(s));
            }
        }
    }

    private void saveAndApply(Stage stage) {
        if (!validateAll()) {
            return;
        }

        List<DirectoryDTO> dirs = directories.stream().map(MutableDirectory::toDto).collect(Collectors.toList());
        List<SectionDTO> sections = this.sections.stream().map(MutableSection::toDto).collect(Collectors.toList());

        // Always save the display order (section/subsection/button name order)
        LayoutOrderService layoutOrderService = SpringContext.getBean(LayoutOrderService.class);
        layoutOrderService.saveToMyOwnFile(sections);

        LoadFromJsonService loadFromJsonService = SpringContext.getBean(LoadFromJsonService.class);
        LayoutAndButtonsDTO defaultDto = loadFromJsonService.loadLayout(
                SettingsServiceBase.getFileDir(FileType.LAYOUT_AND_BUTTONS.name(), ConfigType.DEFAULT))
                .orElse(new LayoutAndButtonsDTO(new ArrayList<>(), new ArrayList<>()));

        List<DirectoryDTO> customOnlyDirectories = buildCustomOnlyDirectories(dirs, defaultDto);
        List<SectionDTO> customOnlySections = buildCustomOnlySections(sections, defaultDto);

        LayoutAndButtonsDTO customDto = new LayoutAndButtonsDTO(customOnlyDirectories, customOnlySections);
        SpringContext.getBean(LayoutService.class).save(customDto);

        contentLoader.loadContent();
        resizeWindow.resizeMainWindow();
        stage.close();
    }

    private List<DirectoryDTO> buildCustomOnlyDirectories(List<DirectoryDTO> dirs, LayoutAndButtonsDTO defaultDto) {
        List<DirectoryDTO> customOnlyDirs = new ArrayList<>();
        for (DirectoryDTO dir : dirs) {
            if (defaultDto.directories().stream().noneMatch(d -> d.equals(dir))) {
                customOnlyDirs.add(dir);
            }
        }
        return customOnlyDirs;
    }

    /**
     * Returns only the sections/subsections/buttons that are new or modified
     * compared to the default file. Sections/subsections that only exist in the
     * default are omitted entirely (no need to duplicate them). Sections or
     * subsections that contain at least one custom button are included with only
     * those custom buttons.
     */
    private List<SectionDTO> buildCustomOnlySections(List<SectionDTO> editorSections, LayoutAndButtonsDTO defaultDto) {
        List<SectionDTO> result = new ArrayList<>();

        for (SectionDTO editorSec : editorSections) {
            // Find the matching section in the default file (null if not present)
            Optional<SectionDTO> defaultSecOpt = defaultDto.layout().stream()
                    .filter(s -> s.sectionName().equals(editorSec.sectionName()))
                    .findFirst();

            List<SubSectionDTO> customSubSections = new ArrayList<>();

            for (SubSectionDTO editorSub : editorSec.subSections()) {
                Optional<SubSectionDTO> defaultSubOpt = defaultSecOpt
                        .flatMap(ds -> ds.subSections().stream()
                                .filter(ss -> ss.subSectionName().equals(editorSub.subSectionName()))
                                .findFirst());

                List<ButtonDTO> customButtons = new ArrayList<>();
                for (ButtonDTO editorBtn : editorSub.buttons()) {
                    boolean existsInDefault = defaultSubOpt
                            .map(ds -> ds.buttons().stream()
                                    .anyMatch(db -> Objects.equals(db, editorBtn)))
                            .orElse(false);
                    if (!existsInDefault) {
                        customButtons.add(editorBtn);
                    }
                }

                // Only include this subsection if it has custom (user-added/edited) buttons
                if (!customButtons.isEmpty()) {
                    customSubSections.add(new SubSectionDTO(editorSub.subSectionName(), customButtons));
                }
            }

            // Include this section only if it has custom subsections OR doesn't exist in default at all
            if (!customSubSections.isEmpty() || defaultSecOpt.isEmpty()) {
                result.add(new SectionDTO(editorSec.sectionName(), customSubSections));
            }
        }

        return result;
    }

    private boolean validateAll() {
        List<String> errors = new ArrayList<>();

        // Variables
        Set<String> dirNames = new HashSet<>();
        for (MutableDirectory dir : directories) {
            String name = dir.name.get();
            if (name == null || name.trim().isEmpty()) {
                errors.add("Variable names cannot be empty.");
            } else if (!dirNames.add(name.trim())) {
                errors.add("Variable names must be unique. Duplicate found: " + name);
            }
        }

        // Sections
        Set<String> sectionNames = new HashSet<>();
        for (MutableSection sec : sections) {
            String secName = sec.name.get();
            if (secName == null || secName.trim().isEmpty()) {
                errors.add("Section names cannot be empty.");
            } else if (!sectionNames.add(secName.trim())) {
                errors.add("Section names must be unique. Duplicate found: " + secName);
            }

            // SubSections
            Set<String> subNames = new HashSet<>();
            for (MutableSubSection sub : sec.subSections) {
                String subName = sub.name.get();
                if (subName == null || subName.trim().isEmpty()) {
                    errors.add("Subsection names cannot be empty (in section '" + secName + "').");
                } else if (!subNames.add(subName.trim())) {
                    errors.add("Subsection names must be unique within a section. Duplicate found: " + subName);
                }

                // Buttons
                Set<String> btnNames = new HashSet<>();
                for (MutableButton btn : sub.buttons) {
                    String btnName = btn.name.get();
                    if (btnName == null || btnName.trim().isEmpty()) {
                        errors.add("Button names cannot be empty (in subsection '" + subName + "').");
                    } else if (!btnNames.add(btnName.trim())) {
                        errors.add("Button names must be unique within a subsection. Duplicate found: " + btnName);
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            PopupController.showPopup(String.join("\n", errors), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-on-dark-background");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-on-dark-background");
        return label;
    }

    private void updateValidationUI(TextField field, Tooltip tooltip, String errorMessage) {
        if (errorMessage != null) {
            if (!field.getStyleClass().contains("text-field-error")) {
                field.getStyleClass().add("text-field-error");
            }
            tooltip.setText(errorMessage);
            if (field.getTooltip() != tooltip) {
                field.setTooltip(tooltip);
                if (!tooltip.getStyleClass().contains("tooltip-custom")) {
                    tooltip.getStyleClass().add("tooltip-custom");
                }
            }
            if (field.getScene() != null && field.getScene().getWindow() != null) {
                if (tooltip.getScene() != null) {
                    tooltip.getScene().getStylesheets().setAll(field.getScene().getStylesheets());
                }
                var bounds = field.localToScreen(field.getBoundsInLocal());
                if (bounds != null && !tooltip.isShowing()) {
                    tooltip.show(field, bounds.getMinX(), bounds.getMaxY() + 2);
                }
            }
        } else {
            field.getStyleClass().remove("text-field-error");
            field.setTooltip(null);
            tooltip.hide();
        }
    }

    // --- Editor Layouts ---

    private VBox createDirectoriesEditor(Stage stage) {
        ListView<MutableDirectory> dirList = new ListView<>(directories);
        dirList.setPrefWidth(300);
        // Disable drag-and-drop for variables
        dirList.setCellFactory(tv -> new ListCell<MutableDirectory>() {
            @Override
            protected void updateItem(MutableDirectory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(0, 0, 0, 15));

        TextField nameField = new TextField();
        Tooltip varErrorTooltip = new Tooltip();

        TextField pathField = new TextField();
        Button browseButton = new Button("Browse");
        browseButton.getStyleClass().add("button-default");
        TextArea descField = new TextArea();
        descField.setPrefRowCount(3);

        browseButton.setOnAction(e -> {
            javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
            try {
                java.io.File initialDir = new java.io.File(System.getProperty("user.dir"));
                if (initialDir.exists() && initialDir.isDirectory()) {
                    directoryChooser.setInitialDirectory(initialDir);
                }
            } catch (Exception ex) {
                // Ignore and use default
            }
            java.io.File selectedDirectory = directoryChooser.showDialog(stage);
            if (selectedDirectory != null) {
                java.nio.file.Path basePath = java.nio.file.Paths.get("").toAbsolutePath();
                java.nio.file.Path targetPath = selectedDirectory.toPath().toAbsolutePath();
                try {
                    String relativePath = basePath.relativize(targetPath).toString().replace("\\", "/");
                    pathField.setText(relativePath.isEmpty() ? "." : relativePath);
                } catch (IllegalArgumentException ex) {
                    pathField.setText(selectedDirectory.getAbsolutePath().replace("\\", "/"));
                }
            }
        });

        HBox pathBox = new HBox(5, pathField, browseButton);
        HBox.setHgrow(pathField, Priority.ALWAYS);

        form.add(createLabel("Variable name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(createLabel("Path location:"), 0, 1);
        form.add(pathBox, 1, 1);
        form.add(createLabel("Description:"), 0, 2);
        form.add(descField, 1, 2);

        VBox rightSide = new VBox(10, createHeaderLabel("Editing variable"), form);
        rightSide.setDisable(true); // default hidden/disabled

        dirList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            nameField.getProperties().put("isLoading", true);
            if (oldVal != null) {
                nameField.textProperty().unbindBidirectional(oldVal.name);
                pathField.textProperty().unbindBidirectional(oldVal.path);
                descField.textProperty().unbindBidirectional(oldVal.description);
            }
            if (newVal != null) {
                rightSide.setDisable(false);
                nameField.setText(newVal.name.get());
                pathField.setText(newVal.path.get());
                descField.setText(newVal.description.get());

                nameField.textProperty().bindBidirectional(newVal.name);
                pathField.textProperty().bindBidirectional(newVal.path);
                descField.textProperty().bindBidirectional(newVal.description);

                boolean isAppLoc = APP_LOCATION.equals(newVal.name.get());
                nameField.setDisable(isAppLoc);
                pathField.setDisable(isAppLoc);
                browseButton.setDisable(isAppLoc);

                // Initial validation check for when we select an item
                String currentName = newVal.name.get();
                updateValidationMessage(nameField, varErrorTooltip, currentName);
            } else {
                rightSide.setDisable(true);
                nameField.clear();
                pathField.clear();
                descField.clear();
                updateValidationUI(nameField, varErrorTooltip, null);
            }
            nameField.getProperties().put("isLoading", false);
        });

        nameField.textProperty().addListener((o, oldValue, newValue) -> {
            dirList.refresh();
            updateValidationMessage(nameField, varErrorTooltip, newValue);
            Boolean isLoading = (Boolean) nameField.getProperties().get("isLoading");
            if (isLoading == null || !isLoading) {
                if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                    updateButtonVariableNames(oldValue, newValue);
                }
            }
        });

        nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                sortDirectoriesAlphabetically(dirList);
            }
        });

        Button addBtn = new Button("Add variable");
        addBtn.getStyleClass().add("button-default");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addBtn, Priority.ALWAYS);
        addBtn.setOnAction(e -> {
            MutableDirectory newDir = new MutableDirectory(new DirectoryDTO("NEW_VAR", "", ""));
            directories.add(newDir);
            sortDirectoriesAlphabetically(dirList);
            Platform.runLater(() -> dirList.getSelectionModel().select(newDir));
        });

        Button delBtn = new Button("Remove variable");
        delBtn.getStyleClass().add("button-default");
        delBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(delBtn, Priority.ALWAYS);
        delBtn.setOnAction(e -> {
            MutableDirectory sel = dirList.getSelectionModel().getSelectedItem();
            if (sel != null) {
                String removedName = sel.name.get();
                if (APP_LOCATION.equals(removedName)) {
                    PopupController.showPopup("Cannot remove the reserved APP_LOCATION variable.", javafx.scene.control.Alert.AlertType.WARNING);
                    return;
                }
                boolean isUsed = false;
                for (MutableSection sec : sections) {
                    for (MutableSubSection sub : sec.subSections) {
                        for (MutableButton btn : sub.buttons) {
                            if (removedName.equals(btn.scriptLocationParamName.get())) {
                                isUsed = true;
                                break;
                            }
                        }
                    }
                }
                if (isUsed) {
                    PopupController.showPopup("Cannot remove variable '" + removedName + "' because it is currently used by one or more buttons.", javafx.scene.control.Alert.AlertType.WARNING);
                    return;
                }
                directories.remove(sel);
            }
        });

        HBox listBtns = new HBox(10, addBtn, delBtn);
        VBox leftSide = new VBox(10, createHeaderLabel("Variables"), dirList, listBtns);
        VBox.setVgrow(dirList, Priority.ALWAYS);

        HBox mainBox = new HBox(10, leftSide, rightSide);
        mainBox.setPadding(new Insets(15));

        VBox root = new VBox(mainBox);
        VBox.setVgrow(mainBox, Priority.ALWAYS);
        return root;
    }

    private void updateValidationMessage(TextField nameField, Tooltip varErrorTooltip, String newValue) {
        String errorMsg = null;
        if (newValue == null || newValue.trim().isEmpty()) {
            errorMsg = "Variable name cannot be empty.";
        } else if (directories.stream().filter(d -> newValue.equals(d.name.get())).count() > 1) {
            errorMsg = "Variable name must be unique.";
        }
        updateValidationUI(nameField, varErrorTooltip, errorMsg);
    }

    private void updateButtonVariableNames(String oldName, String newName) {
        if (oldName == null || oldName.isEmpty()) return;
        for (MutableSection sec : sections) {
            for (MutableSubSection sub : sec.subSections) {
                for (MutableButton btn : sub.buttons) {
                    if (oldName.equals(btn.scriptLocationParamName.get())) {
                        btn.scriptLocationParamName.set(newName);
                    }
                }
            }
        }
    }

    private VBox createLayoutEditor() {
        ListView<MutableSection> sectionList = new ListView<>(sections);
        ListView<MutableSubSection> subSectionList = new ListView<>();
        ListView<MutableButton> buttonList = new ListView<>();
        setDraggableListView(sectionList);
        setDraggableListView(subSectionList);
        setDraggableListView(buttonList);

        sectionList.setPrefWidth(200);
        subSectionList.setPrefWidth(200);
        buttonList.setPrefWidth(250);

        Button addSecBtn = new Button("+");
        addSecBtn.getStyleClass().add("button-default");
        addSecBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addSecBtn, Priority.ALWAYS);

        Button delSecBtn = new Button("-");
        delSecBtn.getStyleClass().add("button-default");
        delSecBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(delSecBtn, Priority.ALWAYS);

        Button addSubBtn = new Button("+");
        addSubBtn.getStyleClass().add("button-default");
        addSubBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addSubBtn, Priority.ALWAYS);

        Button delSubBtn = new Button("-");
        delSubBtn.getStyleClass().add("button-default");
        delSubBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(delSubBtn, Priority.ALWAYS);

        Button addBtnBtn = new Button("+");
        addBtnBtn.getStyleClass().add("button-default");
        addBtnBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addBtnBtn, Priority.ALWAYS);

        Button delBtnBtn = new Button("-");
        delBtnBtn.getStyleClass().add("button-default");
        delBtnBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(delBtnBtn, Priority.ALWAYS);

        subSectionList.setDisable(true);
        addSubBtn.setDisable(true);
        delSubBtn.setDisable(true);

        buttonList.setDisable(true);
        addBtnBtn.setDisable(true);
        delBtnBtn.setDisable(true);

        addSecBtn.setOnAction(e -> sections.add(new MutableSection(new SectionDTO("New Section", new ArrayList<>()))));
        delSecBtn.setOnAction(e -> {
            MutableSection s = sectionList.getSelectionModel().getSelectedItem();
            if (s != null)
                sections.remove(s);
        });

        addSubBtn.setOnAction(e -> {
            MutableSection s = sectionList.getSelectionModel().getSelectedItem();
            if (s != null)
                s.subSections.add(new MutableSubSection(new SubSectionDTO("New Subsection", new ArrayList<>())));
        });
        delSubBtn.setOnAction(e -> {
            MutableSection s = sectionList.getSelectionModel().getSelectedItem();
            MutableSubSection sub = subSectionList.getSelectionModel().getSelectedItem();
            if (s != null && sub != null)
                s.subSections.remove(sub);
        });

        addBtnBtn.setOnAction(e -> {
            MutableSubSection sub = subSectionList.getSelectionModel().getSelectedItem();
            MutableSection sec = sectionList.getSelectionModel().getSelectedItem();
            if (sub != null && sec != null) {
                sub.buttons.add(new MutableButton(new ButtonDTO("New Button", "", new ArrayList<>(), ElementType.PYTHON,
                        true, false, "", "", true, sec.name.get(), sub.name.get())));
            }
        });
        delBtnBtn.setOnAction(e -> {
            MutableSubSection sub = subSectionList.getSelectionModel().getSelectedItem();
            MutableButton b = buttonList.getSelectionModel().getSelectedItem();
            if (sub != null && b != null)
                sub.buttons.remove(b);
        });

        VBox secBox = new VBox(5, createHeaderLabel("Sections"), sectionList, new HBox(5, addSecBtn, delSecBtn));
        VBox subBox = new VBox(5, createHeaderLabel("Subsections"), subSectionList, new HBox(5, addSubBtn, delSubBtn));
        VBox btnBox = new VBox(5, createHeaderLabel("Buttons"), buttonList, new HBox(5, addBtnBtn, delBtnBtn));

        VBox.setVgrow(sectionList, Priority.ALWAYS);
        VBox.setVgrow(subSectionList, Priority.ALWAYS);
        VBox.setVgrow(buttonList, Priority.ALWAYS);

        HBox listsBox = new HBox(10, secBox, subBox, btnBox);

        VBox rightForm = new VBox(10);
        rightForm.setPadding(new Insets(0, 0, 0, 15));
        rightForm.setMinWidth(450);
        HBox.setHgrow(rightForm, Priority.ALWAYS);

        sectionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                subSectionList.setDisable(false);
                addSubBtn.setDisable(false);
                delSubBtn.setDisable(false);
                subSectionList.setItems(newVal.subSections);
                populateSectionEditForm(rightForm, newVal, sectionList);
            } else {
                subSectionList.setDisable(true);
                addSubBtn.setDisable(true);
                delSubBtn.setDisable(true);
                subSectionList.setItems(FXCollections.emptyObservableList());
                rightForm.getChildren().clear();
            }
        });

        subSectionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                buttonList.setDisable(false);
                addBtnBtn.setDisable(false);
                delBtnBtn.setDisable(false);
                buttonList.setItems(newVal.buttons);
                populateSubSectionEditForm(rightForm, newVal, subSectionList);
            } else {
                buttonList.setDisable(true);
                addBtnBtn.setDisable(true);
                delBtnBtn.setDisable(true);
                buttonList.setItems(FXCollections.emptyObservableList());
                // Fall back to section form
                MutableSection selSec = sectionList.getSelectionModel().getSelectedItem();
                if (selSec != null)
                    populateSectionEditForm(rightForm, selSec, sectionList);
            }
        });

        buttonList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                populateButtonEditForm(rightForm, newVal, buttonList);
            else {
                MutableSubSection selSub = subSectionList.getSelectionModel().getSelectedItem();
                if (selSub != null)
                    populateSubSectionEditForm(rightForm, selSub, subSectionList);
            }
        });

        // Add mouse click listeners to force switching the right view even if the item is already selected
        sectionList.setOnMouseClicked(e -> {
            MutableSection sel = sectionList.getSelectionModel().getSelectedItem();
            if (sel != null) populateSectionEditForm(rightForm, sel, sectionList);
        });

        subSectionList.setOnMouseClicked(e -> {
            MutableSubSection sel = subSectionList.getSelectionModel().getSelectedItem();
            if (sel != null) populateSubSectionEditForm(rightForm, sel, subSectionList);
        });

        buttonList.setOnMouseClicked(e -> {
            MutableButton sel = buttonList.getSelectionModel().getSelectedItem();
            if (sel != null) populateButtonEditForm(rightForm, sel, buttonList);
        });

        refreshLayoutForm = () -> {
            MutableButton selBtn = buttonList.getSelectionModel().getSelectedItem();
            if (selBtn != null) {
                populateButtonEditForm(rightForm, selBtn, buttonList);
            } else {
                MutableSubSection selSub = subSectionList.getSelectionModel().getSelectedItem();
                if (selSub != null) {
                    populateSubSectionEditForm(rightForm, selSub, subSectionList);
                } else {
                    MutableSection selSec = sectionList.getSelectionModel().getSelectedItem();
                    if (selSec != null) {
                        populateSectionEditForm(rightForm, selSec, sectionList);
                    }
                }
            }
        };

        HBox mainBox = new HBox(10, listsBox, rightForm);
        mainBox.setPadding(new Insets(15));

        VBox root = new VBox(mainBox);
        VBox.setVgrow(mainBox, Priority.ALWAYS);
        return root;
    }

    private void populateSectionEditForm(VBox form, MutableSection section, ListView<MutableSection> list) {
        form.getChildren().clear();
        form.getChildren().add(createHeaderLabel("Editing section"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(220); // Ensure labels are fully visible
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField(section.name.get());
        Runnable validate = updateValidationMessageRunnable(section, nameField);

        nameField.textProperty().addListener((o, old, nev) -> {
            section.name.set(nev);
            list.refresh();
            validate.run();
        });
        validate.run();

        grid.add(createLabel("Section name:"), 0, 0);
        grid.add(nameField, 1, 0);
        form.getChildren().add(grid);
    }

    private Runnable updateValidationMessageRunnable(MutableSection section, TextField nameField) {
        Tooltip sectionErrorTooltip = new Tooltip();

        return () -> {
            String val = section.name.get();
            String errorMsg = null;
            if (val == null || val.trim().isEmpty()) {
                errorMsg = "Section name cannot be empty.";
            } else if (sections.stream().filter(s -> val.equals(s.name.get())).count() > 1) {
                errorMsg = "Section name must be unique.";
            }
            updateValidationUI(nameField, sectionErrorTooltip, errorMsg);
        };
    }

    private void populateSubSectionEditForm(VBox form, MutableSubSection subSection, ListView<MutableSubSection> list) {
        form.getChildren().clear();
        form.getChildren().add(createHeaderLabel("Editing subsection"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(220); // Ensure labels are fully visible
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField(subSection.name.get());
        Tooltip subErrorTooltip = new Tooltip();
        
        Runnable validate = () -> {
            String val = subSection.name.get();
            long count = list.getItems().stream().filter(s -> val.equals(s.name.get())).count();
            String errorMsg = null;
            if (val == null || val.trim().isEmpty()) {
                errorMsg = "Subsection name cannot be empty.";
            } else if (count > 1) {
                errorMsg = "Subsection name must be unique.";
            }
            updateValidationUI(nameField, subErrorTooltip, errorMsg);
        };

        nameField.textProperty().addListener((o, old, nev) -> {
            subSection.name.set(nev);
            list.refresh();
            validate.run();
        });
        validate.run();

        grid.add(createLabel("Subsection name:"), 0, 0);
        grid.add(nameField, 1, 0);
        form.getChildren().add(grid);
    }

    private void populateButtonEditForm(VBox form, MutableButton btn, ListView<MutableButton> list) {
        form.getChildren().clear();
        form.getChildren().add(createHeaderLabel("Editing button"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(220); // Ensure labels are fully visible
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField(btn.name.get());
        Tooltip btnErrorTooltip = new Tooltip();
        
        Runnable validate = () -> {
            String val = btn.name.get();
            long count = list.getItems().stream().filter(b -> val.equals(b.name.get())).count();
            String errorMsg = null;
            if (val == null || val.trim().isEmpty()) {
                errorMsg = "Button name cannot be empty.";
            } else if (count > 1) {
                errorMsg = "Button name must be unique.";
            }
            updateValidationUI(nameField, btnErrorTooltip, errorMsg);
        };

        nameField.textProperty().addListener((o, old, nev) -> {
            btn.name.set(nev);
            list.refresh();
            validate.run();
        });
        validate.run();

        VBox commandsContainer = new VBox(5);
        Runnable updateCommandsString = () -> {
            java.util.List<String> currentCmds = new java.util.ArrayList<>();
            commandsContainer.getChildren().forEach(node -> {
                if (node instanceof HBox row) {
                    if (row.getChildren().size() > 0 && row.getChildren().get(0) instanceof TextField tf) {
                        currentCmds.add(tf.getText());
                    }
                }
            });
            btn.commands.set(String.join("\n", currentCmds));
        };

        java.util.function.Consumer<String> addCommandRow = (cmdText) -> {
            HBox row = new HBox(5);
            TextField cmdField = new TextField(cmdText);
            cmdField.setPrefWidth(300);
            HBox.setHgrow(cmdField, Priority.ALWAYS);
            cmdField.textProperty().addListener((o, old, nev) -> updateCommandsString.run());

            Button removeCmdBtn = new Button("-");
            removeCmdBtn.getStyleClass().add("button-default");
            removeCmdBtn.setOnAction(e -> {
                commandsContainer.getChildren().remove(row);
                updateCommandsString.run();
            });

            row.getChildren().addAll(cmdField, removeCmdBtn);
            commandsContainer.getChildren().add(commandsContainer.getChildren().size() - 1, row);
            updateCommandsString.run();
        };

        Button addCmdBtn = new Button("+ Add Command");
        addCmdBtn.getStyleClass().add("button-default");
        addCmdBtn.setOnAction(e -> addCommandRow.accept(""));
        commandsContainer.getChildren().add(addCmdBtn);

        String currentCommands = btn.commands.get();
        if (currentCommands != null && !currentCommands.isEmpty()) {
            for (String cmd : currentCommands.split("\n")) {
                addCommandRow.accept(cmd);
            }
        } else {
            addCommandRow.accept("");
        }

        ComboBox<ElementType> typeBox = new ComboBox<>(FXCollections.observableArrayList(ElementType.values()));
        typeBox.setValue(btn.elementType.get());
        typeBox.valueProperty().addListener((o, old, nev) -> {
            btn.elementType.set(nev);
            populateButtonEditForm(form, btn, list); // Re-render conditionally
        });

        ComboBox<String> locationBox = new ComboBox<>();
        directories.forEach(d -> locationBox.getItems().add(d.name.get()));
        locationBox.setValue(btn.scriptLocationParamName.get());
        locationBox.valueProperty()
                .addListener((o, old, nev) -> btn.scriptLocationParamName.set(nev != null ? nev : ""));

        CheckBox autoCloseCheck = new CheckBox();
        autoCloseCheck.setSelected(btn.autoCloseConsole.get());
        autoCloseCheck.selectedProperty().addListener((o, old, nev) -> btn.autoCloseConsole.set(nev));
        autoCloseCheck.getStyleClass().add("check-box-on-dark-background");

        CheckBox popupCheck = new CheckBox();
        popupCheck.setSelected(btn.popupInputDisplayed.get());
        popupCheck.getStyleClass().add("check-box-on-dark-background");

        TextField popupMsgField = new TextField(btn.popupInputMessage.get());
        popupMsgField.textProperty().addListener((o, old, nev) -> btn.popupInputMessage.set(nev));

        popupCheck.selectedProperty().addListener((o, old, nev) -> {
            btn.popupInputDisplayed.set(nev);
            if (!nev) {
                popupMsgField.setText("");
            }
        });

        Label popupMsgLabel = createLabel("Input script param popup text:");
        popupMsgLabel.visibleProperty().bind(popupCheck.selectedProperty());
        popupMsgLabel.managedProperty().bind(popupCheck.selectedProperty());
        popupMsgField.visibleProperty().bind(popupCheck.selectedProperty());
        popupMsgField.managedProperty().bind(popupCheck.selectedProperty());

        TextArea descField = new TextArea(btn.description.get());
        descField.setWrapText(true);
        descField.setPrefRowCount(3);
        descField.textProperty().addListener((o, old, nev) -> btn.description.set(nev));

        CheckBox visibleDefCheck = new CheckBox();
        visibleDefCheck.setSelected(btn.visibleAsDefault.get());
        visibleDefCheck.selectedProperty().addListener((o, old, nev) -> btn.visibleAsDefault.set(nev));
        visibleDefCheck.getStyleClass().add("check-box-on-dark-background");

        int row = 0;
        grid.add(createLabel("Button name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(createLabel("Element type:"), 0, row);
        grid.add(typeBox, 1, row++);

        ElementType t = btn.elementType.get();
        if (t != ElementType.LINK) {
            grid.add(createLabel("Script directory variable:"), 0, row);
            grid.add(locationBox, 1, row++);
        }

        grid.add(createLabel(t == ElementType.LINK ? "URLs (1 per line):" : "Commands:"), 0, row);
        grid.add(commandsContainer, 1, row++);
        grid.add(createLabel("Description (tooltip):"), 0, row);
        grid.add(descField, 1, row++);
        grid.add(createLabel("Visible as default:"), 0, row);
        grid.add(visibleDefCheck, 1, row++);

        if (t != ElementType.LINK) {
            grid.add(createLabel("Auto close console:"), 0, row);
            grid.add(autoCloseCheck, 1, row++);
            grid.add(createLabel("Show input script param popup:"), 0, row);
            grid.add(popupCheck, 1, row++);
            grid.add(popupMsgLabel, 0, row);
            grid.add(popupMsgField, 1, row++);
        }

        form.getChildren().add(grid);
    }

    private void sortDirectoriesAlphabetically(ListView<MutableDirectory> dirList) {
        MutableDirectory selected = dirList.getSelectionModel().getSelectedItem();
        directories.sort((d1, d2) -> {
            boolean isApp1 = APP_LOCATION.equals(d1.name.get());
            boolean isApp2 = APP_LOCATION.equals(d2.name.get());
            if (isApp1 && !isApp2) return -1;
            if (!isApp1 && isApp2) return 1;
            return d1.name.get().toLowerCase().compareTo(d2.name.get().toLowerCase());
        });
        if (selected != null) {
            Platform.runLater(() -> dirList.getSelectionModel().select(selected));
        }
    }

    private <T> void setDraggableListView(ListView<T> listView) {
        listView.setCellFactory(tv -> {
            ListCell<T> cell = new ListCell<T>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };

            cell.setOnDragDetected(event -> {
                if (cell.isEmpty())
                    return;
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(cell.getIndex()));
                db.setContent(cc);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    if (event.getGestureSource() instanceof ListCell sourceCell) {
                        if (sourceCell.getListView() == listView) {
                            event.acceptTransferModes(TransferMode.MOVE);
                        }
                    }
                }
                event.consume();
            });

            cell.setOnDragEntered(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    if (event.getGestureSource() instanceof ListCell sourceCell
                            && sourceCell.getListView() == listView) {
                        cell.setStyle("-fx-border-color: #0078d7; -fx-border-width: 2 0 0 0;");
                    }
                }
            });

            cell.setOnDragExited(event -> {
                cell.setStyle("");
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString() && event.getGestureSource() instanceof ListCell sourceCell
                        && sourceCell.getListView() == listView) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    int dropIndex = cell.isEmpty() ? listView.getItems().size() : cell.getIndex();

                    if (draggedIndex >= 0 && draggedIndex < listView.getItems().size() && draggedIndex != dropIndex) {
                        int insertIndex = dropIndex;
                        if (draggedIndex < dropIndex) {
                            insertIndex--;
                        }
                        T draggedItem = listView.getItems().remove(draggedIndex);

                        if (insertIndex > listView.getItems().size()) {
                            insertIndex = listView.getItems().size();
                        }
                        listView.getItems().add(insertIndex, draggedItem);
                        listView.getSelectionModel().select(draggedItem);
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                cell.setStyle("");
                event.consume();
            });

            return cell;
        });
    }

    // --- Mutable Wrapper Classes ---

    static class MutableDirectory {
        final SimpleStringProperty name = new SimpleStringProperty();
        final SimpleStringProperty path = new SimpleStringProperty();
        final SimpleStringProperty description = new SimpleStringProperty();

        MutableDirectory(DirectoryDTO dto) {
            this.name.set(dto.name());
            this.path.set(dto.path());
            this.description.set(dto.description());
        }

        DirectoryDTO toDto() {
            return new DirectoryDTO(name.get(), path.get(), description.get());
        }

        @Override
        public String toString() {
            return name.get();
        }
    }

    static class MutableSection {
        final SimpleStringProperty name = new SimpleStringProperty();
        final ObservableList<MutableSubSection> subSections = FXCollections.observableArrayList();

        MutableSection(SectionDTO dto) {
            this.name.set(dto.sectionName());
            if (dto.subSections() != null) {
                dto.subSections().forEach(ss -> this.subSections.add(new MutableSubSection(ss)));
            }
        }

        SectionDTO toDto() {
            return new SectionDTO(name.get(),
                    subSections.stream().map(ss -> ss.toDto(name.get())).collect(Collectors.toList()));
        }

        @Override
        public String toString() {
            return name.get();
        }
    }

    static class MutableSubSection {
        final SimpleStringProperty name = new SimpleStringProperty();
        final ObservableList<MutableButton> buttons = FXCollections.observableArrayList();

        MutableSubSection(SubSectionDTO dto) {
            this.name.set(dto.subSectionName());
            if (dto.buttons() != null) {
                dto.buttons().forEach(b -> this.buttons.add(new MutableButton(b)));
            }
        }

        SubSectionDTO toDto(String sectionName) {
            return new SubSectionDTO(name.get(),
                    buttons.stream().map(b -> b.toDto(sectionName, name.get())).collect(Collectors.toList()));
        }

        @Override
        public String toString() {
            return name.get();
        }
    }

    static class MutableButton {
        final SimpleStringProperty name = new SimpleStringProperty();
        final SimpleStringProperty scriptLocationParamName = new SimpleStringProperty();
        final SimpleStringProperty commands = new SimpleStringProperty();
        final SimpleObjectProperty<ElementType> elementType = new SimpleObjectProperty<>();
        final SimpleBooleanProperty autoCloseConsole = new SimpleBooleanProperty();
        final SimpleBooleanProperty popupInputDisplayed = new SimpleBooleanProperty();
        final SimpleStringProperty popupInputMessage = new SimpleStringProperty();
        final SimpleStringProperty description = new SimpleStringProperty();
        final SimpleBooleanProperty visibleAsDefault = new SimpleBooleanProperty();

        MutableButton(ButtonDTO dto) {
            this.name.set(dto.getButtonName());
            this.scriptLocationParamName.set(dto.getScriptLocationParamName());
            this.commands.set(dto.getCommands() == null ? "" : String.join("\n", dto.getCommands()));
            this.elementType.set(dto.getElementType());
            this.autoCloseConsole.set(dto.isAutoCloseConsole());
            this.popupInputDisplayed.set(dto.isPopupInputDisplayed());
            this.popupInputMessage.set(dto.getPopupInputMessage());
            this.description.set(dto.getDescription());
            this.visibleAsDefault.set(dto.isVisibleAsDefault());
        }

        ButtonDTO toDto(String sectionName, String subSectionName) {
            List<String> cmdList = Arrays.stream(commands.get().split("\n"))
                                         .map(String::trim)
                                         .filter(s -> !s.isEmpty())
                                         .collect(Collectors.toList());
            return new ButtonDTO(name.get(), scriptLocationParamName.get(), cmdList,
                    elementType.get(), autoCloseConsole.get(), popupInputDisplayed.get(),
                    popupInputMessage.get(), description.get(), visibleAsDefault.get(), sectionName, subSectionName);
        }

        @Override
        public String toString() {
            return name.get();
        }
    }
}
