package com.rhat.r_hat.tools;

import android.util.Log;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by PartyJat on 2016/8/25.
 */
public class HttpUtils {

    public static String[] getWeather(String urlPath) {
        String resultNum = "-500";
        String resultJson = "";
        String[] resultAry = new String[2];
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int len = 0;
            URL url = new URL(urlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);        //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据

            int response = httpURLConnection.getResponseCode();
            Log.v("responNum", ""+response);
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                if((resultJson = dealResponseResult(inptStream)) != null){
                    if(resultJson.equals("1") || resultJson.equals("0") || resultJson.equals("-1")){
                        resultNum = resultJson;
                    }else{
                        resultNum = "1";
                    }
                }else{
                    resultNum = "0";
                }
            }

            InputStream inStream = httpURLConnection.getInputStream();
            while ((len = inStream.read(data)) != -1) {
                outStream.write(data, 0, len);
            }
            inStream.close();
            resultJson = new String(outStream.toByteArray());
            Log.v("testPath", resultJson);
        }catch(Exception e){
            e.printStackTrace();
            resultNum = "0";
        }
        resultAry[0] = resultNum;
        resultAry[1] = resultJson;
        return resultAry;
    }

    /*
     * Function  :   发送Post请求到服务器
     * Param     :   params请求体内容，encode编码格式
     */
    //返回字符串
    public static String[] postData(Map<String, String> params, String encode, String urlStr){
        String resultNum = "-500";
        String resultJson = "";
        String[] resultAry = new String[2];
        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        try {
            URL url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);        //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据*/
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                if((resultJson = dealResponseResult(inptStream)) != null){
                    if(resultJson.equals("1") || resultJson.equals("0") || resultJson.equals("-1")){
                        resultNum = resultJson;
                    }else{
                        resultNum = "1";
                    }
                }else{
                    resultNum = "0";
                }
                Log.v("debug","post true");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("debug","post false");
        }
        resultAry[0] = resultNum;
        resultAry[1] = resultJson;
        return resultAry;
    }

    /*
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     */
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            //遍历Map
            Set<String> key = params.keySet();
            for(Iterator it = key.iterator();it.hasNext();){
                String k = (String) it.next();
                stringBuffer.append(k)
                        .append("=")
                        .append(URLEncoder.encode(params.get(k),encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    /*
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    public static String dealResponseResult(InputStream inputStream) {
        String resultJson = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultJson = new String(byteArrayOutputStream.toByteArray());
        Log.v("ByteSize", data.toString());
        return resultJson;
    }
}
