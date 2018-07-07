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

import com.google.common.collect.Maps;

import website2018.base.BaseEndPoint;
import website2018.domain.News;
import website2018.domain.Video;
import website2018.exception.ErrorCode;
import website2018.exception.ServiceException;
import website2018.repository.EndedDao;
import website2018.repository.NewsDao;
import website2018.repository.VideoDao;

@Controller
public class NewsController extends BaseEndPoint {

    @Autowired EndedDao endedDao;
    @Autowired VideoDao videoDao;
    @Autowired NewsDao newsDao;
    
    @RequestMapping(value = "/news_1", method = RequestMethod.GET)
    public String news(@RequestParam(defaultValue="") String project,@RequestParam(defaultValue="") String game, @RequestParam(defaultValue="0") Integer pageNumber, Model model) {

        boolean all = project.equals("") && game.equals("");
        boolean other = project.equals("") && game.equals("其他");
        
        if(! (all || other || projectWhiteList.contains(project))) {
            throw new ServiceException("请正常使用网站", ErrorCode.BAD_REQUEST);
        }
        if(! (all || other || gameWhiteList.contains(game))) {
            throw new ServiceException("请正常使用网站", ErrorCode.BAD_REQUEST);
        }
        
        model.addAttribute("project", project);
        model.addAttribute("projectEnglish", project.equals("足球") ? "football" : "basketball");
        model.addAttribute("game", game);
        model.addAttribute("pageTitle", "新闻资讯");
        Map<String, Object> requestMap = Maps.newHashMap();
        if (!all) {
            requestMap.put("project", project);
            requestMap.put("game", game);
        }
        Page page = newsDao.findAll(buildSpecification(requestMap, News.class), new PageRequest(pageNumber, 12, getSort("id", "DIRE")));
        model.addAttribute("page", page);
        String search = BaseEndPoint.encodeParameterStringWithPrefix(requestMap, "");
        model.addAttribute("pageUrl", "?" + search);

        List<Video> videos;
        if(all) {
            videos = videoQueryer.findByProjectGameTypeCount(null, null, "视频", BaseEndPoint.RIGHT_VIDEO_COUNT);
        }else if(other) {
            videos = videoQueryer.findByProjectGameTypeCount(null, "其他", "视频", BaseEndPoint.RIGHT_VIDEO_COUNT);
        }else {
            videos = videoQueryer.findByProjectGameTypeCount(project, game, "视频", BaseEndPoint.RIGHT_VIDEO_COUNT);
        }
        model.addAttribute("videos", videos);

        List<Video> luxiangs;
        if(all) {
            luxiangs = videoQueryer.findByProjectGameTypeCount(null, null, "录像", BaseEndPoint.RIGHT_LUXIANG_COUNT, true);
        }else if(other) {
            luxiangs = videoQueryer.findByProjectGameTypeCount(null, "其他", "录像", BaseEndPoint.RIGHT_LUXIANG_COUNT, true);
        }else {
            luxiangs = videoQueryer.findByProjectGameTypeCount(project, game, "录像", BaseEndPoint.RIGHT_LUXIANG_COUNT, true);
        }
        model.addAttribute("luxiangs", luxiangs);

        model.addAttribute("menu", "news");
        return "news";
    }
    
    @RequestMapping(value = "/news_1/{id}", method = RequestMethod.GET)
    public String newsInner(@PathVariable Long id, Model model) {
        News news = newsDao.findOne(id);
        model.addAttribute("news", news);

        String project = news.project;
        String game = news.game;
        model.addAttribute("pageTitle", news.title);
        model.addAttribute("game", game);
        model.addAttribute("videos", videoQueryer.findByProjectGameTypeCount(project, game, "视频", BaseEndPoint.RIGHT_VIDEO_COUNT));
        model.addAttribute("luxiangs", videoQueryer.findByProjectGameTypeCount(project, game, "录像", BaseEndPoint.RIGHT_LUXIANG_COUNT, true));

        model.addAttribute("menu", "news");
        return "newsInner";
    }
}
