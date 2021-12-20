package gov.nist.hit.hl7.igamt.serialization.newImplementation.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import gov.nist.hit.hl7.igamt.coconstraints.model.*;
import gov.nist.hit.hl7.igamt.ig.model.ResourceSkeletonBone;
import gov.nist.hit.hl7.igamt.ig.service.CoConstraintSerializationHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import gov.nist.hit.hl7.igamt.common.base.domain.Type;
import gov.nist.hit.hl7.igamt.common.base.domain.ValuesetBinding;
import gov.nist.hit.hl7.igamt.common.base.domain.ValuesetStrength;
import gov.nist.hit.hl7.igamt.common.base.domain.display.DisplayElement;
import gov.nist.hit.hl7.igamt.conformanceprofile.domain.ConformanceProfile;
import gov.nist.hit.hl7.igamt.conformanceprofile.service.ConformanceProfileService;
import gov.nist.hit.hl7.igamt.datatype.domain.ComplexDatatype;
import gov.nist.hit.hl7.igamt.datatype.domain.Component;
import gov.nist.hit.hl7.igamt.datatype.domain.Datatype;
import gov.nist.hit.hl7.igamt.datatype.service.DatatypeService;
import gov.nist.hit.hl7.igamt.display.service.DisplayInfoService;
import gov.nist.hit.hl7.igamt.ig.domain.Ig;
import gov.nist.hit.hl7.igamt.ig.domain.verification.IgamtObjectError;
import gov.nist.hit.hl7.igamt.ig.domain.verification.VerificationResult;
import gov.nist.hit.hl7.igamt.ig.service.IgService;
import gov.nist.hit.hl7.igamt.segment.domain.Field;
import gov.nist.hit.hl7.igamt.segment.domain.Segment;
import gov.nist.hit.hl7.igamt.segment.service.SegmentService;
import gov.nist.hit.hl7.igamt.serialization.newImplementation.service.parser.CoConstraintSpreadSheetParser;
import gov.nist.hit.hl7.igamt.serialization.newImplementation.service.parser.ParsedCoConstraint;
import gov.nist.hit.hl7.igamt.serialization.newImplementation.service.parser.ParsedGroup;
import gov.nist.hit.hl7.igamt.serialization.newImplementation.service.parser.ParsedTable;
import gov.nist.hit.hl7.igamt.serialization.newImplementation.service.parser.ParserResults;

@Service
public class ExcelImportServiceImpl implements ExcelImportService {

    @Autowired
    DisplayInfoService displayInfoService;

    @Autowired
    IgService igService;

    @Autowired
    SegmentService segmentService;

    @Autowired
    DatatypeService datatypeService;

    @Autowired
    ConformanceProfileService conformanceProfileService;

    @Autowired
    CoConstraintSerializationHelper coConstraintSerializationHelper;

    static String newLine = System.getProperty("line.separator");


    @Override
    public ParserResults readFromExcel(InputStream excelStream, String igID, String conformanceProfileID, String contextId, String segmentRef) throws Exception {
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(excelStream);

        //Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);

        ResourceSkeletonBone targetSegment = this.coConstraintSerializationHelper.getSegmentRef(
                this.coConstraintSerializationHelper.getConformanceProfileSkeleton(conformanceProfileID),
                new StructureElementRef(contextId),
                new StructureElementRef(segmentRef)
        );

        Segment segment = segmentService.findById(targetSegment.getResource().getId());

//		//Iterate through each rows one by one
        CoConstraintSpreadSheetParser parser = new CoConstraintSpreadSheetParser(sheet);
        ParserResults parserResults = processCoConstraintTable(parser.parseTable(sheet), segment, igID, parser.wrongHeaderStructure, parser.emptyCellInRow);


        CoConstraintTableConditionalBinding coConstraintTableConditionalBinding = new CoConstraintTableConditionalBinding();
        coConstraintTableConditionalBinding.setValue(parserResults.getCoConstraintTable());
        ConformanceProfile cs = conformanceProfileService.findById(conformanceProfileID);
        List<CoConstraintBinding> coConstraintsBindings = cs.getCoConstraintsBindings();
        if (coConstraintsBindings == null) {
            coConstraintsBindings = new ArrayList<CoConstraintBinding>();
        }
        boolean foundOne = false;
        for (CoConstraintBinding coConstraintBinding : coConstraintsBindings) {
            if (coConstraintBinding.getContext().getPathId().equals(contextId)) {
                foundOne = true;
                List<CoConstraintBindingSegment> bindings = coConstraintBinding.getBindings();
                for (CoConstraintBindingSegment coConstraintBindingSegment : bindings) {
                    if (coConstraintBindingSegment.getSegment().getPathId().equals(segmentRef)) {
                        Optional<IgamtObjectError> match = parserResults.getVerificationResult().getErrors().stream().filter((error) ->
                        {
                            return error.getSeverity().equals("ERROR");

                        }).findFirst();
                        if (!match.isPresent()) {
                            coConstraintBindingSegment.getTables().add(coConstraintTableConditionalBinding);
                        }
                    }
                }
            }
        }
        conformanceProfileService.save(cs);
        System.out.println("SAVED");

        return parserResults;
    }


