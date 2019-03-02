package website2018.api.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springside.modules.utils.mapper.BeanMapper;
import org.springside.modules.web.MediaTypes;
import website2018.base.BaseEndPoint;
import website2018.domain.Issue;
import website2018.dto.admin.IssueAdminDTO;
import website2018.dto.admin.MatchAdminDTO;
import website2018.service.admin.IssueAdminService;
import website2018.utils.DateUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
public class IssueAdminEndpoint extends BaseEndPoint {

    private static Logger logger = LoggerFactory.getLogger(IssueAdminEndpoint.class);

    @Autowired
    private IssueAdminService issueAdminService;


    @RequestMapping(value = "/api/admin/issue/page", method = RequestMethod.GET, produces = MediaTypes.JSON_UTF_8)
    public Page<IssueAdminDTO> listByPage(HttpServletRequest request, Pageable pageable) {

        assertAdmin();

        Page<Issue> orders = issueAdminService.findAll(buildSpecification(request, Issue.class), new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), getSort("id", "DIRE")));

        List<IssueAdminDTO> dtos = BeanMapper.mapList(orders, Issue.class, IssueAdminDTO.class);
        for (IssueAdminDTO issueAdminDTO : dtos) {
            if (issueAdminDTO.playDate != null) {
                issueAdminDTO.playDateStr = DateUtils.getDefaultDateStr(issueAdminDTO.playDate);
            }
            if (issueAdminDTO.addTime != null) {
                issueAdminDTO.addTimeStr = DateUtils.getDefaultDateStr(issueAdminDTO.addTime);
            }

            if (issueAdminDTO.updateTime != null) {
                issueAdminDTO.updateTimeStr = DateUtils.getDefaultDateStr(issueAdminDTO.updateTime);
            }
        }

        Page<IssueAdminDTO> dtoPage = new PageImpl(dtos, pageable, orders.getTotalElements());

        logService.log("查询竞猜期次", null);

        return dtoPage;
    }

    @RequestMapping(value = "/api/admin/issue/{id}", method = RequestMethod.DELETE)
    public void deleteIssue(@PathVariable("id") Long id) {

        assertAdmin();

        issueAdminService.delete(id);

        logService.log("删除竞猜期次", null);
    }

    @RequestMapping(value = "/api/admin/issue/{id}", method = RequestMethod.GET, produces = MediaTypes.JSON_UTF_8)
    public IssueAdminDTO listOneNews(@PathVariable("id") Long id) {

        assertAdmin();

        Issue issue = issueAdminService.findOne(id);

        IssueAdminDTO dto = BeanMapper.map(issue, IssueAdminDTO.class);


        return dto;
    }

    @RequestMapping(value = "/api/admin/issue", method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8)
    public void createNews(@RequestBody IssueAdminDTO issueAdminDTO) {

        assertAdmin();

      //  Issue issue = BeanMapper.map(issueAdminDTO, Issue.class);


        issueAdminService.create( issueAdminDTO);

        logService.log("添加竞猜期次", "/issueForm/" + issueAdminDTO.id);
    }
    @RequestMapping(value = "/api/admin/issue/{id}", method = RequestMethod.PUT, consumes = MediaTypes.JSON_UTF_8)
    public void modifyIssue(@RequestBody IssueAdminDTO issueAdminDTO) throws Exception{
        issueAdminService.modifyIssue(issueAdminDTO);
    }
    @RequestMapping(value = "/api/admin/updateIssueUser", method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8)
    public void updateIssueUser(@RequestBody IssueAdminDTO issueAdminDTO) {
        assertAdmin();
        Issue issue = BeanMapper.map(issueAdminDTO, Issue.class);

        issueAdminService.updateIssueUser(issue);
        logService.log("设置用户中奖", "/updateIssueUser/" + issue.id);
    }


    @RequestMapping(value = "/api/admin/issuePublic", method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8)
    public void issuePublic(@RequestBody IssueAdminDTO issueAdminDTO) {

        assertAdmin();


        issueAdminService.issuePublic(issueAdminDTO);

        logService.log("发布竞猜期次", "/issuePublic/" + issueAdminDTO.id);
    }

    @RequestMapping(value = "/api/admin/issueEnd", method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8)
    public void issueEnd(@RequestBody IssueAdminDTO issueAdminDTO) {

        assertAdmin();


        issueAdminService.issueEnd(issueAdminDTO);

        logService.log("结束竞猜期次", "/issuePublic/" + issueAdminDTO.id);
    }
}