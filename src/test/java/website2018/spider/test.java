package website2018.spider;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.select.Elements;

import org.jsoup.nodes.Element;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/7/15.
 */
public class test {
    // 官网文档：HttpClient是线程安全的
   static CloseableHttpClient httpClient;
    // 已爬取过的url们
    protected Cache<String, String> fetched;
    @PostConstruct
    public void init() {

        // httpClient的初始化
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36";
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5 * 1000).setConnectTimeout(5 * 1000)
                .build();
        httpClient = HttpClientBuilder.create().setUserAgent(userAgent).setMaxConnTotal(1).setMaxConnPerRoute(1)
                .setDefaultRequestConfig(requestConfig).build();

        // 已爬取过的url们
        fetched = CacheBuilder.newBuilder().maximumSize(10000).expireAfterAccess(7, TimeUnit.DAYS).build();

        // 特定爬虫的初始化方法
        initForOneSpider();
    }

    protected void initForOneSpider() {

    }

    public static  void main(String[] args)throws Exception{
        Pattern p = Pattern.compile(".*(" + "PPTV|CCTV|企鹅|章鱼|直播迷|龙珠|腾讯|ZF|上海|俄罗斯".replace(",", "|") + ").*");
        Matcher matcher = p.matcher("直播迷(直播TV)");
        if(matcher.matches()) {
            System.out.println("ddd");
        }
        Document d= readDocFromByJsoup("w.zhibo.me:8088/pptv.php?id=301202");
        Elements elsurl = d.select("script");
        for( Element e1:elsurl) {
            System.out.println(e1.html());

            String dd="http://sportstream365.com/VideoPlayerSportstream.swf?s=m7zO0NBYz9DASM_MQs_QyPj6f0bJZoNoZaYzjatCi1OLdB3TU_NKbub65ldl5uQk6pvqGShohGfmpeSXFyv4hSiY6RlaK4T7h5uZaCo4FhTkpIanJnlnluibGpvrGZspaHh7hPj66CjkZGanKrinJmfnayo4ZxTl56bqmxnrGegZGxlb6hkaGykEJ6YlFmVCtQEA&v=1529021531439";
            System.out.println(dd.length());
                int b=e1.html().indexOf("data=\\\"");
              String v=  e1.html().substring(b+7, b + 274);
                System.out.println(v);

        }
    }


    public static Document readDocFromByJsoup(String url) {
        try {
            return Jsoup.connect(url).get();
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