    //NEW
    public CoConstraintHeaders processHeaders(ParsedTable table, Map<Integer, CoConstraintHeader> headerMap, Segment segment, List<IgamtObjectError> errors) throws Exception {
        CoConstraintHeaders coConstraintHeaders = new CoConstraintHeaders();
        List<CoConstraintHeader> selectors = createHeaders(table.getIfHeaders(), headerMap, segment, false, errors);
        List<CoConstraintHeader> constraints = createHeaders(table.getThenHeaders(), headerMap, segment, false, errors);
        List<CoConstraintHeader> narratives = createHeaders(table.getNarrativeHeaders(), headerMap, segment, true, errors);
        coConstraintHeaders.setSelectors(selectors);
        coConstraintHeaders.setConstraints(constraints);
        coConstraintHeaders.setNarratives(narratives);
        if (checkCardinalityColumns(constraints)) {
            IgamtObjectError igamtObjectError = new IgamtObjectError("Wrong Table Structure", "Use a template as a starting point", Type.COCONSTRAINTBINDINGS, null, "Varies cells should be followed by a cardinality Column",
                    "first row", "ERROR", "handleBy");
            errors.add(igamtObjectError);
        }

        if (table.isHasGrouper()) {
            coConstraintHeaders.setGrouper(processGrouper(table.getGrouperValue()));
        }

        System.out.println("Proccessed all headers");
        return coConstraintHeaders;
    }

    public CoConstraintGrouper processGrouper(String grouperValue) {
        CoConstraintGrouper grouper = new CoConstraintGrouper();
        String[] value = grouperValue.split(":");
        String pathId = value[1].split("-")[1].replace(".", "-");
        grouper.setPathId(pathId);
        return grouper;
    }

    private boolean checkCardinalityColumns(List<CoConstraintHeader> constraints) {
        for (int i = 0; i < constraints.size(); i++) {
            if (((DataElementHeader) constraints.get(i)).getColumnType().equals("VARIES")) {

            }
        }
        return false;
    }


//	 //NEW
//	 void checkAndAddGroup(int i) {
//	        if(i > 1) {
//	            groupHeader.add(i);
//	        }
//	    }

    public ParserResults processCoConstraintTable(ParsedTable parsedTable, Segment segment, String igID, boolean wrongHeaderStructure, boolean emptyCellInRow) throws Exception {
        ParserResults parserResults = new ParserResults();
        CoConstraintTable coConstraintTable = new CoConstraintTable();
        VerificationResult verificationResult = new VerificationResult();
        List<IgamtObjectError> errors = new ArrayList<IgamtObjectError>();
        verificationResult.setErrors(errors);
        parserResults.setVerificationResult(verificationResult);
        if (parsedTable.isHasGrouper() != parsedTable.getParsedGroups().size() > 0) {
            IgamtObjectError igamtObjectError = new IgamtObjectError("Wrong Table Structure", "Use a template as a starting point", Type.COCONSTRAINTBINDINGS, null, "If coconstraint table contains groups, then table must contain a group By column right after THEN columns",
                    "first row", "ERROR", "handleBy");
            errors.add(igamtObjectError);
        }

        Map<Integer, CoConstraintHeader> headerMap = new HashMap<Integer, CoConstraintHeader>();

//		Row row1 = rowIterator.next();
//		Row row2 = rowIterator.next();

        if (wrongHeaderStructure == true) {
            IgamtObjectError igamtObjectError = new IgamtObjectError("Wrong Table Structure", "Use a template as a starting point", Type.COCONSTRAINTBINDINGS, null, "Wrong Table Structure Or Empty Spread Sheet, the first row of the spread sheet should only contains cells with following values : Usage, Cardinality, IF, THEN, NARRATIVES, - Case Sensitive-",
                    "first row", "ERROR", "handleBy");
            errors.add(igamtObjectError);
        } else {


            CoConstraintHeaders coConstraintHeaders = this.processHeaders(parsedTable, headerMap, segment, errors);
//		CoConstraintHeaders coConstraintHeaders = processHeaders(row1,row2,headerMap,segmentID);
            if (emptyCellInRow == true) {
//	    	 IgamtObjectError igamtObjectError = new IgamtObjectError("Wrong Table Structure", "Use a template as a starting point", Type.COCONSTRAINTBINDINGS, null, "Empty cell in a Row causing erroneous Table strucutre. Please verify that headers do not have empty columns",
//				      "first row", "ERROR", "handleBy");
//			errors.add(igamtObjectError);
            } else {
                List<CoConstraint> coConstraintsFree = new ArrayList<CoConstraint>();
                List<CoConstraintGroupBinding> groups = new ArrayList<CoConstraintGroupBinding>();

                for (ParsedCoConstraint parsedCoConstraint : parsedTable.getParsedCoConstraints()) {
                    CoConstraint coConstraint = processCoConstraintRow(parsedCoConstraint, headerMap, igID, errors);
                    coConstraintsFree.add(coConstraint);
                }
                groups = processGroupList(parsedTable.getParsedGroups(), headerMap, igID, errors);
                coConstraintTable.setCoConstraints(coConstraintsFree);
                coConstraintTable.setGroups(groups);
                coConstraintTable.setHeaders(coConstraintHeaders);
                coConstraintTable.setId(UUID.randomUUID().toString());
                coConstraintTable.setTableType(CollectionType.TABLE);

                System.out.println("Groups result : " + groups.size());
                parserResults.setCoConstraintTable(coConstraintTable);
                return parserResults;
            }
        }
        return parserResults;
    }


