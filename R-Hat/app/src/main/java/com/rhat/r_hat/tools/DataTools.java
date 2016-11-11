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
        //创建一个SharedPreferences实例来以文件形式进行本地数据存储
        //第一个参数为文件名，第二个参数为这个文件的读写权限
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        //除了读取，其他操作都要打开一个编辑器
        SharedPreferences.Editor editor = sp.edit();
        //把数据以（key, value）的形式保存到文件中
        editor.putString(key, value);
        //提交更改
        editor.commit();
    }
    //读取
    public String load(Context context, String fileName, String key) {
        //创建一个SharedPreferences实例来以文件形式进行本地数据存储
        //第一个参数为文件名，第二个参数为这个文件的读写权限
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        //用key来取出值
        //第二个参数为默认值，即如果找不到key，默认返回“”
        return sp.getString(key, "").toString();
    }
    //删除
    public void dalete(Context context, String fileName, String key){
        //创建一个SharedPreferences实例来以文件形式进行本地数据存储
        //第一个参数为文件名，第二个参数为这个文件的读写权限
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        //除了读取，其他操作都要打开一个编辑器
        SharedPreferences.Editor editor = sp.edit();
        //用key来删除对应的值
        editor.remove(key);
        //提交更改
        editor.commit();
    }
    //清空
    public void clear(Context context, String fileName){
        //创建一个SharedPreferences实例来以文件形式进行本地数据存储
        //第一个参数为文件名，第二个参数为这个文件的读写权限
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        //除了读取，其他操作都要打开一个编辑器
        SharedPreferences.Editor editor = sp.edit();
        //清空这个文件
        editor.clear();
        //提交更改
        editor.commit();
    }

    /*
    * Json转换
    * */
    //List<Object>转Json数组字符串
    public String listToJsonArray (List<Diary> list) {
        //创建一个Jsan数组实例
        JsonArray jsonArray = new JsonArray();
        //创建一个Jsan对象
        JsonObject jsonObject;
        //保存列表的长度
        int count = list.size();
        //for循环来进行赋值
        for(int i = 0; i < count; i++)
        {
            //创建一个Jsan对象实例
            jsonObject = new JsonObject();
            //在Json对象中增加属性
            jsonObject.addProperty("id" , list.get(i).getId());
            jsonObject.addProperty("title", list.get(i).getTitle());
            jsonObject.addProperty("diary", list.get(i).getDiary());
            jsonObject.addProperty("date", list.get(i).getDate());
            //把赋好值的Json对象添加到Json数组中
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    //Json转JsonArray
    public JsonArray jsonToJsonArray(String json) {
        //创建一个Jsan解析器对象，用来解析Json字符串
        JsonParser parser = new JsonParser();
        //创建一个Jsan元素类，上面的解析器把Json字符串解析成Json元素
        JsonElement el = parser.parse(json);
        //创建一个Jsan数组，把Json元素转换成Json数组
        JsonArray jsonAry = el.getAsJsonArray();
        //返回Json数组
        return jsonAry;
    }

    //JsonArray转DiaryList
    public List<Diary> jsonArrayToDiaryList(JsonArray jsonAry) {
        //创建一个列表
        List<Diary> list = new ArrayList<Diary>();
        //创建一个Json数组的迭代器
        Iterator it = jsonAry.iterator();
        //Gson是谷歌提供的用来转换Json和Java对象的API
        Gson gson = new Gson();
        //用迭代器遍历Json数组
        while (it.hasNext()) {
            //创建一个Json元素来存储Json数组读取出来的元素
            JsonElement e = (JsonElement) it.next();
            //利用Gson把Json字符串转换成Diary对象，并添加到列表中
            list.add(gson.fromJson(e, Diary.class));
        }
        //返回列表
        return list;
    }

}
