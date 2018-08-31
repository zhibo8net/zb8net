package website2018.controller;

import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.utils.mapper.BeanMapper;

import com.google.common.collect.Lists;

import website2018.base.BaseEndPoint;
import website2018.domain.Ended;
import website2018.domain.ImageBag;
import website2018.domain.News;
import website2018.domain.Video;
import website2018.dto.*;
import website2018.repository.ImageBagDao;
import website2018.repository.NewsDao;
import website2018.service.IndexService;

@Controller
public class IndexController extends BaseEndPoint {

    @Autowired
    IndexService indexService;

    @Autowired
    NewsDao newsDao;
    @Autowired
    ImageBagDao imageBagDao;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String index(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("pageTitle", "体育直播吧");
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.YEAR, -10);
        //视屏
        List<Ended> endedEntitys = indexService.findNewEndeds(calendar.getTime());

        List<EndedDTO> endedDTOList = Lists.newArrayList();
            List<PageEndedDTO> upPageEndedDTO = Lists.newArrayList();
              int i=0;
            for(Ended e : endedEntitys) {

                EndedDTO ed = BeanMapper.map(e, EndedDTO.class);
                if(i%8==0){
                    ed.isFirstRow=true;
                }
                for(Video v : e.videos) {
                if(v.type.equals("集锦")) {
                    ed.hasJijin = true;
                }else if(v.type.equals("录像")) {
                    ed.hasLuxiang = true;
                }else if(v.type.equals("视频")) {
                    ed.hasShiping = true;
                }else{
                    ed.other = true;
                }
                    break;
            }
                endedDTOList.add(ed);
                i++;
           }
        List<EndedDTO> list1 = Lists.newArrayList();
        List<EndedDTO> list2 = Lists.newArrayList();
        List<EndedDTO> list3 = Lists.newArrayList();
        List<EndedDTO> list4 = Lists.newArrayList();
        if(endedDTOList.size()>=8){
            list1=endedDTOList.subList(0,8);
            if(endedDTOList.size()>=16){
                list2=endedDTOList.subList(8,16);

                if(endedDTOList.size()>=24){
                    list3=endedDTOList.subList(16,24);

                    if(endedDTOList.size()>=32){
                        list4=endedDTOList.subList(24,32);
                    }else{
                        list4=endedDTOList.subList(24,endedDTOList.size());
                    }

                }else{
                    list3=endedDTOList.subList(16,endedDTOList.size());
                }

            }else{
                list2=endedDTOList.subList(8,endedDTOList.size());
            }

        }else{
            list1=endedDTOList;
        }


        List<PageEndedDTO> upList = Lists.newArrayList();
        List<PageEndedDTO> downList = Lists.newArrayList();
        for(int j=0;j<list1.size();j++){
            PageEndedDTO upDTO=new PageEndedDTO();
             upDTO.leftEndedDTO=list1.get(j);
            if(j<list2.size()&&list2.size()!=0){
                upDTO.rightEndedDTO=list2.get(j);
            }
            upList.add(upDTO);
            PageEndedDTO downDTO=null;
            if(j<list3.size()&&list3.size()!=0){
                if(downDTO==null){
                    downDTO= new PageEndedDTO();
                }
                downDTO.leftEndedDTO=list3.get(j);
            }
            if(j<list4.size()&&list4.size()!=0){
                if(downDTO==null){
                    downDTO= new PageEndedDTO();
                }
                downDTO.rightEndedDTO=list4.get(j);
            }
            if(downDTO!=null){
                downList.add(downDTO);
            }

        }

        request.setAttribute("upList", upList);
        request.setAttribute("downList", downList);
        //图片

        List<ImageBag> imageBagList = imageBagDao.findTop10ByAddTimeGreaterThanOrderByIdDesc(calendar.getTime());
        request.setAttribute("imageBagList", imageBagList);
        List<ImageDTO> imagelist = Lists.newArrayList();
        List<ImageDTO> imagelist1 = Lists.newArrayList();
        List<ImageDTO> imagelist2 = Lists.newArrayList();
        int g=0;
        for(ImageBag n:imageBagList){
            ImageDTO ed = BeanMapper.map(n, ImageDTO.class);
            if(g%5==0){
                ed.isFirstRow=true;
            }
            if(StringUtils.isNotEmpty(ed.title)&&ed.title.length()>=8){
                ed.title=ed.title.substring(0,8);
            }
            imagelist.add(ed);
            g++;
        }
        if(imagelist.size()>=5){
            imagelist1=imagelist.subList(0,5);
            if(imagelist.size()>=10){
                imagelist2=imagelist.subList(5,10);
            }else{
                imagelist2=imagelist.subList(5,imagelist.size());
            }

        }else{
            imagelist1=imagelist;
        }
        List<PageImageDTO> upImageList = Lists.newArrayList();
        for(int j=0;j<imagelist1.size();j++){
            PageImageDTO upDTO=new PageImageDTO();
            upDTO.leftImageDTO=imagelist1.get(j);
            if(j<imagelist2.size()&&imagelist2.size()!=0){
                upDTO.rightImageDTO=imagelist2.get(j);
            }
            upImageList.add(upDTO);
        }
        request.setAttribute("upImageList", upImageList);


        //新闻
        List<News> newsListx=Lists.newArrayList();
         List<News> newsListxzq =   newsDao.findTop16ByProjectOrderByAddTimeDesc("足球");
        List<News> newsListxlq =   newsDao.findTop16ByProjectOrderByAddTimeDesc("篮球");
        if(newsListxzq!=null&&newsListxlq.size()!=0){
            newsListx.addAll(newsListxzq);
        }
        if(newsListxlq!=null&&newsListxlq.size()!=0){
            newsListx.addAll(newsListxlq);
        }