    private CoConstraint processCoConstraintRow(ParsedCoConstraint parsedCoConstraint, Map<Integer, CoConstraintHeader> headerMap, String igID, List<IgamtObjectError> errors) throws Exception {
        CoConstraint coConstraint = new CoConstraint();
        String id;
        boolean cloned;
        coConstraint.setCloned(false);
        CoConstraintRequirement requirement = new CoConstraintRequirement();
        CoConstraintCardinality cardinality = new CoConstraintCardinality();
        requirement.setCardinality(cardinality);
        Map<String, CoConstraintCell> cells = new HashMap<>();
        coConstraint.setCells(cells);
        coConstraint.setRequirement(requirement);

        // New after integration of interface model
        List<Map.Entry<Integer, String>> entries = Stream.concat(
                Stream.concat(
                        parsedCoConstraint.ifs.entrySet().stream(),
                        parsedCoConstraint.then.entrySet().stream()),
                parsedCoConstraint.narratives.entrySet().stream())
                .collect(Collectors.toList());

        CoConstraintUsage coConstraintUsage = CoConstraintUsage.valueOf(parsedCoConstraint.getUsage());
        requirement.setUsage(coConstraintUsage);
        System.out.println(newLine + " USAGE : " + requirement.getUsage().name());

        cardinality.setMin(parsedCoConstraint.getMinCardinality());
        System.out.println(newLine + " MIN : " + cardinality.getMin());

        cardinality.setMax(parsedCoConstraint.getMaxCardinality());
        System.out.println(newLine + " MAX : " + cardinality.getMax());
        int i = 0;
        for (Map.Entry<Integer, String> entry : entries) {
//	    	 	// column index
//	    	 	entry.getKey();
//	    	 	// column value
//	    	 	entry.getValue(); 	
            CoConstraintHeader coConstraintHeader = headerMap.get(entry.getKey());
            int j = entries.size();
            if (i < entries.size() - 1) {
                CoConstraintCell coConstraintCell = processConstraintCell(entry.getKey(), entry.getValue(), entries.get(i + 1).getValue(), headerMap, igID, errors);
                if (coConstraintHeader != null) {
                    cells.put(coConstraintHeader.getKey(), coConstraintCell);
                }
            } else {
                CoConstraintCell coConstraintCell = processConstraintCell(entry.getKey(), entry.getValue(), null, headerMap, igID, errors);
                if (coConstraintHeader != null) {
                    cells.put(coConstraintHeader.getKey(), coConstraintCell);
                }
            }

            i++;
        }

        return coConstraint;

    }

    private List<CoConstraintGroupBinding> processGroupList(List<ParsedGroup> parsedGroups,
                                                            Map<Integer, CoConstraintHeader> headerMap, String igID, List<IgamtObjectError> errors) throws Exception {
        List<CoConstraintGroupBinding> groups = new ArrayList<CoConstraintGroupBinding>();
//		List<CoConstraint> coConstraintsListGroup = new ArrayList<CoConstraint>();
//		CoConstraintGroupBindingContained group = new CoConstraintGroupBindingContained();

        for (ParsedGroup parsedGroup : parsedGroups) {
            CoConstraintGroupBindingContained group = new CoConstraintGroupBindingContained();
            CoConstraintGroupBindingContained coConstraintGroupBindingHeaderInfo = processHeaderGroup(parsedGroup);
            group.setId(coConstraintGroupBindingHeaderInfo.getId());
            group.setType(GroupBindingType.CONTAINED);
            group.setRequirement(coConstraintGroupBindingHeaderInfo.getRequirement());
            group.setName(coConstraintGroupBindingHeaderInfo.getName());

            List<CoConstraint> coConstraintsListGroup = new ArrayList<CoConstraint>();
            group.setCoConstraints(coConstraintsListGroup);

            for (ParsedCoConstraint parsedCoConstraint : parsedGroup.getParsedCoConstraints()) {
                CoConstraint coConstraint = processCoConstraintRow(parsedCoConstraint, headerMap, igID, errors);
                coConstraintsListGroup.add(coConstraint);
            }
            groups.add(group);
        }


//					for(ParsedCoConstraint parsedCoConstraint : parsedGroup.getParsedCoConstraints()) {
//					CoConstraint coConstraint = processCoConstraintRow(parsedCoConstraint, headerMap, igID);
//					coConstraintsListGroup.add(coConstraint);					
//				} 

        return groups;
    }

