module io.github.openstarruler.launchpad {
	requires kotlin.stdlib;
	requires kotlin.stdlib.jdk7;
	requires kotlin.stdlib.jdk8;

	requires javafx.controls;
	requires javafx.fxml;
	requires com.google.gson;
	requires org.eclipse.jgit;
	requires java.desktop;
	requires static org.jetbrains.annotations;
	requires java.security.jgss;

	exports io.github.openstarruler.launchpad.adapter;
	exports io.github.openstarruler.launchpad.model;
	exports io.github.openstarruler.launchpad.adapter.sr2utils;
	opens io.github.openstarruler.launchpad.view;
	opens io.github.openstarruler.launchpad.adapter to com.google.gson;
}
