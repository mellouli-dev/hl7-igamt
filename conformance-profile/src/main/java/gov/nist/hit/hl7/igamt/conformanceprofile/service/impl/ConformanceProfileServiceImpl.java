/**
 * 
 * This software was developed at the National Institute of Standards and Technology by employees of
 * the Federal Government in the course of their official duties. Pursuant to title 17 Section 105
 * of the United States Code this software is not subject to copyright protection and is in the
 * public domain. This is an experimental system. NIST assumes no responsibility whatsoever for its
 * use by other parties, and makes no guarantees, expressed or implied, about its quality,
 * reliability, or any other characteristic. We would appreciate acknowledgement if the software is
 * used. This software can be redistributed and/or modified freely provided that any derivative
 * works bear some notice that they are derived from it, and any modified versions bear some notice
 * that they have been modified.
 * 
 */
package gov.nist.hit.hl7.igamt.conformanceprofile.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import gov.nist.hit.hl7.igamt.coconstraints.model.CoConstraintBinding;
import gov.nist.hit.hl7.igamt.coconstraints.model.CoConstraintBindingSegment;
import gov.nist.hit.hl7.igamt.coconstraints.model.CoConstraintTableConditionalBinding;
import gov.nist.hit.hl7.igamt.coconstraints.service.CoConstraintService;
import gov.nist.hit.hl7.igamt.common.binding.domain.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nist.hit.hl7.igamt.common.base.domain.Link;
import gov.nist.hit.hl7.igamt.common.base.domain.MsgStructElement;
import gov.nist.hit.hl7.igamt.common.base.domain.ProfileType;
import gov.nist.hit.hl7.igamt.common.base.domain.RealKey;
import gov.nist.hit.hl7.igamt.common.base.domain.Ref;
import gov.nist.hit.hl7.igamt.common.base.domain.Resource;
import gov.nist.hit.hl7.igamt.common.base.domain.Role;
import gov.nist.hit.hl7.igamt.common.base.domain.Scope;
import gov.nist.hit.hl7.igamt.common.base.domain.Type;
import gov.nist.hit.hl7.igamt.common.base.domain.Usage;
import gov.nist.hit.hl7.igamt.common.base.domain.ValuesetBinding;
import gov.nist.hit.hl7.igamt.common.base.domain.display.ViewScope;
import gov.nist.hit.hl7.igamt.common.base.exception.ValidationException;
import gov.nist.hit.hl7.igamt.common.base.model.SectionType;
import gov.nist.hit.hl7.igamt.common.base.util.ReferenceIndentifier;
import gov.nist.hit.hl7.igamt.common.base.util.ReferenceLocation;
import gov.nist.hit.hl7.igamt.common.base.util.RelationShip;
import gov.nist.hit.hl7.igamt.common.base.util.ValidationUtil;
import gov.nist.hit.hl7.igamt.common.base.domain.Comment;
import gov.nist.hit.hl7.igamt.common.binding.service.BindingService;
import gov.nist.hit.hl7.igamt.common.change.entity.domain.ChangeItemDomain;
import gov.nist.hit.hl7.igamt.common.change.entity.domain.ChangeType;
import gov.nist.hit.hl7.igamt.common.change.entity.domain.PropertyType;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.ConformanceProfile;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.Group;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.MessageProfileIdentifier;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.SegmentRef;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.SegmentRefOrGroup;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.ConformanceProfileConformanceStatement;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.ConformanceProfileDisplayModel;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.ConformanceProfileStructureDisplay;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.ConformanceProfileStructureTreeModel;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.DisplayConformanceProfileMetadata;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.DisplayConformanceProfilePostDef;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.DisplayConformanceProfilePreDef;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.GroupDisplayModel;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.GroupStructureTreeModel;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.SegmentLabel;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.SegmentRefDisplayModel;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.display.SegmentRefStructureTreeModel;
import gov.nist.hit.hl7.igamt.conformanceprofile.exception.ConformanceProfileValidationException;
import gov.nist.hit.hl7.igamt.conformanceprofile.repository.ConformanceProfileRepository;
import gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService;
import gov.nist.hit.hl7.igamt.constraints.domain.ConformanceStatement;
import gov.nist.hit.hl7.igamt.constraints.domain.DisplayPredicate;
import gov.nist.hit.hl7.igamt.constraints.domain.Level;
import gov.nist.hit.hl7.igamt.constraints.domain.Predicate;
import gov.nist.hit.hl7.igamt.constraints.domain.display.ConformanceStatementsContainer;
import gov.nist.hit.hl7.igamt.constraints.repository.ConformanceStatementRepository;
import gov.nist.hit.hl7.igamt.constraints.repository.PredicateRepository;
import gov.nist.hit.hl7.igamt.datatype.domain.ComplexDatatype;
import gov.nist.hit.hl7.igamt.datatype.domain.Component;
import gov.nist.hit.hl7.igamt.datatype.domain.Datatype;
import gov.nist.hit.hl7.igamt.datatype.domain.display.BindingDisplay;
import gov.nist.hit.hl7.igamt.datatype.domain.display.ComponentDisplayDataModel;
import gov.nist.hit.hl7.igamt.datatype.domain.display.ComponentStructureTreeModel;
import gov.nist.hit.hl7.igamt.datatype.domain.display.DatatypeLabel;
import gov.nist.hit.hl7.igamt.datatype.domain.display.DisplayValuesetBinding;
import gov.nist.hit.hl7.igamt.datatype.domain.display.SubComponentDisplayDataModel;
import gov.nist.hit.hl7.igamt.datatype.domain.display.SubComponentStructureTreeModel;
import gov.nist.hit.hl7.igamt.datatype.service.DatatypeService;
import gov.nist.hit.hl7.igamt.segment.domain.Field;
import gov.nist.hit.hl7.igamt.segment.domain.Segment;
import gov.nist.hit.hl7.igamt.segment.domain.display.FieldDisplayDataModel;
import gov.nist.hit.hl7.igamt.segment.domain.display.FieldStructureTreeModel;
import gov.nist.hit.hl7.igamt.segment.service.SegmentService;
import gov.nist.hit.hl7.igamt.valueset.domain.Valueset;
import gov.nist.hit.hl7.igamt.valueset.service.ValuesetService;

/**
 *
 * @author Maxence Lefort on Mar 9, 2018.
 */
@Service("conformanceProfileService")
public class ConformanceProfileServiceImpl implements ConformanceProfileService {

  @Autowired
  ConformanceProfileRepository conformanceProfileRepository;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  SegmentService segmentService;

  @Autowired
  DatatypeService datatypeService;

  @Autowired
  ValuesetService valuesetService;

  @Autowired
  private ConformanceStatementRepository conformanceStatementRepository;

  @Autowired
  private PredicateRepository predicateRepository;

  @Autowired
  private BindingService bindingService;

  @Autowired
  private CoConstraintService coConstraintService;

  @Override
  public ConformanceProfile create(ConformanceProfile conformanceProfile) {
    conformanceProfile.setId(new String());
    return conformanceProfileRepository.save(conformanceProfile);
  }

  @Override
  public ConformanceProfile save(ConformanceProfile conformanceProfile) {
    conformanceProfile.setUpdateDate(new Date());
    return conformanceProfileRepository.save(conformanceProfile);
  }

  @Override
  public List<ConformanceProfile> findAll() {
    return conformanceProfileRepository.findAll();
  }

  @Override
  public void delete(ConformanceProfile conformanceProfile) {
    conformanceProfileRepository.delete(conformanceProfile);
  }

  @Override
  public void delete(String key) {
    conformanceProfileRepository.deleteById(key);
  }

  @Override
  public void removeCollection() {
    conformanceProfileRepository.deleteAll();

  }

