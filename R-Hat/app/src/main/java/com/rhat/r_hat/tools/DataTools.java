package com.rhat.r_hat.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rhat.r_hat.model.Diary;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by PartyJat on 2016/8/25.
 */
public class DataTools {

    /*
    *数据存储
    * */
    //保存
    public void save(Context context, String fileName, String key, String value){
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }
    //读取
    public String load(Context context, String fileName, String key) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getString(key, "").toString();
    }
    //读取全部
    public Map loadAll(Context context, String fileName) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getAll();
    }
    //删除
    public void dalete(Context context, String fileName, String key){
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }
    //清空
    public void clear(Context context, String fileName){
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    /*
    * Json转换
    * */
    //List<Object>转Json数组字符串
    public String listToJsonArray (List<Diary> list) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        int count = list.size();
        for(int i = 0; i < count; i++)
        {
            jsonObject = new JsonObject();
            jsonObject.addProperty("id" , list.get(i).getId());
            jsonObject.addProperty("title", list.get(i).getTitle());
            jsonObject.addProperty("diary", list.get(i).getDiary());
            jsonObject.addProperty("date", list.get(i).getDate());
            jsonObject.addProperty("mood", list.get(i).getMood());
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    //Json转JsonArray
    public JsonArray jsonToJsonArray(String json) {
        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(json);
        JsonArray jsonAry = el.getAsJsonArray();
        return jsonAry;
    }

    //Json获取天气
    public String jsonGetWeather(String jsonStr) {
        String weatherStr = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            weatherStr = jsonObject.getString("data");
            jsonObject = new JSONObject(weatherStr);
            weatherStr = jsonObject.getString("weather");
        }catch (Exception e){
            e.printStackTrace();
        }
        return weatherStr;
    }

    //JsonArray转DiaryList
    public List<Diary> jsonArrayToDiaryList(JsonArray jsonAry) {
        List<Diary> list = new ArrayList<Diary>();
        Iterator it = jsonAry.iterator();
        Gson gson = new Gson();
        while (it.hasNext()) {
            JsonElement e = (JsonElement) it.next();
            list.add(gson.fromJson(e, Diary.class));
        }
        return list;
    }

}
