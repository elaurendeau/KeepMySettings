package com.elliot.kms.views;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.ViewPart;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v1.DbxEntry;
import com.dropbox.core.v1.DbxWriteMode;
import com.elliot.kms.constant.ViewConstant;
import com.elliot.kms.util.FileUtil;
import com.elliot.kms.util.ZipUtil;

public class KMSView extends ViewPart {
	private Text securityTokenText;

	private TabFolder tabFolder;
	
	private TabItem authenticationTab;
	private TabItem mainTab;

	private Button exportButton;
	private Button importButton;
	private Button deleteButton;
	private Button loginButton;

	private DbxEntry.File metaDataFile;

	private String workspacePath;

	private DbxWebAuthNoRedirect webAuth;
	private DbxRequestConfig config;
	private String authorizeUrl;

	private DbxClientV1 client;

	public KMSView() {
		super();
		workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

		if(new File(workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS).exists()) {
			FileUtil.deleteFile(workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS);
		}
	}


	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		tabFolder = new TabFolder(parent, SWT.NONE);
		
		authenticationTab = new TabItem(tabFolder, SWT.NONE);
		authenticationTab.setText("Authentication");

		Composite authenticationComposite = new Composite(tabFolder, SWT.NONE);
		authenticationTab.setControl(authenticationComposite);
		authenticationComposite.setLayout(new GridLayout(3, false));

		Label dropboxLabel = new Label(authenticationComposite, SWT.NONE);
		dropboxLabel.setText("Connect to dropbox");
		new Label(authenticationComposite, SWT.NONE);

		Button dropboxButton = new Button(authenticationComposite, SWT.NONE);

		GridData gd_dropboxButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_dropboxButton.widthHint = 95;
		dropboxButton.setLayoutData(gd_dropboxButton);
		dropboxButton.setText("Get Access Code");

		Label securityTokenLabel = new Label(authenticationComposite, SWT.NONE);
		securityTokenLabel.setText("Dropbox security token");
		new Label(authenticationComposite, SWT.NONE);

		securityTokenText = new Text(authenticationComposite, SWT.BORDER);
		securityTokenText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(authenticationComposite, SWT.NONE);
		new Label(authenticationComposite, SWT.NONE);

		loginButton = new Button(authenticationComposite, SWT.NONE);

		GridData gd_loginButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_loginButton.widthHint = 95;
		loginButton.setLayoutData(gd_loginButton);
		loginButton.setText("Connect");

		mainTab = new TabItem(tabFolder, SWT.NONE);
		mainTab.setText("Main");

		Composite mainComposite = new Composite(tabFolder, SWT.NONE);
		mainTab.setControl(mainComposite);
		mainComposite.setLayout(new GridLayout(2, false));

		Label exportLabel = new Label(mainComposite, SWT.NONE);
		exportLabel.setText("Export current workspace's settings");

		exportButton = new Button(mainComposite, SWT.NONE);

		GridData gd_exportButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_exportButton.widthHint = 75;
		exportButton.setLayoutData(gd_exportButton);
		exportButton.setText("Export");

		Label importLabel = new Label(mainComposite, SWT.NONE);
		importLabel.setText("Import cloud settings");

		importButton = new Button(mainComposite, SWT.NONE);
		GridData gd_importButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_importButton.widthHint = 75;
		importButton.setLayoutData(gd_importButton);
		importButton.setText("Import");

		Label deleteLabel = new Label(mainComposite, SWT.NONE);
		deleteLabel.setText("Delete cloud settings");

		deleteButton = new Button(mainComposite, SWT.NONE);
		GridData gd_deleteButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_deleteButton.widthHint = 75;
		deleteButton.setLayoutData(gd_deleteButton);
		deleteButton.setText("Delete");
		
		updateView();

		dropboxButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:

					final String APP_KEY = "26z6tem7sf0yiah";
					final String APP_SECRET = "16ojs6wf2hh8h57";

					try {
						if (webAuth == null) {
							DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

							config = new DbxRequestConfig("JavaTutorial/1.0", Locale.getDefault().toString());
							webAuth = new DbxWebAuthNoRedirect(config, appInfo);
							authorizeUrl = webAuth.start();
						}

						final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
								IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null,
								null, null);
						browser.openURL(new URL(authorizeUrl));

