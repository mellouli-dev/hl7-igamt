package gov.nist.hit.hl7.igamt.ig.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mongodb.client.result.UpdateResult;

import gov.nist.hit.hl7.igamt.common.base.controller.BaseController;
import gov.nist.hit.hl7.igamt.common.base.domain.AccessType;
import gov.nist.hit.hl7.igamt.common.base.domain.DocumentMetadata;
import gov.nist.hit.hl7.igamt.common.base.domain.DomainInfo;
import gov.nist.hit.hl7.igamt.common.base.domain.Link;
import gov.nist.hit.hl7.igamt.common.base.domain.Scope;
import gov.nist.hit.hl7.igamt.common.base.domain.Section;
import gov.nist.hit.hl7.igamt.common.base.domain.TextSection;
import gov.nist.hit.hl7.igamt.common.base.exception.ResourceNotFoundException;
import gov.nist.hit.hl7.igamt.common.base.exception.ValidationException;
import gov.nist.hit.hl7.igamt.common.base.model.ResponseMessage;
import gov.nist.hit.hl7.igamt.common.base.model.ResponseMessage.Status;
import gov.nist.hit.hl7.igamt.common.base.util.RelationShip;
import gov.nist.hit.hl7.igamt.common.base.wrappers.AddingInfo;
import gov.nist.hit.hl7.igamt.common.base.wrappers.AddingWrapper;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.ConformanceProfile;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.event.display.MessageEventTreeNode;
import gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService;
import gov.nist.hit.hl7.igamt.conformanceprofile.service.event.MessageEventService;
import gov.nist.hit.hl7.igamt.constraints.domain.ConformanceStatement;
import gov.nist.hit.hl7.igamt.constraints.repository.ConformanceStatementRepository;
import gov.nist.hit.hl7.igamt.datatype.domain.ComplexDatatype;
import gov.nist.hit.hl7.igamt.datatype.domain.Datatype;
import gov.nist.hit.hl7.igamt.datatype.domain.display.DatatypeLabel;
import gov.nist.hit.hl7.igamt.datatype.domain.display.DatatypeSelectItemGroup;
import gov.nist.hit.hl7.igamt.datatype.service.DatatypeService;
import gov.nist.hit.hl7.igamt.display.model.IGDisplayInfo;
import gov.nist.hit.hl7.igamt.display.service.DisplayInfoService;
import gov.nist.hit.hl7.igamt.ig.controller.wrappers.CloneResponse;
import gov.nist.hit.hl7.igamt.ig.controller.wrappers.CopyWrapper;
import gov.nist.hit.hl7.igamt.ig.controller.wrappers.CreationWrapper;
import gov.nist.hit.hl7.igamt.ig.controller.wrappers.IGContentMap;
import gov.nist.hit.hl7.igamt.ig.domain.Ig;
import gov.nist.hit.hl7.igamt.ig.domain.IgDocumentConformanceStatement;
import gov.nist.hit.hl7.igamt.ig.domain.datamodel.IgDataModel;
import gov.nist.hit.hl7.igamt.ig.exceptions.AddingException;
import gov.nist.hit.hl7.igamt.ig.exceptions.CloneException;
import gov.nist.hit.hl7.igamt.ig.exceptions.IGConverterException;
import gov.nist.hit.hl7.igamt.ig.exceptions.IGNotFoundException;
import gov.nist.hit.hl7.igamt.ig.exceptions.IGUpdateException;
import gov.nist.hit.hl7.igamt.ig.exceptions.SectionNotFoundException;
import gov.nist.hit.hl7.igamt.ig.exceptions.XReferenceFoundException;
import gov.nist.hit.hl7.igamt.ig.model.AddDatatypeResponseObject;
import gov.nist.hit.hl7.igamt.ig.model.AddMessageResponseObject;
import gov.nist.hit.hl7.igamt.ig.model.AddSegmentResponseObject;
import gov.nist.hit.hl7.igamt.ig.model.AddValueSetResponseObject;
import gov.nist.hit.hl7.igamt.ig.model.IGDisplay;
import gov.nist.hit.hl7.igamt.ig.model.IgSummary;
import gov.nist.hit.hl7.igamt.ig.model.TreeNode;
import gov.nist.hit.hl7.igamt.ig.service.CrudService;
import gov.nist.hit.hl7.igamt.ig.service.DisplayConverterService;
import gov.nist.hit.hl7.igamt.ig.service.IgService;
import gov.nist.hit.hl7.igamt.segment.domain.Segment;
import gov.nist.hit.hl7.igamt.segment.domain.display.SegmentSelectItemGroup;
import gov.nist.hit.hl7.igamt.segment.service.SegmentService;
import gov.nist.hit.hl7.igamt.valueset.domain.Valueset;
import gov.nist.hit.hl7.igamt.valueset.domain.display.ValuesetLabel;
import gov.nist.hit.hl7.igamt.valueset.service.ValuesetService;
import gov.nist.hit.hl7.igamt.xreference.exceptions.XReferenceException;
import gov.nist.hit.hl7.igamt.xreference.service.RelationShipService;


@RestController
public class IGDocumentController extends BaseController {

  @Autowired
  IgService igService;
  
  @Autowired RelationShipService relationShipService;

  @Autowired
  DisplayConverterService displayConverter;

  @Autowired
  MessageEventService messageEventService;

  @Autowired
  ConformanceProfileService conformanceProfileService;

  @Autowired
  CrudService crudService;

  @Autowired
  DatatypeService datatypeService;

  @Autowired
  SegmentService segmentService;

  @Autowired
  ValuesetService valuesetService;
  
  @Autowired
  ConformanceStatementRepository conformanceStatementRepository;
  
  @Autowired
  DisplayInfoService displayInfoService;



  private static final String DATATYPE_DELETED = "DATATYPE_DELETED";
  private static final String SEGMENT_DELETED = "SEGMENT_DELETED";
  private static final String VALUESET_DELETE = "VALUESET_DELETE";
  private static final String CONFORMANCE_PROFILE_DELETE = "CONFORMANCE_PROFILE_DELETE";

