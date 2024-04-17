package com.pnfmaster.android.chat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIAssistant{
    // public static final String APP_ID = "61872386";
    public static final String API_KEY = "Lb0TBhOswHkiutLhW9ECI4xH";
    public static final String SECRET_KEY = "iYOfF5xRRAXUQ4386KX0exOkmpEHPbQ6";

    // Store the dialog content.
    public JSONArray Dialogue_Content;

    AIAssistant(){
        Dialogue_Content = new JSONArray();
    }

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public String GetAnswer(String user_msg) throws IOException, JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("role", "user");
        jsonObject.put("content", user_msg);

        // 将JSONObject添加到JSONArray中
        // 这里就是把用户说的话添加进对话内容里，然后发给ai
        Dialogue_Content.put(jsonObject);

        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(mediaType,  "{\"messages\":" +
                Dialogue_Content.toString() +
                ",\"disable_search\":false,\"enable_citation\":false}");


        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" +
                        getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();

        // 解析出ai的回答
        JSONObject json_feedback = new JSONObject(response.body().string());
        // 这里在开发的时候遇到了一个问题，注意response在上一行被取出里边的内容之后就自动关闭了，不能多次传参。
        String re = json_feedback.getString("result");
        // 接下来把ai的回答加入到Dialogue_Content中
        JSONObject jsontmp = new JSONObject();
        jsontmp.put("assistant", re);
        Dialogue_Content.put(jsontmp);

        return re;
    }

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    public String getAccessToken() throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }
}