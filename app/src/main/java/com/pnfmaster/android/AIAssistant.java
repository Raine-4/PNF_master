package com.pnfmaster.android;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIAssistant{

    // Store the dialog content.
    public JSONArray Dialogue_Content;

    public AIAssistant() { Dialogue_Content = new JSONArray(); }

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS) // 连接超时设置为60秒
			.writeTimeout(60, TimeUnit.SECONDS) // 写入超时设置为60秒
			.readTimeout(60, TimeUnit.SECONDS) // 读取超时设置为60秒
            .build();

    public String GetAnswer(String user_msg, String background) throws IOException, JSONException {

        // 将JSONObject添加到JSONArray中
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("role", "user");
        jsonObject.put("content", user_msg);
        Dialogue_Content.put(jsonObject);

        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(mediaType,
                "{" +
                        "\"messages\":" + Dialogue_Content.toString() +
                        ",\"system\": " + "\"" + background + "\""
                + "}");

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" +
                        getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();

        String responseString = response.body().string(); // response body只能调用一次

        Log.d("AIAssistant", "response: " + responseString);
        // 解析出ai的回答
        JSONObject json_feedback = new JSONObject(responseString);
        Log.d("AIAssistant", "json_feedback: " + json_feedback);
        String reply = json_feedback.getString("result");
        // 接下来把ai的回答加入到Dialogue_Content中
        JSONObject jsontmp = new JSONObject();
        jsontmp.put("assistant", reply);
        Dialogue_Content.put(jsontmp);

        return reply;
    }

    /**
     * 从用户的API_KEK和SECRET_KEK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    public String getAccessToken() throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + BuildConfig.API_KEY
                + "&client_secret=" + BuildConfig.SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }
}