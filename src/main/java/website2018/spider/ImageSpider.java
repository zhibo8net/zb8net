package website2018.spider;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import website2018.domain.Image;
import website2018.domain.ImageBag;
import website2018.dto.ImageSource;
import website2018.repository.ImageBagDao;

@Component
public class ImageSpider extends BaseSpider {

    @Autowired
    ImageBagDao imageBagDao;

    List<ImageSource> imageSources = Lists.newArrayList();

    @Override
    public void initForOneSpider() {
        imageSources.add(new ImageSource("足球", "直播吧足球图片", "http://tu.zhibo8.cc/zuqiu/all/"));
        imageSources.add(new ImageSource("篮球", "直播吧篮球图片", "http://tu.zhibo8.cc/nba/all/"));
    }

    @Scheduled(cron = "0 30 1/2 * * *")
    @Transactional
    public void runSchedule() throws Exception {
        if(MyApplication.DONT_RUN_SCHEDULED) {
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    fetchImage();
                }catch(Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "ImageSpider - " + System.currentTimeMillis());
        t.start();
    }
    
    @Transactional
    public void fetchImage() throws Exception {
        
        List<ImageBag> entitys = Lists.newArrayList();

        for (ImageSource is : imageSources) {
            System.out.println(is.project);
            System.out.println(is.name);

            Document doc = readDocFrom(is.link);
            Elements links = doc.select(".albumbox a.pica1");

            Pattern p = Pattern.compile("/(\\d+)\\) ");
            imagesEach: for (Element l : links) {
                String title = l.select("img").attr("alt");
               // System.out.println(title);
                String _insideUrl = "http://tu.zhibo8.cc/" + l.attr("href");

                // 在缓存中判断是否爬取过该url
                if (fetched.getIfPresent(_insideUrl) != null) {
                    System.out.println("已抓过的链接，不重复抓取（从缓存中判断）");
                    continue imagesEach;
                }

                List<ImageBag> maybeExistedEntitys = imageBagDao.findBySource(_insideUrl);
                if (maybeExistedEntitys.size() > 0) {
                    System.out.println("已存在的ImageBag，不重复抓取（从数据库中判断）");
                } else {
                    Document _insideDoc = readDocFrom(_insideUrl);
                    fetched.put(_insideUrl, "1");
                    
                    ImageBag bag = new ImageBag();
                    bag.project = is.project;
                    bag.title = title;
                    bag.source = _insideUrl;
                    
                    String includeNum = _insideDoc.select(".btn_up_down").html();
                    Matcher m = p.matcher(includeNum);
                    if(m.find()) {
                        int count = Integer.valueOf(m.group(1));
                        System.out.println("图片数量：" + count);
                        for(int i = 1; i <= count; i++) {
                            String oneImgUrl = _insideUrl + "/" + i;
                            Document oneImgDoc = readDocFrom(oneImgUrl);
                            String imageSrc = "http:" + oneImgDoc.select("#image_wrap img").attr("src");
                            String imageFilePath = downloadFile(imageSrc);
                            
                            Image image = new Image();
                            image.name = imageFilePath;
                            image.bag = bag;
                            image.addTime = new Date();
                            bag.images.add(image);
                        }
                    }
                    
                    if(bag.images.size() > 0) {
                        bag.addTime = new Date();
                        entitys.add(bag);
                    }
                }

            }

            Thread.sleep(100 * 1);
        }

        imageBagDao.save(entitys);
        logger.warn("添加了图片条数：" + entitys.size());
    }
}
