package vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"SMALL_CLASS","ORIGIN_DUPLICATEURL","CATEGORY_ID",
	"MIDDLE_CLASS","REGIST_DATE","APPLICATION","ALIAS","KEYWORD","DOCID","APPDATE",
	"AGENT","MAIN_CLASS","SUMMARY","MODIFY_DATE","SUMMARY_EN","PUBLISHER_NAME",
	"TITLE_EN","TITLE_EQ","APPID","DETAIL_CLASS","F_IDENTIFIER","FORM_TYPE",
	"CATEGORY_TYPE","FIELD_TYPE","INVENTOR","DATE","LIST_CONTENT","CREATE_DATE","FILE_ORIGIN_NAME",
	"IPC","REPRESENT_IMAGE","COUNTRY_CODE"})

//@JsonIgnoreProperties(ignoreUnknown = true)

public class Article implements Serializable {
	
	private String ORIGIN_ORIGINALURL, TITLE,PUBLISHER_PLACENAME;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return " url "+this.ORIGIN_ORIGINALURL+" "+" title "+this.TITLE+" publisher"+this.PUBLISHER_PLACENAME;
	}

	public String getPUBLISHER_PLACENAME() {
		return PUBLISHER_PLACENAME;
	}
	
	public void setPUBLISHER_PLACENAME(String PUBLISHER_PLACENAME) {
		this.PUBLISHER_PLACENAME = PUBLISHER_PLACENAME;
	}
	public String getORIGIN_ORIGINALURL() {
		return ORIGIN_ORIGINALURL;
	}

	public void setORIGIN_ORIGINALURL(String ORIGIN_ORIGINALURL) {
	this.ORIGIN_ORIGINALURL = ORIGIN_ORIGINALURL;
	}

	public String getTITLE() {
		return TITLE;
	}

	public void setTITLE(String TITLE) {
		this.TITLE = TITLE;
	}
	
	

}
