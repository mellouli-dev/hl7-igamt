package gov.nist.hit.hl7.igamt.workspace.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import gov.nist.hit.hl7.igamt.common.base.domain.DocumentStructure;
import gov.nist.hit.hl7.igamt.common.base.domain.DocumentType;
import gov.nist.hit.hl7.igamt.ig.domain.Ig;
import gov.nist.hit.hl7.igamt.workspace.domain.*;
import gov.nist.hit.hl7.igamt.workspace.exception.CreateRequestException;
import gov.nist.hit.hl7.igamt.workspace.exception.WorkspaceForbidden;
import gov.nist.hit.hl7.igamt.workspace.exception.WorkspaceNotFound;
import gov.nist.hit.hl7.igamt.workspace.model.*;
import gov.nist.hit.hl7.igamt.workspace.service.WorkspaceUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import gov.nist.hit.hl7.igamt.common.base.domain.Type;
import gov.nist.hit.hl7.igamt.common.base.exception.ForbiddenOperationException;
import gov.nist.hit.hl7.igamt.ig.service.IgService;
import gov.nist.hit.hl7.igamt.workspace.repository.WorkspaceRepo;
import gov.nist.hit.hl7.igamt.workspace.service.WorkspaceService;
import gov.nist.hit.hl7.resource.change.service.OperationService;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

	
	@Autowired
	WorkspaceRepo workspaceRepo;
	
	@Autowired
	OperationService operationService;
	
	@Autowired 
	IgService igService;

	@Autowired
    MongoTemplate mongoTemplate;

	@Autowired
	WorkspaceUserService workspaceUserService;
	
	@Override
	public Workspace findById(String id) {
		return workspaceRepo.findById(id).orElse(null);
	}

	@Override
	public Workspace create(WorkspaceCreateRequest createInfo, String username) throws CreateRequestException {
		checkWorkspaceCreateRequest(createInfo);
//		String logoImageId = logo != null && !logo.isEmpty() ? uploadLogo(logo, username) : null;
		Workspace workspace = new Workspace();
		WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata();
		workspaceMetadata.setDescription(createInfo.getDescription());
		workspaceMetadata.setTitle(createInfo.getTitle());
//		workspaceMetadata.setLogoImageId(logoImageId);
		workspace.setAccessType(createInfo.getAccessType());
		workspace.setMetadata(workspaceMetadata);
		workspace.setUsername(username);
		workspace.setFolders(new HashSet<>());
		workspace.setDocuments(new HashSet<>());
		return this.workspaceRepo.save(workspace);
	}

	@Override
	public Workspace createFolder(String workspaceId, AddFolderRequest addFolderRequest, String username) throws Exception {
		Workspace workspace = this.workspaceRepo.findById(workspaceId)
				.orElseThrow(() -> new WorkspaceNotFound(workspaceId));
		if(!this.workspaceUserService.isAdmin(workspace, username)) {
			throw new WorkspaceForbidden();
		}

		if(addFolderRequest == null) {
			throw new Exception("Folder creation request is invalid");
		} else {
			if(Strings.isNullOrEmpty(addFolderRequest.getTitle())) {
				throw new Exception("Folder title is required");
			}
		}

		if(workspace.getFolders() == null) {
			workspace.setFolders(new HashSet<>());
		}

		FolderMetadata folderMetadata = new FolderMetadata();
		folderMetadata.setTitle(addFolderRequest.getTitle());
		folderMetadata.setDescription(addFolderRequest.getDescription());
		Folder folder = new Folder();
		folder.setId(UUID.randomUUID().toString());
		folder.setChildren(new HashSet<>());
		folder.setMetadata(folderMetadata);
		int position = workspace.getFolders().stream().mapToInt(Folder::getPosition).max().orElse(0) + 1;
		folder.setPosition(position);
		workspace.getFolders().add(folder);
		return this.workspaceRepo.save(workspace);
	}

	@Override
	public Workspace updateFolder(String workspaceId, String folderId, AddFolderRequest addFolderRequest, String username) throws Exception {
		Workspace workspace = this.workspaceRepo.findById(workspaceId)
				.orElseThrow(() -> new WorkspaceNotFound(workspaceId));
		if(!this.workspaceUserService.isAdmin(workspace, username)) {
			throw new WorkspaceForbidden();
		}

		if(addFolderRequest == null) {
			throw new Exception("Folder creation request is invalid");
		} else {
			if(Strings.isNullOrEmpty(addFolderRequest.getTitle())) {
				throw new Exception("Folder title is required");
			}
		}

		if(workspace.getFolders() == null) {
			workspace.setFolders(new HashSet<>());
		}

		Folder folder = workspace.getFolders().stream()
				.filter((f) -> f.getId().equals(folderId)).findAny()
				.orElseThrow(() -> new Exception("Folder not found " + folderId));
		FolderMetadata folderMetadata = new FolderMetadata();
		folderMetadata.setTitle(addFolderRequest.getTitle());
		folderMetadata.setDescription(addFolderRequest.getDescription());
		folder.setMetadata(folderMetadata);
		return this.workspaceRepo.save(workspace);
	}

	@Override
	public Workspace saveHomeContent(String workspaceId, HomeContentWrapper home, String username) throws Exception {
		Workspace workspace = this.workspaceRepo.findById(workspaceId)
				.orElseThrow(() -> new WorkspaceNotFound(workspaceId));
		if(!this.workspaceUserService.isAdmin(workspace, username)) {
			throw new WorkspaceForbidden();
		}

		workspace.setHomePageContent(home.getValue());
		this.workspaceRepo.save(workspace);

		return workspace;
	}

	@Override
	public Workspace saveMetadata(String workspaceId, WorkspaceMetadataWrapper metadataWrapper, String username) throws Exception {
		Workspace workspace = this.workspaceRepo.findById(workspaceId)
				.orElseThrow(() -> new WorkspaceNotFound(workspaceId));
		if(!this.workspaceUserService.isAdmin(workspace, username)) {
			throw new WorkspaceForbidden();
		}

		if(metadataWrapper == null) {
			throw new Exception("Workspace update request is invalid");
		} else {
			if(Strings.isNullOrEmpty(metadataWrapper.getTitle())) {
				throw new Exception("Workspace title is required");
			}
		}

		workspace.getMetadata().setTitle(metadataWrapper.getTitle());
		workspace.getMetadata().setDescription(metadataWrapper.getDescription());
		this.workspaceRepo.save(workspace);
		return workspace;
	}

	@Override
	public WorkspaceInfo addToWorkspace(String workspaceId, String folderId, String username, DocumentStructure document, Type type) throws Exception {
		Workspace workspace = this.workspaceRepo.findById(workspaceId)
				.orElseThrow(() -> new WorkspaceNotFound(workspaceId));
		WorkspacePermissionType permission = workspaceUserService.getWorkspacePermissionTypeByFolder(workspace, username, folderId);
		if(permission != null && permission.equals(WorkspacePermissionType.EDIT)) {
			Folder folder = workspace.getFolders().stream()
					.filter((f) -> f.getId().equals(folderId))
					.findFirst()
					.orElseThrow(() -> new Exception("Folder not found"));
			int position = workspace.getFolders().stream()
					.mapToInt(Folder::getPosition)
					.max()
					.orElseThrow(() -> new Exception("Folder not found"));

			DocumentLink link = new DocumentLink();
			link.setId(document.getId());
			link.setType(type);
			link.setPosition(position + 1);
			folder.getChildren().add(link);
			return this.toWorkspaceInfo(this.workspaceRepo.save(workspace), username);
		} else {
			throw new WorkspaceForbidden();
		}
	}

