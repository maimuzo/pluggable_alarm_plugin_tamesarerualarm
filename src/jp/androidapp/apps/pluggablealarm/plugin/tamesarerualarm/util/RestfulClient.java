package jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;


public class RestfulClient {
    private static final String TAG = "RestfulClient";
    private String mBasicAuthUsername = "";
    private String mBasicAuthPassword = "";
    private HashMap<String, String> mHeaders;
    private boolean mIsVarboseMode = false;

    public RestfulClient(){
    	mBasicAuthUsername = "";
    	mBasicAuthPassword = "";
    	mHeaders = null;
    }

    public RestfulClient(HashMap<String, String> headers){
    	mBasicAuthUsername = "";
    	mBasicAuthPassword = "";
    	mHeaders = headers;
    }

    public RestfulClient(String user, String password, HashMap<String, String> headers){
    	mBasicAuthUsername = user;
    	mBasicAuthPassword = password;
    	mHeaders = headers;
    }
    
    public void setVerboseMode(boolean isVarbose){
    	mIsVarboseMode = isVarbose;
    }
    
	public String get(String uri, HashMap<String,String> map) throws ClientProtocolException, IOException {
		String fulluri;

		if(null == map){
			fulluri = uri;
		} else {
			fulluri = uri + packQueryString(map);
		}
		
		HttpGet method = new HttpGet(fulluri);
		return EntityUtils.toString(doRequest(method));
	}

	public Document get(String uri, HashMap<String,String> map, DocumentBuilder builder) throws ClientProtocolException, IOException, SAXException {
		String fulluri;

		if(null == map){
			fulluri = uri;
		} else {
			fulluri = uri + packQueryString(map);
		}
		
		HttpGet method = new HttpGet(fulluri);
		return getDOM(doRequest(method), builder);
	}
	
    public void get(String uri, HashMap<String,String> map, DefaultHandler handler) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        String fulluri;

        if(null == map){
            fulluri = uri;
        } else {
            fulluri = uri + packQueryString(map);
        }
        
