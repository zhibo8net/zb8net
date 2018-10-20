package website2018.spider;

import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import website2018.MyApplication;
import website2018.base.BaseSpider;
import website2018.domain.News;
import website2018.domain.ReplaceWord;
import website2018.dto.NewsSource;
import website2018.repository.NewsDao;
import website2018.repository.ReplaceWordDao;

@Component
public class NewsSpider extends BaseSpider {

    @Autowired ReplaceWordDao replaceWordDao;
    
    @Autowired
    NewsDao newsDao;

    List<NewsSource> newsSources = Lists.newArrayList();
    List<NewsSource> newsSources2 = Lists.newArrayList();

    @Override
    public void initForOneSpider() {
        newsSources.add(new NewsSource("足球", null, "直播吧足球新闻", "https://news.zhibo8.cc/zuqiu/"));
        newsSources.add(new NewsSource("篮球", null, "直播吧篮球新闻", "https://news.zhibo8.cc/nba/"));
        
        newsSources2.add(new NewsSource("篮球", "NBA", "90VS_NBA", "http://www.90vs.com/lqnews/list_87_1.html"));
        newsSources2.add(new NewsSource("篮球", "CBA", "90VS_CBA", "http://www.90vs.com/lqnews/list_68_1.html"));
        newsSources2.add(new NewsSource("足球", "中超", "90VS_中超", "http://www.90vs.com/news2/list_3_1.html"));
        newsSources2.add(new NewsSource("足球", "亚冠", "90VS_亚冠", "http://www.90vs.com/news2/list_38_1.html"));
        newsSources2.add(new NewsSource("足球", "欧冠", "90VS_欧冠", "http://www.90vs.com/news2/list_119_1.html"));
        newsSources2.add(new NewsSource("足球", "英超", "90VS_英超", "http://www.90vs.com/news2/list_44_1.html"));
        newsSources2.add(new NewsSource("足球", "意甲", "90VS_意甲", "http://www.90vs.com/news2/list_50_1.html"));
        newsSources2.add(new NewsSource("足球", "德甲", "90VS_德甲", "http://www.90vs.com/news2/list_55_1.html"));
        newsSources2.add(new NewsSource("足球", "西甲", "90VS_西甲", "http://www.90vs.com/news2/list_67_1.html"));
        newsSources2.add(new NewsSource("足球", "法甲", "90VS_法甲", "http://www.90vs.com/news2/list_150_1.html"));
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
                    fetchNews();
                }catch(Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "ThreadName-" + System.currentTimeMillis());
        t.start();
    }
    
    @Transactional
    public void fetchNews() throws Exception {
        List<ReplaceWord> replaceWords = (List<ReplaceWord>)replaceWordDao.findAll();
        
        List<News> entitys = Lists.newArrayList();

        //fetchFromZhibo8(entitys, replaceWords);
        
        fetchFrom90VS(entitys, replaceWords);

        newsDao.save(entitys);
        logger.warn("添加了新闻条数：" + entitys.size());
    }
    
    /**
     * 从直播吧抓取新闻
     * @param entitys
     * @param replaceWords
     * @throws Exception
     */
    public void fetchFromZhibo8(List<News> entitys, List<ReplaceWord> replaceWords) throws Exception{

        for (NewsSource ns : newsSources) {

            Document doc = readDocFrom(ns.link);
            Elements links = doc.select("#middle .m_left .content ul li a");

            newssEach: for (Element l : links) {
                
                try {
                    
                    String _insideUrl = "http:" + l.attr("href");

                    // 在缓存中判断是否爬取过该url
                    if (fetched.getIfPresent(_insideUrl) != null) {
                        System.out.println("已抓过的链接，不重复抓取（从缓存中判断）");
                        continue newssEach;
                    }

                    List<News> maybeExistedEntitys = newsDao.findBySource(_insideUrl);
                    if (maybeExistedEntitys.size() > 0) {
                        System.out.println("已存在的新闻，不重复抓取（从数据库中判断）");
                    } else {
                        Document _insideDoc = readDocFrom(_insideUrl);
                        fetched.put(_insideUrl, "1");
                        
                        String title = _insideDoc.select("h1").html();
                        title = Jsoup.clean(title, Whitelist.none());
                        
                        String content = _insideDoc.select("#signals").html();
                        content = Jsoup.clean(content, Whitelist.basic());
                        
                        //应用替换词
                        for(ReplaceWord rw : replaceWords) {
                            if(content.contains(rw.fromWord)) {
                                System.out.println("将" + rw.fromWord + "替换为" + rw.toWord);
                                content = content.replaceAll(rw.fromWord, rw.toWord);
                                System.out.println("现在含有"+ rw.fromWord + "的状况：" + content.contains(rw.fromWord));
                            }
                        }
                        if(content.length() > 20000) {
                            System.out.println("这篇新闻过于长了。");
                            continue newssEach;
                        }
                        
                        News news = new News();
                        news.title = title;
                        news.source = _insideUrl;
                        news.project = ns.project;
                        news.game = game(news.title);
                        news.image="";
                        news.content = content;
                        news.addTime = new Date();
                        news.updateTime = new Date();
                        entitys.add(news);
                    }
                    
                }catch(Exception e) {
                    continue newssEach;
                }

            }

            Thread.sleep(100 * 1);
        }
    }

    /**
     * 从90VS抓取新闻
     * @param entitys
     * @param replaceWords
     * @throws Exception
     */
    public void fetchFrom90VS(List<News> entitys, List<ReplaceWord> replaceWords) throws Exception{

        for (NewsSource ns : newsSources2) {

            Document doc = readDocFrom(ns.link);
            Elements links = doc.select(".list-dot li a");

            newssEach: for (Element l : links) {
                
                try {
                    
                    String _insideUrl = l.attr("href");

                    // 在缓存中判断是否爬取过该url
                    if (fetched.getIfPresent(_insideUrl) != null) {
                        System.out.println("已抓过的链接，不重复抓取（从缓存中判断）");
                        continue newssEach;
                    }

                    List<News> maybeExistedEntitys = newsDao.findBySource(_insideUrl);
                    if (maybeExistedEntitys.size() > 0) {
                        System.out.println("已存在的新闻，不重复抓取（从数据库中判断）");
                    } else {
                        Document _insideDoc = readDocFrom(_insideUrl);
                        fetched.put(_insideUrl, "1");
                        
                        String title = _insideDoc.select("h3").html();
                        title = Jsoup.clean(title, Whitelist.none());
                        String[] titleParts = title.split(" ");
                        title = titleParts[0];
                        
                        String content = _insideDoc.select("#Zoom").html();
                        content = Jsoup.clean(content, Whitelist.basic());
                        
                        //应用替换词
                        for(ReplaceWord rw : replaceWords) {
                            if(content.contains(rw.fromWord)) {
                                System.out.println("将" + rw.fromWord + "替换为" + rw.toWord);
                                content = content.replaceAll(rw.fromWord, rw.toWord);
                                System.out.println("现在含有"+ rw.fromWord + "的状况：" + content.contains(rw.fromWord));
                            }
                        }
                        
                        content = content.replaceAll("90vs体育讯 ", "");
                        content = content.replaceAll("【更多.*资讯】", "");
                        
                        if(content.length() > 20000) {
                            System.out.println("这篇新闻过于长了。");
                            continue newssEach;
                        }
                        
                        News news = new News();
                        news.title = title;
                        news.source = _insideUrl;
                        news.project = ns.project;
                        news.game = ns.game;
                        news.image = "";
                        news.content = content;
                        news.addTime = new Date();
                        entitys.add(news);
                    }
                    System.out.println("从90VS成功抓取到一篇新闻……");
                }catch(Exception e) {
                    logger.error("从90VS抓取新闻出现错误……");
                    continue newssEach;
                }

            }

            Thread.sleep(100 * 1);
        }
    }
    
}
