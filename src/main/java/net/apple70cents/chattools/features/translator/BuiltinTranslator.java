package net.apple70cents.chattools.features.translator;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.gui.components.EditBox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BuiltinTranslator extends AbstractTranslator {
    private final String api;
    private final boolean usePost;

    public BuiltinTranslator(EditBox editBox, String api, boolean usePost) {
        super(editBox);
        this.api = api;
        this.usePost = usePost;
    }

    @Override
    public String translate(String text) throws Exception {
        String method = usePost ? "POST" : "GET";
        if (api.isBlank()) {
            throw new Exception(TextUtils.trans("texts.translator.requireApi").getString());
        }
        String url = api;
        if (url.contains("{text}")) {
            url = url.replace("{text}", URLEncoder.encode(text, StandardCharsets.UTF_8));
        } else {
            url += URLEncoder.encode(text, StandardCharsets.UTF_8);
        }
        URL formattedUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) formattedUrl.openConnection();
        conn.setRequestMethod(method);
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "Chrome/138.0.0.0");
        conn.setRequestProperty("Accept-Charset", "utf-8");

        LoggerUtils.info("[ChatTools] Builtin Translator Visiting \"" + api + "\" with method: " + method);
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed with HTTP Error Code " + responseCode);
        } else {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }
    }
}
