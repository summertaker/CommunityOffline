package com.summertaker.communityoffline.parser;

import android.text.Html;

import com.summertaker.communityoffline.common.BaseParser;
import com.summertaker.communityoffline.data.ArticleDetailData;
import com.summertaker.communityoffline.data.ArticleListData;
import com.summertaker.communityoffline.data.CommentData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodayhumorParser extends BaseParser {

    public void parseList(String response, ArrayList<ArticleListData> dataList) {
        /*
        <a href="view.php?table=bestofbest&no=363965&page=1">
            <div class="listLineBox list_tr_sisa" mn='754830'>
                <div class="list_iconBox">
                        <div class='board_icon_mini sisa' style='align-self:center'></div>
                </div>
                <div>
                    <span class="list_no">363965</span>
                    <span class="listDate">2017/09/22 11:45</span>
                    <span class="list_writer" is_member="yes">carryon</span>
                </div>
                <div>
                    <h2 class="listSubject" >네이버를 조져야됨..<span class="list_comment_count"> <span class="memo_count">[3]</span></span></h2>
                </div>
                <div>
                    <span class="list_viewTitle">조회:</span><span class="list_viewCount">1374</span>	            <span class="list_okNokTitle">추천:</span><span class="list_okNokCount">53</span>
                    <span class="list_iconWrap">
                    </span>
                </div>
            </div>
        </a>
        */

        //Log.d(mTag, response);

        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);
        Element root = doc.select("#remove_favorite_alert_div").first();

        if (root != null) {

            for (Element row : doc.select("a")) {
                String title = "";
                String commentCount = "";
                String recommendCount = "";
                String url = "";

                Element el = row.select(".listSubject").first();
                if (el == null) {
                    continue;
                }
                title = el.text();
                //title = title.replaceAll("[0-9]", "").replace("[]", "");

                //Element a = row; //row.select("a").first();
                url = row.attr("href");
                url = "http://m.todayhumor.co.kr/" + url;

                el = row.select(".memo_count").first();
                if (el != null) {
                    String str = el.text();
                    title = title.replace(str, "");
                    title = title.trim();

                    str = str.replace("[", "").replace("]", "");
                    commentCount = str;
                }

                el = row.select(".list_okNokCount").first();
                if (el != null) {
                    recommendCount = el.text();
                }

                //Log.d(mTag, title + " / " + like);

                ArticleListData data = new ArticleListData();
                data.setTitle(title);
                data.setCommentCount(commentCount);
                data.setRecommendCount(recommendCount);
                data.setUrl(url);
                dataList.add(data);
            }
        }
    }

    public ArticleDetailData parseDetail(String response) {
        ArticleDetailData data = new ArticleDetailData();

        Document doc = Jsoup.parse(response);

        Element root = doc.select(".viewContent").first();

        //-----------------------------------------------------------------------------------------------------
        // https://stackoverflow.com/questions/26346698/parsing-html-into-formatted-plaintext-using-jsoup
        //-----------------------------------------------------------------------------------------------------
        //HtmlToPlainText toPlainText = new HtmlToPlainText();

        String search = "var parent_table = \"(\\w+)\";";
        Pattern pattern = Pattern.compile(search);
        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            //Log.d(mTag, "parentTable: " + matcher.group(1));
            data.setParentTable(matcher.group(1));
        }

        search = "var parent_id = \"(\\w+)\";";
        pattern = Pattern.compile(search);
        matcher = pattern.matcher(response);
        while (matcher.find()) {
            //Log.d(mTag, "parentId: " + matcher.group(1));
            data.setParentId(matcher.group(1));
        }

        if (root != null) {
            String content = root.html();
            //Log.d(mTag, content);

            //content = toPlainText.getPlainText(root);
            //Log.d(mTag, content);

            //-------------------------------------------
            // https://regexone.com/lesson/whitespaces
            //-------------------------------------------
            // \\s 공백
            // . Any Character
            // (…) Capture Group
            // (.|\") 아무 문자 또는 " 기호
            //-------------------------------------------
            /*
            1)
            <div> <br> </div>
            표현 replaceAll("\\s*<div>\\s*<br>\\s*</div>\\s*", "");

            2)
            <div> &nbsp; </div>
            표현 replaceAll("\\s*<div>\\s*&nbsp;\\s*</div>\\s*", "");

            3) 1)과 2)를 합해서
            표현 replaceAll("\\s*<div>\\s*(<br>|&nbsp;)\\s*</div>\\s*", "");

            4) <div style=""> <br> &nbsp; </div>
            표현 replaceAll("\\s*<div\\s*(.|\")*>\\s*(<br>)*(&nbsp;)*\\s*</div>\\s*", "");
            */
            content = content.replaceAll("\\s*<img.+?>\\s*", "");
            content = content.replaceAll("\\s*<div\\s*(.|\")*>\\s*(<br>)*(&nbsp;)*\\s*</div>\\s*", "");
            content = content.replaceAll("</div>\\s*<br>\\s*", "</div>");
            content = content.replaceAll("<br\\s*.*>\\s*<br\\s*.*>\\s*<br\\s*.*>\\s*", "<br><br>");
            content = content.replaceAll("<br\\s*.*>\\s*<br\\s*.*>\\s*<br\\s*.*>\\s*", "<br><br>");
            content = Html.fromHtml(content).toString();

            data.setContent(content);

            ArrayList<String> thumbnails = new ArrayList<>();
            ArrayList<String> images = new ArrayList<>();

            for (Element img : root.select("img")) {
                String src = img.attr("src");
                //src = "http://www.keyakizaka46.com" + src;

                thumbnails.add(src);
                images.add(src);
            }

            data.setThumbnails(thumbnails);
            data.setImages(images);
        }

        return data;
    }

    public ArrayList<CommentData> parseComment(String response) {
        /*
        <a href="view.php?table=bestofbest&no=363965&page=1">
            <div class="listLineBox list_tr_sisa" mn='754830'>
                <div class="list_iconBox">
                        <div class='board_icon_mini sisa' style='align-self:center'></div>
                </div>
                <div>
                    <span class="list_no">363965</span>
                    <span class="listDate">2017/09/22 11:45</span>
                    <span class="list_writer" is_member="yes">carryon</span>
                </div>
                <div>
                    <h2 class="listSubject" >네이버를 조져야됨..<span class="list_comment_count"> <span class="memo_count">[3]</span></span></h2>
                </div>
                <div>
                    <span class="list_viewTitle">조회:</span><span class="list_viewCount">1374</span>	            <span class="list_okNokTitle">추천:</span><span class="list_okNokCount">53</span>
                    <span class="list_iconWrap">
                    </span>
                </div>
            </div>
        </a>
        */

        //Log.d(mTag, response);

        ArrayList<CommentData> dataList = new ArrayList<>();

        if (response == null || response.isEmpty()) {
            return dataList;
        }

        try {
            JSONObject jsonObject = new JSONObject(response);

            // Getting JSON Array node
            JSONArray jsonArray = jsonObject.getJSONArray("memos");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String recommend = obj.getString("ok");
                int recommendCount = Integer.parseInt(recommend);
                if (recommendCount < 10) {
                    continue;
                }

                String content = obj.getString("memo").trim();

                // 사진 찾기
                String thumbnail = "";
                Document html = Jsoup.parse(content);
                for (Element img : html.select("img")) {
                    thumbnail = img.attr("data-original");
                    //Log.d(mTag, thumbnail);
                }

                // 내용을 일반 텍스트로 변환
                content = content.replaceAll("\\s*<img.+?>\\s*(<br>)*\\s*", "");
                content = content.replaceAll("\\s*(&nbsp;)+\\s*", " ");
                content = content.replaceAll("\\s*(&gt;)+\\s*", ">");
                content = content.replaceAll("\\s*(&lt;)+\\s*", "<");

                // 맨 마지막 "<br />" 잘라내기
                int start = content.length() - 6;
                if (start >= 0) {
                    int end = content.length();
                    //Log.d(mTag, content.substring(start, end));

                    if ("<br />".equals(content.substring(start, end))) {
                        content = content.substring(0, start);
                    }
                    //Log.d(mTag, content);
                }
                content = content.replaceAll("<br />", "\n");
                content = content.replaceAll("<br>", "");

                content = content + " (+" + recommend + ")";

                //Log.d(mTag, content + " / 추천: " + recommend);

                CommentData data = new CommentData();
                data.setThumbnail(thumbnail);
                data.setImage(thumbnail);
                data.setContent(content);
                dataList.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}