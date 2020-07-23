package com.app.googledrive;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GGDriveUtils {
	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	// Directory to store user credentials for this application.
	private static final java.io.File CREDENTIALS_FOLDER //
	= new java.io.File(System.getProperty("user.home"), "credentials");
	private static final String CLIENT_SECRET_FILE_NAME = "client_secret.json";
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
	// Global instance of the {@link FileDataStoreFactory}.
	private static FileDataStoreFactory DATA_STORE_FACTORY;
	// Global instance of the HTTP transport.
	private static HttpTransport HTTP_TRANSPORT;
	private static Drive _driveService;
	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(CREDENTIALS_FOLDER);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
	public static Credential getCredentials() throws IOException {
		java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);
		if (!clientSecretFilePath.exists()) {
			throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME //
					+ " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
		}
		InputStream in = new FileInputStream(clientSecretFilePath);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		return credential;
	}
	public static Drive getDriveService() throws IOException {
		if (_driveService != null) {
			return _driveService;
		}
		Credential credential = getCredentials();
		//
		_driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential) //
				.setApplicationName(APPLICATION_NAME).build();
		return _driveService;
	}

	public final File createFolder(String folderIdParent, String folderName) throws IOException {
		File fileMetadata = new File();
		fileMetadata.setName(folderName);
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		if (folderIdParent != null) {
			List<String> parents = Arrays.asList(folderIdParent);
			fileMetadata.setParents(parents);
		}
		Drive driveService = GGDriveUtils.getDriveService();
		// Create a Folder.
		// Returns File object with id & name fields will be assigned values
		File file = driveService.files().create(fileMetadata).setFields("id, name").execute();
		return file;
	}
	
	
	// com.google.api.services.drive.model.File
	public final List<File> getGoogleSubFolders(String googleFolderIdParent) throws IOException {
		Drive driveService = GGDriveUtils.getDriveService();

		String pageToken = null;
		List<com.google.api.services.drive.model.File> list = new ArrayList<com.google.api.services.drive.model.File>();

		String query = null;
		if (googleFolderIdParent == null) {
			query = " mimeType = 'application/vnd.google-apps.folder' " //
					+ " and 'root' in parents";
		} else {
			query = "'" + googleFolderIdParent + "' in parents";
		}

		do {
			FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
					// Fields will be assigned values: id, name, createdTime
					.setFields("nextPageToken, files(id, name, createdTime, mimeType)")//
					.setPageToken(pageToken).execute();
			for (com.google.api.services.drive.model.File file : result.getFiles()) {
				list.add(file);
				System.out.println(file.getId()+"1111111111 " + file.getName());
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		//
		return list;
	}
	
	public void getFilesToDownload(String localLocation, com.google.api.services.drive.model.File file) throws IOException, InterruptedException
	{
		List<com.google.api.services.drive.model.File> files = getGoogleSubFolders(file.getId());
		if(file.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")) {
			java.io.File f = new java.io.File(localLocation+"\\"+file.getName());
			if(!f.exists()) {
				f.mkdir();
			}
			for (com.google.api.services.drive.model.File folder : files) {
				
				localLocation = f.getAbsolutePath()+"\\";
				getFilesToDownload(localLocation, folder);
				System.out.println(file.getId()+" : " + file.getName());
				System.out.println("Path: "+localLocation);
			}
		}else {
			downloadFile(file, localLocation);
		}
	}

	private void downloadFile(com.google.api.services.drive.model.File f, String rootFolder) throws IOException {
		Drive driveService=GGDriveUtils.getDriveService();
		System.out.println(f.getName() + "- " +f.getMimeType());
		Get file = driveService.files().get(f.getId());
		HttpResponse httpResponse = null;
		if (file!=null) {
			httpResponse = file.executeMedia();
		}
		if (null != httpResponse) {
			InputStream instream = httpResponse.getContent();
			FileOutputStream output = new FileOutputStream(rootFolder+"\\"+f.getName());
			try {
				int l;
				byte[] tmp = new byte[2048];
				while ((l = instream.read(tmp)) != -1) {
					output.write(tmp, 0, l);
				}
			} finally {
				output.close();
				instream.close();
			}
		}
	}
	// com.google.api.services.drive.model.File
	public final List<File> getGoogleRootFolders() throws IOException {
		return getGoogleSubFolders("1K-wNkQM7aGnsV0Uctayo8YJU6Sjr9w1D");
	}
	public void uploadFile(String folderId, String path) { 
		try{
			Drive driveService = GGDriveUtils.getDriveService();			
			java.io.File filePath = new java.io.File(path);
			java.io.File[] temp = filePath.listFiles();

			
			for(java.io.File f : temp) {
				if(f.isDirectory()) {
					File fileMetadata = new File();
					fileMetadata.setName(f.getName());
					fileMetadata.setParents(Arrays.asList(folderId));
					fileMetadata.setParents(Collections.singletonList(folderId));
					System.out.println(f.getName());
					fileMetadata.setMimeType("application/vnd.google-apps.folder");
					File file = driveService.files().create(fileMetadata)
							.setFields("id, parents")
							.execute();
					System.out.println("File ID: " + file.getId());
					uploadFile(file.getId(),f.getAbsolutePath());
				}else {
					File fileMetadata = new File();
					fileMetadata.setName(f.getName());
					fileMetadata.setParents(Arrays.asList(folderId));
					FileContent mediaContent = new FileContent("/", f);
					File file = driveService.files().create(fileMetadata, mediaContent)
							.setFields("id, parents")
							.execute();
					System.out.println("File ID: " + file.getId());
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


}