  private static final String TABLE_OF_CONTENT_UPDATED = "TABLE_OF_CONTENT_UPDATED";
  private static final String METATDATA_UPDATED = "METATDATA_UPDATED";



  public IGDocumentController() {}


  @RequestMapping(value = "/api/igdocuments/{id}/datatypeLabels", method = RequestMethod.GET,
      produces = {"application/json"})
  public @ResponseBody Set<DatatypeLabel> getDatatypeLabels(@PathVariable("id") String id,
      Authentication authentication) throws IGNotFoundException {
    Ig igdoument = findIgById(id);
    Set<DatatypeLabel> result = new HashSet<DatatypeLabel>();

    for (Link link : igdoument.getDatatypeRegistry().getChildren()) {
      Datatype dt = this.datatypeService.findById(link.getId());
      if (dt != null) {
        DatatypeLabel label = new DatatypeLabel();
        label.setDomainInfo(dt.getDomainInfo());
        label.setExt(dt.getExt());
        label.setId(dt.getId());
        label.setLabel(dt.getLabel());
        if (dt instanceof ComplexDatatype)
          label.setLeaf(false);
        else
          label.setLeaf(true);
        label.setName(dt.getName());
        result.add(label);
      }
    }
    return result;
  }

  @RequestMapping(value = "/api/igdocuments/{id}/conformancestatement", method = RequestMethod.GET,
      produces = {"application/json"})
  public IgDocumentConformanceStatement getIgDocumentConformanceStatement(
      @PathVariable("id") String id, Authentication authentication) throws IGNotFoundException {
    Ig igdoument = findIgById(id);
    return igService.convertDomainToConformanceStatement(igdoument);
  }

  @RequestMapping(value = "/api/igdocuments/{id}/{viewScope}/datatypeFalvorOptions/{dtId}",
      method = RequestMethod.GET, produces = {"application/json"})
  public @ResponseBody List<DatatypeSelectItemGroup> getDatatypeFlavorsOptions(
      @PathVariable("id") String id, @PathVariable("viewScope") String viewScope,
      @PathVariable("dtId") String dtId, Authentication authentication) throws IGNotFoundException {
    Ig igdoument = findIgById(id);
    List<DatatypeSelectItemGroup> result = new ArrayList<DatatypeSelectItemGroup>();
    Set<String> ids = this.gatherIds(igdoument.getDatatypeRegistry().getChildren());

    Datatype d = this.datatypeService.findById(dtId);

    result = datatypeService.getDatatypeFlavorsOptions(ids, d, viewScope);
    return result;
  }

  @RequestMapping(value = "/api/igdocuments/{id}/{viewScope}/segmentFalvorOptions/{segId}",
      method = RequestMethod.GET, produces = {"application/json"})
  public @ResponseBody List<SegmentSelectItemGroup> getSegmentFlavorsOptions(
      @PathVariable("id") String id, @PathVariable("viewScope") String viewScope,
      @PathVariable("segId") String segId, Authentication authentication)
      throws IGNotFoundException {
    Ig igdoument = findIgById(id);
    List<SegmentSelectItemGroup> result = new ArrayList<SegmentSelectItemGroup>();
    Set<String> ids = this.gatherIds(igdoument.getSegmentRegistry().getChildren());

    Segment s = this.segmentService.findById(segId);

    result = segmentService.getSegmentFlavorsOptions(ids, s, viewScope);
    return result;
  }

  @RequestMapping(value = "/api/igdocuments/{id}/valuesetLabels", method = RequestMethod.GET,
      produces = {"application/json"})
  public @ResponseBody Set<ValuesetLabel> getValuesetLabels(@PathVariable("id") String id,
      Authentication authentication) throws IGNotFoundException {
    Ig igdoument = findIgById(id);
    Set<ValuesetLabel> result = new HashSet<ValuesetLabel>();

    for (Link link : igdoument.getValueSetRegistry().getChildren()) {
      Valueset vs = this.valuesetService.findById(link.getId());
      if (vs != null && vs.getDomainInfo() != null) {
        ValuesetLabel label = new ValuesetLabel();
        label.setId(vs.getId());
        label.setScope(vs.getDomainInfo().getScope());
        label.setLabel(vs.getBindingIdentifier());
        label.setName(vs.getName());
        label.setVersion(vs.getDomainInfo().getVersion());
        result.add(label);
      }
    }
    return result;
  }

//  /**
//   * 
//   * @param id
//   * @param response
//   * @throws ExportException
//   */
//  @RequestMapping(value = "/api/igdocuments/{id}/export/html", method = RequestMethod.GET)
//  public @ResponseBody void exportIgDocumentToHtml(@PathVariable("id") String id,
//      HttpServletResponse response) throws ExportException {
//    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    if (authentication != null) {
//      String username = authentication.getPrincipal().toString();
//      ExportedFile exportedFile = igExportService.exportIgDocumentToHtml(username, id);
//      response.setContentType("text/html");
//      response.setHeader("Content-disposition",
//          "attachment;filename=" + exportedFile.getFileName());
//      try {
//        FileCopyUtils.copy(exportedFile.getContent(), response.getOutputStream());
//      } catch (IOException e) {
//        throw new ExportException(e, "Error while sending back exported IG Document with id " + id);
//      }
//    } else {
//      throw new AuthenticationCredentialsNotFoundException("No Authentication ");
//    }
//  }


  // @RequestMapping(value = "/api/igdocuments/{id}/export/html", method = RequestMethod.GET)
  // public @ResponseBody void exportIgDocumentToHtml(@PathVariable("id") String id,
  // HttpServletResponse response) throws ExportException {
  // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  // if (authentication != null) {
  // String username = authentication.getPrincipal().toString();
  // ExportedFile exportedFile = igExportService.exportIgDocumentToHtml(username, id);
  // File coCons;
  // response.setContentType("text/html");
  // response.setHeader("Content-disposition",
  // "attachment;filename=" + "CoconstraintExcel.xsl");
  // try {
  // coCons = new ClassPathResource("CoconstaintHTMLForConverting.html").getFile();
  // InputStream targetStream = new FileInputStream(coCons);
  // FileCopyUtils.copy(targetStream, response.getOutputStream());
  // } catch (IOException e) {
  // throw new ExportException(e, "Error while sending back exported IG Document with id " + id);
  // }
  // } else {
  // throw new AuthenticationCredentialsNotFoundException("No Authentication ");
  // }
  // }

