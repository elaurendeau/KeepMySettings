package com.elliot.kms.constant;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

public class ViewConstant {
	public final static String FILE_NAME_KEEPMYSETTINGS = ".KeepMySettings";
	public final static String FILE_NAME_METADATA = ".metadata";
	public final static String FILE_NAME_BACKUP_METADATA = ".metadata_backup";
	public final static String FILE_NAME_COPY_METADATA = ".metadata_copy";
	public final static String FILE_NAME_ZIP_METADATA = "metadata.zip";
	public final static String FILE_NAME_BAT_KEEPMYSETTINGS = "KeepMySettings.bat";
	
	public final static List<String> BATCH_FILE_CONTENT = Arrays.asList("@echo off",
			"taskkill /F /pid " + ManagementFactory.getRuntimeMXBean().getName().substring(0, ManagementFactory.getRuntimeMXBean().getName().indexOf("@")),
			"timeout /t 1 /nobreak > NUL",
			"rename \""+ ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + File.separatorChar + FILE_NAME_METADATA + "\"" + " \"" +  FILE_NAME_BACKUP_METADATA + "\"",
			"xcopy \""+ ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS + File.separatorChar + FILE_NAME_METADATA + "\" \""+ ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + File.separatorChar + FILE_NAME_METADATA + "\\\" /E /Y", 
			"start "+ System.getProperty("eclipse.home.location") + "eclipse.exe", 
			"exit");
}