    private CoConstraintGroupBindingContained processHeaderGroup(ParsedGroup parsedGroup) {
        CoConstraintGroupBindingContained coConstraintGroupBinding = new CoConstraintGroupBindingContained();
        String id;
        CoConstraintRequirement requirement = new CoConstraintRequirement();
        coConstraintGroupBinding.setRequirement(requirement);
        CoConstraintCardinality coConstraintCardinality = new CoConstraintCardinality();
        requirement.setCardinality(coConstraintCardinality);
        GroupBindingType type;
        String name;
        List<CoConstraint> groupCoConstraints = new ArrayList<CoConstraint>();

        CoConstraintUsage coConstraintUsage = CoConstraintUsage.valueOf(parsedGroup.getUsage().replaceAll("\\s", ""));
        requirement.setUsage(coConstraintUsage);
        System.out.println(newLine + " USAGE GROUP : " + requirement.getUsage().name());
        coConstraintCardinality.setMin(parsedGroup.getMinCardinality());
        System.out.println(newLine + " MIN : " + coConstraintCardinality.getMin());
        coConstraintCardinality.setMax(parsedGroup.getMaxCardinality().replaceAll("\\s", ""));
        System.out.println(newLine + " MAX : " + coConstraintCardinality.getMax());

        coConstraintGroupBinding.setName(parsedGroup.getName().replaceAll("\\s", ""));
        System.out.println(newLine + " Group Name : " + parsedGroup.getName());


        return coConstraintGroupBinding;
    }

    private boolean isGroupHeader(Row r) {
        System.out.println("Number of cells : " + r.getPhysicalNumberOfCells() + " starts with for name : " + r.getCell(3).getStringCellValue());
        return r.getPhysicalNumberOfCells() == 4 && r.getCell(3).getStringCellValue().startsWith("Group name");
    }

    public Map<Integer, String> parseHeader(Row row, int start, int end) {
        Map<Integer, String> header = new HashMap<>();
        for (int i = start; i <= end; i++) {
            header.put(i, this.getCellValue(row.getCell(i)));
        }
        return header;
    }

    public List<CoConstraintHeader> createHeaders(Map<Integer, String> values, Map<Integer, CoConstraintHeader> headerMap, Segment segment, boolean narrative, List<IgamtObjectError> errors) throws Exception {
        List<CoConstraintHeader> headers = new ArrayList<CoConstraintHeader>();
        for (Integer location : values.keySet()) {
            if (!narrative) {
                CoConstraintHeader coConstraintHeader = processIfHeaderCell(values.get(location), segment, errors);
                if (coConstraintHeader != null) {
                    headers.add(coConstraintHeader);
                }
                headerMap.put(location, coConstraintHeader);
            } else {
                CoConstraintHeader coConstraintHeader = processNarrativeHeaderCell(values.get(location), errors);
                headers.add(coConstraintHeader);
                headerMap.put(location, coConstraintHeader);
            }
        }
        return headers;
    }