  /**
   * 
   * @param id
   * @param response
   * @param authentication
   * @throws ExportException
   */
//  @RequestMapping(value = "/api/igdocuments/{id}/export/word", method = RequestMethod.GET)
//  public @ResponseBody void exportIgDocumentToWord(@PathVariable("id") String id,
//      HttpServletResponse response, Authentication authentication) throws ExportException {
//    if (authentication != null) {
//      String username = authentication.getPrincipal().toString();
//      ExportedFile exportedFile = igExportService.exportIgDocumentToWord(username, id);
//      response.setContentType(
//          "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
//      response.setHeader("Content-disposition",
//          "attachment;filename=" + exportedFile.getFileName());
//      try {
//        FileCopyUtils.copy(exportedFile.getContent(), response.getOutputStream());
//      } catch (IOException e) {
//        throw new ExportException(e, "Error while sending back exported IG Document with id " + id);
//      }
//    } else {
//      throw new AuthenticationCredentialsNotFoundException("No Authentication ");
//    }
//  }

  @RequestMapping(value = "/api/igdocuments", method = RequestMethod.GET,
      produces = {"application/json"})
  public @ResponseBody List<IgSummary> getUserIG(Authentication authentication,
      @RequestParam("type") AccessType type) {
    String username = authentication.getPrincipal().toString();
    List<Ig> igdouments = new ArrayList<Ig>();

    if (type != null) {
      if (type.equals(AccessType.PUBLIC)) {

        igdouments = igService.findAllPreloadedIG();

      } else if (type.equals(AccessType.PRIVATE)) {

        igdouments = igService.findByUsername(username, Scope.USER);

      } else if (type.equals(AccessType.ALL)) {

        igdouments = igService.findAllUsersIG();

      } else if (type.equals(AccessType.SHARED)) {
        // TODO
      } else {

        igdouments = igService.findByUsername(username, Scope.USER);

      }
      return igService.convertListToDisplayList(igdouments);
    } else {
      igdouments = igService.findByUsername(username, Scope.USER);
      return igService.convertListToDisplayList(igdouments);
    }
  }

  /**
   * 
   * @param id
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws IGConverterException
 * @throws ResourceNotFoundException 
   */
  @RequestMapping(value = "/api/igdocuments/{id}/display", method = RequestMethod.GET,
      produces = {"application/json"})

  public @ResponseBody IGDisplay getIgDisplay(@PathVariable("id") String id,
      Authentication authentication) throws IGNotFoundException, IGConverterException, ResourceNotFoundException {

    Ig igdoument = findIgById(id);
    
    IGContentMap igData = igService.collectData(igdoument);
   
    IGDisplay ret = displayConverter.convertDomainToModel(igdoument,igData);
    
//    igService.buildDependencies(igData);
    
//    List<RelationShip> relationShip=relationShipService.findAll();
//    System.out.println(relationShip);
//    
    return ret;
  }

  /**
   *
   * @param id
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws IGConverterException
   */
  @RequestMapping(value = "/api/igdocuments/{id}", method = RequestMethod.GET,
          produces = {"application/json"})

  public @ResponseBody Ig getIg(@PathVariable("id") String id,
                                              Authentication authentication) throws IGNotFoundException {

    return findIgById(id);
  }
  
  @RequestMapping(value = "/api/igdocuments/{id}/grand", method = RequestMethod.GET, produces = {"application/json"})
  public @ResponseBody IgDataModel getIgGrandObject(@PathVariable("id") String id, Authentication authentication) throws Exception {
    
    return this.igService.generateDataModel(findIgById(id));
}

