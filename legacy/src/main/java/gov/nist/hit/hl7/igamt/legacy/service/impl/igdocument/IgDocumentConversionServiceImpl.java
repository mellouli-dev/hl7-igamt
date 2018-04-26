package gov.nist.hit.hl7.igamt.legacy.service.impl.igdocument;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.CompositeProfileStructure;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.CompositeProfiles;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.DatatypeLibrary;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.DatatypeLink;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.IGDocument;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.IGDocumentScope;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.Message;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.Messages;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.Profile;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.ProfileComponentLibrary;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.ProfileComponentLink;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.Section;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.SegmentLibrary;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.SegmentLink;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.TableLibrary;
import gov.nist.healthcare.tools.hl7.v2.igamt.lite.domain.TableLink;
import gov.nist.hit.hl7.auth.domain.Account;
import gov.nist.hit.hl7.auth.repository.AccountRepository;
import gov.nist.hit.hl7.igamt.ig.domain.*;
import gov.nist.hit.hl7.igamt.ig.service.IgService;
import gov.nist.hit.hl7.igamt.legacy.repository.IGDocumentRepository;
import gov.nist.hit.hl7.igamt.legacy.service.ConversionService;
import gov.nist.hit.hl7.igamt.shared.domain.CompositeKey;
import gov.nist.hit.hl7.igamt.shared.domain.DomainInfo;
import gov.nist.hit.hl7.igamt.shared.domain.Link;
import gov.nist.hit.hl7.igamt.shared.domain.Registry;
import gov.nist.hit.hl7.igamt.shared.domain.Scope;
import gov.nist.hit.hl7.igamt.shared.domain.TextSection;
import gov.nist.hit.hl7.igamt.shared.domain.Type;
import gov.nist.hit.hl7.igamt.shared.domain.ValueSetConfigForExport;
import gov.nist.hit.hl7.igamt.shared.domain.ValueSetRegistry;



public class IgDocumentConversionServiceImpl implements ConversionService{

	  
	  private static IgService igService=
		      (IgService) context.getBean("igService");
	  
	 
	  private static IGDocumentRepository legacyRepository =
  		      (IGDocumentRepository) legacyContext.getBean("igDocumentRepository");
	  
	   AccountRepository accountRepository =
		      userContext.getBean(AccountRepository.class);

	public IgDocumentConversionServiceImpl() {
		super();
	}

	@Override
	public void convert() {
		// TODO Auto-generated method stub
		List<IGDocument> igs =  legacyRepository.findAll();
		System.out.println(accountRepository.findAll().get(0).getEmail());

		for(IGDocument ig: igs) {
			convert(ig);
		}
		
	}


	private void convert(IGDocument ig) {
		
		
	
		Ig newIg= new Ig();
//		Metadata
		CompositeKey key = new CompositeKey(ig.getId());
		newIg.setId(key);	
		if(ig.getAccountId() !=null) {
				Account acc = accountRepository.findByAccountId(ig.getAccountId());
				if(acc!=null) {
						if (acc.getUsername() !=null) {
							
							newIg.setUsername(acc.getUsername());
						}
				}
				
		}
		IgMetaData newMetaData= new IgMetaData();
		newMetaData.setIdentifier(ig.getMetaData().getIdentifier());
		newMetaData.setCoverPicture(ig.getMetaData().getCoverPicture());
		newMetaData.setImplementationNotes(ig.getMetaData().getImplementationNotes());
		newMetaData.setOrgName(ig.getMetaData().getOrgName());
		newMetaData.setTopics(ig.getMetaData().getTopics());
		newMetaData.setSpecificationName(ig.getMetaData().getSpecificationName());
		newMetaData.setSubTitle(ig.getMetaData().getSubTitle());
		newMetaData.setTitle(ig.getMetaData().getTitle());
		newIg.setMetaData(newMetaData);		
		newIg.setComment(ig.getComment());
		newIg.setCreatedFrom(ig.getCreatedFrom());
		newIg.setDescription(ig.getMetaData().getDescription());
		DomainInfo domain= new DomainInfo();
		if(ig.getScope().equals(IGDocumentScope.PRELOADED)) {
		domain.setScope(Scope.PRELOADED);
		}else if(ig.getScope().equals(IGDocumentScope.USER)) {
			domain.setScope(Scope.USER);
		}else if(ig.getScope().equals(IGDocumentScope.HL7STANDARD)) {
			domain.setScope(Scope.HL7STANDARD);

		}
		newIg.setDomainInfo(domain);
		newIg.setName(ig.getMetaData().getTitle());
		newIg.setUpdateDate(ig.getDateUpdated());
		newIg.setCreationDate(ig.getDateUpdated());
		newIg.setComment(ig.getAuthorNotes());
		
		if(ig.getChildSections() !=null && !ig.getChildSections().isEmpty())
		addNaratives(newIg, ig.getChildSections());
		addProfile(newIg, ig.getProfile(),ig.getChildSections().size()+1);
			
		igService.save(newIg);
	}

