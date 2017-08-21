/**
 * Copyright © 2017 Lhasa Limited
 * File created: 17/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.webapp.client.view;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.novartis.pcs.ontology.webapp.client.view.AddOntologyPopup.LabelFeature.ERROR;
import static com.novartis.pcs.ontology.webapp.client.view.AddOntologyPopup.LabelFeature.LABEL;
import static com.novartis.pcs.ontology.webapp.client.view.AddOntologyPopup.LabelFeature.TOOLTIP;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.webapp.client.OntoBrowserServiceAsync;
import com.novartis.pcs.ontology.webapp.client.util.UrlValidator;

/**
 * @author Artur Polit
 * @since 17/08/2017
 */
public class AddOntologyPopup implements OntoBrowserPopup, ClickHandler, KeyPressHandler {
	private static final int MAX_LEN = 64;

	private final OntoBrowserServiceAsync service;

	private final DialogBox dialogBox = new DialogBox(false, true);
	private final BusyIndicatorHandler busyIndicator = new WidgetBusyIndicatorHandler(
			dialogBox.getCaption().asWidget());
	private final TextBox nameField = new ClipboardAwareTextBox();
	private final TextArea descriptionField = new TextArea();
	private final CheckBox isInternalField = new CheckBox("check if it is not published");
	private final TextBox namespaceField = new ClipboardAwareTextBox();
	private final TextBox sourceUriField = new ClipboardAwareTextBox();
	private final TextBox releaseUriField = new ClipboardAwareTextBox();
	private final TextBox referencePrefixField = new ClipboardAwareTextBox();

	private final Label nameError = createLabel("", ERROR);
	private final Label descriptionError = createLabel("", ERROR);
	private final Label namespaceError = createLabel("", ERROR);
	private final Label referencePrefixError = createLabel("", ERROR);
	private final Label sourceUriError = createLabel("", ERROR);
	private final Label releaseUriError = createLabel("", ERROR);
	private final List<Label> errorLabels = Arrays.asList(descriptionError, nameError, namespaceError, releaseUriError, referencePrefixError,
			sourceUriError);

	private final Button createButton = new Button("Create");

	public AddOntologyPopup(final OntoBrowserServiceAsync service) {

		this.service = service;

		nameField.setMaxLength(MAX_LEN);
		nameField.setVisibleLength(MAX_LEN);
		nameField.addKeyPressHandler(this);

		descriptionField.setCharacterWidth(MAX_LEN);
		descriptionField.setVisibleLines(2);
		descriptionField.addKeyPressHandler(this);

		namespaceField.setVisibleLength(MAX_LEN);
		namespaceField.setMaxLength(256);
		namespaceField.addKeyPressHandler(this);

		sourceUriField.setVisibleLength(MAX_LEN);
		sourceUriField.setMaxLength(1024);
		sourceUriField.addKeyPressHandler(this);

		releaseUriField.setVisibleLength(MAX_LEN);
		sourceUriField.setMaxLength(1024);
		releaseUriField.addKeyPressHandler(this);

		referencePrefixField.setVisibleLength(MAX_LEN);
		referencePrefixField.setMaxLength(16);
		referencePrefixField.addKeyPressHandler(this);

		dialogBox.setText("Create Ontology");
		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(true);
		dialogBox.addStyleName("gwt-ModalDialogBox");

		addDialogWidgets();
	}

	@Override
	public void onClick(final ClickEvent event) {
		submit();
	}

	@Override
	public void onKeyPress(final KeyPressEvent event) {
		int keyCode = event.getCharCode();
		if(keyCode == KEY_ENTER) {
			submit();
		}
	}

	@Override
	public void show() {
		clearValidation();
		clear();
		dialogBox.center();
		nameField.setFocus(true);
	}

