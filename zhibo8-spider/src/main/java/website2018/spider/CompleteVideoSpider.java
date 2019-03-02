package website2018.spider;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import website2018.MyApplication;
import website2018.base.BaseSpider;
import website2018.domain.Video;
import website2018.dto.VideoType;
import website2018.repository.VideoDao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CompleteVideoSpider extends BaseSpider {

    @Autowired
    VideoDao videoDao;



    @Scheduled(cron = "0 0/10 * * * *")
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

        Calendar date= Calendar.getInstance();
        date.add(Calendar.DATE, -3);

        List<Video> maybeExistedEntitys = videoDao.findByAddTimeGreaterThanOrderByIdDesc(date.getTime());
        for(Video video:maybeExistedEntitys){
            try {
                if (StringUtils.isNotBlank(video.link)) {
                    continue;
                }
                Document _insideDoc = readDocFrom(video.source);
                if (_insideDoc == null) {
                    continue;
                }
                if (video.id == 103797) {
                    System.out.println("拼成优酷视频：");
                }
                String link = _insideDoc.select(".player-box a").attr("href");

                if (StringUtils.isBlank(link)) {
                    String _insideHtml = _insideDoc.select("#liveTemplate").html();
                    //尝试获取优酷的视频
                    Matcher m = p.matcher(_insideHtml);
                    if (m.matches()) {
                        String id = m.group(1);
                        link = "http://v.youku.com/v_show/id_" + id + ".html";
                        System.out.println("拼成优酷视频：" + link);
                    } else {
                        System.out.println("没能匹配" + _insideHtml);
                    }
                }

                if (StringUtils.isBlank(link)) {
                    continue;
                }

                video.link = link;
                videoDao.update(video.link, video.id);
                Thread.sleep(1000 * 1);
            }catch (Exception e){
                System.out.println("没能匹配" + video.id);
            }
        }




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
