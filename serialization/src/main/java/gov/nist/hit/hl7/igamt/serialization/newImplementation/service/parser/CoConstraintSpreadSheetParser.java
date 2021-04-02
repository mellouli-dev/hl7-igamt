package gov.nist.hit.hl7.igamt.serialization.newImplementation.service.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoConstraintSpreadSheetParser {
	private int if_start = 3;
	private int if_end = 3;
	private int then_start = 0;
	private int then_end = 0;
	private int narratives_start = 0;
	private int narratives_end = 0;
	private int cc_end = 0;
	private final int HEADER_ROW = 1;
	private final int CC_ROW_START = 2;
	private final int USAGE = 0;
	private final int MIN_CARD = 1;
	private final int MAX_CARD = 2;
	private List<Integer> groupHeader = new ArrayList<>();

	public boolean wrongHeaderStructure = false;

	public CoConstraintSpreadSheetParser(Sheet sheet) {
		int numberMergedCells = sheet.getNumMergedRegions();
		if(numberMergedCells == 0) {
			this.wrongHeaderStructure = true;
		}
//		for(int i = 0; i < sheet.getNumMergedRegions(); i++) {
//			CellRangeAddress region = sheet.getMergedRegion(i);
//			Cell value = sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
//			if(value.getCellType() != Cell.CELL_TYPE_STRING || (!((value.getStringCellValue().equals("IF") || value.getStringCellValue().equals("THEN") || value.getStringCellValue().equals("NARRATIVES"))))) {
//				this.wrongHeaderStructure = true;
//			}
//		}
		for(int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);
			Cell value = sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
			if(value.getCellType() == Cell.CELL_TYPE_STRING) {
				switch (value.getStringCellValue()) {
				case "IF" :
					this.if_start = region.getFirstColumn();
					this.if_end = region.getLastColumn();
					break;
				case "THEN" :
					this.then_start = region.getFirstColumn();
					this.then_end = region.getLastColumn();
					break;
				case "NARRATIVES" :
					this.narratives_start = region.getFirstColumn();
					this.narratives_end = region.getLastColumn();
					break;
				default:
					checkAndAddGroup(region.getFirstRow());
				}
			}
		}
		if(this.then_end == 0) {
			this.then_start = this.if_end+1;
			this.then_end = this.if_end+1;
		}
		if(this.narratives_end == 0) {
			this.narratives_start = this.then_end+1;
			this.narratives_end = this.then_end+1;
		}
		if(this.groupHeader.size() > 0) {
			this.cc_end = this.groupHeader.stream().reduce(Integer.MAX_VALUE, Math::min) - 1;
		} else {
			this.cc_end = sheet.getLastRowNum();
		}
	}

	public ParsedTable parseTable(Sheet sheet) {
		ParsedTable table = new ParsedTable();

		// Parse Headers
		Row headers = sheet.getRow(HEADER_ROW);
		if(headers != null) {
		table.setIfHeaders(this.parseHeader(headers, if_start, if_end));
		table.setThenHeaders(this.parseHeader(headers, then_start, then_end));
		table.setNarrativeHeaders(this.parseHeader(headers, narratives_start, narratives_end));
		}

		// Parse Table
		for (int i = CC_ROW_START; i <= sheet.getLastRowNum(); i++) {

			//Parse Group
			if(this.groupHeader.contains(i)) {
				ParsedGroup group = this.parseGroup(i, sheet);
				table.getParsedGroups().add(group);
			}
			// Parse Co-Constraint
			else if(i <= this.cc_end) {
				Row cc = sheet.getRow(i);
				ParsedCoConstraint coConstraint = this.parseCoConstraint(cc);
				table.getParsedCoConstraints().add(coConstraint);
			}
		}
		return table;
	}

	public Map<Integer, String> parseHeader(Row row, int start, int end) {
		Map<Integer, String> header = new HashMap<>();
		for(int i = start; i <= end; i++) {
			header.put(i, row.getCell(i).getStringCellValue());
		}
		return header;
	}


	public ParsedGroup parseGroup(int start, Sheet sheet) {
		ParsedGroup parsedGroup = new ParsedGroup();

		// Parse Group Header
		Row header = sheet.getRow(start);
		for(int j = 0; j <= header.getLastCellNum(); j++) {
			Cell cell = header.getCell(j);
			if(j == USAGE) {
				parsedGroup.usage = cell.getStringCellValue();
			} else if(j == MIN_CARD) {
				parsedGroup.minCardinality = Integer.parseInt(cell.getStringCellValue().trim());
			} else if(j == MAX_CARD) {
				parsedGroup.maxCardinality = cell.getStringCellValue();
			} else if(cell.getCellType() == Cell.CELL_TYPE_STRING) {
				parsedGroup.name = cell.getStringCellValue();
				break;
			}
		}

		// Parse Group Co-Constraints
		for (int i = start + 1; i <= sheet.getLastRowNum(); i++) {
			Row cc = sheet.getRow(i);
			if(this.groupHeader.contains(i)) {
				return  parsedGroup;
			} else {
				ParsedCoConstraint coConstraint = this.parseCoConstraint(cc);
				parsedGroup.getParsedCoConstraints().add(coConstraint);
			}
		}
		return parsedGroup;
	}

	public ParsedCoConstraint parseCoConstraint(Row row) {
		ParsedCoConstraint coConstraint = new ParsedCoConstraint();

		// Parse Co-Constraint Cells
		for(int j = 0; j <= row.getLastCellNum(); j++) {
			Cell cell = row.getCell(j);
			if(j == USAGE) {
				coConstraint.usage = cell.getStringCellValue();
			} else if(j == MIN_CARD) {
				coConstraint.minCardinality = (int) cell.getNumericCellValue();
			} else if(j == MAX_CARD) {
				coConstraint.maxCardinality = cell.getStringCellValue();
			} else if(j >= this.if_start && j <= this.if_end) {
				coConstraint.ifs.put(j, cell.getStringCellValue());
			} else if(j >= this.then_start && j <= this.then_end) {
				coConstraint.then.put(j, cell.getStringCellValue());
			} else if(j >= this.narratives_start && j <= this.narratives_end) {
				coConstraint.narratives.put(j, cell.getStringCellValue());
			}
		}
		return coConstraint;
	}

	void checkAndAddGroup(int i) {
		if(i > 1) {
			groupHeader.add(i);
		}
	}

	@Override
	public String toString() {
		return "ExcelExplorer{" +
				"if_start=" + if_start +
				", if_end=" + if_end +
				", then_start=" + then_start +
				", then_end=" + then_end +
				", narratives_start=" + narratives_start +
				", narratives_end=" + narratives_end +
				", groupHeader=" + groupHeader +
				'}';
	}
}
