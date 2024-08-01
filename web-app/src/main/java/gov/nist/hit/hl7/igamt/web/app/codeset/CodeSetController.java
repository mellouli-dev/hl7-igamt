package gov.nist.hit.hl7.igamt.web.app.codeset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import gov.nist.hit.hl7.igamt.common.base.exception.ForbiddenOperationException;
import gov.nist.hit.hl7.igamt.common.base.exception.ResourceNotFoundException;
import gov.nist.hit.hl7.igamt.common.base.model.ResponseMessage;
import gov.nist.hit.hl7.igamt.common.base.model.ResponseMessage.Status;
import gov.nist.hit.hl7.igamt.common.base.service.CommonService;
import gov.nist.hit.hl7.igamt.valueset.domain.Code;
import gov.nist.hit.hl7.igamt.valueset.domain.CodeSet;
import gov.nist.hit.hl7.igamt.valueset.domain.CodeSetVersion;
import gov.nist.hit.hl7.igamt.valueset.model.CodeRaw;
import gov.nist.hit.hl7.igamt.valueset.model.CodeSetCreateRequest;
import gov.nist.hit.hl7.igamt.valueset.model.CodeSetInfo;
import gov.nist.hit.hl7.igamt.valueset.model.CodeSetListItem;
import gov.nist.hit.hl7.igamt.valueset.model.CodeSetListType;
import gov.nist.hit.hl7.igamt.valueset.model.CodeSetVersionContent;
import gov.nist.hit.hl7.igamt.valueset.service.CodeSetService;
import gov.nist.hit.hl7.igamt.valueset.service.impl.TableCSVGenerator;

@RestController
public class CodeSetController {


	@Autowired
	CodeSetService codeSetService;
	@Autowired
	CommonService commonService;

	@RequestMapping(value = "/api/code-set/create", method = RequestMethod.POST, produces = { "application/json" })
	public ResponseMessage<String> createCodeSet(
			@RequestBody CodeSetCreateRequest CodeSetCreateRequest,
			Authentication authentication
	) {
		CodeSet codeSet = this.codeSetService.createCodeSet(CodeSetCreateRequest, authentication.getName());
		return new ResponseMessage<>(ResponseMessage.Status.SUCCESS, "Code Set Created Successfully",  codeSet.getId(), codeSet.getId(), new Date());
	}


