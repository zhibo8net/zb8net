package website2018.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.utils.mapper.BeanMapper;

import com.google.common.collect.Lists;

import website2018.base.BaseEndPoint;
import website2018.domain.Ended;
import website2018.domain.Video;
import website2018.dto.DailyLivesDTO;
import website2018.dto.EndedDTO;
import website2018.service.IndexService;

@Controller
public class IndexController extends BaseEndPoint {

    @Autowired
    IndexService indexService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String index(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("pageTitle", "体育直播吧");
        List<Ended> footballEndedEntitys = indexService.findEndeds("足球");
        List<EndedDTO> footballEndeds = Lists.newArrayList();
        List<EndedDTO> footballEndeds2 = Lists.newArrayList();
        for(Ended e : footballEndedEntitys) {
            EndedDTO ed = BeanMapper.map(e, EndedDTO.class);
            for(Video v : e.videos) {
                if(v.type.equals("集锦")) {
                    ed.hasJijin = true;
                }else if(v.type.equals("录像")) {
                    ed.hasLuxiang = true;
                }
            }
            if(ed.hasJijin || ed.hasLuxiang) {
                if(ed.name.length() < 13) {
                    footballEndeds.add(ed);
                }else {
                    footballEndeds2.add(ed);
                }
            }
            if(footballEndeds.size() == 8) {
                break;
            }
        }
        request.setAttribute("footballEndeds", footballEndeds);
        request.setAttribute("footballEndeds2", footballEndeds2);

        List<Ended> basketballEndedEntitys = indexService.findEndeds("篮球");
        List<EndedDTO> basketballEndeds = Lists.newArrayList();
        List<EndedDTO> basketballEndeds2 = Lists.newArrayList();
        for(Ended e : basketballEndedEntitys) {
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
                    basketballEndeds.add(ed);
                }else if(ed.name.length() > 13){
                    basketballEndeds2.add(ed);
                }
            }
            if(basketballEndeds.size() == 8) {
                break;
            }
        }
        request.setAttribute("basketballEndeds", basketballEndeds);
        request.setAttribute("basketballEndeds2", basketballEndeds2);

        List<DailyLivesDTO> dailyLives = indexService.dailyLives();
        request.setAttribute("dailyLives", dailyLives);
        
        List<Video> videos = indexService.findVideos();
        request.setAttribute("videos", videos);

        List<Video> footballLuxiangs = indexService.findLuxiangs("足球");
        request.setAttribute("footballLuxiangs", footballLuxiangs);

        List<Video> basketballLuxiangs = indexService.findLuxiangs("篮球");
        request.setAttribute("basketballLuxiangs", basketballLuxiangs);
        
        List<String> days = indexService.days();
        request.setAttribute("days", days);

        request.setAttribute("pageAds", indexService.pageAds());
        
        request.setAttribute("menu", "index");
        return page(request, response, "index");
    }

}
