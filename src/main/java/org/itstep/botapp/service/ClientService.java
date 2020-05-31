package org.itstep.botapp.service;

import org.itstep.botapp.model.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ClientService {
    private final String urlOfGetSub = "http://localhost:8080/api/user/";

    @Autowired
    private final RestTemplate restTemplate;

    public ClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Equipment> getUserSubscription(long userId){
        Equipment[] equipments = restTemplate.getForObject(urlOfGetSub + userId, Equipment[].class);
        return new ArrayList<>(Arrays.asList(equipments));
    }

}
