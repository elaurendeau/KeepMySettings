package com.elliot.kms.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import com.elliot.kms.constant.ViewConstant;

public class FileUtil {

	public static void copy(String sourceDir, String targetDir) throws IOException {

		abstract class MyFileVisitor implements FileVisitor<Path> {
			boolean isFirst = true;
			Path ptr;
		}

		MyFileVisitor copyVisitor = new MyFileVisitor() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				// Move ptr forward
				if (!isFirst) {
					// .. but not for the first time since ptr is already in
					// there
					Path target = ptr.resolve(dir.getName(dir.getNameCount() - 1));
					ptr = target;
				}
				Files.copy(dir, ptr, StandardCopyOption.COPY_ATTRIBUTES);
				isFirst = false;
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path target = ptr.resolve(file.getFileName());
				Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				throw exc;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Path target = ptr.getParent();
				// Move ptr backwards
				ptr = target;
				return FileVisitResult.CONTINUE;
			}
		};

		copyVisitor.ptr = Paths.get(targetDir);
		Files.walkFileTree(Paths.get(sourceDir), copyVisitor);
	}
	
	public static void deleteFile(String filePath) {
		Path path = null;
		try {
			path = Paths
					.get(filePath);
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc)
						throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});

		} catch (NoSuchFileException x) {
			System.err.format("%s: no such" + " file or directory%n", path);
		} catch (DirectoryNotEmptyException x) {
			System.err.format("%s not empty%n", path);
		} catch (IOException x) {
			// File permission problems are caught here.
			System.err.println(x);
		}
	}
}
