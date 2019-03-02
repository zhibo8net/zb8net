package website2018.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import website2018.base.BaseEndPoint;
import website2018.dto.web.IssueWebDTO;
import website2018.service.issue.IssueService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class IssueController extends BaseEndPoint {


    @Autowired
    private IssueService issueService;

    @RequestMapping(value = "/jingcai")
    public String jingcai(HttpServletRequest request, HttpServletResponse response) {

      IssueWebDTO issueWebDTO= issueService.queryLastIssue(request);

        request.setAttribute("issue",issueWebDTO);

        return page(request, response, "jingcai");
    }
    @RequestMapping(value = "/jingcainotice")
    public String jingcainotice(HttpServletRequest request, HttpServletResponse response) {

        IssueWebDTO issueWebDTO= issueService.queryLastJingcainotice();

        request.setAttribute("issue",issueWebDTO);

        return page(request, response, "jingcainotice");
    }
    }
