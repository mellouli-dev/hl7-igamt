package gov.nist.hit.hl7.igamt.bootstrap.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nist.hit.hl7.igamt.common.base.domain.Link;
import gov.nist.hit.hl7.igamt.common.base.domain.Registry;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.ConformanceProfile;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.event.MessageEvent;
import gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService;
import gov.nist.hit.hl7.igamt.conformanceprofile.service.event.MessageEventService;
import gov.nist.hit.hl7.igamt.ig.domain.Ig;
import gov.nist.hit.hl7.igamt.ig.service.IgService;
import gov.nist.hit.hl7.igamt.valueset.domain.Code;
import gov.nist.hit.hl7.igamt.valueset.domain.Valueset;
import gov.nist.hit.hl7.igamt.valueset.domain.property.Constant.SCOPE;
import gov.nist.hit.hl7.igamt.valueset.service.ValuesetService;


@Service
public class MessageEventFacory {
  @Autowired
  ConformanceProfileService conformanceProfileService;

  @Autowired
  MessageEventService messageEventService;


  @Autowired
  ValuesetService valueSetService;

  @Autowired
  IgService igdocumentService;


  public void createMessageEvent() {
    List<Ig> standardIgs = igdocumentService.finByScope(SCOPE.HL7STANDARD.toString());
    for (Ig ig : standardIgs) {
      Registry messages = ig.getConformanceProfileRegistry();
      List<ConformanceProfile> shortConformanceProfiles = new ArrayList<ConformanceProfile>();
      for (Link l : messages.getChildren()) {
        ConformanceProfile cp = conformanceProfileService.findDisplayFormat(l.getId());
        if (cp != null) {
          shortConformanceProfiles.add(cp);
          createMessageEvent(shortConformanceProfiles, cp.getDomainInfo().getVersion());

        }

      }
    }

  }


  private void createMessageEvent(List<ConformanceProfile> shortConformanceProfiles,
      String version) {


    List<Valueset> HL70354s =
        valueSetService.findByDomainInfoScopeAndDomainInfoVersionAndBindingIdentifier(
            SCOPE.HL7STANDARD.toString(), version, "0354");
    Valueset HL70354 = null;
    if (HL70354s != null) {
      if (!HL70354s.isEmpty()) {
        HL70354 = HL70354s.get(0);
        for (ConformanceProfile cp : shortConformanceProfiles) {

          Code c = findCodeByValue(HL70354, fixUnderscore(cp.getStructID()));
          if (c != null) {

            String label = c.getDescription();
            label = label == null ? "Varies" : label; // Handle ACK
            label =label.replaceAll("\\s+","");

            String[] ss = label.trim().split(",");
  
            List<String> events = Arrays.asList(ss);

            MessageEvent messageEvent = new MessageEvent(cp.getId(), cp.getStructID(), events,
                cp.getDescription(), version);
            messageEventService.save(messageEvent);

          }
        }

      }

    }
    // TODO Auto-generated method stub

  }


  private Code findCodeByValue(Valueset vs, String structID) {
    if (vs.getCodes() != null)
      for (Code c : vs.getCodes()) {
        if (structID.equals(c.getValue())) {
          return c;
        }
      }
    return null;
  }

  public String fixUnderscore(String structID) {
    if (structID.endsWith("_")) {
      int pos = structID.length();
      return structID.substring(0, pos - 1);
    } else {
      return structID;
    }
  }



}
