package org.scoula.external.youthapi;

import org.scoula.policy.dto.YouthPolicyApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class YouthPolicyApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youthcenter.apikey}")
    private String apiKey;

    @Value("${youthcenter.apiurl}")
    private String BASE_URL;

    public YouthPolicyApiResponse fetchPolicies(int pageNum, int pageSize) {
        String url = BASE_URL +
                "?apiKeyNm=" + apiKey +
                "&pageNum=" + pageNum +
                "&pageSize=" + pageSize +
                "&pageType=1" +
                "&rtnType=json";

        return restTemplate.getForObject(url, YouthPolicyApiResponse.class);
    }
}