  /**
   *
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws IGUpdateException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/section", method = RequestMethod.POST,
          produces = {"application/json"})

  public @ResponseBody ResponseMessage<Object> updateIg(
          @PathVariable("id") String id,
          @RequestBody Section section,
          Authentication authentication)
          throws IGNotFoundException, IGUpdateException {
    Ig ig = findIgById(id);
    if(!ig.getUsername().equals(authentication.getPrincipal().toString())) {
      return new ResponseMessage<Object>(Status.FAILED, TABLE_OF_CONTENT_UPDATED, ig.getId(), new Date());
    } else {
      Section igSection = this.findSectionById(ig.getContent(), section.getId());
      igSection.setDescription(section.getDescription());
      igSection.setLabel(section.getLabel());
      this.igService.save(ig);
      return new ResponseMessage<Object>(Status.SUCCESS, TABLE_OF_CONTENT_UPDATED, ig.getId(), new Date());
    }
  }

  /**
   * 
   * @param id
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws IGUpdateException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/updatetoc", method = RequestMethod.POST,
      produces = {"application/json"})

  public @ResponseBody ResponseMessage<Object> get(@PathVariable("id") String id,
      @RequestBody List<TreeNode> toc, Authentication authentication)
      throws IGNotFoundException, IGUpdateException {

    Set<TextSection> content = displayConverter.convertTocToDomain(toc);

    UpdateResult updateResult = igService.updateAttribute(id, "content", content);
    if (!updateResult.wasAcknowledged()) {
      throw new IGUpdateException(id);
    }
    return new ResponseMessage<Object>(Status.SUCCESS, TABLE_OF_CONTENT_UPDATED, id, new Date());
  }

  /**
   *
   * @param id
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws IGUpdateException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/update/sections", method = RequestMethod.POST,
          produces = {"application/json"})

  public @ResponseBody ResponseMessage<Object> updateSections(@PathVariable("id") String id,
                                                   @RequestBody Set<TextSection> content, Authentication authentication)
          throws IGNotFoundException, IGUpdateException {

    UpdateResult updateResult = igService.updateAttribute(id, "content", content);
    if (!updateResult.wasAcknowledged()) {
      throw new IGUpdateException(id);
    }
    return new ResponseMessage<Object>(Status.SUCCESS, TABLE_OF_CONTENT_UPDATED, id, new Date());
  }

  @RequestMapping(value = "/api/igdocuments/{id}/updatemetadata", method = RequestMethod.POST,
      produces = {"application/json"})

  public @ResponseBody ResponseMessage<Object> get(@PathVariable("id") String id,
      @RequestBody DocumentMetadata metadata, Authentication authentication)
      throws IGNotFoundException, IGUpdateException {
    UpdateResult updateResult = igService.updateAttribute(id, "metadata", metadata);
    if (!updateResult.wasAcknowledged()) {
      throw new IGUpdateException("Could not update IG Metadata ");
    }
    return new ResponseMessage<Object>(Status.SUCCESS, METATDATA_UPDATED, id, new Date());
  }


  @RequestMapping(value = "/api/igdocuments/findMessageEvents/{version:.+}",
      method = RequestMethod.GET, produces = {"application/json"})

  public @ResponseBody ResponseMessage<List<MessageEventTreeNode>> getMessageEvents(
      @PathVariable("version") String version, Authentication authentication) {
    try {
    		
      List<MessageEventTreeNode> list = messageEventService.findByHl7Version(version);
      
      return new ResponseMessage<List<MessageEventTreeNode>>(Status.SUCCESS, null, null, null, false, null, list);
    } catch (Exception e) {
      throw e;
    }

  }


  /**
   * 
   * @param wrapper
   * @param authentication
   * @return
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws FileNotFoundException
   * @throws IOException
   * @throws AddingException
   */
  @RequestMapping(value = "/api/igdocuments/create", method = RequestMethod.POST,
      produces = {"application/json"})
  public @ResponseBody ResponseMessage<String> create(@RequestBody CreationWrapper wrapper,
      Authentication authentication) throws JsonParseException, JsonMappingException,
      FileNotFoundException, IOException, AddingException {

    try {
      String username = authentication.getPrincipal().toString();
      Ig empty = igService.createEmptyIg();
      Set<String> savedIds = new HashSet<String>();
      for (AddingInfo ev : wrapper.getMsgEvts()) {
        ConformanceProfile profile = conformanceProfileService.findById(ev.getOriginalId());
        if (profile != null) {
          ConformanceProfile clone = profile.clone();
          clone.setUsername(username);
          clone.getDomainInfo().setScope(Scope.USER);
          clone.setEvent(ev.getName());
          clone.setName(ev.getExt());
         // clone.setIdentifier(ev.getExt());
          clone = conformanceProfileService.save(clone);
          savedIds.add(clone.getId());
        }
      }
      empty.setUsername(username);
      DomainInfo info = new DomainInfo();
      info.setScope(Scope.USER);
      empty.setDomainInfo(info);
      empty.setMetadata(wrapper.getMetadata());
      crudService.AddConformanceProfilesToEmptyIg(savedIds, empty);
     
      Ig ret = igService.save(empty);

      return new ResponseMessage<String>(Status.SUCCESS, "", "IG created Successfuly", ret.getId(),
          false, ret.getUpdateDate(), ret.getId());

    } catch (Exception e) {
      throw e;
    }

  }


  public IgService getIgService() {
    return igService;
  }


  public void setIgService(IgService igService) {
    this.igService = igService;
  }



  /**
   * 
   * @param id
   * @param sectionId
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws SectionNotFoundException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/section/{sectionId}", method = RequestMethod.GET,
      produces = {"application/json"})

  public @ResponseBody TextSection findSectionById(@PathVariable("id") String id,
      @PathVariable("sectionId") String sectionId, Authentication authentication)
      throws IGNotFoundException, SectionNotFoundException {
    Ig ig = igService.findIgContentById(id);
    if (ig != null) {
      TextSection s = igService.findSectionById(ig.getContent(), sectionId);
      if (s == null) {
        throw new SectionNotFoundException("Section Not Foud");
      } else {
        return s;
      }
    } else {
      throw new IGNotFoundException("Cannot found Id document");
    }
  }


  /**
   * 
   * @param id
   * @param datatypeId
   * @param authentication
 * @return 
   * @return
   * @throws IGNotFoundException
   * @throws XReferenceException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/datatypes/{datatypeId}/crossref",
      method = RequestMethod.GET, produces = {"application/json"})
  public @ResponseBody List<RelationShip> findDatatypeCrossRef(
      @PathVariable("id") String id, @PathVariable("datatypeId") String datatypeId,
      Authentication authentication) throws IGNotFoundException, XReferenceException {
	  
	 return this.relationShipService.findCrossReferences(datatypeId);
    
  }

  /**
   * 
   * @param id
   * @param segmentId
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws XReferenceException
   */
//  @RequestMapping(value = "/api/igdocuments/{id}/segments/{segmentId}/crossref",
//      method = RequestMethod.GET, produces = {"application/json"})
//  public @ResponseBody Map<String, List<CrossRefsNode>> findSegmentCrossRef(
//      @PathVariable("id") String id, @PathVariable("segmentId") String segmentId,
//      Authentication authentication) throws IGNotFoundException, XReferenceException {
//    Ig ig = findIgById(id);
//    Set<String> filterConformanceProfileIds =
//        gatherIds(ig.getConformanceProfileRegistry().getChildren());
//    Map<String, List<CrossRefsNode>> results =
//        xRefService.getSegmentReferences(segmentId, filterConformanceProfileIds);
//    return results;
//  }


