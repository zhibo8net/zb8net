package website2018.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String jingcai( Model model,HttpServletRequest request, HttpServletResponse response) {

      IssueWebDTO issueWebDTO= issueService.queryLastIssue(request);

        model.addAttribute("issue", issueWebDTO);
        return "jingcai";
    }
    @RequestMapping(value = "/jingcainotice")
    public String jingcainotice( Model model,HttpServletRequest request, HttpServletResponse response) {

        IssueWebDTO issueWebDTO= issueService.queryLastJingcainotice();

        model.addAttribute("issue", issueWebDTO);
        return "jingcainotice";
    }
    }