        HttpGet method = new HttpGet(fulluri);
        parseBySAX(doRequest(method), handler);
    }

    public void get(String uri, HashMap<String,String> map, CustomPullParser pullParser) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        String fulluri;

        if(null == map){
            fulluri = uri;
        } else {
            fulluri = uri + packQueryString(map);
        }
        
        HttpGet method = new HttpGet(fulluri);
        parseByPullParser(doRequest(method), pullParser);
    }

    public void get(String uri, HashMap<String,String> map, OnAccessListener onAccessListener) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        String fulluri;

        if(null == map){
            fulluri = uri;
        } else {
            fulluri = uri + packQueryString(map);
        }
        
        HttpGet method = new HttpGet(fulluri);
        BufferedInputStream is = new BufferedInputStream(doRequest(method).getContent());
        try {
            onAccessListener.onAccess(is, uri);
        } finally{
            is.close();
        }
    }

    public String post(String uri, HashMap<String,String> map) throws ClientProtocolException, IOException {
        HttpPost method = new HttpPost(uri);
        if(null != map){
            List<NameValuePair> paramList = packEntryParams(map);
            method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
        }
        return EntityUtils.toString(doRequest(method));
    }

    /**
     * 
     * @param uri example "http://mysite.com"
     * @param map example key-value pair
     * @param filePostName example "param_name_of_file"
     * @param fileSchemeUrl example "file:///path/to/file"
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String post(String uri, HashMap<String,String> map, String filePostName, String fileSchemeUrl) throws ClientProtocolException, IOException {
        List<NameValuePair> paramList = null;
        if(null != map){
            paramList = packEntryParams(map);
        } else {
            paramList = new ArrayList<NameValuePair>();
        }
        HttpMultipartRequest request = new HttpMultipartRequest(
                uri,
                paramList,
                filePostName,
                fileSchemeUrl
                );
        return request.send();
    }

	public Document post(String uri, HashMap<String,String> map, DocumentBuilder builder) throws ClientProtocolException, IOException, SAXException {
		HttpPost method = new HttpPost(uri);
		if(null != map){
			List<NameValuePair> paramList = packEntryParams(map);
			method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
		}
		return getDOM(doRequest(method), builder);
	}

    public void post(String uri, HashMap<String,String> map, DefaultHandler handler) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        HttpPost method = new HttpPost(uri);
        if(null != map){
            List<NameValuePair> paramList = packEntryParams(map);
            method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
        }
        parseBySAX(doRequest(method), handler);
    }
	
    public void post(String uri, HashMap<String,String> map, CustomPullParser pullParser) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        HttpPost method = new HttpPost(uri);
        if(null != map){
            List<NameValuePair> paramList = packEntryParams(map);
            method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
        }
        parseByPullParser(doRequest(method), pullParser);
    }
    
    public void post(String uri, HashMap<String,String> map, OnAccessListener onAccessListener) throws ClientProtocolException, IOException {
        HttpPost method = new HttpPost(uri);
        if(null != map){
            List<NameValuePair> paramList = packEntryParams(map);
            method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
        }
        BufferedInputStream is = new BufferedInputStream(doRequest(method).getContent());
        try {
            onAccessListener.onAccess(is, uri);
        } finally{
            is.close();
        }
    }

    public String put(String uri, HashMap<String,String> map) throws ClientProtocolException, IOException {
		HttpPut method = new HttpPut(uri);
		if(null != map){
			List<NameValuePair> paramList = packEntryParams(map);
			method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
		}
		return EntityUtils.toString(doRequest(method));
	}

	public Document put(String uri, HashMap<String,String> map, DocumentBuilder builder) throws ClientProtocolException, IOException, SAXException {
		HttpPut method = new HttpPut(uri);
		if(null != map){
			List<NameValuePair> paramList = packEntryParams(map);
			method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
		}
		return getDOM(doRequest(method), builder);
	}

    public void put(String uri, HashMap<String,String> map, DefaultHandler handler) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        HttpPut method = new HttpPut(uri);
        if(null != map){
            List<NameValuePair> paramList = packEntryParams(map);
            method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
        }
        parseBySAX(doRequest(method), handler);
    }

    public void put(String uri, HashMap<String,String> map, CustomPullParser pullParser) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        HttpPut method = new HttpPut(uri);
        if(null != map){
            List<NameValuePair> paramList = packEntryParams(map);
            method.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
        }
        parseByPullParser(doRequest(method), pullParser);
    }

    public String delete(String uri, HashMap<String,String> map) throws ClientProtocolException, IOException {
		String fulluri;

		if(null == map){
			fulluri = uri;
		} else {
			fulluri = uri + packQueryString(map);
		}
		
		HttpDelete method = new HttpDelete(fulluri);
		return EntityUtils.toString(doRequest(method));
	}

	public Document delete(String uri, HashMap<String,String> map, DocumentBuilder builder) throws ClientProtocolException, IOException, SAXException {
		String fulluri;

		if(null == map){
			fulluri = uri;
		} else {
			fulluri = uri + packQueryString(map);
		}
		
		HttpDelete method = new HttpDelete(fulluri);
		return getDOM(doRequest(method), builder);
	}
	
    public void delete(String uri, HashMap<String,String> map, DefaultHandler handler) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        String fulluri;

        if(null == map){
            fulluri = uri;
        } else {
            fulluri = uri + packQueryString(map);
        }
        
        HttpDelete method = new HttpDelete(fulluri);
        parseBySAX(doRequest(method), handler);
    }

    public void delete(String uri, HashMap<String,String> map, CustomPullParser pullParser) throws ClientProtocolException, IOException, SAXException, IllegalStateException, ParserConfigurationException {
        String fulluri;

        if(null == map){
            fulluri = uri;
        } else {
            fulluri = uri + packQueryString(map);
        }
        
        HttpDelete method = new HttpDelete(fulluri);
        parseByPullParser(doRequest(method), pullParser);
    }

    /*
	 * DocumentBuilderFactory.newInstance()
	 * 	.setValidating(true)
	 * 	.setIgnoringElementContentWhitespace(true)
	 * 	.newDocumentBuilder()
	 *  .parse(hoge);
	 *  でうまく空ノードを取ってくれそうだけど、バリデータが実装されてないのか例外が出る。
	 *  また
	 *  Node.normalize()もなんか変
	 *  なので、自前で改行やスペースだけのテキストノードを削除する。
	 */
    public static Node removeEmptyNodes(Node currentNode) {
        NodeList list = currentNode.getChildNodes();
        int n = list.getLength();
        if(0 < n){
            for (int i = 0; i < n; i++) {
                Node childNode = list.item(i);
                String value = childNode.getNodeValue();
                // Log.v(TAG, "value : " + value);
                if(Node.TEXT_NODE == childNode.getNodeType() && value.trim().equals("")){
                	// Log.v(TAG, "remove " + Integer.toString(i) + "th node of " + currentNode.getNodeName());
                	currentNode.removeChild(childNode);
                }else{
                	removeEmptyNodes(childNode);
                }
            }
        }
        return currentNode;
    }

	
	private HttpEntity doRequest(HttpUriRequest method) throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		
		// BASIC認証用のユーザ名が設定されていれば、BASIC認証を行う
		if(!mBasicAuthUsername.equals("")){
			URI uri = method.getURI();
			client.getCredentialsProvider().setCredentials(
				new AuthScope(uri.getHost(), uri.getPort()),
				new UsernamePasswordCredentials(mBasicAuthUsername, mBasicAuthPassword));
		}
		
		// 独自ヘッダーが用意されていればそれを使う (BASIC認証のヘッダー上書きに注意)
		if(null != mHeaders && 0 < mHeaders.size()){
			Iterator<String> ite = mHeaders.keySet().iterator();
			while(ite.hasNext()){
				String name = ite.next();
				method.addHeader(name, mHeaders.get(name));
			}
		}
		
		HttpResponse response = null;
		
		try {
			if(mIsVarboseMode){
				Log.d(TAG, "HTTP method: " + method.getMethod() + " " + method.getURI().toASCIIString());
				
				Header[] headers = method.getAllHeaders();
				if(0 == headers.length){
					Log.d(TAG, "HTTP Header: no header.");
				} else {
					for(Header header: headers){
						Log.d(TAG, "HTTP Header: " + header.toString());
					}
				}
			}
			response = client.execute(method);
			int statuscode = response.getStatusLine().getStatusCode();
			
			//リクエストが成功 200 OK and 201 CREATED
			if (statuscode == HttpStatus.SC_OK | statuscode == HttpStatus.SC_CREATED){ 
				return response.getEntity();
			} else {
				throw new HttpResponseException(statuscode, "Response code is " + Integer.toString(statuscode) + ", url:" + method.getURI());
			}
		}catch (RuntimeException e) {
			method.abort();
			Log.v(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	private static List<NameValuePair> packEntryParams(HashMap<String,String> map){
		if(null == map){
			throw new RuntimeException("map is null");
		}

		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
		Map.Entry<String, String> entry;

		while(itr.hasNext()){
			entry = itr.next();
			paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			Log.d(TAG, "HTTP params: " + entry.getKey() + " = " + entry.getValue() );
		}
		return paramList;
	}
	
	private static String packQueryString(HashMap<String,String> map) throws UnsupportedEncodingException{
		if(null == map){
			throw new RuntimeException("map is null");
		}
		
		StringBuilder sb = new StringBuilder(100);
		Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
		Map.Entry<String, String> entry;
		
		while(itr.hasNext()){
			entry = itr.next();
			if(0 == sb.length()){
				sb.append("?");
			}else{
				sb.append("&");
			}
			sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return sb.toString();
	}
	
	private static Document getDOM(HttpEntity entity, DocumentBuilder builder) throws IOException, SAXException{
		BufferedInputStream is = new BufferedInputStream(entity.getContent());
		Document doc = null;
		try {
			doc = builder.parse(is);
			return doc;
		} finally{
			is.close();
		}
	}
	
    private static void parseBySAX(HttpEntity entity, DefaultHandler handler) throws ParserConfigurationException, IllegalStateException, IOException, SAXException{
        BufferedInputStream is = new BufferedInputStream(entity.getContent());
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(is, handler);
        } finally{
            is.close();
        }
    }
    
    private static void parseByPullParser(HttpEntity entity, CustomPullParser pullParser) throws ParserConfigurationException, IllegalStateException, IOException, SAXException{
        BufferedInputStream is = new BufferedInputStream(entity.getContent());
        try {
            pullParser.parseByPullParser(is);
        } finally{
            is.close();
        }
    }

    public interface CustomPullParser {
        void parseByPullParser(BufferedInputStream is);
    }

    public interface OnAccessListener{
        void onAccess(BufferedInputStream is, String url);
    }
    /**
     * Example of CustomPullParser
     * 
    public class ExampleCustomPullParser implements CustomPullParser{
        public void parseByPullParser(BufferedInputStream is){
            XmlPullParserFactory factory;
            try {
                factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(is, "UTF-8");
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        System.out.println("Start document");
                    } else if (eventType == XmlPullParser.END_DOCUMENT) {
                        System.out.println("End document");
                    } else if (eventType == XmlPullParser.START_TAG) {
                        System.out.println("Start tag " + xpp.getName());
                    } else if (eventType == XmlPullParser.END_TAG) {
                        System.out.println("End tag " + xpp.getName());
                    } else if (eventType == XmlPullParser.TEXT) {
                        System.out.println("Text " + xpp.getText());
                    }
                    eventType = xpp.next();
                }
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    */
    
}