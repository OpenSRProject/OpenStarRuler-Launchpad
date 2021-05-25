package com.dalolorn.sr2modmanager.view;

import javafx.scene.control.Alert;

public class ResizableAlert extends Alert {
	public ResizableAlert(AlertType alertType, String contentText) {
		super(alertType, contentText);
		this.setResizable(true);
	}
}
