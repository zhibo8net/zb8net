package website2018.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import website2018.base.BaseEndPoint;
import website2018.domain.Ended;
import website2018.domain.Video;
import website2018.dto.ProjectVideo;
import website2018.exception.ErrorCode;
import website2018.exception.ServiceException;
import website2018.repository.EndedDao;
import website2018.repository.VideoDao;

@Controller
public class VideoController extends BaseEndPoint {
    
    @Autowired EndedDao endedDao;
    @Autowired VideoDao videoDao;
    
    @RequestMapping(value = "/video_1/{id}", method = RequestMethod.GET)
    public String ended(@PathVariable Long id, Model model) {

        Ended entity = endedDao.findOne(id);
        if(entity == null) {
            throw new ServiceException("不存在的比赛", ErrorCode.BAD_REQUEST);
        }
        model.addAttribute("entity", entity);

        model.addAttribute("project", entity.project);
        model.addAttribute("pageTitle", entity.name);
        List<Video> videos = videoQueryer.findByProjectGameTypeCount(entity.project, null, "视频", BaseEndPoint.RIGHT_VIDEO_COUNT);
        model.addAttribute("videos", videos);

        List<Video> luxiangs = videoQueryer.findByProjectGameTypeCount(entity.project, null, "录像", BaseEndPoint.RIGHT_LUXIANG_COUNT, true);
        model.addAttribute("luxiangs", luxiangs);
        
        model.addAttribute("menu", (entity.project.equals("足球") ? "football" : "basketball")+"Video");
        return "ended";
    }

    @RequestMapping(value = "/projectVideo_1", method = RequestMethod.GET)
    public String projectVideo(String project, Model model) {

        if(! (projectWhiteList.contains(project))) {
            throw new ServiceException("请正常使用网站", ErrorCode.BAD_REQUEST);
        }
        model.addAttribute("project", project);
        model.addAttribute("pageTitle", project+"集锦");
        model.addAttribute("projectEnglish", project.equals("足球") ? "football" : "basketball");
        List<String> games = Lists.newArrayList();
        if(project.equals("篮球")) {
            games = Lists.newArrayList("NBA|CBA".split("\\|"));
        }else {
            games = Lists.newArrayList("英超|意甲|西甲|法甲|德甲|中超|欧冠".split("\\|"));
        }
        
        List<ProjectVideo> projectVideos = Lists.newArrayList();
        for(String g : games) {
            ProjectVideo pv = new ProjectVideo();
            pv.name = g;
            List<Video> videos = videoQueryer.findByProjectGameTypeCount(project, g, "视频", BaseEndPoint.RIGHT_VIDEO_COUNT);
            for(int i = 0; (i < videos.size()) && (i < 4); i++) {
                pv.videos.add(videos.get(i));
            }
            projectVideos.add(pv);
        }
        model.addAttribute("projectVideos", projectVideos);

        List<Video> videos = videoQueryer.findByProjectGameTypeCount(project, null, "视频", BaseEndPoint.RIGHT_VIDEO_COUNT);
        model.addAttribute("videos", videos);

        List<Video> luxiangs = videoQueryer.findByProjectGameTypeCount(project, null, "录像", BaseEndPoint.RIGHT_LUXIANG_COUNT, true);
        model.addAttribute("luxiangs", luxiangs);

        model.addAttribute("menu", (project.equals("足球") ? "football" : "basketball")+"Video");
        return "projectVideo";
    }

    @RequestMapping(value = "/gameVideo_1", method = RequestMethod.GET)
    public String gameVideo(String project, String game, @RequestParam(defaultValue="0") Integer pageNumber, Model model) {

        if(! projectWhiteList.contains(project)) {
            throw new ServiceException("请正常使用网站", ErrorCode.BAD_REQUEST);
        }
        if(! gameWhiteList.contains(game)) {
            throw new ServiceException("请正常使用网站", ErrorCode.BAD_REQUEST);
        }
        model.addAttribute("project", project);
        model.addAttribute("game", game);
        model.addAttribute("projectEnglish", project.equals("足球") ? "football" : "basketball");
        model.addAttribute("pageTitle", game+"集锦");
        Map<String, Object> requestMap = Maps.newHashMap();
        requestMap.put("project", project);
        requestMap.put("game", game);
        requestMap.put("type", "视频");
        Page page = videoDao.findAll(buildSpecification(requestMap, Video.class), new PageRequest(pageNumber, 12, getSort("id", "DIRE")));
        model.addAttribute("page", page);
        String search = BaseEndPoint.encodeParameterStringWithPrefix(requestMap, "");
        model.addAttribute("pageUrl", "?" + search);

        List<Video> videos = videoQueryer.findByProjectGameTypeCount(project, game, "视频", BaseEndPoint.RIGHT_VIDEO_COUNT);
        model.addAttribute("videos", videos);

        List<Video> luxiangs = videoQueryer.findByProjectGameTypeCount(project, game, "录像", BaseEndPoint.RIGHT_LUXIANG_COUNT, true);
        model.addAttribute("luxiangs", luxiangs);

        model.addAttribute("menu", (project.equals("足球") ? "football" : "basketball")+"Video");
        return "gameVideo";
    }
    
}
