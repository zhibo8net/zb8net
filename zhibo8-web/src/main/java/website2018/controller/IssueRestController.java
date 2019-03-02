package website2018.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springside.modules.web.MediaTypes;
import website2018.base.BaseEndPoint;
import website2018.dto.user.ReturnResponse;
import website2018.dto.web.AddIssueDTO;
import website2018.dto.web.IssueWebDTO;
import website2018.service.issue.IssueService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class IssueRestController extends BaseEndPoint {


    @Autowired
    private IssueService issueService;
    @RequestMapping(value = "/api/user/addIssue", produces = MediaTypes.JSON_UTF_8)
    public ReturnResponse addIssue(@RequestBody(required = false) AddIssueDTO addIssueDTO, HttpServletRequest request) {


        return null;
    }
}