  /**
   * 
   * @param id
   * @param valuesetId
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws XReferenceException
   */
//  @RequestMapping(value = "/api/igdocuments/{id}/valuesets/{valuesetId}/crossref",
//      method = RequestMethod.GET, produces = {"application/json"})
//  public @ResponseBody Map<String, List<CrossRefsNode>> findValueSetCrossRef(
//      @PathVariable("id") String id, @PathVariable("valuesetId") String valuesetId,
//      Authentication authentication) throws IGNotFoundException, XReferenceException {
//    Ig ig = findIgById(id);
//    Set<String> filterDatatypeIds = gatherIds(ig.getDatatypeRegistry().getChildren());
//    Set<String> filterSegmentIds = gatherIds(ig.getSegmentRegistry().getChildren());
//    Set<String> filterConformanceProfileIds =
//        gatherIds(ig.getConformanceProfileRegistry().getChildren());
//    Map<String, List<CrossRefsNode>> results = xRefService.getValueSetReferences(id, filterDatatypeIds,
//        filterSegmentIds, filterConformanceProfileIds);
//    return results;
//  }


  /**
   * 
   * @param id
   * @param datatypeId
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws XReferenceFoundException
   * @throws XReferenceException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/datatypes/{datatypeId}/delete",
      method = RequestMethod.DELETE, produces = {"application/json"})
  public ResponseMessage deleteDatatype(@PathVariable("id") String id,
      @PathVariable("datatypeId") String datatypeId, Authentication authentication)
      throws IGNotFoundException, XReferenceFoundException, XReferenceException {
//    Map<String, List<CrossRefsNode>> xreferences = findDatatypeCrossRef(id, datatypeId, authentication);
//    if (xreferences != null && !xreferences.isEmpty()) {
//      throw new XReferenceFoundException(datatypeId, xreferences);
//    }
    Ig ig = findIgById(id);
    Link found = findLinkById(datatypeId, ig.getDatatypeRegistry().getChildren());
    if (found != null) {
      ig.getDatatypeRegistry().getChildren().remove(found);
    }
    Datatype datatype = datatypeService.findById(datatypeId);
    if (datatype != null) {
      if (datatype.getDomainInfo().getScope().equals(Scope.USER)) {
        datatypeService.delete(datatype);
      }
    }
    igService.save(ig);
    return new ResponseMessage(Status.SUCCESS, DATATYPE_DELETED, datatypeId, new Date());
  }


  /**
   * 
   * @param id
   * @param segmentId
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws XReferenceFoundException
   * @throws XReferenceException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/segments/{segmentId}/delete",
      method = RequestMethod.DELETE, produces = {"application/json"})
  public ResponseMessage deleteSegment(@PathVariable("id") String id,
      @PathVariable("segmentId") String segmentId, Authentication authentication)
      throws IGNotFoundException, XReferenceFoundException, XReferenceException {
  //  Map<String, List<CrossRefsNode>> xreferences = findSegmentCrossRef(id, segmentId, authentication);
//    if (xreferences != null && !xreferences.isEmpty()) {
//      throw new XReferenceFoundException(segmentId, xreferences);
//    }
    Ig ig = findIgById(id);
    Link found = findLinkById(segmentId, ig.getSegmentRegistry().getChildren());
    if (found != null) {
      ig.getSegmentRegistry().getChildren().remove(found);
    }
    Segment segment = segmentService.findById(segmentId);
    if (segment != null) {
      if (segment.getDomainInfo().getScope().equals(Scope.USER)) {
        segmentService.delete(segment);
      }
    }
    ig = igService.save(ig);
    return new ResponseMessage(Status.SUCCESS, SEGMENT_DELETED, segmentId, new Date());
  }

  /**
   * 
   * @param id
   * @param valuesetId
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws XReferenceFoundException
   * @throws XReferenceException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/valuesets/{valuesetId}/delete",
      method = RequestMethod.DELETE, produces = {"application/json"})
  public ResponseMessage deleteValueSet(@PathVariable("id") String id,
      @PathVariable("valuesetId") String valuesetId, Authentication authentication)
      throws IGNotFoundException, XReferenceFoundException, XReferenceException {
//    Map<String, List<CrossRefsNode>> xreferences = findValueSetCrossRef(id, valuesetId, authentication);
//    if (xreferences != null && !xreferences.isEmpty()) {
//      throw new XReferenceFoundException(valuesetId, xreferences);
//    }

    Ig ig = findIgById(id);
    Link found = findLinkById(valuesetId, ig.getValueSetRegistry().getChildren());
    if (found != null) {
      ig.getValueSetRegistry().getChildren().remove(found);
    }
    Valueset valueSet = valuesetService.findById(valuesetId);
    if (valueSet != null) {
      if (valueSet.getDomainInfo().getScope().equals(Scope.USER)) {
        valuesetService.delete(valueSet);
      }
    }
    ig = igService.save(ig);
    return new ResponseMessage(Status.SUCCESS, VALUESET_DELETE, valuesetId, new Date());
  }


  /**
   * 
   * @param id
   * @param conformanceProfileId
   * @param authentication
   * @return
   * @throws IGNotFoundException
   * @throws XReferenceFoundException
   * @throws XReferenceException
   */
  @RequestMapping(value = "/api/igdocuments/{id}/conformanceprofiles/{conformanceprofileId}/delete",
      method = RequestMethod.DELETE, produces = {"application/json"})
  public ResponseMessage deleteConformanceProfile(@PathVariable("id") String id,
      @PathVariable("conformanceprofileId") String conformanceProfileId,
      Authentication authentication)
      throws IGNotFoundException, XReferenceFoundException, XReferenceException {

    Ig ig = findIgById(id);
    Link found =
        findLinkById(conformanceProfileId, ig.getConformanceProfileRegistry().getChildren());
    if (found != null) {
      ig.getConformanceProfileRegistry().getChildren().remove(found);
    }
    ConformanceProfile conformanceProfile =
        conformanceProfileService.findById(conformanceProfileId);
    if (conformanceProfile != null) {
      if (conformanceProfile.getDomainInfo().getScope().equals(Scope.USER)) {
        conformanceProfileService.delete(conformanceProfile);
      }
    }
    ig = igService.save(ig);
    return new ResponseMessage(Status.SUCCESS, CONFORMANCE_PROFILE_DELETE, conformanceProfileId,
        new Date());
  }


