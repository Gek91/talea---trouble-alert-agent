package functions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.chat.v1.model.Card;
import com.google.api.services.chat.v1.model.CardHeader;
import com.google.api.services.chat.v1.model.KeyValue;
import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.Section;
import com.google.api.services.chat.v1.model.WidgetMarkup;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

import functions.PubSubChatFunction.PubSubMessage;

public class PubSubChatFunction implements BackgroundFunction<PubSubMessage> {

	private static final Logger logger = Logger.getLogger(PubSubChatFunction.class.getName());

	private static final String URI = System.getenv("CHAT_WEBHOOK");
	
	private static final HttpTransport httpTransport = getHttpTransport();
	private static final HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public void accept(PubSubMessage message, Context context) {

		AlertInfo alertInfo = getAlertInfoFromPubSubMessage(message);
    
		Message botMessage = buildBotMessage(alertInfo);
		
		sendMessageToBot(botMessage);
	}

	private static HttpTransport getHttpTransport() {
		try {
			return GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private AlertInfo getAlertInfoFromPubSubMessage(PubSubMessage message) {
	
		try {
			AlertInfo result = new AlertInfo();
			
			Map<String, Object> valueMap = objectMapper.readValue(new String(Base64.getDecoder().decode(message.data)), Map.class);
			
			Map<String, Object> incidentMap = (Map<String, Object>) valueMap.get("incident");
			
			Map<String, Object> resourceMap = (Map<String, Object>) incidentMap.get("resource");
			
			Map<String, Object> labelsMap = (Map<String, Object>) resourceMap.get("labels");
			
			result.projectId = (String) labelsMap.get("project_id");
			result.policy = (String) incidentMap.get("policy_name");
			result.version = (String) labelsMap.get("version_id");
			result.resourceType = (String) resourceMap.get("type");
			
			return result;
			
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Message buildBotMessage(AlertInfo alertInfo) {
		
		Message message = new Message();
	    
	    Card card = new Card();
	    CardHeader header = new CardHeader();
	    header.setTitle("Talea - Trouble Alert Agent");
	    header.setImageUrl("https://goo.gl/aeDtrS");
	    card.setHeader(header);
	    
	    Section section = new Section();
	    
	    section.setWidgets(Arrays.asList(
	    		new WidgetMarkup().setKeyValue(new KeyValue().setContent(alertInfo.policy)),
	    		new WidgetMarkup().setKeyValue(new KeyValue().setTopLabel("Project id").setContent(alertInfo.projectId)),
	    		new WidgetMarkup().setKeyValue(new KeyValue().setTopLabel("Version").setContent(alertInfo.version)),
	    		new WidgetMarkup().setKeyValue(new KeyValue().setTopLabel("Resource type").setContent(alertInfo.resourceType))
	    		));
	    
	    card.setSections(Collections.singletonList(section));
	    
	    message.setCards(Collections.singletonList(card));
	    
	    return message;
	}
	
	private void sendMessageToBot(Message botMessage) {
		
		try {
			
			GenericUrl url = new GenericUrl(URI);
			
			HttpContent content = new ByteArrayContent("application/json", objectMapper.writeValueAsBytes(botMessage));
		    HttpRequest request = requestFactory.buildPostRequest(url, content);
		    request.execute();
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class AlertInfo {
		String projectId;
		String policy;
		String version;
		String resourceType;
	}
	
	public static class PubSubMessage {
		String data;
		Map<String, String> attributes;
		String messageId;
		String publishTime;
	}
	
}
