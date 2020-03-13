# word cloud

<video src="./WebContent/img/wordcloud_demo.mp4"></video>



## 프로그램 설명

IT 관련 사이트의 기사를 크롤링해서 워드 클라우드로 보여주는 프로그램



## 주요 기술 및 라이브러리

![image-20200314014652511](https://tva1.sinaimg.cn/large/00831rSTgy1gcss5zk0h9j31an0u0wig.jpg)



## 프로그램 주요 과정 설명

1. ITFIND(https://www.itfind.or.kr/) 사이트의 '오늘의 ICT 뉴스' 에서 하둡

   과 관련된 내용 검색해 Ajax를 이용하여 결과를 서버단으로 보냄

   ![image-20200314015051375](https://tva1.sinaimg.cn/large/00831rSTgy1gcssa2dwpuj31620owwlv.jpg)

2. 1번의 검색 결과를 Jackson을 이용해 가져온 뒤, 필요한 정보(url, 출처, 본 문 타이 틀)만 객체화

3. Jsoup을 이용해 각각의 기사들의 본문 내용들을 크롤링함

4. KOMORAN 형태소 분석기를 이용해 특수문자를 제거한 본문 내용들의 형태소를 분석함

5. 의존 명사를 제외한 명사들만 List로 저장

6. 5번의 리스트를 Spark의 데이터셋으로 저장

7. 길이가 2 이상인, 문자열의 중복 횟수를 세고, 두 번이상 언급된 명사들의 데이터 셋을 생성

8. 7번에서 만든 데이터셋을 Json으로 바꾸고 View단(index.html)으로 보냄

9. jQCloud를 이용하여 가장 빈도수가 높은 단어는 #f39700 색으로 표현하고, 글자의 빈도수와 글자 크기 비례하게 해 화면에 출력함

![image-20200314015548135](https://tva1.sinaimg.cn/large/00831rSTgy1gcssf81llvj31a00sg130.jpg)