	private void addDialogWidgets() {
		VerticalPanel dialogVPanel = new VerticalPanel();
		HorizontalPanel buttonsHPanel = new HorizontalPanel();
		Button cancelButton = new Button("Cancel");

		cancelButton.addClickHandler(event -> {
			clear();
			dialogBox.hide();
		});

		createButton.addClickHandler(this);
		buttonsHPanel.add(createButton);
		buttonsHPanel.add(cancelButton);
		buttonsHPanel.addStyleName("dialog-buttons");

		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		dialogVPanel.add(createLabel("Name:", LABEL));
		dialogVPanel.add(nameField);
		dialogVPanel.add(createLabel("Identifier like name (e.g. bfo)", TOOLTIP));
		dialogVPanel.add(nameError);
		dialogVPanel.add(createLabel("Description:", LABEL));
		dialogVPanel.add(descriptionField);
		dialogVPanel.add(descriptionError);
		dialogVPanel.add(createLabel("Is internal:", LABEL));
		dialogVPanel.add(isInternalField);

		dialogVPanel.add(createLabel("Namespace:", LABEL));
		dialogVPanel.add(namespaceField);
		dialogVPanel.add(createLabel("OBO default namespace (e.g. bfo)", TOOLTIP));
		dialogVPanel.add(namespaceError);
		dialogVPanel.add(createLabel("Source URI:", LABEL));
		dialogVPanel.add(sourceUriField);
		dialogVPanel
				.add(createLabel("Main URI of the ontology (e.g. http://purl.obolibrary.org/obo/bfo.owl)", TOOLTIP));
		dialogVPanel.add(sourceUriError);
		dialogVPanel.add(createLabel("Release/version URI:", LABEL));
		dialogVPanel.add(releaseUriField);
		dialogVPanel.add(createLabel("URI of the current version (e.g. http://purl.obolibrary.org/obo/bfo/2.0/bfo.owl)",
				TOOLTIP));
		dialogVPanel.add(releaseUriError);

		dialogVPanel.add(createLabel("Reference Id:", LABEL));
		dialogVPanel.add(referencePrefixField);
		dialogVPanel.add(createLabel("For identifiers generation (e.g. BFO_)", TOOLTIP));

		dialogVPanel.add(referencePrefixError);

		dialogVPanel.add(buttonsHPanel);
		dialogVPanel.setCellHorizontalAlignment(buttonsHPanel, VerticalPanel.ALIGN_CENTER);

		dialogBox.setWidget(dialogVPanel);
	}

	private void clear() {
		nameField.setValue(null);
		descriptionField.setValue(null);
		namespaceField.setValue(null);
		sourceUriField.setValue(null);
		releaseUriField.setValue(null);
		referencePrefixField.setValue(null);
	}

	private void clearValidation() {
		errorLabels.forEach(label -> label.setText(""));
	}

	enum LabelFeature {
		ERROR, LABEL, TOOLTIP
	}

	private Label createLabel(final String label, LabelFeature... features) {
		Set<LabelFeature> featureSet = EnumSet.noneOf(LabelFeature.class);
		featureSet.addAll(Arrays.asList(features));

		Label labelWidget = new Label(label);
		if (featureSet.contains(LABEL)) {
			labelWidget.setStyleName("dialog-label");
		}
		if (featureSet.contains(ERROR)) {
			labelWidget.setStyleName("dialog-error");
		}
		if (featureSet.contains(TOOLTIP)) {
			labelWidget.setStyleName("dialog-tooltip");
		}

		return labelWidget;
	}

	private boolean validateRequired() {
		boolean validName = !isEmpty(nameField, nameError);
		boolean validReference = !isEmpty(referencePrefixField, referencePrefixError);
		boolean validNameSpace = !isEmpty(namespaceField, namespaceError);
		boolean validSourceUri = true;
		boolean validRelease = true;

		if (!isInternalField.getValue()) {
			validSourceUri = UrlValidator.validate(sourceUriField.getValue(), sourceUriError);
			validRelease = UrlValidator.validate(releaseUriField.getValue(), releaseUriError);
		}
		return validName && validReference && validNameSpace && validSourceUri && validRelease;
	}

	private boolean isEmpty(ValueBoxBase<String> widget, HasText errorField) {
		boolean empty = widget.getValue().trim().isEmpty();
		if (empty) {
			errorField.setText("This field should not be empty");
		} else {
			errorField.setText("");
		}
		return empty;
	}

	private void submit() {
		clearValidation();
		if (!validateRequired()) {
			return;
		}

		Ontology ontology = new Ontology(nameField.getValue().trim(), null, null);
		ontology.setSourceNamespace(namespaceField.getValue().trim());
		ontology.setReferenceIdPrefix(referencePrefixField.getValue().trim());
		ontology.setDescription(descriptionField.getValue().trim());

		ontology.setInternal(isInternalField.getValue());
		ontology.setSourceUri(sourceUriField.getValue().trim());
		ontology.setSourceRelease(releaseUriField.getValue().trim());

		busyIndicator.busy();
		service.addOntology(ontology, new AsyncCallback<Void>() {
			@Override
			public void onFailure(final Throwable caught) {
				GWT.log("Failed to create new ontology", caught);
				nameError.setText(caught.getMessage());
				busyIndicator.idle();
			}

			@Override
			public void onSuccess(final Void result) {
				clear();
				busyIndicator.idle();
				dialogBox.hide();
			}
		});

	}

}
/*
 * ---------------------------------------------------------------------* This
 * software is the confidential and proprietary information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY --- No part of this
 * confidential information shall be disclosed and it shall be used only in
 * accordance with the terms of a written license agreement entered into by
 * holder of the information with LHASA Ltd.
 * ---------------------------------------------------------------------
 */
