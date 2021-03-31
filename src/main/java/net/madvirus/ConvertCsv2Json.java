package net.madvirus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ConvertCsv2Json {
	public static void main(String[] args) {
	      String[] conversations = readCsvFile("D://sample.csv"); // csv 파일을 string 배열로 변환
	      
	      String[] name = name("D://sample.csv", conversations); // csv 파일에서 제목으로 쓸 컨텐츠 네임 호출
	      
	      for(int a = 0; a < conversations.length; a++)
	      {
	    	  convertJavaObject2JsonFile(conversations, "D://", a, "D://sample.csv", name); // string 배열을 json 파일로 생성
	      }
	    }
	 
	    /**
	     * 
	     * Read CSV File into Memory
	     * @param filePath
	     * @return
	     */
	    private static String[] readCsvFile(String filePath) { // csv 파일을 string 배열로 변환
	    BufferedReader fileReader = null;
	    CSVParser csvParser = null; 
	 
	    List<String> conversations = new ArrayList<String>();
	    
	    try {
	      fileReader = new BufferedReader(new FileReader(filePath));
	      csvParser = new CSVParser(fileReader,
	          CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
	      int a = 0;
	 
	      Iterable<CSVRecord> csvRecords = csvParser.getRecords();
	      
	      for (CSVRecord csvRecord : csvRecords) { // list에 dialogflow에 사용할 수 있는 json 형식에 맞추어 읽어들인 csv 파일의 정보와 함께 입력
	    	  String conversation = "{\"id\": \"059ae626-91d5-43c3-a3b3-a560a5dba126\","
	    	  		+ "  \"name\": \"" + csvRecord.get("contentName") + csvRecord.get("contentType") +"\","
	    	  		+ "  \"auto\": true,"
	    	  		+ "  \"contexts\": [],"
	    	  		+ "  \"responses\": ["
	    	  		+ "    {"
	    	  		+ "      \"resetContexts\": false,"
	    	  		+ "      \"action\": \"\","
	    	  		+ "      \"affectedContexts\": [],"
	    	  		+ "      \"parameters\": [],"
	    	  		+ "      \"messages\": ["
	    	  		+ "        {"
	    	  		+ "          \"type\": \"0\","
	    	  		+ "          \"title\": \"\","
	    	  		+ "          \"textToSpeech\": \"\","
	    	  		+ "          \"lang\": \"ko\","
	    	  		+ "          \"speech\": ["
	    	  		+ "            \""+csvRecord.get("answer")+"\""
	    	  		+ "          ],"
	    	  		+ "          \"condition\": \"\""
	    	  		+ "        }"
	    	  		+ "      ],"
	    	  		+ "      \"speech\": []"
	    	  		+ "    }"
	    	  		+ "  ],"
	    	  		+ "  \"priority\": 500000,"
	    	  		+ "  \"webhookUsed\": false,"
	    	  		+ "  \"webhookForSlotFilling\": false,"
	    	  		+ "  \"fallbackIntent\": false,"
	    	  		+ "  \"events\": [],"
	    	  		+ "  \"conditionalResponses\": [],"
	    	  		+ "  \"condition\": \"\","
	    	  		+ "  \"conditionalFollowupEvents\": []}";
	    	  conversations.add(conversation);
	    	  
	    	  String con = "["
	    	  		+ "  {"
	    	  		+ "    \"id\": \"1da8c95c-2cfa-479e-b40b-8dd66998831d\","
	    	  		+ "    \"data\": ["
	    	  		+ "      {"
	    	  		+ "        \"text\": \""+csvRecord.get("question")+"\","
	    	  		+ "        \"userDefined\": false"
	    	  		+ "      }"
	    	  		+ "    ],"
	    	  		+ "    \"isTemplate\": false,"
	    	  		+ "    \"count\": 0,"
	    	  		+ "    \"lang\": \"ko\","
	    	  		+ "    \"updated\": 0"
	    	  		+ "  }"
	    	  		+ "]";
	    	  conversations.add(con);
	      }
	 
	    } catch (Exception e) {
	      System.out.println("Reading CSV Error!");
	      e.printStackTrace();
	    } finally {
	      try {
	        fileReader.close();
	        csvParser.close();
	      } catch (IOException e) {
	        System.out.println("Closing fileReader/csvParser Error!");
	        e.printStackTrace();
	      }
	    }
	    
	    String[] conversationss = conversations.toArray(new String[conversations.size()]); // string 배열에 list 값 입력
	    
	    for(int a = 0; a < conversationss.length; a++)
	    {
	    	conversationss[a] = conversationss[a].replaceAll("\\\"", "'"); // string 배열에 존재하는 큰 따옴표를 작은 따옴표로 대체
	    }
	    
	    return conversationss;
	  }
	    
	    private static String[] name(String filePath, String[] conversations) // 파일 제목으로 쓰기 위해 컨텐츠 네임을 가진 배열을 호출
	    {
	    	BufferedReader fileReader = null;
		    CSVParser csvParser = null;
		    int w = conversations.length/2;
		    String[] names = new String[w];
		    try {
			      fileReader = new BufferedReader(new FileReader(filePath));
			      csvParser = new CSVParser(fileReader,
			          CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
			      Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			      
			      int r = 0;
			      
			      for (CSVRecord csvRecord : csvRecords)
			      {
			    	  names[r] = csvRecord.get("contentName")+csvRecord.get("contentType"); // string 배열에 컨텐츠 네임과 컨텐츠 타입을 동시 입력
			    	  r++;
			      }
		    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    finally {
		    	try {
					fileReader.close();
					csvParser.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
			return names;
	    	
	    }
	 
	    /**
	     * Convert Java Object to JSON File
	     * @param customers
	     * @param pathFile
	     */
	    private static void convertJavaObject2JsonFile(String[] conversations, String pathFile, int a, String filePath, String[] name) {
	        
	        
	        
	        if(a%2==0) { // 배열에 들어가있는 입력값/출력값의 json의 제목을 따로 구현
	        	File file = new File(pathFile+name[a/2]+".json");
	        	try {
	        		// Serialize Java object info JSON file.
	        		 FileWriter fw = new FileWriter(file, true);
	        		 fw.write(conversations[a]);
	                 fw.flush();
	                 fw.close();
	        	} catch (IOException e) {
	        		e.printStackTrace();
	        	}
	        }
	        else {
	        	File file = new File(pathFile+name[a/2]+"_usersays_ko.json");
	        	try {
	        		// Serialize Java object info JSON file.
	        		FileWriter fw = new FileWriter(file, true);
	        		 fw.write(conversations[a]);
	                 fw.flush();
	                 fw.close();
	        	} catch (IOException e) {
	        		e.printStackTrace();
	        	}
	        }
	    }
}
