package website2018.spider;

import java.util.Date;
import java.util.List;

import freemarker.template.utility.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import website2018.MyApplication;
import website2018.base.BaseSpider;
import website2018.domain.Ended;
import website2018.domain.Video;
import website2018.dto.VideoType;
import website2018.repository.EndedDao;

@Component
public class EndedSpider extends BaseSpider {

    @Autowired
    EndedDao endedDao;
    List<VideoType> videoTypes = Lists.newArrayList();

    @Override
    public void initForOneSpider() {
          videoTypes.add(new VideoType("足球", "录像", "http://www.azhibo.com/bisailuxiang/zuqiu"));
        videoTypes.add(new VideoType("篮球", "录像", "http://www.azhibo.com/bisailuxiang/lanqiu"));


    }
    @Scheduled(cron = "0 11 1/2 * * *")
    //  @Scheduled(cron = "0 0/1 * * * *")
    @Transactional
    public void runSchedule() throws Exception {
        if(MyApplication.DONT_RUN_SCHEDULED) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    fetchEnded_20190216();
                }catch(Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "EndedSpider - " + System.currentTimeMillis());
        t.start();
    }
    @Transactional
    public void fetchEnded_20190216() throws Exception {
        for (VideoType vt : videoTypes) {
            try {
                List<Ended> entitys = Lists.newArrayList();
                Document doc = readDocFrom(vt.azhibo);
                if(doc==null){
                    logger.error("抓取ended错误："+ vt.azhibo+" 抓取数据为空 doc");
                    continue ;
                }
                Elements endeds = doc.select(".video-tape>ul>li");
                for (Element en : endeds) {
                    try {
                        Ended ended = new Ended();

                        ended.project = vt.project;
                        Elements  spanHtml = en.select("span");
                        String game = spanHtml.get(0).html();
                        String ename = spanHtml.get(1).html();
                        ended.name = ename;
                        ended.game=game;
                        ended.addTime = new Date();

                        Elements  aHtml = en.select(".content a");
                        if(aHtml==null){
                            continue;
                        }
                        boolean flag=false;
                        for(Element e:aHtml){
                          String vSource= "http://www.azhibo.com/"+ e.attr("href");
                            if(!flag){
                                ended.source= vSource;
                                flag=true;
                            }

                            Document d = readDocFrom(vSource);
                            if(d==null){
                                continue;
                            }
                            String link = d.select(".player-box a").attr("href");
                            if (StringUtils.isBlank(link)||StringUtils.isEmpty(link)) {
                                String _insideHtml = d.select("#liveTemplate").html();
                                Document doc_insideHtml = Jsoup.parse(_insideHtml);
                                link = doc_insideHtml.select("iframe").attr("src");
                            }
                            if(StringUtils.isEmpty(link)){
                                continue;
                            }
                            Video video = new Video();
                            video.project = vt.project;
                            video.game = ended.game;
                            video.type = type(vt.name);

                            video.name = e.html();
                            video.link = link;
                            String image = downloadFile(d.select("#player-sidebar .selected img").attr("src"));

                            video.image = image;
                            video.ended = ended;
                            video.source = vSource;
                            video.addTime = new Date();
                            ended.videos.add(video);
                        }
                        entitys.add(ended);
                    }catch (Exception e){
                        logger.error("抓取ended错误",e);
                    }

                }
                if(entitys!=null&&entitys.size()>0){
                    endedDao.save(entitys);
                }

            } catch (Exception e) {
                logger.error("抓取ended错误",vt.azhibo,e);
            }


        }
    }

    @Transactional
    public void fetchEnded() throws Exception {
        try {


        Document azhibo = readDocFrom("http://www.azhibo.com/");
        
        if(azhibo != null) {
            List<Ended> entitys = Lists.newArrayList();
            Elements projects = azhibo.select(".match-event .soccer.sport");
            // //System.out.println("项目数量：" + projects.size());
            // 足球，篮球……
            for (Element p : projects) {
                String pname = p.select(".sport-legend>span").html();
                //System.out.println("项目：" + pname);
                Elements endeds = p.select("ul.hot-match li");
                // //System.out.println("\t已结束比赛数量：" + endeds.size());
                // 曼联vs利物浦，皇马vs巴萨……
                endedsEach: for (Element e : endeds) {
                    //保证系列链接都不在数据库里，再插入新的值
                    Elements as = e.select("a");
                    for(Element a : as) {
                        String aLink = "http://www.azhibo.com" + a.attr("href");
                        List<Ended> existeds = endedDao.findBySource(aLink);
                        for(Ended ex : existeds) {
                            System.out.println("删除一条已有记录……");
                            endedDao.delete(ex);
                        }
                    }
                    String innerGate=null;
                    try {
                         innerGate = "http://www.azhibo.com" + e.select("a").get(0).attr("href");
                    }catch (Exception e1){

                    }
                    if(StringUtils.isEmpty(innerGate)){
                        continue ;
                    }
                    try {
                        Document inner = readDocFrom(innerGate);
                        Ended ended = new Ended();
                        ended.source = innerGate;
                        ended.project = pname;
                        String ename = e.select("span.match-name").html();
                        ended.name = ename;
                        //System.out.println("\t已结束比赛：" + ename);
                        Elements types = inner.select("#player-sidebar ul");
                        // //System.out.println("\t\t视频类别数量：" + types.size());
                        // 录像，集锦……
                        for (Element t : types) {
                            if (t.select("li").size() > 0) {
                                String tname = t.select("li").get(0).html();
                                //System.out.println("\t\t视频类别：" + tname);
                                Elements videos = t.select("li a.video-cover");
                                //System.out.println("\t\t\t视频数量：" + videos.size());
                                // 上半场录像，下半场录像……
                                for (Element v : videos) {
                                    String text = v.select("span").html();
                                    if (ended.game == null) {
                                        String gameByText = game(text);
                                        if (gameByText.length() > 0) {
                                            ended.game = gameByText;
                                        }
                                    }
                                    String source = "http://www.azhibo.com" + v.attr("href");
                                    
                                    try {
                                        Document d = readDocFrom(source);
                                        String link = d.select(".player-box a").attr("href");
        
                                        Video video = new Video();
                                        video.project = pname;
                                        video.game = ended.game;
                                        video.type = type(tname);
                                        video.name = text;
                                        video.link = link;
                                        video.image = "";
                                        video.ended = ended;
                                        video.source = source;
                                        video.addTime = new Date();
                                        ended.videos.add(video);
        
                                       System.out.println("\t\t\t视频：" + text + " 地址：" + link);
                                    }catch(Exception ex) {
                                        //System.out.println("抓取视频出现错误：" + source);
                                    }
                                    
                                    Thread.sleep(1000);
                                }
                            }
                        }
                        ended.addTime = new Date();
                        if(ended.videos.size() > 0) {
                            entitys.add(ended);
                        }
                        
                    }catch(Exception ex) {
                       ex.printStackTrace();
                    }
                    
                    Thread.sleep(1000);
                }
            }

            // 将组装好的实体入库
            for (Ended e : entitys) {
                for (Video v : e.videos) {
                    v.game = e.game;
                }
            }
            if(entitys!=null&&entitys.size()>0){
                endedDao.save(entitys);
            }


        }
    }catch (Exception e){
            e.printStackTrace();
        }
    }
}