  @RequestMapping(value = "/api/igdocuments/{id}/conformanceprofiles/{conformanceProfileId}/clone",
      method = RequestMethod.POST, produces = {"application/json"})
  public ResponseMessage<CloneResponse> cloneConformanceProfile(@RequestBody CopyWrapper wrapper,
      @PathVariable("id") String id,
      @PathVariable("conformanceProfileId") String conformanceProfileId,
      Authentication authentication) throws CloneException, IGNotFoundException {
    Ig ig = findIgById(id);
    String username = authentication.getName();
    ConformanceProfile profile = conformanceProfileService.findById(wrapper.getSelected().getOriginalId());
    if (profile == null) {
      throw new CloneException("Failed to build conformance profile tree structure");
    }
    ConformanceProfile clone = profile.clone();
    clone.setUsername(username);
    clone.setIdentifier(wrapper.getSelected().getExt());
    clone.getDomainInfo().setScope(Scope.USER);
    clone = conformanceProfileService.save(clone);

    
    if(clone.getBinding() != null && clone.getBinding().getConformanceStatementIds() != null) {
      for(String csId: clone.getBinding().getConformanceStatementIds()){
        Optional<ConformanceStatement> container = this.conformanceStatementRepository.findById(csId);
        if(container.isPresent()){
          container.get().addSourceId(clone.getId());
          this.conformanceStatementRepository.save(container.get());
        }
      }
    }
    ig.getConformanceProfileRegistry().getChildren().add(new Link(clone.getId(), clone.getDomainInfo(), ig.getConformanceProfileRegistry().getChildren().size() + 1));
    ig = igService.save(ig);
    
    CloneResponse response = new CloneResponse();
    response.setId(clone.getId());
    response.setReg(ig.getConformanceProfileRegistry());
    response.setDisplay(displayInfoService.convertConformanceProfile(clone));
    
    return new ResponseMessage<CloneResponse>(Status.SUCCESS, "", "Conformance profile clone Success", clone.getId(), false, clone.getUpdateDate(),response);
  }


  @RequestMapping(value = "/api/igdocuments/{id}/segments/{segmentId}/clone",
      method = RequestMethod.POST, produces = {"application/json"})
  public ResponseMessage<CloneResponse> cloneSegment(@RequestBody CopyWrapper wrapper,
      @PathVariable("id") String id, @PathVariable("segmentId") String segmentId,
      Authentication authentication)
      throws IGNotFoundException, ValidationException, CloneException {
    Ig ig = findIgById(id);
    String username = authentication.getPrincipal().toString();
    Segment segment = segmentService.findById(segmentId);
    if (segment == null) {
      throw new CloneException("Cannot find segment with id=" + segmentId);
    }
    Segment clone = segment.clone();
    clone.setUsername(username);
    clone.setName(segment.getName());
    clone.setExt(wrapper.getSelected().getExt());
    clone.getDomainInfo().setScope(Scope.USER);

    clone = segmentService.save(clone);
    
    if(clone.getBinding() != null && clone.getBinding().getConformanceStatementIds() != null) {
      for(String csId: clone.getBinding().getConformanceStatementIds()){
        Optional<ConformanceStatement> container = this.conformanceStatementRepository.findById(csId);
        if(container.isPresent()){
          container.get().addSourceId(clone.getId());
          this.conformanceStatementRepository.save(container.get());
        }
      }
    }
    ig.getSegmentRegistry().getChildren().add(new Link(clone.getId(), clone.getDomainInfo(), ig.getSegmentRegistry().getChildren().size() + 1));
    ig=igService.save(ig);
    CloneResponse response = new CloneResponse();
    response.setId(clone.getId());
    response.setReg(ig.getSegmentRegistry());
    response.setDisplay(displayInfoService.convertSegment(clone));
    
    return new ResponseMessage<CloneResponse>(Status.SUCCESS, "", "Segment profile clone Success", clone.getId(), false, clone.getUpdateDate(),response);
  }



  @RequestMapping(value = "/api/igdocuments/{id}/datatypes/{datatypeId}/clone",
      method = RequestMethod.POST, produces = {"application/json"})
  public ResponseMessage<CloneResponse> copyDatatype(@RequestBody CopyWrapper wrapper,
      @PathVariable("id") String id, @PathVariable("datatypeId") String datatypeId,
      Authentication authentication) throws IGNotFoundException, CloneException {
    Ig ig = findIgById(id);
    String username = authentication.getPrincipal().toString();
    Datatype datatype = datatypeService.findById(datatypeId);
    if (datatype == null) {
      throw new CloneException("Cannot find datatype with id=" + datatypeId);
    }
    Datatype clone = datatype.clone();
    clone.setUsername(username);
    clone.setId(new ObjectId().toString());
    clone.setExt(wrapper.getSelected().getExt());
    clone.getDomainInfo().setScope(Scope.USER);

    clone = datatypeService.save(clone);
    
    if(clone.getBinding() != null && clone.getBinding().getConformanceStatementIds() != null) {
      for(String csId: clone.getBinding().getConformanceStatementIds()){
        Optional<ConformanceStatement> container = this.conformanceStatementRepository.findById(csId);
        if(container.isPresent()){
          container.get().addSourceId(clone.getId());
          this.conformanceStatementRepository.save(container.get());
        }
      }
    }
    ig.getDatatypeRegistry().getChildren().add(new Link(clone.getId(), clone.getDomainInfo(), ig.getDatatypeRegistry().getChildren().size()+1));
    ig=igService.save(ig);
    CloneResponse response = new CloneResponse();
    response.setId(clone.getId());
    response.setReg(ig.getDatatypeRegistry());
    response.setDisplay(displayInfoService.convertDatatype(clone));
    return new ResponseMessage<CloneResponse>(Status.SUCCESS, "", "Datatype clone Success", clone.getId(), false, clone.getUpdateDate(), response);
  }


  @RequestMapping(value = "/api/igdocuments/{id}/valuesets/{valuesetId}/clone",
      method = RequestMethod.POST, produces = {"application/json"})

