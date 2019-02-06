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
import org.jsoup.Connection;
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
//        Pattern p = Pattern.compile(".*(" + "PPTV|CCTV|企鹅|章鱼|直播迷|龙珠|腾讯|ZF|上海|俄罗斯".replace(",", "|") + ").*");
//        Matcher matcher = p.matcher("直播迷(直播TV)");
//        if(matcher.matches()) {
//            System.out.println("ddd");
//        }
//        Document d= readDocFromByJsoup("http://w.zhibo.me:8088/tv/elstv.php?id=1605946");
//        Elements elsurl = d.select("script");
//
//        for( Element ve1:elsurl) {
//
//            int b=ve1.html().indexOf("<object id=");
//            int b1=ve1.html().indexOf("</object>");
//            String v=  ve1.html().substring(b, b1+9);
//            System.out.println(v);
//
//        }

        testFetchBasketballRank();
    }
    public static Document readDocFrom(String url) {
        try {
            String html = readFromUrl(url);
            if(html == null) {
                return null;
            }
            Document doc = Jsoup.parse(html);
            return doc;
        }catch(Exception e) {
            return null;
        }
    }
    public static void testFetchBasketballRank(){
        Document basketballRankDoc = readDocFrom("http://api.sstream365.com?id=1");
        if (basketballRankDoc == null) {

            return;
        }

        Elements tbodys = basketballRankDoc.select("tbody");
        for (Element tbody : tbodys) {
            for (Element tr : tbody.select("tr")) {
                Elements tds = tr.select("td");
                if (tds.size() >=6) {
                    System.out.println(tds.get(4).html());
                }
            }
        }
    }
    public static void testFetchFootballJsb(){
        Document footballJsb = readDocFromByJsoup("https://www.zhibo8.cc/zuqiu/");
        if (footballJsb == null) {

            return;
        }

        Elements tbodys = footballJsb.select("#jfb tbody");
        for (Element tbody : tbodys) {
            for (Element tr : tbody.select("tr")) {
                Elements tds = tr.select("td");
                if (tds.size() >=5) {
                    System.out.println(tds.get(1).html());
                }
            }
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
    public static String readFromUrl(String url) throws Exception {
        return readFromUrl(url, "UTF-8");
    }

    public static String readFromUrl(String url, String encoding) throws Exception {

        CloseableHttpResponse remoteResponse = null;

        InputStream entityInputStream = null;

        try {

            HttpGet httpGet = new HttpGet(url);

            remoteResponse = httpClient.execute(httpGet);

            HttpEntity entity = remoteResponse.getEntity();

            entityInputStream = entity.getContent();

            String str = IOUtils.toString(entityInputStream, encoding);

            return str;
        } catch (Exception e) {
          e.printStackTrace();
            return null;
        } finally {
            if (entityInputStream != null) {
                try {
                    entityInputStream.close();
                } catch (Exception e) {
                    // ...
                }
            }
            if (remoteResponse != null) {
                try {
                    remoteResponse.close();
                } catch (Exception e) {
                    // ...
                }
            }
        }

    }
    public static String readDocFromByJsoupReqJson(String url) {
        try {
            Connection.Response res = Jsoup.connect(url).header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")
                    .timeout(10000).ignoreContentType(true).execute();

            return res.body();
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
