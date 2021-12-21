module SR2ModManager {
	requires kotlin.stdlib;

	requires javafx.controls;
	requires javafx.fxml;
	requires com.google.gson;
	requires org.eclipse.jgit;
	requires java.desktop;
	requires static org.jetbrains.annotations;
	requires java.security.jgss;

	exports com.dalolorn.sr2modmanager.adapter;
	exports com.dalolorn.sr2modmanager.model;
	exports com.dalolorn.sr2modmanager.adapter.sr2utils;
	opens com.dalolorn.sr2modmanager.view;
	opens com.dalolorn.sr2modmanager.adapter to com.google.gson;
}