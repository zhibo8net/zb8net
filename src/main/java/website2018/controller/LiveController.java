package website2018.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springside.modules.utils.mapper.BeanMapper;

import com.google.common.collect.Lists;

import website2018.base.BaseEndPoint;
import website2018.domain.Ended;
import website2018.domain.FriendLink;
import website2018.domain.Video;
import website2018.dto.DailyLivesDTO;
import website2018.dto.EndedDTO;
import website2018.exception.ErrorCode;
import website2018.exception.ServiceException;
import website2018.service.LiveService;

@Controller
public class LiveController extends BaseEndPoint {

    @Autowired
    LiveService liveService;
    
    @RequestMapping(value = "/live_1", method = RequestMethod.GET)
    public String live(String project, String game, Model model) {
        
        if(! (projectWhiteList.contains(project))) {
            throw new ServiceException("请正常使用网站", ErrorCode.BAD_REQUEST);
        }

        model.addAttribute("project", project);
        model.addAttribute("projectEnglish", project.equals("足球") ? "football" : "basketball");
        if(game != null) {
            model.addAttribute("game", game);
            model.addAttribute("pageTitle", game+"直播");
        }else {
            model.addAttribute("game", project);
            model.addAttribute("pageTitle", project+"直播");
        }
        
        List<Ended> projectEndedEntitys = liveService.findEndeds(project);
        List<EndedDTO> projectEndeds = Lists.newArrayList();
        List<EndedDTO> projectEndeds2 = Lists.newArrayList();
        for(Ended e : projectEndedEntitys) {
            EndedDTO ed = BeanMapper.map(e, EndedDTO.class);
            for(Video v : e.videos) {
                if(v.type.equals("集锦")) {
                    ed.hasJijin = true;
                }else if(v.type.equals("录像")) {
                    ed.hasLuxiang = true;
                }
            }
            if(ed.hasJijin || ed.hasLuxiang) {
                if(ed.name.length() < 13 && ed.name.length() > 3) {
                    projectEndeds.add(ed);
                }else if(ed.name.length() > 13){
                    projectEndeds2.add(ed);
                }
            }
            if(projectEndeds.size() == 8) {
                break;
            }
        }
        model.addAttribute("projectEndeds", projectEndeds);
        model.addAttribute("projectEndeds2", projectEndeds2);

        List<DailyLivesDTO> dailyLives = liveService.queryDailyLives(project, game);
        model.addAttribute("dailyLives", dailyLives);
        
        List<Video> videos = liveService.findVideos(project, game);
        model.addAttribute("videos", videos);

        List<Video> projectLuxiangs = liveService.findLuxiangs(project, game);
        model.addAttribute("luxiangs", projectLuxiangs);

        List<FriendLink> friendLinks = liveService.findFriendLinks();
        model.addAttribute("friendLinks", friendLinks);
        
        List<String> days = liveService.days();
        model.addAttribute("days", days);

        model.addAttribute("menu", (project.equals("足球") ? "football" : "basketball")+"Live");
        return "live";
    }

}
