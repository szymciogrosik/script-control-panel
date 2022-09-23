package org.openjfx.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.openjfx.dto.ElementType;
import org.openjfx.dto.LoadedElement;
import org.openjfx.service.LoadFromCsvService;
import org.openjfx.service.GitBashService;

public class PrimaryController implements Initializable {

    private static final int SPACING_BETWEEN_BUTTONS = 5;

    @FXML
    public VBox primaryPage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addSectionHeader("Commands to invoke on environment");
        addElementsToScene(ElementType.SERVICE_COMMAND);
        addSectionHeader("Links");
        addElementsToScene(ElementType.LINK);
    }

    private void addSectionHeader(String headerName) {
        VBox section = new VBox();
        Text text = new Text(headerName);
        text.setStyle("-fx-font: normal bold 20 Langdon");
        text.setFill(Color.ORANGE);
        section.getChildren().add(text);
        primaryPage.getChildren().add(section);
    }

    private void addElementsToScene(ElementType type) {
        List<LoadedElement> loadedElements = loadElementsFromCsvFile(type);
        addElementsToScene(loadedElements, type);
    }

    private void addElementsToScene(List<LoadedElement> loadedElements, ElementType type) {
        Set<AbstractMap.SimpleEntry<Integer, String>> uniqueSectionsSet = loadedElements.stream()
                 .map(elem -> new AbstractMap.SimpleEntry <>(
                         elem.getSectionDisplayOrder(), elem.getSectionName()
                 ))
                 .collect(Collectors.toSet());

        List<AbstractMap.SimpleEntry<Integer, String>> uniqueSections = new ArrayList<>(uniqueSectionsSet);
        uniqueSections.sort(Map.Entry.comparingByKey());

        for (AbstractMap.SimpleEntry<Integer, String> section : uniqueSections) {
            primaryPage.getChildren().add(createHeaderForSection(section.getValue()));
            primaryPage.setBackground(new Background(new BackgroundFill(Color.rgb(33,33,33), CornerRadii.EMPTY, Insets.EMPTY)));

            HBox rows = new HBox();
            rows.setSpacing(SPACING_BETWEEN_BUTTONS);

            List<LoadedElement> sectionElements = loadedElements.stream()
                                                                  .filter(elem -> elem.getSectionName().equals(section.getValue()))
                                                                  .sorted(Comparator.comparing(LoadedElement::getCommandOrder))
                                                                  .collect(Collectors.toList());
            for (LoadedElement loadedElement : sectionElements) {
                rows.getChildren().add(createButton(loadedElement.getButtonName(), loadedElement.getCommand(), loadedElement.getDescription(), type));
            }
            primaryPage.getChildren().add(rows);
        }
    }

    private VBox createHeaderForSection(String sectionName) {
        VBox section = new VBox();
        Text text = new Text(sectionName);
        text.getStyleClass().add("text");
        text.setFill(Color.ORANGE);
        section.getChildren().add(text);
        return section;
    }

    private Button createButton(String buttonName, String command, String description, ElementType type) {
        Button button = new Button(buttonName);
        if (ElementType.SERVICE_COMMAND.equals(type)) {
            button.setOnMouseClicked(event -> GitBashService.runCommand(command));
        } else if (ElementType.LINK.equals(type)) {
            button.setOnMouseClicked(event -> openPageInBrowser(command));
        }
        button.setTooltip(createTooltip(description));
        return button;
    }

    private void openPageInBrowser(String url) {
        try {
            URI u = new URI(url);
            java.awt.Desktop.getDesktop().browse(u);
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private Tooltip createTooltip(String tooltipText) {
        Tooltip tt = new Tooltip();
        tt.setText(tooltipText);
        return tt;
    }

    private List<LoadedElement> loadElementsFromCsvFile(ElementType type) {
        try {
            return LoadFromCsvService.load(type);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
