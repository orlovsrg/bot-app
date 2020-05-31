package org.itstep.botapp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@ToString
@Component
public class Equipment {
    private int id;
    private String title;
    private int price;
    private String url;
    private String imgUrl;
    private int storeId;
    private String storeName;
    private String type;
    private List<Integer> usersId;
}