	@RequestMapping(value = "/api/code-set/{id}/state", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@PreAuthorize("AccessResource('CODESET', #id, READ)")
	public CodeSetInfo getCodeSetInfo(
			Authentication authentication,
			@PathVariable("id") String id
	) throws  ResourceNotFoundException, ForbiddenOperationException {
		String username = authentication.getPrincipal().toString();
		return codeSetService.getCodeSetInfo(id, username);
	}


	@RequestMapping(value = "/api/code-set/{id}/applyInfo", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@PreAuthorize("AccessResource('CODESET', #id, WRITE)")
	public ResponseMessage<?> saveCodeSet(
			Authentication authentication,
			@PathVariable("id") String id,  @RequestBody CodeSetInfo content
	) throws  ResourceNotFoundException, ForbiddenOperationException {
		String username = authentication.getPrincipal().toString();
		CodeSet ret =  codeSetService.saveCodeSetContent(id, content, username);
		return new ResponseMessage<>(Status.SUCCESS, "Code Set Saved Successfully", ret.getId(), null);
	}


	@RequestMapping(value = "/api/code-set/{id}/updateViewers", method = RequestMethod.POST, produces = { "application/json" })
	@PreAuthorize("AccessResource('CODESET', #id, WRITE)")
	public @ResponseBody ResponseMessage<String> updateViewers(@PathVariable("id") String id, @RequestBody List<String> viewers, Authentication authentication) throws Exception {
		this.codeSetService.updateViewers(id, viewers, authentication.getName());
		return new ResponseMessage<>(Status.SUCCESS, "", "Code Set Shared Users Successfully Updated", id, false, new Date(), id);
	}


	@RequestMapping(value = "/api/code-set/{id}/code-set-version/{versionId}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@PreAuthorize("AccessResource('CODESETVERSION', #versionId, READ)")
	public CodeSetVersionContent getCodeSetVersion(
			@PathVariable("id") String id,
			@PathVariable("versionId") String versionId
	) throws ResourceNotFoundException {
		return codeSetService.getCodeSetVersionContent(id, versionId);
	}


	@RequestMapping(value = "/api/code-set/{id}/code-set-version/{versionId}", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@PreAuthorize("AccessResource('CODESETVERSION', #versionId, WRITE)")
	public ResponseMessage<?> saveCodeSetVersion(
			@PathVariable("id") String id,
			@PathVariable("versionId") String versionId,
			@RequestBody CodeSetVersionContent content,
			Authentication authentication
	) throws ForbiddenOperationException, ResourceNotFoundException {
		String username = authentication.getPrincipal().toString();
		CodeSetVersion ret = codeSetService.saveCodeSetVersionContent(id, versionId, content, username);
		return new ResponseMessage<>(Status.SUCCESS, "Code Set Saved Successfully", ret.getId(), null);
	}


	@RequestMapping(value = "/api/code-set/{id}/code-set-version/{versionId}/commit", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@PreAuthorize("AccessResource('CODESETVERSION', #versionId, WRITE)")
	public ResponseMessage<?> commit(
			@PathVariable("id") String id,
			@PathVariable("versionId") String versionId,
			@RequestBody CodeSetVersionContent content,
			Authentication authentication
	) throws ForbiddenOperationException, ResourceNotFoundException {
		String username = authentication.getPrincipal().toString();
		CodeSetVersion ret = codeSetService.commit(id, versionId, content, username);
		return new ResponseMessage<>(Status.SUCCESS, "Code Set Committed Successfully", ret.getId(), null);
	}


	@RequestMapping(value = "/api/code-sets", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody List<CodeSetListItem> getCodeSets(
			Authentication authentication,
			@RequestParam("type") CodeSetListType type
	) throws ForbiddenOperationException {
		String username = authentication.getPrincipal().toString();
		List<CodeSet> codesets = new ArrayList<>();
		if (type != null) {
			if (type.equals(CodeSetListType.PRIVATE)) {
				codesets = codeSetService.findByPrivateAudienceEditor(username);
			} else if (type.equals(CodeSetListType.PUBLIC)) {
				codesets = codeSetService.findByPublicAudienceAndStatusPublished();
			} else if (type.equals(CodeSetListType.SHARED)) {
				codesets = codeSetService.findByPrivateAudienceViewer(username);
			} else if (type.equals(CodeSetListType.ALL)) {
				commonService.checkAuthority(authentication, "ADMIN");
				codesets = codeSetService.findAllPrivateCodeSet();
			}
		} else {
			codesets = codeSetService.findByPrivateAudienceEditor(username);
		}
		return codeSetService.convertToDisplayList(codesets);
	}


	@RequestMapping(value = "/api/code-sets/exportCSV/{id}", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded; charset=UTF-8")
	@PreAuthorize("AccessResource('CODESETVERSION', #id, READ)")
	public void exportCSV(@PathVariable("id") String id, HttpServletResponse response)
			throws IOException, ResourceNotFoundException {
		CodeSetVersion codeSetVersion = this.codeSetService.findById(id);
		String csvContent = new TableCSVGenerator().generate(codeSetVersion.getCodes());
		try (InputStream content = IOUtils.toInputStream(csvContent, "UTF-8")) {
			response.setContentType("text/csv");
			response.setHeader("Content-disposition", "attachment;filename=" + codeSetVersion.getVersion()
			+ "-" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");
			FileCopyUtils.copy(content, response.getOutputStream());
		}
	}


	@RequestMapping(value = "/api/code-sets/importCSV", method = RequestMethod.POST)
	public List<Code> uploadCSVFile(@RequestParam("file") MultipartFile file) {
		if (!file.isEmpty()) {
			try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
				ColumnPositionMappingStrategy<CodeRaw> strategy = new ColumnPositionMappingStrategy<>();
				strategy.setType(CodeRaw.class);
				strategy.setColumnMapping("value", "pattern", "description", "codeSystem", "usage", "comments");
				CsvToBean<CodeRaw> csvToBean = new CsvToBeanBuilder<CodeRaw>(reader)
						.withMappingStrategy(strategy)
						.withIgnoreLeadingWhiteSpace(true)
						.withSkipLines(1)
						.build();
				List<CodeRaw> rawCodes = csvToBean.parse();
				return rawCodes.stream().map(CodeRaw::convertToCode).collect(Collectors.toList());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}


	@RequestMapping(value = "/api/code-set/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@PreAuthorize("AccessResource('CODESET', #id, WRITE)")
	public ResponseMessage<String> deleteCodeSet(
			Authentication authentication,
			@PathVariable("id") String id
	) throws ForbiddenOperationException, ResourceNotFoundException {
		String username = authentication.getPrincipal().toString();
		this.codeSetService.deleteCodeSet(id, username);
		return new ResponseMessage<>(ResponseMessage.Status.SUCCESS, "Code Set  Deleted Successfully",  id, id, new Date());
	}


	@RequestMapping(value = "/api/code-set/{id}/clone", method = RequestMethod.POST, produces = {
	"application/json" })
	@PreAuthorize("AccessResource('CODESET', #id, READ)")
	public @ResponseBody ResponseMessage<String> copy(
			@PathVariable("id") String id,
			Authentication authentication
	) throws  ResourceNotFoundException, ForbiddenOperationException {
		String username = authentication.getPrincipal().toString();
		CodeSet clone = codeSetService.clone(id, username);
		return new ResponseMessage<>(
				Status.SUCCESS,
				"",
				"Code Set Cloned Successfully",
				clone.getId(),
				false,
				clone.getDateUpdated(),
				clone.getId()
		);
	}


	@RequestMapping(value = "/api/code-set/{id}/publish", method = RequestMethod.POST, produces = {
	"application/json" })
	@PreAuthorize("IsAdmin() && AccessResource('CODESET', #id, WRITE)")
	public @ResponseBody ResponseMessage<String> publish(@PathVariable("id") String id,  Authentication authentication) throws
	ForbiddenOperationException, ResourceNotFoundException {
		String username = authentication.getPrincipal().toString();
		commonService.checkAuthority(authentication, "ADMIN");
		CodeSet published = codeSetService.publish(id, username);
		return new ResponseMessage<>(
				Status.SUCCESS,
				"",
				"Code Set Published Successfully",
				published.getId(),
				false,
				published.getDateUpdated(),
				published.getId()
		);
	}


	@RequestMapping(value = "/api/code-set/{id}/code-set-version/{versionId}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@PreAuthorize("AccessResource('CODESET', #id, WRITE)")
	public ResponseMessage<String> deleteCodeSetVersion(
			Authentication authentication,
			@PathVariable("id") String id,
			@PathVariable("versionId") String versionId
	) throws Exception  {
		String username = authentication.getPrincipal().toString();
		codeSetService.deleteCodeSetVersion(id, versionId, username);
		return new ResponseMessage<>(ResponseMessage.Status.SUCCESS, "Code Set  Deleted Successfully",  id, new Date());
	}


	@RequestMapping(value = "/api/code-set/{id}/latest", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@PreAuthorize("AccessResource('CODESET', #id, READ)")
	public CodeSetVersionContent getCodeSetLatest(
			@PathVariable("id") String id
	) throws ResourceNotFoundException {
		return codeSetService.getLatestCodeVersion(id);
	}
}