    public String getCellValue(Cell cell) {
        String result = "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                result = String.valueOf(Math.round(cell.getNumericCellValue()));
                break;
            case Cell.CELL_TYPE_STRING:
                result = cell.getStringCellValue();
                break;
        }
        return result;
    }

    public CoConstraintCell processConstraintCell(Integer columnIndex, String cellValue, String cardValue, Map<Integer, CoConstraintHeader> headerMap, String igID, List<IgamtObjectError> errors) throws Exception {
        CoConstraintHeader coConstraintHeader = headerMap.get(columnIndex);
        if (coConstraintHeader != null && coConstraintHeader.getType().equals(HeaderType.DATAELEMENT)) {
            switch (((DataElementHeader) coConstraintHeader).getColumnType()) {
                case CODE:
                    CodeCell codeCell = processCodeCell(cellValue, errors);
                    return codeCell;

                case VALUE:
                    ValueCell valueCell = new ValueCell();
                    valueCell.setValue(cellValue);
                    return valueCell;


                case DATATYPE:
                    DatatypeCell datatypeCell = new DatatypeCell();
                    String datatypeRegularExpression = "\\s*Value\\s*:(\\s*\\w)*\\s*,\\s*Flavor\\s*:(\\s*\\w)*\\s*";
                    if (cellValue != null && cellValue != "") {
                        if (cellValue.matches(datatypeRegularExpression)) {
                            String datatypeName = cellValue.split(",")[0].split(":")[1].replaceAll("\\s+", "");
                            String flavor = cellValue.split(",")[1].split(":")[1].replaceAll("\\s+", "");
                            String fixedName = datatypeName;
                            String variableName = flavor.contains("_") ? flavor.split("_")[1] : null;

                            // condition ? value_if_true : value_if_false;
                            datatypeCell.setValue(datatypeName);
                            Ig igDocument = igService.findById(igID);
                            String datatypeId = "NOT FOUND";
                            Set<DisplayElement> datatypes = displayInfoService.convertDatatypeRegistry(igDocument.getDatatypeRegistry());
                            Optional<DisplayElement> match = datatypes.stream().filter((displayElement) -> {
                                if (displayElement.getVariableName() != null) {
                                    return displayElement.getFixedName().equals(fixedName) && displayElement.getVariableName().equals(variableName);
                                } else {
                                    return displayElement.getFixedName().equals(fixedName) && variableName == null;
                                }
                            }).findFirst();

                            if (match.isPresent()) {
                                DisplayElement displayElement = match.get();
                                datatypeCell.setDatatypeId(displayElement.getId());
                                datatypeCell.setType(ColumnType.DATATYPE);
                            } else {
//					throw new Exception("Couldn't find datatype : " + datatypeName );
                                IgamtObjectError igamtObjectError = new IgamtObjectError("Datatype not found", "", Type.COCONSTRAINTBINDINGS, null, "Couldn't find datatype : " + datatypeName,
                                        "location", "ERROR", "handleBy");
                                errors.add(igamtObjectError);
                            }

                        } else {
//	    					throw new Exception("Invalid Datatype Cell expression : " + cellValue +
//	    							" . Should match the following regular expression : " + datatypeRegularExpression 
//	    							+ ". Example : Value: CE,  Flavor: CE_01");

                            IgamtObjectError igamtObjectError = new IgamtObjectError("Invalid datatype cell expression", "Value: CE,  Flavor: CE_01", Type.COCONSTRAINTBINDINGS, null, "Invalid Datatype Cell expression : " + cellValue +
                                    " . Should match the following regular expression : " + datatypeRegularExpression
                                    + ".",
                                    "location", "ERROR", "handleBy");
                            errors.add(igamtObjectError);
                        }
                    }
//	    		return null;	


//	            	
                    return datatypeCell;


                case VALUESET:
                    ValueSetCell valueSetCell = processValueSetCell(cellValue, igID, errors);
                    return valueSetCell;

                case VARIES:
                    VariesCell variesCell = new VariesCell();
                    variesCell.setCardinalityMax(cardValue);
//	            	CoConstraintCell coConstraintCell = new CoConstraintCell();
//	            	String x = "a : 1";
//	            	x.matches("Code\\w*:\\\\w*[a-zA-Z] : [0-9]");
                    //Strength:\s*[A-Z],\s*Location:\[[0-9](,[0-9])*\],\s*Valuesets:\s*\[[a-zA-Z0-9_](,[a-zA-Z0-9_])*\]

                    if (cellValue.startsWith("Code:")) {
                        CodeCell codeCellVaries = processCodeCell(cellValue, errors);
                        variesCell.setCellValue(codeCellVaries);
                        variesCell.setCellType(ColumnType.CODE);
                        System.out.println("In VARIES CODE : " + cellValue);
                        System.out.println("In VARIES CODE : " + cellValue);

                        return variesCell;
                    } else if (cellValue.startsWith("Strength:")) {
                        ValueSetCell valueSetCellVaries = processValueSetCell(cellValue, igID, errors);
                        variesCell.setCellValue(valueSetCellVaries);
                        variesCell.setCellType(ColumnType.VALUESET);
                        System.out.println("In VARIES VALUESET : " + cellValue);
//	            		System.out.println("In VARIES VALUESET : " + valueSetCellVaries.getBindings().size());

                        return variesCell;
                    } else {
                        ValueCell valueCell2 = new ValueCell();
                        valueCell2.setValue(cellValue);
                        variesCell.setCellValue(valueCell2);
                        variesCell.setCellType(ColumnType.VALUE);
                        System.out.println("In VARIES VALUE : " + valueCell2.getValue());
                        System.out.println("In VARIES VALUE : " + cellValue);


                        return variesCell;

                    }

            }
            return null;
        } else if (coConstraintHeader != null && coConstraintHeader.getType().equals(HeaderType.NARRATIVE)) {
            ValueCell valueCell = new ValueCell();
            valueCell.setValue(cellValue);
            return valueCell;
        }

        return null;
    }

    public CoConstraintHeader processIfHeaderCell(String cellValue, Segment segment, List<IgamtObjectError> errors) throws Exception {
        DataElementHeader dataElementHeader = new DataElementHeader();
        if (cellValue == null) {
            IgamtObjectError igamtObjectError = new IgamtObjectError("Empty Header Cell", "CODE OBX-3", Type.COCONSTRAINTBINDINGS, null, "Invalid header value",
                    "table_headers", "ERROR", "handleBy");
            errors.add(igamtObjectError);
        } else {
            String[] splitCellValue = cellValue.split("\\s+");
            ;

            String columnType = splitCellValue[0];
            if (!(columnType.equals("VALUE") || columnType.equals("VARIES") || columnType.equals("DATATYPE") || columnType.equals("VALUESET") || columnType.equals("CODE") || columnType.equals("Cardinality"))) {
                IgamtObjectError igamtObjectError = new IgamtObjectError("Invalid header type value", "CODE OBX-3", Type.COCONSTRAINTBINDINGS, null, "Invalid header value, encountred " + columnType + " expected values : " + " CODE, VALUE, VALUESET, DATATYPE, VARIES.",
                        "table_headers", "ERROR", "handleBy");
                errors.add(igamtObjectError);
            } else {
                if (!columnType.equals("Cardinality")) {
                    String name = splitCellValue[1];
                    String stringKey = name.split("-")[1].replace(".", "-");
//			int key = Integer.parseInt(name.split("-")[1]);
                    String datatype = name.split("-")[0];
                    dataElementHeader.setColumnType(ColumnType.valueOf(columnType));
                    dataElementHeader.setKey(stringKey);

                    System.out.println(" type : " + dataElementHeader.getColumnType().name() + " and name : " + name + " and key : " + stringKey);
                    return dataElementHeader;
                }
            }
        }
        return null;

    }

    public DataElementHeaderInfo processPath(Segment segment, String headerName) throws Exception {
        String[] path = headerName.split("\\.");
        DataElementHeaderInfo dataElementHeaderInfo = new DataElementHeaderInfo();
        if (path.length == 1) {
            Field field = fetchDatatypeFromSegment(segment, path[0]);
            if (field != null && field.getRef() != null) {
                Datatype datatype = datatypeService.findById(field.getRef().getId());
                if (datatype != null) {
                    CoConstraintCardinality coConstraintCardinality = new CoConstraintCardinality();
                    coConstraintCardinality.setMax(field.getMax());
                    coConstraintCardinality.setMin(field.getMin());
                    dataElementHeaderInfo.setCardinality(coConstraintCardinality);
                    dataElementHeaderInfo.setDatatype(datatype.getName()); //name not ID
                    dataElementHeaderInfo.setLocation(Integer.parseInt(path[0]));
                    dataElementHeaderInfo.setType(Type.FIELD);
                    dataElementHeaderInfo.setVersion(datatype.getDomainInfo().getVersion());
                    dataElementHeaderInfo.setParent(segment.getName());
                } else {
                    throw new Exception("Cannot find datatype related to path : " + segment.getName() + "-" + headerName);
                }
            } else {
                throw new Exception("Invalid path : " + segment.getName() + "-" + headerName);
            }
        } else if (path.length == 2) {
            Field field = fetchDatatypeFromSegment(segment, path[0]);
            Datatype datatype1 = datatypeService.findById(field.getRef().getId());
            if (datatype1 instanceof ComplexDatatype) {
                Component component = fetchDatatypeFromComplexDatatype((ComplexDatatype) datatype1, path[1]);
                Datatype datatype2 = datatypeService.findById(field.getRef().getId());
//			CoConstraintCardinality coConstraintCardinality = new CoConstraintCardinality();
//			coConstraintCardinality.setMax(component.getMaxLength());
//			coConstraintCardinality.setMin(Integer.parseInt(component.getMinLength()));
//			dataElementHeaderInfo.setCardinality(coConstraintCardinality);
                dataElementHeaderInfo.setDatatype(datatype2.getName());
                dataElementHeaderInfo.setLocation(Integer.parseInt(path[1]));
                dataElementHeaderInfo.setType(Type.COMPONENT);
                dataElementHeaderInfo.setVersion(datatype2.getDomainInfo().getVersion());
                dataElementHeaderInfo.setParent(datatype1.getName());
            } else {
                throw new Exception("Invalid path : " + segment.getName() + "-" + headerName);
            }
        } else if (path.length == 3) {
            Field field = fetchDatatypeFromSegment(segment, path[0]);
            Datatype datatype1 = datatypeService.findById(field.getRef().getId());
            if (datatype1 instanceof ComplexDatatype) {
                Component component1 = fetchDatatypeFromComplexDatatype((ComplexDatatype) datatype1, path[1]);
                Datatype datatype2 = datatypeService.findById(component1.getRef().getId());
                if (datatype2 instanceof ComplexDatatype) {
                    Component component2 = fetchDatatypeFromComplexDatatype((ComplexDatatype) datatype2, path[2]);
                    Datatype datatype3 = datatypeService.findById(component2.getRef().getId());
//				CoConstraintCardinality coConstraintCardinality = new CoConstraintCardinality();
//				coConstraintCardinality.setMax(component2.getMaxLength());
//				coConstraintCardinality.setMin(Integer.parseInt(component2.getMinLength()));
//				dataElementHeaderInfo.setCardinality(coConstraintCardinality);
                    dataElementHeaderInfo.setDatatype(datatype3.getName());
                    dataElementHeaderInfo.setLocation(Integer.parseInt(path[2]));
                    dataElementHeaderInfo.setType(Type.SUBCOMPONENT);
                    dataElementHeaderInfo.setVersion(datatype3.getDomainInfo().getVersion());
                    dataElementHeaderInfo.setParent(datatype2.getName());

                } else {
                    throw new Exception("Invalid path : " + segment.getName() + "-" + headerName);
                }
            } else {
                throw new Exception("Invalid path : " + segment.getName() + "-" + headerName);

            }
        } else if (path.length > 3) {
            throw new Exception("Invalid path : " + segment.getName() + "-" + headerName);
        }

        return dataElementHeaderInfo;
    }


    private Component fetchDatatypeFromComplexDatatype(ComplexDatatype datatype, String path) {
        Component component = new Component();
        for (Component c : datatype.getComponents()) {
            if (c.getPosition() == Integer.parseInt(path)) {
                component = c;
            }
        }
        //TODO check datatype instance of
        return component;
    }

    private Field fetchDatatypeFromSegment(Segment segment, String path) {
        Field field = new Field();
        for (Field f : segment.getChildren()) {
            if (f.getPosition() == Integer.parseInt(path)) {
                field = f;
            }
        }
        //TODO check datatype instance of
//		Datatype datatype = datatypeService.findById(field.getRef().getId());
        return field;
    }

    public CodeCell processCodeCell(String cellValue, List<IgamtObjectError> errors) throws Exception {
//		String codeRegularExpression = "\\s*Code\\s*:(\\s*\\w)*\\s*,\\s*Code System\\s*:(\\s*\\w)*\\s*,\\s*Location\\s*:\\s*([0-9](?:\\s*or\\s*[0-9])*)\\s*";
        String codeRegularExpression = "\\s*Code\\s*:(.)*\\s*,\\s*Code System\\s*:(\\s*\\w)*\\s*,\\s*Location\\s*:\\s*([0-9](?:\\s*or\\s*[0-9])*)\\s*";

        if (cellValue != null && cellValue != "") {
            if (cellValue.matches(codeRegularExpression)) {
                CodeCell codeCell = new CodeCell();
                String[] splitCodeCellValue = cellValue.split(",");
                String codeValue = splitCodeCellValue[0].split(":")[1];
                if (codeValue.contains(" ")) {
                    IgamtObjectError igamtObjectError = new IgamtObjectError(" Code Cell Containing White Space ", "Code:AAAA,  Code System:BBBB, Location: 1 or 4", Type.COCONSTRAINTBINDINGS, null, "Code cell value : " + cellValue + " should not contain white space."
                            + " . Should match the following regular expression : " + codeRegularExpression,
                            "location", "INFO", "handleBy");
                    errors.add(igamtObjectError);
                }
                System.out.println(newLine + " CODE VALUE : " + codeValue);
                String codeSystemValue = splitCodeCellValue[1].split(":")[1];
                System.out.println(newLine + " CODESystem VALUE : " + codeSystemValue);
                List<Integer> locations = new ArrayList<Integer>();
                String[] LocationsString = splitCodeCellValue[2].split(":")[1].split("or");
                for (String s : LocationsString) {
                    System.out.println(newLine + " the STRING OF LOCATION S : " + s);
                    locations.add(Integer.parseInt(s.replaceAll("\\s", "")));
                }
                System.out.println(newLine + " Locations VALUE : " + locations);
                codeCell.setCode(codeValue);
                codeCell.setCodeSystem(codeSystemValue);
                codeCell.setLocations(locations);
                return codeCell;
            } else {
//					throw new Exception("Invalid Code Cell expression : " + cellValue +
//							" . Should match the following regular expression : " + codeRegularExpression 
//							+ ". Example : Code: IF1 code 1,  Code System: IF1 codesystem 1, Location: 1 or 4");
//					
                IgamtObjectError igamtObjectError = new IgamtObjectError("Invalid Code Cell expression", "Code:AAAA,  Code System:BBBB, Location: 1 or 4", Type.COCONSTRAINTBINDINGS, null, "Invalid Code Cell expression : " + cellValue +
                        " . Should match the following regular expression : " + codeRegularExpression,
                        "table_headers", "ERROR", "handleBy");
                errors.add(igamtObjectError);
            }
        }
        return null;
    }


    public ValueSetCell processValueSetCell(String cellValue, String igID, List<IgamtObjectError> errors) throws Exception {
        String valueSetRegularExpression = "\\s*Strength\\s*:(\\s*[A-Z])\\s*,\\s*Location\\s*:\\s*(\\[\\s*[0-9\\s*]\\s*(?:,\\s*[0-9]\\s*)*\\s*\\])\\s*,\\s*Valuesets\\s*:\\s*(\\[\\s*[a-zA-Z0-9_]*(?:\\s*,\\s*[a-zA-Z0-9_]*)*\\s*\\])\\s*";
        ValueSetCell valueSetCell = new ValueSetCell();
        List<ValuesetBinding> list = new ArrayList<ValuesetBinding>();
        if (cellValue != null && cellValue != "") {
            if (cellValue.matches(valueSetRegularExpression)) {
                System.out.println("ValueSet cell value is : " + cellValue);
                ValuesetBinding valueSetBinding = new ValuesetBinding();
                List<String> valueSets = new ArrayList<String>();

//    	 ValuesetStrength strength = new ValuesetStrength();
//			System.out.println("LOOK HERE : " + cell.getStringCellValue().split(",")[0].split(":")[1]);
                Pattern pattern = Pattern.compile(valueSetRegularExpression);
                Matcher matcher = pattern.matcher(cellValue);
                String usage = "";
                String locations = "";
                String allValueSets = "";
                if (matcher.find()) {
                    usage = matcher.group(1);
                    locations = matcher.group(2);
                    allValueSets = matcher.group(3);
                    System.out.println("Found value: " + matcher.group(0));
                    System.out.println("Found value: " + matcher.group(1));
                    System.out.println("Found value: " + matcher.group(2));
                } else {
                    System.out.println("NO MATCH");
                }

                valueSetBinding.setStrength(ValuesetStrength.valueOf(usage.replaceAll("\\s", "")));
                Set<Integer> locations2 = new HashSet<Integer>();
//			String[] splitCodeCellValue2 = cell.getStringCellValue().split(",");
//			String[] LocationsString2 = splitCodeCellValue2[1].split(":")[1].replace("]", "").replace("[", "").split(",");
                for (String s : locations.replace("]", "").replace("[", "").split(",")) {
                    System.out.println(newLine + " the STRING OF LOCATION S : " + s);
                    locations2.add(Integer.parseInt(s.replaceAll("\\s", "")));
                }
                valueSetBinding.setValuesetLocations(locations2);
                Ig igDocument = igService.findById(igID);

//			String[] valueSetsInString = splitCodeCellValue2[2].split(":")[1].replace("]", "").replace("[", "").replaceAll("\\s", "").split(",");
                for (String valueSet : allValueSets.replace("]", "").replace("[", "").replaceAll("\\s", "").split(",")) {
                    Set<DisplayElement> valuesetsdisplay = displayInfoService.convertValueSetRegistry(igDocument.getValueSetRegistry());
                    Optional<DisplayElement> match = valuesetsdisplay.stream().filter((displayElement) -> {
                        return displayElement.getVariableName().equals(valueSet);
                    }).findFirst();

                    if (match.isPresent()) {
                        DisplayElement displayElement = match.get();
                        valueSets.add(displayElement.getId());
                    } else {
//					throw new Exception("Couldn't find valueSet : " + valueSet );

                        IgamtObjectError igamtObjectError = new IgamtObjectError("ValueSet not found", "HL70001", Type.COCONSTRAINTBINDINGS, null, "Couldn't find valueset : " + valueSet,
                                "location", "ERROR", "handleBy");
                        errors.add(igamtObjectError);
                    }
                }
                valueSetBinding.setValueSets(valueSets);
                list.add(valueSetBinding);
                valueSetCell.setBindings(list);
                System.out.println("DASDASD");
            } else {
//			throw new Exception("Invalid ValueSet expression : " + cellValue +
//					" . Should match the following regular expression : " + valueSetRegularExpression 
//					+ ". Example : Strength: S,  Location: [1, 4],  Valuesets: [HL70002, HL70004]");

                IgamtObjectError igamtObjectError = new IgamtObjectError("Invalid ValueSet expression ", "Strength: S,  Location: [1, 4],  Valuesets: [HL70002, HL70004]", Type.COCONSTRAINTBINDINGS, null, "Invalid ValueSet expression : " + cellValue +
                        " . Should match the following regular expression : " + valueSetRegularExpression,
                        "location", "ERROR", "handleBy");
                errors.add(igamtObjectError);
            }

            valueSetCell.setBindings(list);

            return valueSetCell;
        } else {
            valueSetCell.setBindings(list);

            return valueSetCell;
        }
    }

    public CoConstraintHeader processNarrativeHeaderCell(String value, List<IgamtObjectError> errors) {
        System.out.println(newLine + "we in");
//		System.out.println(newLine + "Cell Value : " + cell.getStringCellValue());
        NarrativeHeader narrativeHeader = new NarrativeHeader();
        narrativeHeader.setTitle(value);
        narrativeHeader.setType(HeaderType.NARRATIVE);
        narrativeHeader.setKey(UUID.randomUUID().toString());

//			System.out.println(" narattive title : " + cell.getStringCellValue());
        return narrativeHeader;

    }

}
