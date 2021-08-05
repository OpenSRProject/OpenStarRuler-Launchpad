package com.dalolorn.sr2modmanager.adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyFileVisitor extends SimpleFileVisitor<Path> {
	@NotNull private final Path targetPath;
	@Nullable private Path sourcePath = null;

	public CopyFileVisitor(@NotNull Path targetPath) {
		this.targetPath = targetPath;
	}

	@Override
	@NotNull
	public FileVisitResult preVisitDirectory(
			@NotNull final Path dir,
			@NotNull final BasicFileAttributes attrs
	) throws IOException {
		if (sourcePath == null) {
			sourcePath = dir;
		} else {
			Files.createDirectories(targetPath.resolve(sourcePath
					.relativize(dir)));
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	@NotNull
	public FileVisitResult visitFile(
			@NotNull final Path file,
			@NotNull final BasicFileAttributes attrs
	) throws IOException {
		//noinspection ConstantConditions
		Files.copy(file,
				targetPath.resolve(sourcePath.relativize(file)));
		return FileVisitResult.CONTINUE;
	}
}
