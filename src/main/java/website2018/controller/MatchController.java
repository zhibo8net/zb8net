package website2018.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import website2018.base.BaseEndPoint;
import website2018.domain.FriendLink;
import website2018.service.LiveService;

import java.util.List;

/**
 * Created by Administrator on 2018/9/16.
 */
@Controller
public class MatchController  extends BaseEndPoint {

    @Autowired
    LiveService liveService;

    @RequestMapping(value = "/match_1/{id}")
    public String live(@PathVariable Long id, Model model) {

        List<FriendLink> friendLinks = liveService.findFriendLinks();
        model.addAttribute("friendLinks", friendLinks);

        return "detail";
    }
}