	@SuppressWarnings("unchecked")
	private void addProfile(Ig newIg, Profile profile,int position) {
		
		TextSection infra= new TextSection();
		
		infra.setParentId(newIg.getId().getId());
		
		infra.setId(profile.getId());
		infra.setType(Type.PROFILE);
		infra.setLabel("Message Infrastructure");
		
		infra.setPosition(position);
		Registry profileComponent= createProfileComponentSection(profile.getProfileComponentLibrary());
		profileComponent.setPosition(1);
		Registry conformanceProfile= createConformanceProfile(profile.getMessages());
		
		conformanceProfile.setPosition(2);
		
		Registry compositeProfiles= createCompositeProfiles(profile.getCompositeProfiles());
		compositeProfiles.setPosition(3);
		Registry segments= createSegment(profile.getSegmentLibrary());
		segments.setPosition(4);
		Registry datatypes= createDatatypes(profile.getDatatypeLibrary());
		datatypes.setPosition(5);
		ValueSetRegistry valueSets= createValueSets(profile.getTableLibrary());	
		valueSets.setPosition(6);
		Set<gov.nist.hit.hl7.igamt.shared.domain.Section> children =new HashSet<gov.nist.hit.hl7.igamt.shared.domain.Section>();
		
		children.add(profileComponent);
		children.add( conformanceProfile);
		children.add( compositeProfiles);
		children.add( segments);
		children.add( datatypes);
		children.add(valueSets);
		infra.setChildren(children);
		infra.setParentId(newIg.getId().getId());
		newIg.getContent().add(infra);
		igService.save(newIg);
		
	}
	




	private Registry createCompositeProfiles(CompositeProfiles compositeProfiles) {
		Registry ret = new Registry();
		ret.setType(Type.COMPOSITEPROFILEREGISTRY);
		ret.setLabel("Composite Profiles");
		ret.setId( compositeProfiles.getId());
		
		List <CompositeProfileStructure> ordred = compositeProfiles.getChildren().stream().sorted((CompositeProfileStructure l1, CompositeProfileStructure l2) ->l1.getName().compareTo(l2.getName())).collect(Collectors.toList());
		for( int i = 0 ; i < ordred.size(); i++) {
			CompositeKey key =  new CompositeKey (ordred.get(i).getId());
			Link link = new Link(key, i+1);
			ret.getChildren().add(link);
			
		}	
		return ret;
	}




	private Registry createProfileComponentSection(ProfileComponentLibrary profileComponentLibrary) {
		Registry ret = new Registry();
		ret.setType(Type.PROFILECOMPONENTREGISTRY);
		ret.setId(profileComponentLibrary.getId());
		ret.setLabel("Profile Components");	
		List <ProfileComponentLink > ordred = profileComponentLibrary.getChildren().stream().sorted((ProfileComponentLink l1, ProfileComponentLink l2) ->l1.getName().compareTo(l2.getName())).collect(Collectors.toList());
		for( int i = 0 ; i < ordred.size(); i++) {
			CompositeKey key =  new CompositeKey (ordred.get(i).getId());
			Link link = new Link(key, i+1);
			ret.getChildren().add(link);
			
		}	
		return ret;
	}




