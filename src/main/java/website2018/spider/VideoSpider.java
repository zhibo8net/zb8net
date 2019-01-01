package website2018.spider;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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
import website2018.domain.Video;
import website2018.dto.VideoType;
import website2018.repository.VideoDao;

@Component
public class VideoSpider extends BaseSpider {

    @Autowired
    VideoDao videoDao;

    List<VideoType> videoTypes = Lists.newArrayList();

    @Override
    public void initForOneSpider() {
        videoTypes.add(new VideoType("篮球", "NBA", "http://www.azhibo.com/nbashipin"));
        videoTypes.add(new VideoType("篮球", "CBA", "http://www.azhibo.com/lanqiushipin/tag/16"));
        videoTypes.add(new VideoType("足球", "英超", "http://www.azhibo.com/zuqiushipin/tag/61"));
        videoTypes.add(new VideoType("足球", "意甲", "http://www.azhibo.com/zuqiushipin/tag/62"));
        videoTypes.add(new VideoType("足球", "德甲", "http://www.azhibo.com/zuqiushipin/tag/63"));
        videoTypes.add(new VideoType("足球", "西甲", "http://www.azhibo.com/zuqiushipin/tag/64"));
        videoTypes.add(new VideoType("足球", "法甲", "http://www.azhibo.com/zuqiushipin/tag/65"));
        videoTypes.add(new VideoType("足球", "中超", "http://www.azhibo.com/zuqiushipin/tag/59"));
    }
   @Scheduled(cron = "0 30 1/2 * * *")
   // @Scheduled(cron = "0/1 * * * * *")
    @Transactional
    public void runSchedule() throws Exception {
        if(MyApplication.DONT_RUN_SCHEDULED) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    fetchVideo();
                }catch(Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "VideoSpider - " + System.currentTimeMillis());
        t.start();
    }
    
    @Transactional
    public void fetchVideo() throws Exception {

        Pattern p = Pattern.compile(".*http://player.youku.com/player.php/sid/(.*)/v.swf.*");
        
        List<Video> entitys = Lists.newArrayList();

        videoTypesEach: for (VideoType vt : videoTypes) {
           // System.out.println(vt.project);
           // System.out.println(vt.name);

            try {
                
                Document doc = readDocFrom(vt.azhibo);
                Elements videos = doc.select(".azhibo-video-thumbnials>ul>li");
    
                videosEach: for (Element v : videos) {
                    String _insideUrl = "http://www.azhibo.com" + v.select(".video-title").attr("href");

                    // 在缓存中判断是否爬取过该url
                    if (fetched.getIfPresent(_insideUrl) != null) {
                        System.out.println("已抓过的链接，不重复抓取（从缓存中判断）");
                        continue videosEach;
                    }
    
                    List<Video> maybeExistedEntitys = videoDao.findBySource(_insideUrl);
                    if (maybeExistedEntitys.size() > 0) {
                        System.out.println("已存在的Video，不重复抓取（从数据库中判断）");
                    } else {
                        try {
                            Document _insideDoc = readDocFrom(_insideUrl);
                            fetched.put(_insideUrl, "1");
                            String link = _insideDoc.select(".player-box a").attr("href");
        
                            if (StringUtils.isBlank(link)||StringUtils.isEmpty(link)) {
                                String _insideHtml = _insideDoc.select("#liveTemplate").html();
                                //尝试获取优酷的视频
                                Matcher m = p.matcher(_insideHtml);
                                if(m.matches()) {
                                    String id = m.group(1);
                                    link = "http://v.youku.com/v_show/id_" + id + ".html";
                                    System.out.println("拼成优酷视频：" + link);
                                }else {
                                    System.out.println("没能匹配" + _insideHtml);
                                }
                            }

                            if(StringUtils.isNotBlank(link)&&StringUtils.isNotEmpty(link)) {
                                
                                System.out.println("\t" + link);
                                
                                String title = v.select(".video-title").html();
                                String image = downloadFile(v.select(".cover>img").attr("src"));
        
                               // System.out.println("\t" + title);
                               // System.out.println("\t" + image);
                                
                                Video entity = new Video();
                                entity.project = vt.project;
                                entity.game = vt.name;
                                entity.type = type(title);
                                if (entity.type.equals("")) {
                                    entity.type = "视频";
                                }
                                entity.name = title;
                                entity.link = link;
                                entity.image = image;
                                entity.source = _insideUrl;
                                entity.addTime = new Date();
        
                                entitys.add(entity);
                                
                                Thread.sleep(1000 * 1);
                                
                            }else {
                                System.out.println("未获取到视频地址：" + _insideUrl);
                            }
                            
                        }catch(Exception e) {
                            System.out.println("抓取视频内页出现错误：" + _insideUrl);
                            continue videosEach;
                        }
                    }
    
                }
    
                Thread.sleep(1000 * 1);
            
            }catch(Exception e) {
                System.out.println("抓取视频列表出现错误：" + vt.azhibo);
                continue videoTypesEach;
            }
        }

        videoDao.save(entitys);
        logger.warn("添加了Video条数：" + entitys.size());
    }
    
    public void test() throws Exception{
        String src = readDocFrom("http://www.azhibo.com/zuqiushipin/hotVideos-2016-03-30-197873.html").select("#liveTemplate").html();
        System.out.println(src);

        Pattern p = Pattern.compile(".*http://player.youku.com/player.php/sid/(.*)/v.swf.*");

        //尝试获取优酷的视频
        Matcher m = p.matcher(src);
        if(m.matches()) {
            String id = m.group(1);
            String link = "http://v.youku.com/v_show/id_" + id + ".html";
            System.out.println("拼成优酷视频：" + link);
        }else {
            System.out.println("没能匹配");
        }
    }
}
