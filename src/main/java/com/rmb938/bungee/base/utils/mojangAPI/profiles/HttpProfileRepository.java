package com.rmb938.bungee.base.utils.mojangAPI.profiles;

import com.google.gson.Gson;
import com.rmb938.bungee.base.utils.mojangAPI.http.BasicHttpClient;
import com.rmb938.bungee.base.utils.mojangAPI.http.HttpBody;
import com.rmb938.bungee.base.utils.mojangAPI.http.HttpClient;
import com.rmb938.bungee.base.utils.mojangAPI.http.HttpHeader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HttpProfileRepository implements ProfileRepository {

    private static final int MAX_PAGES_TO_CHECK = 100;
    private static Gson gson = new Gson();
    private HttpClient client;

    public HttpProfileRepository() {
        this(BasicHttpClient.getInstance());
    }

    public HttpProfileRepository(HttpClient client) {
        this.client = client;
    }

    @Override
    public Profile[] findProfilesByCriteria(ProfileCriteria... criteria) {
        try {
            HttpBody body = new HttpBody(gson.toJson(criteria));
            List<HttpHeader> headers = new ArrayList<>();
            headers.add(new HttpHeader("Content-Type", "application/json"));
            List<Profile> profiles = new ArrayList<>();
            for (int i = 1; i <= MAX_PAGES_TO_CHECK; i++) {
                ProfileSearchResult result = post(new URL("https://api.mojang.com/profiles/page/" + i), body, headers);
                if (result.getSize() == 0) {
                    break;
                }
                for (Profile profile : result.getProfiles()) {
                    if (profile.isDemo()) {
                        continue;
                    }
                    if (profile.isLegacy()) {
                        continue;
                    }
                    profiles.add(profile);
                }
            }
            return profiles.toArray(new Profile[profiles.size()]);
        } catch (Exception e) {
            return new Profile[0];
        }
    }

    private ProfileSearchResult post(URL url, HttpBody body, List<HttpHeader> headers) throws IOException {
        String response = client.post(url, body, headers);
        return gson.fromJson(response, ProfileSearchResult.class);
    }

}
