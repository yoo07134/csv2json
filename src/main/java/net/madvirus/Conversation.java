package net.madvirus;

public class Conversation {
	  private String contentName;
	  private String contentType;
	  private String question;
	  private String answer;
	 
	  public Conversation() {
	  }
	 
	  public Conversation(String contentName, String contentType, String question, String answer) { //데이터 값 입력
	    this.contentName = contentName;
	    this.contentType = contentType;
	    this.question = question;
	    this.answer = answer;
	  }
	 
	  public String getcontentName() { // 컨텐츠 네임 호출
	    return contentName;
	  }
	 
	  public void setcontentName(String contentName) { // 컨텐츠 네임 설정
	    this.contentName = contentName;
	  }
	 
	  public String getcontentType() {
	    return contentType;
	  }
	 
	  public void setcontentType(String contentType) {
	    this.contentType = contentType;
	  }
	 
	  public String getquestion() {
	    return question;
	  }
	 
	  public void setquestion(String question) {
	    this.question = question;
	  }
	 
	  public String getanswer() {
	    return answer;
	  }
	 
	  public void setanswer(String answer) {
	    this.answer = answer;
	  }
}