						parent.redraw();

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
			}
		});

		loginButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					
					if(client == null) {
						login();
					} else {
						client = null;
						metaDataFile = null;
						securityTokenText.setText("");
						updateView();
					}
					
					// change the current tab after a sucessfull login
					if(client != null) {
						tabFolder.setSelection(mainTab);
					}
					
					parent.redraw();
					break;
				}
			}
		});

		exportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:

					FileInputStream inputStream = null;
					try {
						File directory = (new File(
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS));

						if (!directory.exists()) {
							directory.mkdir();
						}

						FileUtil.copy(workspacePath + File.separatorChar + ViewConstant.FILE_NAME_METADATA,
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS
										+ File.separatorChar + ViewConstant.FILE_NAME_COPY_METADATA);

						ZipUtil.zipFiles(
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS
										+ File.separatorChar + ViewConstant.FILE_NAME_COPY_METADATA,
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS
										+ File.separatorChar + ViewConstant.FILE_NAME_ZIP_METADATA);

						File newFile = new File(
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS
										+ File.separatorChar + ViewConstant.FILE_NAME_ZIP_METADATA);

						inputStream = new FileInputStream(newFile);

						DbxEntry.File currentFile = searchFilesByName(client.getMetadataWithChildren("/"),
								ViewConstant.FILE_NAME_ZIP_METADATA);

						if (currentFile != null) {
							client.delete(currentFile.path);
						}

						DbxEntry.File uploadedFile = client.uploadFile("/" + newFile.getName(), DbxWriteMode.add(),
								newFile.length(), inputStream);
						inputStream.close();

						FileUtil.deleteFile(workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS);

						searchFiles();
						parent.redraw();

					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}

					break;
				}
			}
		});

		importButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:

					FileOutputStream outputStream = null;

					try {

						File directory = (new File(
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS));

						if (!directory.exists()) {
							directory.mkdir();
						}

						outputStream = new FileOutputStream(workspacePath + File.separatorChar
								+ ViewConstant.FILE_NAME_KEEPMYSETTINGS + File.separatorChar + metaDataFile.name);
						DbxEntry.File downloadedFile = client.getFile("/" + metaDataFile.name, null, outputStream);

						ZipUtil.unZipIt(
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS
										+ File.separatorChar + metaDataFile.name,
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS);

						File metadata = new File(
								workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS
										+ File.separatorChar + ViewConstant.FILE_NAME_COPY_METADATA);
						metadata.renameTo(
								new File(workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS
										+ File.separatorChar + ViewConstant.FILE_NAME_METADATA));

						Path file = Paths.get(workspacePath + File.separatorChar + ViewConstant.FILE_NAME_KEEPMYSETTINGS
								+ File.separatorChar + ViewConstant.FILE_NAME_BAT_KEEPMYSETTINGS);
						Files.write(file, ViewConstant.BATCH_FILE_CONTENT, Charset.forName("UTF-8"));
						Desktop desktop = Desktop.getDesktop();
						desktop.open(file.toFile());

					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (DbxException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					} finally {
						if (outputStream != null) {
							try {
								outputStream.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}

					}
					break;
				}
			}
		});

		deleteButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:

					try {
						DbxEntry.File currentFile;

						currentFile = searchFilesByName(client.getMetadataWithChildren("/"),
								ViewConstant.FILE_NAME_ZIP_METADATA);

						if (currentFile != null) {
							client.delete(currentFile.path);
						}

						searchFiles();

					} catch (DbxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					break;
				}
			}
		});

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void login() {
		try {
			DbxAuthFinish authFinish = webAuth.finish(securityTokenText.getText());
			String accessToken = authFinish.getAccessToken();

			if (accessToken != null) {
				client = new DbxClientV1(config, accessToken);
				searchFiles();
				updateView();
			}
			

		} catch (DbxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		searchFiles();
	}

	private void updateView() {

		if (metaDataFile != null) {
			exportButton.setText("Update");
			importButton.setEnabled(true);
			deleteButton.setEnabled(true);
		} else {
			exportButton.setText("Export");
			importButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}
		
		if(client != null) {
			loginButton.setText("Disconnect");
			exportButton.setEnabled(true);
		} else {
			loginButton.setText("Connect");
			exportButton.setEnabled(false);
		}

	}

	private void searchFiles() {
		if (client != null) {
			try {
				DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
				metaDataFile = searchFilesByName(listing, ViewConstant.FILE_NAME_ZIP_METADATA);
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		updateView();
	}

	private DbxEntry.File searchFilesByName(DbxEntry.WithChildren childs, String name) {

		DbxEntry.File result = null;

		for (DbxEntry child : childs.children) {
			if (child.name.equals(name)) {
				result = child.asFile();
			}
		}

		return result;
	}
}