  public ResponseMessage<CloneResponse> cloneValueSet(@RequestBody CopyWrapper wrapper,
      @PathVariable("id") String id, @PathVariable("valuesetId") String valuesetId,
      Authentication authentication) throws CloneException, IGNotFoundException {
    Ig ig = findIgById(id);
    String username = authentication.getPrincipal().toString();
    Valueset valueset = valuesetService.findById(valuesetId);
    if (valueset == null) {
      throw new CloneException("Cannot find valueset with id=" + valuesetId);
    }
    Valueset clone = valueset.clone();
    clone.getDomainInfo().setScope(Scope.USER);

    clone.setUsername(username);
    clone.setBindingIdentifier(wrapper.getSelected().getExt());
    clone.getDomainInfo().setScope(Scope.USER);
    clone = valuesetService.save(clone);
    ig.getValueSetRegistry().getChildren().add(new Link(clone.getId(), clone.getDomainInfo(),
        ig.getValueSetRegistry().getChildren().size() + 1));
    ig=igService.save(ig);
    CloneResponse response = new CloneResponse();
    response.setId(clone.getId());
    response.setReg(ig.getValueSetRegistry());
    response.setDisplay(displayInfoService.convertValueSet(clone));
    return new ResponseMessage<CloneResponse>(Status.SUCCESS, "", "Value Set clone Success",
        clone.getId(), false, clone.getUpdateDate(), response);
  }


  @RequestMapping(value = "/api/igdocuments/{id}/conformanceprofiles/add",
      method = RequestMethod.POST, produces = {"application/json"})
  public ResponseMessage<IGDisplayInfo> addConforanceProfile(
      @PathVariable("id") String id, @RequestBody AddingWrapper wrapper,
      Authentication authentication) throws IGNotFoundException, AddingException {
    String username = authentication.getPrincipal().toString();
    Ig ig = findIgById(id);
    Set<String> savedIds = new HashSet<String>();
    for (AddingInfo ev : wrapper.getSelected()) {
      ConformanceProfile profile = conformanceProfileService.findById(ev.getOriginalId());
      if (profile != null) {
        ConformanceProfile clone = profile.clone();
        clone.setUsername(username);
        clone.getDomainInfo().setScope(Scope.USER);
        clone.setEvent(ev.getName());
        clone.setIdentifier(ev.getExt());
        clone.setName(ev.getExt());
        clone = conformanceProfileService.save(clone);
        savedIds.add(clone.getId());
      }
    }
   AddMessageResponseObject objects = crudService.addConformanceProfiles(savedIds, ig);
   ig = igService.save(ig);
   IGDisplayInfo info = new IGDisplayInfo();
   info.setIg(ig);
   info.setMessages(displayInfoService.convertConformanceProfiles(objects.getConformanceProfiles()));
   info.setSegments(displayInfoService.convertSegments(objects.getSegments()));
   info.setDatatypes(displayInfoService.convertDatatypes(objects.getDatatypes()));
   info.setValueSets(displayInfoService.convertValueSets(objects.getValueSets()));

    return new ResponseMessage<IGDisplayInfo>(Status.SUCCESS, "",
        "Conformance profile Added Succesfully", ig.getId(), false, ig.getUpdateDate(),
        info);

  }


  @RequestMapping(value = "/api/igdocuments/{id}/segments/add", method = RequestMethod.POST,
      produces = {"application/json"})
  public ResponseMessage<IGDisplayInfo> addSegments(@PathVariable("id") String id,
      @RequestBody AddingWrapper wrapper, Authentication authentication)
      throws IGNotFoundException, ValidationException, AddingException {
    String username = authentication.getPrincipal().toString();
    Ig ig = findIgById(id);
    Set<String> savedIds = new HashSet<String>();
    for (AddingInfo elm : wrapper.getSelected()) {
      if (elm.isFlavor()) {
        Segment segment = segmentService.findById(elm.getOriginalId());
        if (segment != null) {
          Segment clone = segment.clone();
          clone.getDomainInfo().setScope(Scope.USER);

          clone.setUsername(username);
          clone.setName(segment.getName());
          clone.setExt(elm.getExt());
          clone = segmentService.save(clone);
          savedIds.add(clone.getId());
        }
      } else {
        savedIds.add(elm.getId());
      }
    }
    AddSegmentResponseObject objects = crudService.addSegments(savedIds, ig);
    ig = igService.save(ig);
    IGDisplayInfo info = new IGDisplayInfo();
    info.setIg(ig);
    info.setSegments(displayInfoService.convertSegments(objects.getSegments()));
    info.setDatatypes(displayInfoService.convertDatatypes(objects.getDatatypes()));
    info.setValueSets(displayInfoService.convertValueSets(objects.getValueSets()));

    return new ResponseMessage<IGDisplayInfo>(Status.SUCCESS, "",
        "segment Added Succesfully", ig.getId(), false, ig.getUpdateDate(),
        info);
  }


  @RequestMapping(value = "/api/igdocuments/{id}/datatypes/add", method = RequestMethod.POST,
      produces = {"application/json"})
  public ResponseMessage<IGDisplayInfo> addDatatypes(@PathVariable("id") String id,
      @RequestBody AddingWrapper wrapper, Authentication authentication)
      throws IGNotFoundException, AddingException {
    String username = authentication.getPrincipal().toString();
    Ig ig = findIgById(id);
    Set<String> savedIds = new HashSet<String>();
    for (AddingInfo elm : wrapper.getSelected()) {
      if (elm.isFlavor()) {
        Datatype datatype = datatypeService.findById(elm.getOriginalId());
        if (datatype != null) {
          Datatype clone = datatype.clone();
          clone.getDomainInfo().setScope(Scope.USER);

          clone.setUsername(username);
          clone.setName(datatype.getName());
          clone.setExt(elm.getExt());
          clone = datatypeService.save(clone);

          savedIds.add(clone.getId());
        }
      } else {
        savedIds.add(elm.getId());
      }
    }
    AddDatatypeResponseObject objects = crudService.addDatatypes(savedIds, ig);
    ig = igService.save(ig);
    IGDisplayInfo info = new IGDisplayInfo();
    info.setIg(ig);
    info.setDatatypes(displayInfoService.convertDatatypes(objects.getDatatypes()));
    info.setValueSets(displayInfoService.convertValueSets(objects.getValueSets()));

    return new ResponseMessage<IGDisplayInfo>(Status.SUCCESS, "",
        "Data type Added Succesfully", ig.getId(), false, ig.getUpdateDate(),
        info);
  }

