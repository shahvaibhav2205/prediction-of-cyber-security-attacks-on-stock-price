
/**
 * @author Kartik Kapadia
 */
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// //////////////////////////////////////////////////////////////////////////////
package org.nytj4.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;

import org.json.JSONObject;

public class ArticleSearchQueryImpl implements ArticleSearchQuery {

	private static NYTAPIKey apiKey = new NYTAPIKey("c807d4d1c6f9af62ede222b642648339:0:73319947");
	private static String ARTICLE_SEARCH_BASE_URL = "http://api.nytimes.com/svc/search/v2/articlesearch.json";
	private JSONObject responseJSON;

	public ArticleSearchQueryImpl(NYTAPIKey key) {
		apiKey = key;
	}

	@Override
	public ArticleSearchResponse query(Query query) {
		try {
			StringBuilder queryVal = new StringBuilder();
			queryVal.append(pBase());
			if(query.getQuery()!=null && !("".equals(query.getQuery()))){
				queryVal.append(pQuery(query.getQuery()));
			}
			//queryVal.append("&fq=cyber+attack+hacker");
			if(query.getBegindate()!=null && !("".equals(query.getBegindate()))){
				String dateVal = "&begin_date="+query.getBegindate();
				queryVal.append(dateVal);
			}
			if(query.getEnddate()!=null && !("".equals(query.getEnddate()))){
				String dateVal = "&end_date="+query.getEnddate();
				queryVal.append(dateVal);
			}
			if(query.getPage()>0){
				String pageVal = "&page="+query.getPage();
				queryVal.append(pageVal);
			}
			queryVal.append(pAPIKey());
			return query(new URL(queryVal.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ArticleSearchResponse query(String query) {
		return null;
	}

	private ArticleSearchResponse query(URL url) {
		System.out.println(url.toString());
		ArticleSearchResponse searchResult = null;
		try {			
			responseJSON = new JSONObject(HttpRequest.request(url));
			System.out.println(responseJSON.toString());
			if (null != responseJSON) {
				searchResult = new JSONFactoryImpl().searchResponseFromJSON(responseJSON);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return searchResult;
	}

	private String pBase() {
		return ARTICLE_SEARCH_BASE_URL;
	}

	private String pAPIKey() {
		return "&api-key=" + apiKey.getKey();
	}

	private String pQuery(String query) {
		return "?q=" + query;
	}

	public static void main(String[] args) {

		ArticleSearchQueryImpl a = new ArticleSearchQueryImpl(apiKey);
		Query query = new QueryImpl();
		query.setQuery(args[0]);
		query.setBegindate(args[1]);
		query.setEnddate(args[2]);
		query.setPage(1);
		ArticleSearchResponse response = a.query(query);
		List<Article> articlesList = response.getArticles();	

		for(int i=0;i<articlesList.size();i++){
			Article article = articlesList.get(i);
			String body="";
			try{
				body = WebCrawler.crawl(article.getWebUrl());
			}
			catch(Exception e){
				body="";
				//e.printStackTrace();
			}
			article.setBody(body);
			writeArticleToFile(article,query.getQuery());
		}

		int noOfPages = response.getTotal()/10;
		query=null;
		response=null;
		articlesList=null;
		if(noOfPages>100)
			noOfPages=100;
		int counter = 0;
		for(int i=2;i<=noOfPages;i++){
			query = new QueryImpl();
			query.setQuery(args[0]);
			query.setBegindate(args[1]);
			query.setEnddate(args[2]);
			query.setPage(i);
			response = a.query(query);

			try{
				articlesList = response.getArticles();	
				
			}
			catch(Exception e){
				e.printStackTrace();
				continue;
			}
			
			for(int j=0;j<articlesList.size();j++){
				Article article = articlesList.get(j);
				String body="";
				try{
					body = WebCrawler.crawl(article.getWebUrl());
				}
				catch(Exception e){
					body="";
					//e.printStackTrace();
				}
				article.setBody(body);
				counter++;
				writeArticleToFile(article,query.getQuery());
				System.out.println("counter : "+counter);
			}
	
			
		}
		System.out.println("no.of file : "+ counter);


	}




	public static void writeArticleToFile(Article article,String companyName){		   
		try{
			String articleDate=article.getDate();
			dataCleaning dc = new dataCleaning();
			String body = article.toString();
			body = dc.clean(body);
			newArticle.createFile(companyName, articleDate, "nyarticle1", body);
//			String filePath="C:\\BigDataProject\\Data_"+companyName+"\\"+articleDate;
//			File file = new File(filePath);
//			if (!file.exists()) {
//				file.mkdirs();
//				filePath=filePath+"\\"+"nyarticle1.txt";
//			}
//			else{
//				int noOfFiles = file.listFiles().length+1;
//				filePath=filePath+"\\"+"nyarticle"+noOfFiles+".txt";
//			}
//			System.out.println(filePath);
//			file=new File(filePath);
//			FileWriter fw=new FileWriter(file.getAbsoluteFile());
//			BufferedWriter bw=new BufferedWriter(fw);
//			bw.write(article.toString());
//			bw.close();

			System.out.println("Done writing article to file");

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
