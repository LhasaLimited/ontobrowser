/* 

Copyright 2015 Novartis Institutes for Biomedical Research

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.novartis.pcs.ontology.webapp.client.view;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InfoPopup implements OntoBrowserPopup {
	private final PopupPanel popupPanel = new PopupPanel(true);
	private final Label infoLabel = new Label("");

	public InfoPopup() {
		popupPanel.setTitle("Info");
		popupPanel.setGlassEnabled(false);
		popupPanel.setAnimationEnabled(true);

		VerticalPanel panel = new VerticalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		Button reloadButton = new Button("Close");

		infoLabel.addStyleName("info-icon-left");
		panel.add(infoLabel);

		reloadButton.addClickHandler(evt -> popupPanel.hide());
		panel.add(reloadButton);

		popupPanel.setWidget(panel);
	}

	@Override
	public void show() {
		popupPanel.center();
	}

	public void showInfo(String info) {
		infoLabel.setText(info);
		show();
	}
}