  @RequestMapping(value = "/api/igdocuments/{id}/valuesets/add", method = RequestMethod.POST,
      produces = {"application/json"})
  public ResponseMessage<IGDisplayInfo> addValueSets(@PathVariable("id") String id,
      @RequestBody AddingWrapper wrapper, Authentication authentication)
      throws IGNotFoundException, AddingException {
    String username = authentication.getPrincipal().toString();
    Ig ig = findIgById(id);
    Set<String> savedIds = new HashSet<String>();
    for (AddingInfo elm : wrapper.getSelected()) {
      if (elm.isFlavor()) {
        Valueset valueset = valuesetService.findById(elm.getOriginalId());
        if (valueset != null) {
          Valueset clone = valueset.clone();
          clone.getDomainInfo().setScope(Scope.USER);
          clone.setUsername(username);
          clone.setBindingIdentifier(elm.getName());
          clone = valuesetService.save(clone);
          savedIds.add(clone.getId());
        }
      } else {
        savedIds.add(elm.getId());
      }
    }
    AddValueSetResponseObject objects = crudService.addValueSets(savedIds, ig);
    ig = igService.save(ig);
    IGDisplayInfo info = new IGDisplayInfo();
    info.setIg(ig);
    info.setValueSets(displayInfoService.convertValueSets(objects.getValueSets()));

    return new ResponseMessage<IGDisplayInfo>(Status.SUCCESS, "",
        "Value Sets Added Succesfully", ig.getId(), false, ig.getUpdateDate(),info);
  }

//  @RequestMapping(value = "/api/igdocuments/{id}/clone", method = RequestMethod.GET,
//      produces = {"application/json"})
//  public @ResponseBody ResponseMessage<String> copy(@PathVariable("id") String id,
//      Authentication authentication) throws IGNotFoundException, CoConstraintSaveException {
//    String username = authentication.getPrincipal().toString();
//
//    Ig ig = findIgById(id);
//    Ig clone = this.igService.clone(ig, username);
//    clone.getDomainInfo().setScope(Scope.USER);
//    clone.getMetadata().setTitle(clone.getMetadata().getTitle()+"[clone]");
//    clone = igService.save(clone);
//    return new ResponseMessage<String>(Status.SUCCESS, "", "Ig Cloned Successfully", clone.getId(),
//        false, clone.getUpdateDate(), clone.getId());
//  }

  @RequestMapping(value = "/api/igdocuments/{id}/clone", method = RequestMethod.GET,
	      produces = {"application/json"})
	  public @ResponseBody ResponseMessage<String> copy(@PathVariable("id") String id,
	      Authentication authentication) throws IGNotFoundException{
	    String username = authentication.getPrincipal().toString();

	    Ig ig = findIgById(id);
	    Ig clone = this.igService.clone(ig, username);
	    clone.getDomainInfo().setScope(Scope.USER);
	    clone.getMetadata().setTitle(clone.getMetadata().getTitle()+"[clone]");
	    clone = igService.save(clone);
	    return new ResponseMessage<String>(Status.SUCCESS, "", "Ig Cloned Successfully", clone.getId(),
	        false, clone.getUpdateDate(), clone.getId());
	  }

  @RequestMapping(value = "/api/igdocuments/{id}", method = RequestMethod.DELETE,
      produces = {"application/json"})
  public @ResponseBody ResponseMessage<String> archive(@PathVariable("id") String id,
      Authentication authentication) throws IGNotFoundException {

    Ig ig = findIgById(id);
    igService.delete(ig);
    return new ResponseMessage<String>(Status.SUCCESS, "", "Ig deleted Successfully", ig.getId(),
        false, ig.getUpdateDate(), ig.getId());
  }
  

  @RequestMapping(value = "/api/igdocuments/{id}/state", method = RequestMethod.GET,
      produces = {"application/json"})
  public @ResponseBody IGDisplayInfo getState(@PathVariable("id") String id,
      Authentication authentication) throws IGNotFoundException {

    Ig ig = findIgById(id);
    displayInfoService.covertIgToDisplay(ig);
    return  displayInfoService.covertIgToDisplay(ig);
  }
  
  
  

  /**
   * 
   * @param links
   * @return
   */
  private Set<String> gatherIds(Set<Link> links) {
    Set<String> results = new HashSet<String>();
    links.forEach(link -> results.add(link.getId()));
    return results;
  }

  /**
   * 
   * @param id
   * @param links
   * @return
   */
  private Link findLinkById(String id, Set<Link> links) {
    for (Link link : links) {
      if (link.getId().equals(id)) {
        return link;
      }
    }
    return null;
  }

  /**
   * @param content
   * @param sectionId
   * @return
   */
  private TextSection findSectionById(Set<TextSection> content, String sectionId) {
    // TODO Auto-generated method stub
    for (TextSection s : content) {
      TextSection ret = findSectionInside(s, sectionId);
      if (ret != null) {
        return ret;
      }
    }
    return null;

  }

  /**
   * @param s
   * @param sectionId
   * @return
   */
  private TextSection findSectionInside(TextSection s, String sectionId) {
    // TODO Auto-generated method stub
    if (s.getId().equals(sectionId)) {
      return s;
    }
    if (s.getChildren() != null && s.getChildren().size() > 0) {
      for (TextSection ss : s.getChildren()) {
        TextSection ret = findSectionInside(ss, sectionId);
        if (ret != null) {
          return ret;
        }
      }
      return null;
    }
    return null;
  }

  private Ig findIgById(String id) throws IGNotFoundException {
    Ig ig = igService.findById(id);
    if (ig == null) {
      throw new IGNotFoundException(id);
    }
    return ig;
  }
  
  
  

}