  @Override
  public List<ConformanceProfile> findByIdentifier(String identifier) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByIdentifier(identifier);
  }

  @Override
  public List<ConformanceProfile> findByMessageType(String messageType) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByMessageType(messageType);
  }

  @Override
  public List<ConformanceProfile> findByEvent(String messageType) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByEvent(messageType);
  }

  @Override
  public List<ConformanceProfile> findByStructID(String messageType) {
    return conformanceProfileRepository.findByStructID(messageType);
  }

  @Override
  public List<ConformanceProfile> findByDomainInfoVersion(String version) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByDomainInfoVersion(version);
  }

  @Override
  public List<ConformanceProfile> findByDomainInfoScope(String scope) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByDomainInfoScope(scope);
  }

  @Override
  public List<ConformanceProfile> findByDomainInfoScopeAndDomainInfoVersion(String scope, String verion) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByDomainInfoScopeAndDomainInfoVersion(scope, verion);
  }

  @Override
  public List<ConformanceProfile> findByName(String name) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByName(name);
  }

  @Override
  public List<ConformanceProfile> findByDomainInfoScopeAndDomainInfoVersionAndName(String scope, String version,
      String name) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByDomainInfoScopeAndDomainInfoVersionAndName(scope, version, name);
  }

  @Override
  public List<ConformanceProfile> findByDomainInfoVersionAndName(String version, String name) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByDomainInfoVersionAndName(version, name);
  }

  @Override
  public List<ConformanceProfile> findByDomainInfoScopeAndName(String scope, String name) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByDomainInfoScopeAndName(scope, name);
  }

  @Override
  public ConformanceProfile findById(String id) {
    ConformanceProfile conformanceProfile = conformanceProfileRepository.findOneById(id);
    return conformanceProfile;
  }

  @Override
  public DisplayConformanceProfileMetadata convertDomainToMetadata(ConformanceProfile conformanceProfile) {
    if (conformanceProfile != null) {
      DisplayConformanceProfileMetadata result = new DisplayConformanceProfileMetadata();
      result.setDescription(conformanceProfile.getDescription());
      result.setDomainInfo(conformanceProfile.getDomainInfo());
      result.setId(conformanceProfile.getId());
      result.setIdentifier(conformanceProfile.getIdentifier());
      result.setMessageType(conformanceProfile.getMessageType());
      result.setName(conformanceProfile.getName());
      result.setStructId(conformanceProfile.getStructID());
      // result.setAuthorNotes(conformanceProfile.getComment());
      return result;
    }
    return null;
  }

  @Override
  public DisplayConformanceProfilePreDef convertDomainToPredef(ConformanceProfile conformanceProfile) {
    if (conformanceProfile != null) {
      DisplayConformanceProfilePreDef result = new DisplayConformanceProfilePreDef();
      result.setDomainInfo(conformanceProfile.getDomainInfo());
      result.setId(conformanceProfile.getId());
      result.setIdentifier(conformanceProfile.getIdentifier());
      result.setMessageType(conformanceProfile.getMessageType());
      result.setName(conformanceProfile.getName());
      result.setStructId(conformanceProfile.getStructID());
      result.setPreDef(conformanceProfile.getPreDef());
      return result;
    }
    return null;
  }

  @Override
  public DisplayConformanceProfilePostDef convertDomainToPostdef(ConformanceProfile conformanceProfile) {
    if (conformanceProfile != null) {
      DisplayConformanceProfilePostDef result = new DisplayConformanceProfilePostDef();
      result.setDomainInfo(conformanceProfile.getDomainInfo());
      result.setId(conformanceProfile.getId());
      result.setIdentifier(conformanceProfile.getIdentifier());
      result.setMessageType(conformanceProfile.getMessageType());
      result.setName(conformanceProfile.getName());
      result.setStructId(conformanceProfile.getStructID());
      result.setPostDef(conformanceProfile.getPostDef());
      return result;
    }
    return null;
  }

  @Override
  public ConformanceProfile findDisplayFormat(String id) {
    List<ConformanceProfile> cps = conformanceProfileRepository.findDisplayFormat(id);
    if (cps != null && !cps.isEmpty()) {
      return cps.get(0);
    }
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConformanceProfileConformanceStatement convertDomainToConformanceStatement(
      ConformanceProfile conformanceProfile, String documentId, boolean readOnly) {
    if (conformanceProfile != null) {
      ConformanceProfileConformanceStatement result = new ConformanceProfileConformanceStatement();
      result.complete(result, conformanceProfile, SectionType.CONFORMANCESTATEMENTS, readOnly);
      result.setIdentifier(conformanceProfile.getIdentifier());
      result.setMessageType(conformanceProfile.getMessageType());
      result.setName(conformanceProfile.getName());
      result.setStructId(conformanceProfile.getStructID());
      result.setType(Type.CONFORMANCEPROFILE);
      result.setAssociatedDTConformanceStatementMap(new HashMap<String, ConformanceStatementsContainer>());
      result.setAssociatedSEGConformanceStatementMap(new HashMap<String, ConformanceStatementsContainer>());
      result.setStructure(this.convertDomainToContextStructure(conformanceProfile,
          result.getAssociatedSEGConformanceStatementMap(), result.getAssociatedDTConformanceStatementMap())
          .getChildren());
      Set<ConformanceStatement> cfs = new HashSet<ConformanceStatement>();
      if (conformanceProfile.getBinding() != null) {
        cfs = this.collectCS(conformanceProfile.getBinding().getConformanceStatementIds());
      }
      result.setConformanceStatements(cfs);
      result.setAvailableConformanceStatements(this.collectAvaliableConformanceStatements(documentId,
          conformanceProfile.getId(), conformanceProfile.getStructID()));
      return result;
    }
    return null;
  }

  public Set<ConformanceStatement> collectAvaliableConformanceStatements(String documentId, String messageId,
      String structureId) {
    Set<ConformanceStatement> found = this.conformanceStatementRepository
        .findByIgDocumentIdAndStructureId(documentId, structureId);
    Set<ConformanceStatement> result = new HashSet<ConformanceStatement>();
    for (ConformanceStatement cs : found) {
      if (!cs.getSourceIds().contains(messageId))
        result.add(cs);
    }
    return result;
  }

  private Set<ConformanceStatement> collectCS(Set<String> conformanceStatementIds) {
    Set<ConformanceStatement> result = new HashSet<ConformanceStatement>();
    if (conformanceStatementIds != null) {
      for (String id : conformanceStatementIds) {
        Optional<ConformanceStatement> cs = this.conformanceStatementRepository.findById(id);
        if (cs.isPresent())
          result.add(cs.get());
      }
    }

    return result;
  }

  private void validateMsgStructElement(MsgStructElement f) throws ValidationException {
    if (f instanceof SegmentRef) {
      validateSegmentRef((SegmentRef) f);
    } else if (f instanceof Group) {
      validateGroup((Group) f);
    } else {
      throw new ValidationException("Unknown element type " + f.getClass());
    }
  }

  private void validateSegmentRef(SegmentRef f) throws ValidationException {
    try {
      ValidationUtil.validateUsage(f.getUsage(), f.getMin());
      ValidationUtil.validateCardinality(f.getMin(), f.getMax(), f.getUsage());
      if (f.getRef() == null || StringUtils.isEmpty(f.getRef().getId())) {
        throw new ValidationException("Segment not found");
      }
      Segment s = segmentService.findById(f.getRef().getId());
      if (s == null) {
        throw new ValidationException("Segment not found");
      }
    } catch (ValidationException e) {
      throw new ValidationException(f.getPosition() + ":" + e.getMessage());
    }

  }

  private void validateGroup(Group f) throws ValidationException {
    try {
      ValidationUtil.validateUsage(f.getUsage(), f.getMin());
      ValidationUtil.validateCardinality(f.getMin(), f.getMax(), f.getUsage());
    } catch (ValidationException e) {
      throw new ValidationException(f.getPosition() + ":" + e.getMessage());
    }

    if (f.getChildren() != null && !f.getChildren().isEmpty()) {
      for (MsgStructElement child : f.getChildren()) {
        try {
          validateMsgStructElement(child);
        } catch (ValidationException e) {
          String[] message = e.getMessage().split(Pattern.quote(":"));
          throw new ValidationException(f.getPosition() + "." + message[0] + ":" + message[1]);
        }
      }
    }

  }

  @Override
  public void validate(DisplayConformanceProfileMetadata metadata) throws ConformanceProfileValidationException {
    if (!metadata.getDomainInfo().getScope().equals(Scope.HL7STANDARD)) {
      if (StringUtils.isEmpty(metadata.getName())) {
        throw new ConformanceProfileValidationException("Name is missing");
      }
      if (StringUtils.isEmpty(metadata.getIdentifier())) {
        throw new ConformanceProfileValidationException("Identifier is missing");
      }

      if (StringUtils.isEmpty(metadata.getMessageType())) {
        throw new ConformanceProfileValidationException("Message Type is missing");
      }

      if (StringUtils.isEmpty(metadata.getName())) {
        throw new ConformanceProfileValidationException("Name is missing");
      }

      if (StringUtils.isEmpty(metadata.getStructId())) {
        throw new ConformanceProfileValidationException("Message Struct ID is missing");
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService#
   * cloneConformanceProfile(java.util.HashMap, java.util.HashMap,
   * gov.nist.hit.hl7.igamt.common.base.domain.Link, java.lang.String)
   */
  @Override
  public Link cloneConformanceProfile(String key, HashMap<RealKey, String> newKeys, Link l, String username, Scope scope) {
    ConformanceProfile old = this.findById(l.getId());
    ConformanceProfile elm = old.clone();
    elm.getDomainInfo().setScope(scope);
    elm.setOrigin(l.getId());
    Link newLink = l.clone(key);
    newLink.setDomainInfo(elm.getDomainInfo());
    newLink.setOrigin(l.getId());
    updateDependencies(elm, newKeys);
    elm.setId(newLink.getId());
    elm.setUsername(username);
    this.save(elm);
    return newLink;
  }

  /**
   * @param elm
   * @param datatypesMap
   * @param valuesetsMap
   */
  private void updateDependencies(ConformanceProfile elm, HashMap<RealKey, String> newKeys) {
    // TODO Auto-generated method stub

    processAndSubstitute(elm, newKeys);
    if(elm.getBinding() !=null) {
      this.bindingService.substitute(elm.getBinding(), newKeys);
    }
    if(elm.getCoConstraintsBindings() !=null) {
      for (CoConstraintBinding binding : elm.getCoConstraintsBindings()) {
        if(binding.getBindings() !=null) {
          for(CoConstraintBindingSegment segBinding: binding.getBindings()) {
            RealKey segKey = new RealKey(segBinding.getFlavorId(), Type.SEGMENT);
            if(segBinding.getFlavorId() !=null && newKeys.containsKey(segKey)) {
              segBinding.setFlavorId(newKeys.get(segKey));
            }
            if(segBinding.getTables()  !=null ) {
              for( CoConstraintTableConditionalBinding ccBinding: segBinding.getTables()) {
                if(ccBinding.getValue() !=null) {
                  this.coConstraintService.updateDepenedencies(ccBinding.getValue() , newKeys);
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * @param elm
   * @param valuesetsMap
   */
  private void processAndSubstitute(ConformanceProfile cp,  HashMap<RealKey, String> newKeys) {
    // TODO Auto-generated method stub
    for (MsgStructElement segOrgroup : cp.getChildren()) {
      processAndSubstituteSegmentorGroup(segOrgroup, newKeys);
    }

  }

  private void processAndSubstituteSegmentorGroup(MsgStructElement segOrgroup,  HashMap<RealKey, String> newKeys) {
    // TODO Auto-generated method stub
    if (segOrgroup instanceof SegmentRef) {
      SegmentRef ref = (SegmentRef) segOrgroup;
      if (ref.getRef() != null && ref.getRef().getId() != null) {
        RealKey key = new RealKey(ref.getRef().getId(), Type.SEGMENT);
        if (newKeys.containsKey(key)) {
          ref.getRef().setId(newKeys.get(key));
        }

      }
    } else if (segOrgroup instanceof Group) {
      Group g = (Group) segOrgroup;
      for (MsgStructElement child : g.getChildren()) {
        processAndSubstituteSegmentorGroup(child, newKeys);
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService#
   * convertDomainToDisplayStructure(gov.nist.hit.hl7.igamt.conformanceprofile.
   * domain. ConformanceProfile)
   */
  @Override
  public ConformanceProfileStructureDisplay convertDomainToDisplayStructure(ConformanceProfile conformanceProfile,
      boolean readOnly) {
    HashMap<String, Valueset> valueSetsMap = new HashMap<String, Valueset>();
    HashMap<String, Datatype> datatypesMap = new HashMap<String, Datatype>();
    HashMap<String, Segment> segmentsMap = new HashMap<String, Segment>();

    ConformanceProfileStructureDisplay result = new ConformanceProfileStructureDisplay();
    result.complete(result, conformanceProfile, SectionType.STRUCTURE, readOnly);
    String label = conformanceProfile.getName();
    if (conformanceProfile.getIdentifier() != null)
      label = label + "-" + conformanceProfile.getIdentifier();
    result.setLabel(label);

    if (conformanceProfile.getChildren() != null && conformanceProfile.getChildren().size() > 0) {
      for (SegmentRefOrGroup sog : conformanceProfile.getChildren()) {
        if (sog instanceof SegmentRef) {
          result.addSegment(this.createSegmentRefStructureTreeModel((SegmentRef) sog, datatypesMap,
              segmentsMap, valueSetsMap, null, null, conformanceProfile.getBinding(),
              conformanceProfile.getId()));
        } else if (sog instanceof Group) {
          result.addGroup(this.createGroupStructureTreeModel((Group) sog, datatypesMap, segmentsMap,
              valueSetsMap, null, null, conformanceProfile.getBinding(), conformanceProfile.getId()));
        }
      }
    }

    result.setType(Type.CONFORMANCEPROFILE);
    result.setName(conformanceProfile.getStructID());
    return result;
  }

  @Override
  public ConformanceProfileStructureDisplay convertDomainToDisplayStructureFromContext(
      ConformanceProfile conformanceProfile, String contextId, boolean readOnly) {
    HashMap<String, Valueset> valueSetsMap = new HashMap<String, Valueset>();
    HashMap<String, Datatype> datatypesMap = new HashMap<String, Datatype>();
    HashMap<String, Segment> segmentsMap = new HashMap<String, Segment>();

    ConformanceProfileStructureDisplay result = new ConformanceProfileStructureDisplay();
    result.complete(result, conformanceProfile, SectionType.STRUCTURE, readOnly);

    Group g = this.findGroup(conformanceProfile.getChildren(), contextId);
    String label = g.getName();
    result.setLabel(label);
    if (g.getChildren() != null && g.getChildren().size() > 0) {
      for (SegmentRefOrGroup sog : g.getChildren()) {
        if (sog instanceof SegmentRef) {
          result.addSegment(this.createSegmentRefStructureTreeModel((SegmentRef) sog, datatypesMap,
              segmentsMap, valueSetsMap, null, null, conformanceProfile.getBinding(),
              conformanceProfile.getId()));
        } else if (sog instanceof Group) {
          result.addGroup(this.createGroupStructureTreeModel((Group) sog, datatypesMap, segmentsMap,
              valueSetsMap, null, null, conformanceProfile.getBinding(), conformanceProfile.getId()));
        }
      }
    }
    result.setType(Type.GROUP);
    return result;
  }

  /**
   * @param list
   * @param contextId
   * @return
   */
  private Group findGroup(Set<SegmentRefOrGroup> list, String contextId) {
    String[] splits = contextId.split("\\-");

    for (SegmentRefOrGroup sog : list) {
      if (sog instanceof Group) {
        Group g = (Group) sog;
        if (g.getId().equals(splits[0])) {
          if (splits.length > 1)
            return findGroup(g.getChildren(), contextId.replaceAll(splits[0] + "-", ""));
          else
            return g;
        }
      }

    }
    return null;
  }

  private SegmentRefStructureTreeModel createSegmentRefStructureTreeModel(SegmentRef segmentRef,
      HashMap<String, Datatype> datatypesMap, HashMap<String, Segment> segmentsMap,
      HashMap<String, Valueset> valueSetsMap, String parentIdPath, String parentPath, Binding parentBinding,
      String conformanceProfileId) {
    SegmentRefStructureTreeModel result = new SegmentRefStructureTreeModel();
    SegmentRefDisplayModel segmentRefDisplayModel = new SegmentRefDisplayModel(segmentRef);
    if (parentIdPath == null)
      segmentRefDisplayModel.setIdPath(segmentRef.getId());
    else
      segmentRefDisplayModel.setIdPath(parentIdPath + "-" + segmentRef.getId());
    if (parentPath == null)
      segmentRefDisplayModel.setPath("" + segmentRef.getPosition());
    else
      segmentRefDisplayModel.setPath(parentPath + "-" + segmentRef.getPosition());
    StructureElementBinding childSeb = this.findStructureElementBindingByIdFromBinding(parentBinding,
        segmentRef.getId());
    if (childSeb != null) {
      segmentRefDisplayModel.addBinding(this.createBindingDisplay(childSeb, conformanceProfileId,
          ViewScope.CONFORMANCEPROFILE, 1, valueSetsMap));
      if (childSeb.getPredicateId() != null) {
        Optional<Predicate> op = this.predicateRepository.findById(childSeb.getPredicateId());
        if (op.isPresent() && op.get().getTrueUsage() != null && op.get().getFalseUsage() != null) {
          segmentRefDisplayModel.setTrueUsage(op.get().getTrueUsage());
          segmentRefDisplayModel.setFalseUsage(op.get().getFalseUsage());
          segmentRefDisplayModel.setPredicate(op.get());
          if (op.get().getIdentifier() != null)
            segmentRefDisplayModel.getPredicate().setIdentifier(segmentRefDisplayModel.getIdPath());
        }
      }
    }
    segmentRefDisplayModel.setViewScope(ViewScope.CONFORMANCEPROFILE);
    Segment s = this.segmentService.findById(segmentRef.getRef().getId());

    if (s != null) {
      segmentRefDisplayModel.setSegmentLabel(this.createSegmentLabel(s));
      segmentRefDisplayModel.setName(s.getName());
      if (s.getChildren() != null && s.getChildren().size() > 0) {
        for (Field f : s.getChildren()) {
          Datatype childDt = this.findDatatype(f.getRef().getId(), datatypesMap);
          if (childDt != null) {
            FieldStructureTreeModel fieldStructureTreeModel = new FieldStructureTreeModel();
            FieldDisplayDataModel fModel = new FieldDisplayDataModel(f);
            fModel.setViewScope(ViewScope.SEGMENT);
            fModel.setIdPath(segmentRefDisplayModel.getIdPath() + "-" + f.getId());
            fModel.setPath(f.getPosition() + "");
            fModel.setDatatypeLabel(this.createDatatypeLabel(childDt));
            StructureElementBinding childChildSeb = this
                .findStructureElementBindingByIdFromBinding(childSeb, f.getId());
            if (childChildSeb != null) {
              fModel.addBinding(this.createBindingDisplay(childChildSeb, conformanceProfileId,
                  ViewScope.CONFORMANCEPROFILE, 1, valueSetsMap));
              if (childChildSeb.getPredicateId() != null) {
                Optional<Predicate> op = this.predicateRepository
                    .findById(childChildSeb.getPredicateId());
                if (op.isPresent() && op.get().getTrueUsage() != null
                    && op.get().getFalseUsage() != null) {
                  fModel.setTrueUsage(op.get().getTrueUsage());
                  fModel.setFalseUsage(op.get().getFalseUsage());
                  fModel.setPredicate(op.get());
                  if (op.get().getIdentifier() != null)
                    fModel.getPredicate().setIdentifier(fModel.getIdPath());
                }
              }
            }
            StructureElementBinding fSeb = this.findStructureElementBindingByFieldIdForSegment(s,
                f.getId());
            if (fSeb != null) {
              fModel.addBinding(
                  this.createBindingDisplay(fSeb, s.getId(), ViewScope.SEGMENT, 2, valueSetsMap));
              if (fSeb.getPredicateId() != null) {
                Optional<Predicate> op = this.predicateRepository.findById(fSeb.getPredicateId());
                if (op.isPresent() && op.get().getTrueUsage() != null
                    && op.get().getFalseUsage() != null) {
                  fModel.setTrueUsage(op.get().getTrueUsage());
                  fModel.setFalseUsage(op.get().getFalseUsage());
                  fModel.setPredicate(op.get());
                  if (op.get().getIdentifier() != null)
                    fModel.getPredicate().setIdentifier(f.getId());
                }
              }
            }
            fieldStructureTreeModel.setData(fModel);

            if (childDt instanceof ComplexDatatype) {
              ComplexDatatype fieldDatatype = (ComplexDatatype) childDt;
              if (fieldDatatype.getComponents() != null && fieldDatatype.getComponents().size() > 0) {
                for (Component c : fieldDatatype.getComponents()) {
                  Datatype childChildDt = this.findDatatype(c.getRef().getId(), datatypesMap);
                  if (childChildDt != null) {
                    ComponentStructureTreeModel componentStructureTreeModel = new ComponentStructureTreeModel();
                    ComponentDisplayDataModel cModel = new ComponentDisplayDataModel(c);
                    cModel.setViewScope(ViewScope.SEGMENT);
                    cModel.setIdPath(
                        segmentRefDisplayModel.getIdPath() + "-" + f.getId() + "-" + c.getId());
                    cModel.setPath(f.getPosition() + "-" + c.getPosition());
                    cModel.setDatatypeLabel(this.createDatatypeLabel(childChildDt));
                    StructureElementBinding childChildChildSeb = this
                        .findStructureElementBindingByIdFromBinding(childChildSeb, c.getId());
                    if (childChildChildSeb != null) {
                      cModel.addBinding(
                          this.createBindingDisplay(childChildChildSeb, conformanceProfileId,
                              ViewScope.CONFORMANCEPROFILE, 1, valueSetsMap));
                      if (childChildChildSeb.getPredicateId() != null) {
                        Optional<Predicate> op = this.predicateRepository
                            .findById(childChildChildSeb.getPredicateId());
                        if (op.isPresent() && op.get().getTrueUsage() != null
                            && op.get().getFalseUsage() != null) {
                          cModel.setTrueUsage(op.get().getTrueUsage());
                          cModel.setFalseUsage(op.get().getFalseUsage());
                          cModel.setPredicate(op.get());
                          if (op.get().getIdentifier() != null)
                            cModel.getPredicate().setIdentifier(cModel.getIdPath());
                        }
                      }
                    }
                    StructureElementBinding childFSeb = this
                        .findStructureElementBindingByComponentIdFromStructureElementBinding(
                            fSeb, c.getId());
                    if (childFSeb != null) {
                      cModel.addBinding(this.createBindingDisplay(childFSeb, s.getId(),
                          ViewScope.SEGMENT, 2, valueSetsMap));
                      if (childFSeb.getPredicateId() != null) {
                        Optional<Predicate> op = this.predicateRepository
                            .findById(childFSeb.getPredicateId());
                        if (op.isPresent() && op.get().getTrueUsage() != null
                            && op.get().getFalseUsage() != null) {
                          cModel.setTrueUsage(op.get().getTrueUsage());
                          cModel.setFalseUsage(op.get().getFalseUsage());
                          cModel.setPredicate(op.get());
                          if (op.get().getIdentifier() != null)
                            cModel.getPredicate()
                            .setIdentifier(f.getId() + "-" + c.getId());
                        }
                      }
                    }
                    StructureElementBinding cSeb = this
                        .findStructureElementBindingByComponentIdForDatatype(childDt,
                            c.getId());
                    if (cSeb != null) {
                      cModel.addBinding(this.createBindingDisplay(cSeb, childDt.getId(),
                          ViewScope.DATATYPE, 3, valueSetsMap));
                      if (cSeb.getPredicateId() != null) {
                        Optional<Predicate> op = this.predicateRepository
                            .findById(cSeb.getPredicateId());
                        if (op.isPresent() && op.get().getTrueUsage() != null
                            && op.get().getFalseUsage() != null) {
                          cModel.setTrueUsage(op.get().getTrueUsage());
                          cModel.setFalseUsage(op.get().getFalseUsage());
                          cModel.setPredicate(op.get());
                          if (op.get().getIdentifier() != null)
                            cModel.getPredicate().setIdentifier(c.getId());
                        }
                      }
                    }
                    componentStructureTreeModel.setData(cModel);
                    if (childChildDt instanceof ComplexDatatype) {
                      ComplexDatatype componentDatatype = (ComplexDatatype) childChildDt;
                      if (componentDatatype.getComponents() != null
                          && componentDatatype.getComponents().size() > 0) {
                        for (Component sc : componentDatatype.getComponents()) {
                          Datatype childChildChildDt = this.findDatatype(sc.getRef().getId(),
                              datatypesMap);
                          if (childChildChildDt != null) {
                            SubComponentStructureTreeModel subComponentStructureTreeModel = new SubComponentStructureTreeModel();
                            SubComponentDisplayDataModel scModel = new SubComponentDisplayDataModel(
                                sc);
                            scModel.setViewScope(ViewScope.SEGMENT);

                            scModel.setIdPath(segmentRefDisplayModel.getIdPath() + "-"
                                + f.getId() + "-" + c.getId() + "-" + sc.getId());
                            scModel.setPath(f.getPosition() + "-" + c.getPosition() + "-"
                                + sc.getPosition());
                            scModel.setDatatypeLabel(
                                this.createDatatypeLabel(childChildChildDt));
                            StructureElementBinding childChildChildChildSeb = this
                                .findStructureElementBindingByIdFromBinding(
                                    childChildChildSeb, sc.getId());
                            if (childChildChildChildSeb != null) {
                              scModel.addBinding(this.createBindingDisplay(
                                  childChildChildChildSeb, conformanceProfileId,
                                  ViewScope.CONFORMANCEPROFILE, 1, valueSetsMap));
                              if (childChildChildChildSeb.getPredicateId() != null) {
                                Optional<Predicate> op = this.predicateRepository
                                    .findById(childChildChildChildSeb
                                        .getPredicateId());
                                if (op.isPresent() && op.get().getTrueUsage() != null
                                    && op.get().getFalseUsage() != null) {
                                  scModel.setTrueUsage(op.get().getTrueUsage());
                                  scModel.setFalseUsage(op.get().getFalseUsage());
                                  scModel.setPredicate(op.get());
                                  if (op.get().getIdentifier() != null)
                                    scModel.getPredicate()
                                    .setIdentifier(scModel.getIdPath());
                                }
                              }
                            }
                            StructureElementBinding childChildFSeb = this
                                .findStructureElementBindingByComponentIdFromStructureElementBinding(
                                    childFSeb, sc.getId());
                            if (childChildFSeb != null) {
                              scModel.addBinding(this.createBindingDisplay(childChildFSeb,
                                  s.getId(), ViewScope.SEGMENT, 2, valueSetsMap));
                              if (childChildFSeb.getPredicateId() != null) {
                                Optional<Predicate> op = this.predicateRepository
                                    .findById(childChildFSeb.getPredicateId());
                                if (op.isPresent() && op.get().getTrueUsage() != null
                                    && op.get().getFalseUsage() != null) {
                                  scModel.setTrueUsage(op.get().getTrueUsage());
                                  scModel.setFalseUsage(op.get().getFalseUsage());
                                  scModel.setPredicate(op.get());
                                  if (op.get().getIdentifier() != null)
                                    scModel.getPredicate().setIdentifier(f.getId()
                                        + "-" + c.getId() + "-" + sc.getId());
                                }
                              }
                            }
                            StructureElementBinding childCSeb = this
                                .findStructureElementBindingByComponentIdFromStructureElementBinding(
                                    cSeb, sc.getId());
                            if (childCSeb != null) {
                              scModel.addBinding(this.createBindingDisplay(childCSeb,
                                  childDt.getId(), ViewScope.DATATYPE, 3,
                                  valueSetsMap));
                              if (childCSeb.getPredicateId() != null) {
                                Optional<Predicate> op = this.predicateRepository
                                    .findById(childCSeb.getPredicateId());
                                if (op.isPresent() && op.get().getTrueUsage() != null
                                    && op.get().getFalseUsage() != null) {
                                  scModel.setTrueUsage(op.get().getTrueUsage());
                                  scModel.setFalseUsage(op.get().getFalseUsage());
                                  scModel.setPredicate(op.get());
                                  if (op.get().getIdentifier() != null)
                                    scModel.getPredicate().setIdentifier(
                                        c.getId() + "-" + sc.getId());
                                }
                              }
                            }
                            StructureElementBinding scSeb = this
                                .findStructureElementBindingByComponentIdForDatatype(
                                    childChildDt, sc.getId());
                            if (scSeb != null) {
                              scModel.addBinding(this.createBindingDisplay(scSeb,
                                  childChildDt.getId(), ViewScope.DATATYPE, 4,
                                  valueSetsMap));
                              if (scSeb.getPredicateId() != null) {
                                Optional<Predicate> op = this.predicateRepository
                                    .findById(scSeb.getPredicateId());
                                if (op.isPresent() && op.get().getTrueUsage() != null
                                    && op.get().getFalseUsage() != null) {
                                  scModel.setTrueUsage(op.get().getTrueUsage());
                                  scModel.setFalseUsage(op.get().getFalseUsage());
                                  scModel.setPredicate(op.get());
                                  if (op.get().getIdentifier() != null)
                                    scModel.getPredicate()
                                    .setIdentifier(sc.getId());
                                }
                              }
                            }
                            subComponentStructureTreeModel.setData(scModel);
                            componentStructureTreeModel
                            .addSubComponent(subComponentStructureTreeModel);
                          } else {
                            // TODO need to handle exception
                          }
                        }

                      }
                    }
                    fieldStructureTreeModel.addComponent(componentStructureTreeModel);
                  } else {
                    // TODO need to handle exception
                  }
                }
              }
            }
            result.addField(fieldStructureTreeModel);
          } else {
            // TODO need to handle exception
          }

        }
      }

    }
    result.setData(segmentRefDisplayModel);
    return result;
  }

  private SegmentLabel createSegmentLabel(Segment s) {
    SegmentLabel label = new SegmentLabel();
    label.setDomainInfo(s.getDomainInfo());
    label.setExt(s.getExt());
    label.setId(s.getId());
    label.setLabel(s.getLabel());
    label.setName(s.getName());
    return label;
  }

  private GroupStructureTreeModel createGroupStructureTreeModel(Group group, HashMap<String, Datatype> datatypesMap,
      HashMap<String, Segment> segmentsMap, HashMap<String, Valueset> valueSetsMap, String parentIdPath,
      String parentPath, Binding parentBinding, String conformanceProfileId) {
    GroupStructureTreeModel result = new GroupStructureTreeModel();
    GroupDisplayModel groupDisplayModel = new GroupDisplayModel(group);

    if (parentIdPath == null)
      groupDisplayModel.setIdPath(group.getId());
    else
      groupDisplayModel.setIdPath(parentIdPath + "-" + group.getId());

    if (parentPath == null)
      groupDisplayModel.setPath("" + group.getPosition());
    else
      groupDisplayModel.setPath(parentPath + "-" + group.getPosition());

    groupDisplayModel.setViewScope(ViewScope.CONFORMANCEPROFILE);

    StructureElementBinding childSeb = this.findStructureElementBindingByIdFromBinding(parentBinding,
        group.getId());
    if (childSeb != null) {
      groupDisplayModel.addBinding(this.createBindingDisplay(childSeb, conformanceProfileId,
          ViewScope.CONFORMANCEPROFILE, 1, valueSetsMap));
      if (childSeb.getPredicateId() != null) {
        Optional<Predicate> op = this.predicateRepository.findById(childSeb.getPredicateId());
        if (op.isPresent() && op.get().getTrueUsage() != null && op.get().getFalseUsage() != null) {
          groupDisplayModel.setTrueUsage(op.get().getTrueUsage());
          groupDisplayModel.setFalseUsage(op.get().getFalseUsage());
          groupDisplayModel.setPredicate(op.get());
          if (op.get().getIdentifier() != null)
            groupDisplayModel.getPredicate().setIdentifier(groupDisplayModel.getIdPath());
        }
      }
    }

    result.setData(groupDisplayModel);

    if (group.getChildren() != null && group.getChildren().size() > 0) {
      for (SegmentRefOrGroup sog : group.getChildren()) {
        if (sog instanceof SegmentRef) {
          result.addSegment(this.createSegmentRefStructureTreeModel((SegmentRef) sog, datatypesMap,
              segmentsMap, valueSetsMap, groupDisplayModel.getIdPath(), groupDisplayModel.getPath(),
              childSeb, conformanceProfileId));
        } else if (sog instanceof Group) {
          result.addGroup(this.createGroupStructureTreeModel((Group) sog, datatypesMap, segmentsMap,
              valueSetsMap, groupDisplayModel.getIdPath(), groupDisplayModel.getPath(), childSeb,
              conformanceProfileId));
        }
      }
    }
    return result;
  }

  /**
   * @param parentSeb
   * @param id
   * @return
   */
  private StructureElementBinding findStructureElementBindingByIdFromBinding(Binding binding, String id) {
    if (binding != null && binding.getChildren() != null) {
      for (StructureElementBinding child : binding.getChildren()) {
        if (child.getElementId().equals(id))
          return child;
      }
    }
    return null;
  }

  private BindingDisplay createBindingDisplay(StructureElementBinding seb, String sourceId, ViewScope sourceType,
      int priority, HashMap<String, Valueset> valueSetsMap) {
    BindingDisplay bindingDisplay = new BindingDisplay();
    bindingDisplay.setSourceId(sourceId);
    bindingDisplay.setSourceType(sourceType);
    bindingDisplay.setPriority(priority);
    bindingDisplay.setInternalSingleCode(seb.getInternalSingleCode());

    if (seb.getPredicateId() != null) {
      Optional<Predicate> op = this.predicateRepository.findById(seb.getPredicateId());
      if (op.isPresent())
        bindingDisplay.setPredicate(this.predicateRepository.findById(seb.getPredicateId()).get());
    }
    bindingDisplay.setValuesetBindings(this.covertDisplayVSBinding(seb.getValuesetBindings(), valueSetsMap));
    return bindingDisplay;
  }

  private Set<DisplayValuesetBinding> covertDisplayVSBinding(Set<ValuesetBinding> valuesetBindings,
      HashMap<String, Valueset> valueSetsMap) {
    //		if (valuesetBindings != null) {
    //			Set<DisplayValuesetBinding> result = new HashSet<DisplayValuesetBinding>();
    //			for (ValuesetBinding vb : valuesetBindings) {
    //				Valueset vs = valueSetsMap.get(vb.getValuesetId());
    //				if (vs == null) {
    //					vs = this.valuesetService.findById(vb.getValuesetId());
    //					valueSetsMap.put(vs.getId(), vs);
    //				}
    //				if (vs != null) {
    //					DisplayValuesetBinding dvb = new DisplayValuesetBinding();
    //					dvb.setLabel(vs.getBindingIdentifier());
    //					dvb.setName(vs.getName());
    //					dvb.setStrength(vb.getStrength());
    //					dvb.setValuesetId(vb.getValuesetId());
    //					dvb.setValuesetLocations(vb.getValuesetLocations());
    //					dvb.setDomainInfo(vs.getDomainInfo());
    //					result.add(dvb);
    //				}
    //			}
    //			return result;
    //		}
    return null;
  }

  private Datatype findDatatype(String id, HashMap<String, Datatype> datatypesMap) {
    Datatype dt = datatypesMap.get(id);
    if (dt == null) {
      dt = this.datatypeService.findById(id);
      datatypesMap.put(id, dt);
    }
    return dt;
  }

  private DatatypeLabel createDatatypeLabel(Datatype dt) {
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

    return label;
  }

  private StructureElementBinding findStructureElementBindingByFieldIdForSegment(Segment segment, String fId) {
    if (segment != null && segment.getBinding() != null && segment.getBinding().getChildren() != null) {
      for (StructureElementBinding seb : segment.getBinding().getChildren()) {
        if (seb.getElementId().equals(fId))
          return seb;
      }
    }
    return null;
  }

  private StructureElementBinding findStructureElementBindingByComponentIdFromStructureElementBinding(
      StructureElementBinding seb, String cId) {
    if (seb != null && seb.getChildren() != null) {
      for (StructureElementBinding child : seb.getChildren()) {
        if (child.getElementId().equals(cId))
          return child;
      }
    }
    return null;
  }

  private StructureElementBinding findStructureElementBindingByComponentIdForDatatype(Datatype dt, String cid) {
    if (dt != null && dt.getBinding() != null && dt.getBinding().getChildren() != null) {
      for (StructureElementBinding seb : dt.getBinding().getChildren()) {
        if (seb.getElementId().equals(cid))
          return seb;
      }
    }
    return null;
  }

  @Override
  public void applyChanges(ConformanceProfile cp, List<ChangeItemDomain> cItems, String documentId)
      throws JsonProcessingException, IOException {
    Collections.sort(cItems);
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    for (ChangeItemDomain item : cItems) {
      if (item.getPropertyType().equals(PropertyType.NAME)) {
        item.setOldPropertyValue(cp.getName());
        cp.setName((String) item.getPropertyValue());
      }
      else if (item.getPropertyType().equals(PropertyType.ORGANISATION)) {
        item.setOldPropertyValue(cp.getOrganization());
        cp.setOrganization((String) item.getPropertyValue());
      } 
      else if (item.getPropertyType().equals(PropertyType.ROLE)) {
        item.setOldPropertyValue(cp.getRole());
        cp.setRole(Role.valueOf((String) item.getPropertyValue()));
      } 
      else if (item.getPropertyType().equals(PropertyType.PROFILETYPE)) {
        item.setOldPropertyValue(cp.getProfileType());
        cp.setProfileType(ProfileType.valueOf((String) item.getPropertyValue()));
      } 
      else if (item.getPropertyType().equals(PropertyType.AUTHORS)) {
        String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
        item.setOldPropertyValue(cp.getAuthors());
        List<String> authors= mapper.readValue(jsonInString, new TypeReference<List<String>>() {});
        cp.setAuthors(authors);
      }
      else if (item.getPropertyType().equals(PropertyType.COCONSTRAINTBINDINGS)) {
        String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
        item.setOldPropertyValue(cp.getCoConstraintsBindings());
        List<CoConstraintBinding> coconstraints = mapper.readValue(jsonInString, new TypeReference<List<CoConstraintBinding>>() {});
        cp.setCoConstraintsBindings(coconstraints);
      }
      else if (item.getPropertyType().equals(PropertyType.PROFILEIDENTIFIER)) {
        String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
        item.setOldPropertyValue(cp.getAuthors());
        List<MessageProfileIdentifier> profileIdentifier= mapper.readValue(jsonInString, new TypeReference<List<MessageProfileIdentifier>>() {});
        cp.setProfileIdentifier(profileIdentifier);

      }	        
      else if (item.getPropertyType().equals(PropertyType.PREDEF)) {
        item.setOldPropertyValue(cp.getPreDef());
        cp.setPreDef((String) item.getPropertyValue());
      } else if (item.getPropertyType().equals(PropertyType.POSTDEF)) {
        item.setOldPropertyValue(cp.getPostDef());
        cp.setPostDef((String) item.getPropertyValue());
      } else if (item.getPropertyType().equals(PropertyType.AUTHORNOTES)) {
        item.setOldPropertyValue(cp.getAuthorNotes());
        cp.setAuthorNotes((String) item.getPropertyValue());
      } else if (item.getPropertyType().equals(PropertyType.USAGENOTES)) {
        item.setOldPropertyValue(cp.getUsageNotes());
        cp.setUsageNotes((String) item.getPropertyValue());
      } else if (item.getPropertyType().equals(PropertyType.IDENTIFIER)) {
        item.setOldPropertyValue(cp.getIdentifier());
        cp.setIdentifier((String) item.getPropertyValue());
      } else if (item.getPropertyType().equals(PropertyType.USAGE)) {
        SegmentRefOrGroup srog = this.findSegmentRefOrGroupById(cp.getChildren(), item.getLocation());
        if (srog != null) {
          item.setOldPropertyValue(srog.getUsage());
          srog.setUsage(Usage.valueOf((String) item.getPropertyValue()));
        }
      } else if (item.getPropertyType().equals(PropertyType.SEGMENTREF)) {
        SegmentRefOrGroup srog = this.findSegmentRefOrGroupById(cp.getChildren(), item.getLocation());
        if (srog != null && srog instanceof SegmentRef) {
          SegmentRef sr = (SegmentRef) srog;
          item.setOldPropertyValue(sr.getRef());
          String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
          sr.setRef(mapper.readValue(jsonInString, Ref.class));
        }
      } else if (item.getPropertyType().equals(PropertyType.CARDINALITYMIN)) {
        SegmentRefOrGroup srog = this.findSegmentRefOrGroupById(cp.getChildren(), item.getLocation());
        if (srog != null) {
          item.setOldPropertyValue(srog.getMin());
          if (item.getPropertyValue() == null) {
            srog.setMin(0);
          } else {
            srog.setMin((Integer) item.getPropertyValue());
          }
        }
      } else if (item.getPropertyType().equals(PropertyType.CARDINALITYMAX)) {
        SegmentRefOrGroup srog = this.findSegmentRefOrGroupById(cp.getChildren(), item.getLocation());
        if (srog != null) {
          item.setOldPropertyValue(srog.getMax());
          if (item.getPropertyValue() == null) {
            srog.setMax("NA");
          } else {
            srog.setMax((String) item.getPropertyValue());
          }
        }
      } else if (item.getPropertyType().equals(PropertyType.VALUESET)) {
        System.out.println(item);
        String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
        StructureElementBinding seb = this.findAndCreateStructureElementBindingByIdPath(cp, item.getLocation());
        item.setOldPropertyValue(seb.getValuesetBindings());
        seb.setValuesetBindings(this.convertDisplayValuesetBinding(new HashSet<DisplayValuesetBinding>(
            Arrays.asList(mapper.readValue(jsonInString, DisplayValuesetBinding[].class)))));
      } else if (item.getPropertyType().equals(PropertyType.SINGLECODE)) {
        String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
        StructureElementBinding seb = this.findAndCreateStructureElementBindingByIdPath(cp, item.getLocation());
        seb.setInternalSingleCode(mapper.readValue(jsonInString, InternalSingleCode.class));
      } else if (item.getPropertyType().equals(PropertyType.DEFINITIONTEXT)) {
        SegmentRefOrGroup srog = this.findSegmentRefOrGroupById(cp.getChildren(), item.getLocation());
        if (srog != null) {
          item.setOldPropertyValue(srog.getText());
          if (item.getPropertyValue() == null) {
            srog.setText(null);
          } else {
            srog.setText((String) item.getPropertyValue());
          }
        }
      } else if (item.getPropertyType().equals(PropertyType.COMMENT)) {
        String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
        SegmentRefOrGroup srog = this.findSegmentRefOrGroupById(cp.getChildren(), item.getLocation());
        if (srog != null) {
          item.setOldPropertyValue(srog.getComments());
          srog.setComments(
              new HashSet<Comment>(Arrays.asList(mapper.readValue(jsonInString, Comment[].class))));
        }
      } else if (item.getPropertyType().equals(PropertyType.STATEMENT)) {
        String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
        if (item.getChangeType().equals(ChangeType.ADD)) {
          ConformanceStatement cs = mapper.readValue(jsonInString, ConformanceStatement.class);
          cs.addSourceId(cp.getId());
          cs.setStructureId(cp.getStructID());
          cs.setLevel(Level.CONFORMANCEPROFILE);
          cs.setIgDocumentId(documentId);
          cs = this.conformanceStatementRepository.save(cs);
          cp.getBinding().addConformanceStatement(cs.getId());
        } else if (item.getChangeType().equals(ChangeType.DELETE)) {
          item.setOldPropertyValue(item.getLocation());
          this.deleteConformanceStatementById(cp, item.getLocation());
        } else if (item.getChangeType().equals(ChangeType.UPDATE)) {
          ConformanceStatement cs = mapper.readValue(jsonInString, ConformanceStatement.class);
          if (cs.getId() != null) {
            item.setOldPropertyValue(this.conformanceStatementRepository.findById(cs.getId()));
          }
          cs.addSourceId(cp.getId());
          cs.setStructureId(cp.getStructID());
          cs.setLevel(Level.CONFORMANCEPROFILE);
          cs.setIgDocumentId(documentId);
          cs = this.conformanceStatementRepository.save(cs);
        }
      } else if (item.getPropertyType().equals(PropertyType.PREDICATE)) {
        String jsonInString = mapper.writeValueAsString(item.getPropertyValue());
        StructureElementBinding seb = this.findAndCreateStructureElementBindingByIdPath(cp, item.getLocation());
        if (item.getChangeType().equals(ChangeType.ADD)) {
          Predicate p = mapper.readValue(jsonInString, Predicate.class);
          p.addSourceId(cp.getId());
          p.setStructureId(cp.getStructID());
          p.setLevel(Level.CONFORMANCEPROFILE);
          p.setIgDocumentId(documentId);
          p = this.predicateRepository.save(p);
          seb.setPredicateId(p.getId());
        } else if (item.getChangeType().equals(ChangeType.DELETE)) {
          item.setOldPropertyValue(item.getLocation());
          if (seb.getPredicateId() != null) {
            Optional<Predicate> op = this.predicateRepository.findById(seb.getPredicateId());
            if (op.isPresent()) {
              Predicate p = op.get();
              p.removeSourceId(cp.getId());
              this.predicateRepository.save(p);
            }
            item.setOldPropertyValue(seb.getPredicateId());
            seb.setPredicateId(null);
          }

        } else if (item.getChangeType().equals(ChangeType.UPDATE)) {
          Predicate p = mapper.readValue(jsonInString, Predicate.class);
          if (cp.getId() != null) {
            item.setOldPropertyValue(this.predicateRepository.findById(cp.getId()));
          }
          p.addSourceId(cp.getId());
          p.setStructureId(cp.getStructID());
          p.setLevel(Level.CONFORMANCEPROFILE);
          p.setIgDocumentId(documentId);
          p = this.predicateRepository.save(p);
        }
      }
    }
    cp.setBinding(this.makeLocationInfo(cp));
    this.save(cp);
  }

  /**
   * @param children
   * @param location
   * @return
   */
  private SegmentRefOrGroup findSegmentRefOrGroupById(Set<SegmentRefOrGroup> children, String idPath) {
    if (idPath.contains("-")) {
      for (SegmentRefOrGroup srog : children) {
        if (srog instanceof Group) {
          Group g = (Group) srog;
          if (srog.getId().equals(idPath.split("\\-")[0]))
            return this.findSegmentRefOrGroupById(g.getChildren(),
                idPath.replace(idPath.split("\\-")[0] + "-", ""));
        }
      }
    } else {
      for (SegmentRefOrGroup srog : children) {
        if (srog.getId().equals(idPath))
          return srog;
      }
    }
    return null;
  }

  private String deleteConformanceStatementById(ConformanceProfile cp, String location) {
    String toBeDeleted = null;
    for (String id : cp.getBinding().getConformanceStatementIds()) {
      ConformanceStatement cs = this.conformanceStatementRepository.findById(id).get();
      if (cs.getIdentifier().equals(location))
        toBeDeleted = id;
      if (cs.getSourceIds() != null)
        cs.getSourceIds().remove(cp.getId());
      this.conformanceStatementRepository.save(cs);
    }

    if (toBeDeleted != null)
      cp.getBinding().getConformanceStatementIds().remove(toBeDeleted);
    return toBeDeleted;
  }

  private Set<ValuesetBinding> convertDisplayValuesetBinding(
      HashSet<DisplayValuesetBinding> displayValuesetBindings) {
    if (displayValuesetBindings != null) {
      Set<ValuesetBinding> result = new HashSet<ValuesetBinding>();
      for (DisplayValuesetBinding dvb : displayValuesetBindings) {
        ValuesetBinding vb = new ValuesetBinding();
        vb.setStrength(dvb.getStrength());
        vb.setValueSets(dvb.getValueSets());
        vb.setValuesetLocations(dvb.getValuesetLocations());
        result.add(vb);
      }
      return result;
    }
    return null;
  }

  private StructureElementBinding findAndCreateStructureElementBindingByIdPath(ConformanceProfile cp,
      String location) {
    if (cp.getBinding() == null) {
      ResourceBinding binding = new ResourceBinding();
      binding.setElementId(cp.getId());
      cp.setBinding(binding);
    }
    return this.findAndCreateStructureElementBindingByIdPath(cp.getBinding(), location);
  }

  private StructureElementBinding findAndCreateStructureElementBindingByIdPath(ResourceBinding binding,
      String location) {
    if (binding.getChildren() == null) {
      if (location.contains("-")) {
        StructureElementBinding seb = new StructureElementBinding();
        seb.setElementId(location.split("\\-")[0]);
        binding.addChild(seb);
        return this.findAndCreateStructureElementBindingByIdPath(seb,
            location.replace(location.split("\\-")[0] + "-", ""));
      } else {
        StructureElementBinding seb = new StructureElementBinding();
        seb.setElementId(location);
        binding.addChild(seb);
        return seb;
      }
    } else {
      if (location.contains("-")) {
        for (StructureElementBinding seb : binding.getChildren()) {
          if (seb.getElementId().equals(location.split("\\-")[0]))
            return this.findAndCreateStructureElementBindingByIdPath(seb,
                location.replace(location.split("\\-")[0] + "-", ""));
        }
        StructureElementBinding seb = new StructureElementBinding();
        seb.setElementId(location.split("\\-")[0]);
        binding.addChild(seb);
        return this.findAndCreateStructureElementBindingByIdPath(seb,
            location.replace(location.split("\\-")[0] + "-", ""));
      } else {
        for (StructureElementBinding seb : binding.getChildren()) {
          if (seb.getElementId().equals(location))
            return seb;
        }
        StructureElementBinding seb = new StructureElementBinding();
        seb.setElementId(location);
        binding.addChild(seb);
        return seb;
      }
    }
  }

  private StructureElementBinding findAndCreateStructureElementBindingByIdPath(StructureElementBinding binding,
      String location) {
    if (binding.getChildren() == null) {
      if (location.contains("-")) {
        StructureElementBinding seb = new StructureElementBinding();
        seb.setElementId(location.split("\\-")[0]);
        binding.addChild(seb);
        return this.findAndCreateStructureElementBindingByIdPath(seb,
            location.replace(location.split("\\-")[0] + "-", ""));
      } else {
        StructureElementBinding seb = new StructureElementBinding();
        seb.setElementId(location);
        binding.addChild(seb);
        return seb;
      }
    } else {
      if (location.contains("-")) {
        for (StructureElementBinding seb : binding.getChildren()) {
          if (seb.getElementId().equals(location.split("\\-")[0]))
            return this.findAndCreateStructureElementBindingByIdPath(seb,
                location.replace(location.split("\\-")[0] + "-", ""));
        }
        StructureElementBinding seb = new StructureElementBinding();
        seb.setElementId(location.split("\\-")[0]);
        binding.addChild(seb);
        return this.findAndCreateStructureElementBindingByIdPath(seb,
            location.replace(location.split("\\-")[0] + "-", ""));
      } else {
        for (StructureElementBinding seb : binding.getChildren()) {
          if (seb.getElementId().equals(location))
            return seb;
        }
        StructureElementBinding seb = new StructureElementBinding();
        seb.setElementId(location);
        binding.addChild(seb);
        return seb;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService#
   * convertDomainToContextStructure(gov.nist.hit.hl7.igamt.conformanceprofile.
   * domain. ConformanceProfile)
   */
  @Override
  public ConformanceProfileStructureTreeModel convertDomainToContextStructure(ConformanceProfile conformanceProfile,
      HashMap<String, ConformanceStatementsContainer> segMap,
      HashMap<String, ConformanceStatementsContainer> dtMap) {
    ConformanceProfileStructureTreeModel result = new ConformanceProfileStructureTreeModel();
    result.setData(new ConformanceProfileDisplayModel(conformanceProfile));

    if (conformanceProfile.getChildren() != null && conformanceProfile.getChildren().size() > 0) {
      for (SegmentRefOrGroup sog : conformanceProfile.getChildren()) {
        if (sog instanceof SegmentRef) {
          SegmentRefStructureTreeModel segmentRefStructureTreeModel = new SegmentRefStructureTreeModel();
          SegmentRefDisplayModel segmentRefDisplayModel = new SegmentRefDisplayModel((SegmentRef) sog);
          Segment s = this.segmentService.findById(((SegmentRef) sog).getRef().getId());

          if (s.getDomainInfo().getScope().equals(Scope.USER)) {
            if (s.getBinding() != null && s.getBinding().getConformanceStatementIds() != null
                && s.getBinding().getConformanceStatementIds().size() > 0) {
              if (!segMap.containsKey(s.getLabel()))
                segMap.put(s.getLabel(),
                    new ConformanceStatementsContainer(
                        this.collectCS(s.getBinding().getConformanceStatementIds()),
                        Type.SEGMENT, s.getId(), s.getLabel()));
            }

            this.segmentService.collectAssoicatedConformanceStatements(s, dtMap);
          }

          segmentRefDisplayModel.setName(s.getName());
          segmentRefDisplayModel.setIdPath(conformanceProfile.getId() + "-" + sog.getId());
          segmentRefDisplayModel.setPath("1-" + sog.getPosition());
          segmentRefStructureTreeModel.setData(segmentRefDisplayModel);
          result.addChild(segmentRefStructureTreeModel);
        } else if (sog instanceof Group) {
          GroupStructureTreeModel groupStructureTreeModel = new GroupStructureTreeModel();
          GroupDisplayModel groupDisplayModel = new GroupDisplayModel((Group) sog);
          groupDisplayModel.setIdPath(conformanceProfile.getId() + "-" + sog.getId());
          groupDisplayModel.setPath("1-" + sog.getPosition());
          updateChild(groupStructureTreeModel, groupDisplayModel, sog, segMap, dtMap);
          groupStructureTreeModel.setData(groupDisplayModel);
          result.addChild(groupStructureTreeModel);
        }
      }
    }
    return result;
  }

  /**
   * @param map
   * @param groupStructureTreeModel
   * @param groupDisplayModel
   * @param sog
   */
  private void updateChild(GroupStructureTreeModel parentStructureTreeModel, GroupDisplayModel parentDisplayModel,
      SegmentRefOrGroup parent, HashMap<String, ConformanceStatementsContainer> segMap,
      HashMap<String, ConformanceStatementsContainer> dtMap) {
    for (SegmentRefOrGroup child : ((Group) parent).getChildren()) {
      if (child instanceof Group) {
        GroupStructureTreeModel groupStructureTreeModel = new GroupStructureTreeModel();
        GroupDisplayModel groupDisplayModel = new GroupDisplayModel((Group) child);
        groupDisplayModel.setIdPath(parentDisplayModel.getIdPath() + "-" + child.getId());
        groupDisplayModel.setPath(parentDisplayModel.getPath() + "-" + child.getPosition());
        updateChild(groupStructureTreeModel, groupDisplayModel, child, segMap, dtMap);
        groupStructureTreeModel.setData(groupDisplayModel);
        parentStructureTreeModel.addGroup(groupStructureTreeModel);
      } else if (child instanceof SegmentRef) {
        SegmentRefStructureTreeModel segmentRefStructureTreeModel = new SegmentRefStructureTreeModel();
        SegmentRefDisplayModel segmentRefDisplayModel = new SegmentRefDisplayModel((SegmentRef) child);
        Segment s = this.segmentService.findById(((SegmentRef) child).getRef().getId());

        if (s.getDomainInfo().getScope().equals(Scope.USER)) {
          if (s.getBinding() != null && s.getBinding().getConformanceStatementIds() != null
              && s.getBinding().getConformanceStatementIds().size() > 0) {
            if (!segMap.containsKey(s.getLabel()))
              segMap.put(s.getLabel(),
                  new ConformanceStatementsContainer(
                      this.collectCS(s.getBinding().getConformanceStatementIds()), Type.SEGMENT,
                      s.getId(), s.getLabel()));
          }

          this.segmentService.collectAssoicatedConformanceStatements(s, dtMap);
        }

        segmentRefDisplayModel.setName(s.getName());
        segmentRefDisplayModel.setIdPath(parentDisplayModel.getIdPath() + "-" + child.getId());
        segmentRefDisplayModel.setPath(parentDisplayModel.getPath() + "-" + child.getPosition());
        segmentRefStructureTreeModel.setData(segmentRefDisplayModel);
        parentStructureTreeModel.addSegment(segmentRefStructureTreeModel);
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService#
   * makeLocationInfo(
   * gov.nist.hit.hl7.igamt.conformanceprofile.domain.ConformanceProfile)
   */
  @Override
  public ResourceBinding makeLocationInfo(ConformanceProfile cp) {
    if (cp.getBinding() != null && cp.getBinding().getChildren() != null) {
      for (StructureElementBinding seb : cp.getBinding().getChildren()) {
        seb.setLocationInfo(makeLocationInfoForGroupOrSegRef(cp.getChildren(), seb));
      }
      return cp.getBinding();
    }
    return null;
  }

  /**
   * @param list
   * @param seb
   * @return
   */
  private LocationInfo makeLocationInfoForGroupOrSegRef(Set<SegmentRefOrGroup> list, StructureElementBinding seb) {
    if (list != null) {
      for (SegmentRefOrGroup sg : list) {
        if (sg.getId().equals(seb.getElementId())) {
          if (sg instanceof Group) {
            if (seb.getChildren() != null) {
              for (StructureElementBinding childSeb : seb.getChildren()) {
                childSeb.setLocationInfo(
                    this.makeLocationInfoForGroupOrSegRef(((Group) sg).getChildren(), childSeb));
              }
            }
            return new LocationInfo(LocationType.GROUP, sg.getPosition(), sg.getName());
          } else if (sg instanceof SegmentRef) {
            Segment s = this.segmentService.findById(((SegmentRef) sg).getRef().getId());
            if (seb.getChildren() != null) {
              for (StructureElementBinding childSeb : seb.getChildren()) {
                childSeb.setLocationInfo(this.segmentService.makeLocationInfoForField(s, childSeb));
              }
            }
            return new LocationInfo(LocationType.SEGREF, sg.getPosition(), s.getLabel());
          }
        }
      }
    }
    return null;
  }

  @Override
  public Set<RelationShip> collectDependencies(ConformanceProfile cp) {
    // TODO Auto-generated method stub
    Set<RelationShip> used = new HashSet<RelationShip>();
    HashMap<String, Usage> usageMap = new HashMap<String, Usage>();
    for (MsgStructElement segOrgroup : cp.getChildren()) {
      if (segOrgroup instanceof SegmentRef) {
        usageMap.put(segOrgroup.getId(), segOrgroup.getUsage());
        usageMap.put(segOrgroup.getId(), segOrgroup.getUsage());
        SegmentRef ref = (SegmentRef) segOrgroup;

        if (ref.getRef() != null && ref.getRef().getId() != null) {
          RelationShip rel = new RelationShip(new ReferenceIndentifier(ref.getRef().getId(), Type.SEGMENT),
              new ReferenceIndentifier(cp.getId(), Type.CONFORMANCEPROFILE),
              new ReferenceLocation(Type.CONFORMANCEPROFILE, ref.getPosition() + "", ref.getName()));
          rel.setUsage(ref.getUsage());
          used.add(rel);
        }

      } else {
        processSegmentorGroup(cp.getId(), segOrgroup, used, "");
      }
    }
    if (cp.getBinding() != null) {

      Set<RelationShip> bindingDependencies = bindingService.collectDependencies(
          new ReferenceIndentifier(cp.getId(), Type.CONFORMANCEPROFILE), cp.getBinding(), usageMap);
      used.addAll(bindingDependencies);
    }
    if(cp.getCoConstraintsBindings() !=null) {

      Set<RelationShip> CoConstraintsDependencies = coConstraintService.collectDependencies(
          new ReferenceIndentifier(cp.getId(), Type.CONFORMANCEPROFILE), cp.getCoConstraintsBindings());

      used.addAll(CoConstraintsDependencies);
    }
    return used;

  }

  private void processSegmentorGroup(String profileId, MsgStructElement segOrgroup, Set<RelationShip> used,
      String path) {
    // TODO Auto-generated method stub
    if (segOrgroup instanceof SegmentRef) {
      SegmentRef ref = (SegmentRef) segOrgroup;
      if (ref.getRef() != null && ref.getRef().getId() != null) {

        RelationShip rel = new RelationShip(new ReferenceIndentifier(ref.getRef().getId(), Type.SEGMENT),
            new ReferenceIndentifier(profileId, Type.CONFORMANCEPROFILE),

            new ReferenceLocation(Type.GROUP, path + "." + ref.getPosition(), ref.getName()));
        rel.setUsage(ref.getUsage());
        used.add(rel);
      }
    } else if (segOrgroup instanceof Group) {
      Group g = (Group) segOrgroup;
      path += g.getName();
      for (MsgStructElement child : g.getChildren()) {
        processSegmentorGroup(profileId, child, used, path);
      }
    }

  }

  public List<ConformanceProfile> findByIdIn(Set<String> ids) {
    // TODO Auto-generated method stub
    return conformanceProfileRepository.findByIdIn(ids);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService#
   * findDisplayPredicates(java.lang.String, java.lang.String)
   */
  @Override
  public Set<DisplayPredicate> findDisplayPredicates(String sourceId, String documentId) {
    Set<Predicate> predicates = this.predicateRepository.findByIgDocumentIdAndLevel(documentId,
        Level.CONFORMANCEPROFILE);
    Set<DisplayPredicate> result = new HashSet<DisplayPredicate>();
    if (predicates != null) {
      for (Predicate p : predicates) {
        if (p.getSourceIds() != null && p.getSourceIds().contains(sourceId)) {
          Optional<ConformanceProfile> o = this.conformanceProfileRepository.findById(sourceId);
          if (o.isPresent()) {
            DisplayPredicate dp = new DisplayPredicate();
            dp.setPredicate(p);
            ConformanceProfile m = o.get();
            if (m.getBinding() != null && m.getBinding().getChildren() != null) {
              this.markLocation(dp, m.getBinding().getChildren(), m.getLabel(), p.getId());
            }
            result.add(dp);
          }
        }
      }
    }
    return result;
  }

  // @Override
  // public List<ConformanceProfile> findDisplayFormatByIds(Set<String> ids) {
  // return null;
  // }

  private void markLocation(DisplayPredicate dp, Set<StructureElementBinding> children, String location, String pid) {
    for (StructureElementBinding seb : children) {
      if (seb.getPredicateId() != null && seb.getPredicateId().equals(pid)) {
        if (seb.getLocationInfo().getType().equals(LocationType.GROUP)) {
          dp.setLocation(location + "." + seb.getLocationInfo().getName());
        } else if (seb.getLocationInfo().getType().equals(LocationType.SEGREF)) {
          dp.setLocation(location + "." + seb.getLocationInfo().getName());
        } else if (seb.getLocationInfo().getType().equals(LocationType.FIELD)) {
          dp.setLocation(location + "-" + seb.getLocationInfo().getPosition() + "("
              + seb.getLocationInfo().getName() + ")");
        } else if (seb.getLocationInfo().getType().equals(LocationType.COMPONENT)) {
          dp.setLocation(location + "." + seb.getLocationInfo().getPosition() + "("
              + seb.getLocationInfo().getName() + ")");
        } else if (seb.getLocationInfo().getType().equals(LocationType.SUBCOMPONENT)) {
          dp.setLocation(location + "." + seb.getLocationInfo().getPosition() + "("
              + seb.getLocationInfo().getName() + ")");
        }
      } else {
        if (seb.getChildren() != null) {
          if (seb.getLocationInfo().getType().equals(LocationType.GROUP)) {
            this.markLocation(dp, seb.getChildren(), location + "." + seb.getLocationInfo().getName(), pid);
          } else if (seb.getLocationInfo().getType().equals(LocationType.SEGREF)) {
            this.markLocation(dp, seb.getChildren(), location + "." + seb.getLocationInfo().getName(), pid);
          } else if (seb.getLocationInfo().getType().equals(LocationType.FIELD)) {
            this.markLocation(dp, seb.getChildren(), location + "-" + seb.getLocationInfo().getPosition(),
                pid);
          } else if (seb.getLocationInfo().getType().equals(LocationType.COMPONENT)) {
            this.markLocation(dp, seb.getChildren(), location + "." + seb.getLocationInfo().getPosition(),
                pid);
          } else if (seb.getLocationInfo().getType().equals(LocationType.SUBCOMPONENT)) {
            this.markLocation(dp, seb.getChildren(), location + "." + seb.getLocationInfo().getPosition(),
                pid);
          }
        }
      }
    }
  }

  @Override
  public Set<Resource> getDependencies(ConformanceProfile cp) {
    Set<String> segmentIds = collectSegmentIds(cp);
    Set<Resource> ret = new HashSet<Resource>();
    ret.add(cp);
    HashMap<String, Resource> usedDatatypes = new HashMap<String, Resource>();
    List<Segment> segments = this.segmentService.findByIdIn(segmentIds);
    ret.addAll(segments);
    for (Segment seg : segments) {
      this.segmentService.collectResources(seg, usedDatatypes);
    }
    ret.addAll(usedDatatypes.values());
    return ret;
  }

  private Set<String> collectSegmentIds(ConformanceProfile cp) {
    // TODO Auto-generated method stub
    Set<String> ids = new HashSet<String>();
    for (MsgStructElement segOrgroup : cp.getChildren()) {
      if (segOrgroup instanceof SegmentRef) {
        SegmentRef ref = (SegmentRef) segOrgroup;
        if (ref.getRef() != null && ref.getRef().getId() != null)
          ids.add(ref.getRef().getId());
      } else {
        processSegmentorGroup(segOrgroup, ids);
      }
    }
    return ids;

  }

  private void processSegmentorGroup(MsgStructElement segOrgroup, Set<String> ids) {
    // TODO Auto-generated method stub
    if (segOrgroup instanceof SegmentRef) {
      SegmentRef ref = (SegmentRef) segOrgroup;
      if (ref.getRef() != null && ref.getRef().getId() != null) {
        ids.add(ref.getRef().getId());

      }
    } else if (segOrgroup instanceof Group) {
      Group g = (Group) segOrgroup;
      for (MsgStructElement child : g.getChildren()) {
        processSegmentorGroup(child, ids);
      }
    }

  }
}