        List<NewsDTO> newsList= Lists.newArrayList();
        int k=0;
        for(News n:newsListx){
            NewsDTO ed = BeanMapper.map(n, NewsDTO.class);
            if(k%8==0){
                ed.isFirstRow=true;
            }
            newsList.add(ed);
            k++;
            if(StringUtils.isNotEmpty(ed.title)&&ed.title.length()>=14){
                ed.title=ed.title.substring(0,14);
            }
        }

        List<NewsDTO> newlist1 = Lists.newArrayList();
        List<NewsDTO> newlist2 = Lists.newArrayList();
        List<NewsDTO> newlist3 = Lists.newArrayList();
        List<NewsDTO> newlist4 = Lists.newArrayList();
        if(newsList.size()>=8){
            newlist1=newsList.subList(0,8);
            if(newsList.size()>=16){
                newlist2=newsList.subList(8,16);

                if(newsList.size()>=24){
                    newlist3=newsList.subList(16,24);

                    if(endedDTOList.size()>=32){
                        newlist4=newsList.subList(24,32);
                    }else{
                        newlist4=newsList.subList(24,newsList.size());
                    }

                }else{
                    newlist3=newsList.subList(16,newsList.size());
                }

            }else{
                newlist2=newsList.subList(8,newsList.size());
            }

        }else{
            newlist1=newsList;
        }

        List<PageNewsDTO> upNewList = Lists.newArrayList();
        List<PageNewsDTO> downNewList = Lists.newArrayList();
        for(int j=0;j<newlist1.size();j++){
            PageNewsDTO upDTO=new PageNewsDTO();
            upDTO.leftNewsDTO=newlist1.get(j);
            if(j<newlist2.size()&&newlist2.size()!=0){
                upDTO.rightNewsDTO=newlist2.get(j);
            }
            upNewList.add(upDTO);
            PageNewsDTO downDTO=null;
            if(j<newlist3.size()&&newlist3.size()!=0){
                if(downDTO==null){
                    downDTO= new PageNewsDTO();
                }
                downDTO.leftNewsDTO=newlist3.get(j);
            }
            if(j<newlist4.size()&&newlist4.size()!=0){
                if(downDTO==null){
                    downDTO= new PageNewsDTO();
                }
                downDTO.rightNewsDTO=newlist4.get(j);
            }
            if(downDTO!=null){
                downNewList.add(downDTO);
            }

        }

        request.setAttribute("upNewList", upNewList);
        request.setAttribute("downNewList", downNewList);

//        List<Ended> footballEndedEntitys = indexService.findEndeds("足球");
//        List<EndedDTO> footballEndeds = Lists.newArrayList();
//        List<EndedDTO> footballEndeds2 = Lists.newArrayList();
//        for(Ended e : footballEndedEntitys) {
//            EndedDTO ed = BeanMapper.map(e, EndedDTO.class);
//            for(Video v : e.videos) {
//                if(v.type.equals("集锦")) {
//                    ed.hasJijin = true;
//                }else if(v.type.equals("录像")) {
//                    ed.hasLuxiang = true;
//                }
//            }
//            if(ed.hasJijin || ed.hasLuxiang) {
//                if(ed.name.length() < 13) {
//                    footballEndeds.add(ed);
//                }else {
//                    footballEndeds2.add(ed);
//                }
//            }
//            if(footballEndeds.size() == 8) {
//                break;
//            }
//        }
//        request.setAttribute("footballEndeds", footballEndeds);
//        request.setAttribute("footballEndeds2", footballEndeds2);
//
//        List<Ended> basketballEndedEntitys = indexService.findEndeds("篮球");
//        List<EndedDTO> basketballEndeds = Lists.newArrayList();
//        List<EndedDTO> basketballEndeds2 = Lists.newArrayList();
//        for(Ended e : basketballEndedEntitys) {
//            EndedDTO ed = BeanMapper.map(e, EndedDTO.class);
//            for(Video v : e.videos) {
//                if(v.type.equals("集锦")) {
//                    ed.hasJijin = true;
//                }else if(v.type.equals("录像")) {
//                    ed.hasLuxiang = true;
//                }
//            }
//            if(ed.hasJijin || ed.hasLuxiang) {
//                if(ed.name.length() < 13 && ed.name.length() > 3) {
//                    basketballEndeds.add(ed);
//                }else if(ed.name.length() > 13){
//                    basketballEndeds2.add(ed);
//                }
//            }
//            if(basketballEndeds.size() == 8) {
//                break;
//            }
//        }
//        request.setAttribute("basketballEndeds", basketballEndeds);
//        request.setAttribute("basketballEndeds2", basketballEndeds2);

        List<DailyLivesDTO> dailyLives = indexService.dailyLives();
        request.setAttribute("dailyLives", dailyLives);
        
        List<Video> videos = indexService.findVideos();
        request.setAttribute("videos", videos);

        List<Video> footballLuxiangs = indexService.findMyLuxiangs("足球");
        request.setAttribute("footballLuxiangs", footballLuxiangs);


        List<Video> basketballLuxiangs = indexService.findMyLuxiangs("篮球");
        request.setAttribute("basketballLuxiangs", basketballLuxiangs);

        
        List<String> days = indexService.days();
        request.setAttribute("days", days);

        request.setAttribute("pageAds", indexService.pageAds());
        
        request.setAttribute("menu", "index");
        return page(request, response, "index");
    }

}