	private ValueSetRegistry createValueSets(TableLibrary tableLibrary) {
		ValueSetRegistry ret= new ValueSetRegistry();
		ret.setId(tableLibrary.getId());
		ret.setType(Type.VALUESETREGISTRY);
		ret.setLabel("Value Sets");
		ret.setId(tableLibrary.getId());
		ret.setCodesPresence(tableLibrary.getCodePresence());
		ValueSetConfigForExport config= new ValueSetConfigForExport();
		if(tableLibrary.getExportConfig() !=null && tableLibrary.getExportConfig().getInclude() !=null) {
			for (String s: tableLibrary.getExportConfig().getInclude() ) {
				
				//ret.getExportConfig().getInclude().add(new CompositeKey(s));
			}
		}
	
		
		
		ret.setExportConfig(config);
		
		List <TableLink > ordred = tableLibrary.getChildren().stream().sorted().collect(Collectors.toList());
		for( int i = 0 ; i < ordred.size(); i++) {
			CompositeKey key =  new CompositeKey (ordred.get(i).getId());
			Link link = new Link(key, i+1);
			ret.getChildren().add(link);
			
		}
		

	
		return ret;
	}

	private Registry createDatatypes(DatatypeLibrary datatypeLibrary) {
		
		Registry ret = new Registry();
		ret.setType(Type.DATATYPEREGISTRY);
		ret.setLabel("Data Types");
		List <DatatypeLink > ordred = datatypeLibrary.getChildren().stream().sorted().collect(Collectors.toList());
		for( int i = 0 ; i < ordred.size(); i++) {
			CompositeKey key =  new CompositeKey (ordred.get(i).getId());
			Link link = new Link(key, i+1);
			ret.getChildren().add(link);
			
		}	
		return ret;
		
	}




	private Registry createSegment(SegmentLibrary segmentLibrary) {
		Registry ret = new Registry();
		ret.setType(Type.SEGMENTRGISTRY);
		ret.setLabel("Segments and Fields Description");
		List <SegmentLink > ordred = segmentLibrary.getChildren().stream().sorted().collect(Collectors.toList());
		for( int i = 0 ; i < ordred.size(); i++) {
			CompositeKey key =  new CompositeKey (ordred.get(i).getId());
			Link link = new Link(key, i+1);
			ret.getChildren().add(link);
			
		}	
		return ret;
	}




	private Registry createConformanceProfile(Messages messages) {
		
		Registry ret = new Registry();
		ret.setType(Type.CONFORMANCEPROFILEREGISTRY);
		ret.setLabel("Conformance profiles");	
		for( Message m : messages.getChildren()) {
			CompositeKey key =  new CompositeKey (m.getId());
			Link link = new Link(key,m.getPosition());
			ret.getChildren().add(link);
			
		}	
		return ret;
	}
	private void addNaratives(Ig newIg, Set<Section> childSections) {
		// TODO Auto-generated method stub
		Set<gov.nist.hit.hl7.igamt.shared.domain.Section> children = new HashSet<>();
		
		
		
		for(Section s : childSections) {
			children.add(createTextSectionFromSection( s,newIg.getId().getId()));
			}
		newIg.setContent(children);

	}
	private TextSection createTextSectionFromSection(Section s, String parentId) {
		TextSection newSection = new  TextSection();
		newSection.setLabel(s.getSectionTitle());
		newSection.setDescription(s.getSectionDescription());
		newSection.setType(Type.TEXT);
		newSection.setDescription(s.getSectionContents());
		newSection.setPosition(s.getSectionPosition());
		newSection.setParentId(parentId);
		newSection.setId(s.getId());
		if(s.getChildSections() !=null && !s.getChildSections().isEmpty()) {
			Set<gov.nist.hit.hl7.igamt.shared.domain.Section> children = new HashSet<gov.nist.hit.hl7.igamt.shared.domain.Section>();
			for(Section child : s.getChildSections()) {
				
				children.add(createTextSectionFromSection( child, s.getId())) ;
				
			}
			newSection.setChildren(children);
			
		}
		return newSection;
		
	}
	
	

}
