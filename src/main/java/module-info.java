module SR2ModManager {
	requires javafx.controls;
	requires javafx.fxml;
	requires com.google.gson;
	requires org.eclipse.jgit;
	requires java.desktop;

	exports com.dalolorn.sr2modmanager.adapter;
	exports com.dalolorn.sr2modmanager.model;
	opens com.dalolorn.sr2modmanager.view;
}