//	public String uploadLogo(MultipartFile logo, String username) throws IOException {
//		InputStream in = logo.getInputStream();
//		String filename = logo.getOriginalFilename();
//		String extension = FilenameUtils.getExtension(filename);
//		Document metaData = new Document();
//		metaData.put("accountId", username);
//		Set<String> igs= new HashSet<String>();
//		igs.add(ig);
//		metaData.put("igs", igs);
//		Set<String> ids= new HashSet<String>();
//		ids.add(id);
//		metaData.put("type", type);
//		metaData.put("id", ids);
//		String generatedName = UUID.randomUUID().toString() + "." + extension;
//		ObjectId fsFile = storageService.store(in, generatedName, part.getContentType(), metaData);
//		GridFSFile dbFile = storageService.findOne(fsFile.toString());
//		UploadFileResponse response= new UploadFileResponse("/api/storage/file?name="+ dbFile.getFilename());
//		return response;
//	}

	public void checkWorkspaceCreateRequest(
			WorkspaceCreateRequest createInfo
//			MultipartFile logo
	) throws CreateRequestException {
		List<String> errors = new ArrayList<>();
//		// Check Image
//		if(logo != null && !logo.isEmpty()) {
//			String mime = logo.getContentType();
//			String filename = logo.getOriginalFilename();
//			String extension = FilenameUtils.getExtension(filename);
//			if (mime == null || (!mime.equals("image/jpeg") && !mime.equals("image/png"))) {
//				errors.add("File MIME type is not supported : " + mime);
//			}
//			if (extension == null || (!extension.equals("jpg") && !extension.equals("png"))) {
//				errors.add("File extension is not supported : " + extension);
//			}
//			if (logo.getSize() >= 1024 * 1024 * 10) {
//				errors.add("File size is too big");
//			}
//		}

		// Check Fields
		if(createInfo == null) {
			errors.add("Request cannot be null");
		} else {
			if(createInfo.getAccessType() == null) {
				errors.add("Workspace access type is required");
			}
			if(Strings.isNullOrEmpty(createInfo.getTitle())) {
				errors.add("Workspace title is required");
			}
		}

		if(errors.size() > 0) {
			throw new CreateRequestException(errors);
		}
	}

	@Override
	public Workspace save(Workspace workspace) throws ForbiddenOperationException {
		Workspace ret =this.workspaceRepo.save(workspace);
		return ret;
	}

	@Override
	public List<Workspace> findAll() {
		return workspaceRepo.findAll();
	}

	@Override
	public void delete(Workspace workspace) throws ForbiddenOperationException {
		this.workspaceRepo.delete(workspace);
	}

    @Override
    public List<Workspace> findByMember(String username) {
		Criteria criteria = new Criteria();
		criteria.orOperator(
				Criteria.where("userAccessInfo.users").elemMatch(
						criteria.andOperator(
								Criteria.where("username").is(username),
								Criteria.where("pending").is(false)
						)
				),
				Criteria.where("username").is(username)
		);
		Query query = new Query();
        return this.mongoTemplate.find(query, Workspace.class);
    }

    @Override
    public List<Workspace> findByMemberPending(String username) {
		Criteria criteria = new Criteria();
        Query query = new Query().addCriteria(
				criteria.andOperator(
						Criteria.where("username").is(username),
						Criteria.where("pending").is(false)
				)
        );

        return this.mongoTemplate.find(query, Workspace.class);
    }


    @Override
	public List<Workspace> findShared(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Workspace> findByAccessType(WorkspaceAccessType type) {
		return this.workspaceRepo.findByAccessType(type);
	}

	@Override
	public List<Workspace> findByAll() {
		return workspaceRepo.findAll();
	}

	private WorkspaceInfo toWorkspaceInfo(Workspace workspace, String username) {
		WorkspaceInfo workspaceInfo = new WorkspaceInfo();
		workspaceInfo.setAccessType(workspace.getAccessType());
		workspaceInfo.setHomePageContent(workspace.getHomePageContent());
		workspaceInfo.setMetadata(workspace.getMetadata());
		workspaceInfo.setId(workspace.getId());
		workspaceInfo.setOwner(workspace.getUsername());
		workspaceInfo.setCreated(workspace.getCreationDate());
		workspaceInfo.setUpdated(workspace.getUpdateDate());
		workspaceInfo.setAdmin(this.workspaceUserService.isAdmin(workspace, username));
		workspaceInfo.setFolders(new HashSet<>());
		if(workspace.getFolders() != null) {
			for(Folder folder: workspace.getFolders()) {
				WorkspacePermissionType wpt = this.workspaceUserService
						.getWorkspacePermissionTypeByFolder(workspace, username, folder.getId());
				if(wpt != null) {
					FolderInfo folderInfo = new FolderInfo();
					folderInfo.setId(folder.getId());
					folderInfo.setMetadata(folder.getMetadata());
					folderInfo.setChildren(folder.getChildren());
					folderInfo.setPermissionType(wpt);
					folderInfo.setPosition(folder.getPosition());
					folderInfo.setWorkspaceId(workspace.getId());
					folderInfo.setEditors(this.workspaceUserService.getFolderEditors(workspace, folder.getId()));
					workspaceInfo.getFolders().add(folderInfo);
				}
			}
		}
		return workspaceInfo;
	}

	@Override
	public WorkspaceInfo getWorkspaceInfo(String id, String username) throws WorkspaceNotFound, WorkspaceForbidden {
		Workspace workspace = this.workspaceRepo.findById(id)
				.orElseThrow(() -> new WorkspaceNotFound(id));
		if(this.workspaceUserService.hasAccessTo(username, workspace)) {
			return this.toWorkspaceInfo(workspace, username);
		}
		throw new WorkspaceForbidden();
	}

	@Override
	public WorkspaceInfo getWorkspaceInfo(Workspace workspace, String username) throws WorkspaceNotFound, WorkspaceForbidden {
		if(this.workspaceUserService.hasAccessTo(username, workspace)) {
			return this.toWorkspaceInfo(workspace, username);
		}
		throw new WorkspaceForbidden();
	}

	@Override
	public List<WorkspaceListItem> convertToDisplayList(List<Workspace> workspaces) {
		List<WorkspaceListItem> ret = new ArrayList<WorkspaceListItem>();
		for(Workspace workspace: workspaces) {
			WorkspaceListItem item = new WorkspaceListItem();
			item.setId(workspace.getId());
			item.setTitle(workspace.getMetadata().getTitle());
			item.setDescription(workspace.getMetadata().getDescription());
			item.setUsername(workspace.getUsername());
			item.setCoverPicture(workspace.getMetadata().getLogoImageId());
			item.setDateUpdated(workspace.getUpdateDate() != null? workspace.getUpdateDate().toString(): "");
			ret.add(item);
		}
		return ret;
	}


	@Override
	public FolderContent getFolderContent(String workspaceId, String folderId, String username) throws Exception {
		WorkspaceInfo ws = this.getWorkspaceInfo(workspaceId, username);
		FolderInfo folderInfo = ws.getFolders().stream().filter((f) -> f.getId().equals(folderId)).findFirst()
				.orElseThrow(() -> new Exception("Folder not found"));
		List<Ig> igs = folderInfo.getChildren()
				.stream()
				.filter(link -> link.getType().equals(Type.IGDOCUMENT))
				.map(link -> this.igService.findById(link.getId()))
				.collect(Collectors.toList());
		FolderContent content = new FolderContent();
		content.setId(folderId);
		content.setMetadata(folderInfo.getMetadata());
		content.setPermissionType(folderInfo.getPermissionType());
		content.setEditors(folderInfo.getEditors());
		content.setPosition(folderInfo.getPosition());
		content.setChildren(folderInfo.getChildren());
		content.setWorkspaceId(workspaceId);
		content.setDocuments(this.igService.convertListToDisplayList(igs));
		return content;
	}


}
