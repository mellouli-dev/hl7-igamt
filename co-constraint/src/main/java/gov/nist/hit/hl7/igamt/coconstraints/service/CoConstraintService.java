package gov.nist.hit.hl7.igamt.coconstraints.service;

import gov.nist.hit.hl7.igamt.coconstraints.exception.CoConstraintGroupNotFoundException;
import gov.nist.hit.hl7.igamt.coconstraints.model.CoConstraintBinding;
import gov.nist.hit.hl7.igamt.coconstraints.model.CoConstraintGroup;
import gov.nist.hit.hl7.igamt.coconstraints.model.CoConstraintTable;
import gov.nist.hit.hl7.igamt.common.base.domain.Link;
import gov.nist.hit.hl7.igamt.common.base.domain.RealKey;
import gov.nist.hit.hl7.igamt.common.base.domain.Scope;
import gov.nist.hit.hl7.igamt.common.base.util.CloneMode;
import gov.nist.hit.hl7.igamt.common.base.util.ReferenceIndentifier;
import gov.nist.hit.hl7.igamt.common.base.util.RelationShip;
import gov.nist.hit.hl7.igamt.segment.exception.SegmentNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CoConstraintService {

    CoConstraintGroup findById(String id) throws CoConstraintGroupNotFoundException;
    void delete(CoConstraintGroup ccGroup);
    List<CoConstraintGroup> findByBaseSegmentAndDocumentIdAndUsername(String baseSegment, String documentId, String username);
    CoConstraintGroup saveCoConstraintGroup(CoConstraintGroup group);
    CoConstraintTable resolveRefAndMerge(CoConstraintTable table);
    CoConstraintGroup clone(String id, Map<String, String> datatypes, Map<String, String> valueSets);
    CoConstraintGroup createCoConstraintGroupPrototype(String id) throws SegmentNotFoundException;
    Link createIgLink(CoConstraintGroup group, int position, String username);
    Set<RelationShip> collectDependencies(ReferenceIndentifier referenceIndentifier,
                                          List<CoConstraintBinding> coConstraintsBindings);
    List<CoConstraintGroup> findByIdIn(Set<String> ids);
    Collection<? extends RelationShip> collectDependencies(CoConstraintGroup ccGroup) ;
    Link clone(String string, HashMap<RealKey, String> newKeys, Link l, String username,
        Scope user, String documentTarget, CloneMode cloneMode);
    void updateDependencies(CoConstraintGroup elm, HashMap<RealKey, String> newKeys,
        String username, boolean cloned);
    void updateDepenedencies(CoConstraintTable value, HashMap<RealKey, String> newKeys, boolean cloned);
}