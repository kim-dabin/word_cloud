package servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.rowset.internal.Row;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import scala.Tuple2;

import vo.Article;

import static org.apache.spark.sql.functions.*;

@WebServlet(value="/getWords",loadOnStartup = 1)
public class Crawling extends HttpServlet {
	private Logger log = Logger.getLogger(this.getClass());
	
	
	 static {
		 BasicConfigurator.configure();
		 
	 }
	 
	
	private SparkSession spark;
	@Override
	public void init() throws ServletException {
		//스파크 서버
		
		spark = SparkSession.builder().master("local[*]").appName("word_cloud").config("spark.driver.host", "localhost").getOrCreate();
		spark.sparkContext().setLogLevel("INFO");
		log.info("0) 스파크 서버 켜기 완료");
	}
	
	@Override
	public void destroy() {
		//스파크 멈춤
		 spark.stop();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json; charset=UTF-8");
		//대소문자 무시 
		ObjectMapper om = new ObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		
		PrintWriter out =  resp.getWriter();
		
		//유저 사전 경로
		String path = this.getServletContext().getRealPath("WEB-INF"+File.separator+"userdic");
		
		//넘어온 파라미터
		String query = req.getParameter("q");
		
		//만약 넘어온 파라미터가 없다면 하둡으로
		if(query==null) query = "하둡";
		
		//urlencode방식으로 인코딩
		String q = URLEncoder.encode(query, "UTF-8");
		
		//URL을 이용하여 결과 js파일을 얻어옴
	URL con = new URL("https://www.itfind.or.kr/ajax/search_cmd.do?query="+q
			+"&requery=&collection=TREND&alias=ICTNEWS&startnum=0&listnum=10&sort=RANK%2FDESC&startdate=&enddate=&recommend=Y&_=1577528394577");

		
		
		
		
		//InputStream 연결
		InputStream in = con.openStream();

		//Scanner를 이용해서 읽어옴
		Scanner scan = new Scanner(in,"UTF-8");
		
		// json형태
		StringBuilder jsonSb = new StringBuilder();

		//만약 다음줄이 있으면 json 문자열 합침
		while (scan.hasNext()) {
			jsonSb.append(scan.nextLine());
		}//while end
		
		log.info("1) 검색 결과 얻어오기 완료!");
		String json = null;

		//결과를 자바스크립트의 배열로 만들기 위해서 앞뒤 자름
		json = jsonSb.substring(jsonSb.indexOf("\"resultDocumentList\"") + 21);
		json = json.substring(0,json.indexOf("\"totalCount\""));
	
		//	out.print(json+"<h2>");

		
		//log.info(json.contains("ORIGIN_ORIGINALURL"));
		
		//JacksonJson 라이브러리를 이용해 json을 자바의 VO로 변경
		Article[] articles = om.readValue(json, Article[].class);
		//log.info(articles[0].toString());
		log.info("2) json 생성 완료!");
		
	
		Map<String, String> publisherMap = new HashMap<String, String>();
		publisherMap.put("ZDNet Korea", "#articleBody");
		publisherMap.put("전자신문", "#articleBody p");
		publisherMap.put("전자신문-SW&바이오", "#articleBody p");
		publisherMap.put("아이뉴스24", "#articleBody");
		publisherMap.put("디지털타임스", ".art_txt");
		publisherMap.put("디지털타임즈", ".art_txt");
		publisherMap.put("전자신문-컴퓨팅",  "#articleBody p");
		publisherMap.put("블로터닷넷", ".entry-content");
		publisherMap.put("한국데이터진흥원", ".view_cont");
		
		List<String> list = new ArrayList<>();
		
		//Komoran 형태소 분석기 라이브러리를 활용해서 명사만 추출 
		Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
		komoran.setUserDic(path+File.separator+"userdic.txt");
		
		//log.info(path+File.separator);
		int cnt = 0; 
		
		//Article 마다 반복
		for(Article article : articles) {
			try {
				// url 얻어오기
				String url = article.getORIGIN_ORIGINALURL();
				//출처 얻어오기 
				String publisher = article.getPUBLISHER_PLACENAME();
				String publisherElement = null;
				if(publisherMap.containsKey(publisher)) {
					publisherElement = publisherMap.get(publisher);
				}else {
					for(String key: publisherMap.keySet()) {
						if(publisher.contains(key)) {
							publisherElement = publisherMap.get(key);
							break;
						}
					}
				}
				
				if(publisherElement==null) continue;
				
				Document doc = Jsoup.connect(url).get();
				log.info("3) HTML 크롤링"+  ++cnt +"번 읽어오기 완료");
				
		
				
				//본문이 있는 요소 선택 
				Elements elements = doc.select(publisherElement);
				if(elements.size()< 1 )	continue;
				
				Element element = elements.get(0);//art_txt
			
				
				//형태소 분석
				String match = "[^가-힣a-zA-Z0-9]";//한글 및 영문, 숫자 제외
				KomoranResult analyzeResultList = komoran.analyze(element.text().replaceAll(match," "));//특수문자 제거하기 
				log.info("4) 형태소 분석 완료!");
				
				List<Token> tokenList = analyzeResultList.getTokenList();
				
				
				for(Token token : tokenList ) {
					//log.info(token);
					
					//잘려진 토큰 중에 명사만(의존 명사 제외)		
					if(token.getPos().contains("NN")&&!token.getPos().equals("NNB")) {
						list.add(token.getMorph());
					}//if end
					
				}//for end
				
				
			} catch (Exception e) {
			//	e.printStackTrace();
				continue;
			}
				
		}//for end
		
	//	log.info(list.toString());
		
		//명사 데이터셋 
		Dataset<String> ds = spark.createDataset(list, Encoders.STRING());
		
		//길이가 2이상인 문자열만 
		Dataset<String> keyDS = ds.filter((String text)->text.length()>1);
		//keyDS.show();
		
		//명사 카운트 데이터셋 
		Dataset<Tuple2<String, Object>> countingDS = keyDS.groupByKey((String text) -> text, Encoders.STRING()).count();
		
		//중복 횟수가 2이상인 명사들 데이터셋
		Dataset<Tuple2<String, Object>>  countingDS2= countingDS.filter(countingDS.col("count(1)").gt(1));
		
		//컬럼 이름 바꾸기
		Dataset<org.apache.spark.sql.Row> renameDF = countingDS2.withColumnRenamed("count(1)", "weight").withColumnRenamed("value", "text");
		log.info("5) word counting 완료!");
		
		//json으로 바꾸기
		Dataset<String> wordCloudDS = renameDF.toJSON();		
		List<String> wordCloudList = new ArrayList<>();
		wordCloudList.addAll(wordCloudDS.collectAsList());
		//log.info(wordCloudList.toString());
		
		//json을 응답
		out.print(wordCloudList);
		
	
		
	}//doGet() end

}//Crawling end
