package org.itstep.botapp.validator;

import org.springframework.stereotype.Component;

@Component
public class EquipmentValidation {

    public String validTypeProduct(String type) {
        String validType = "";
        if ("phone".equals(type))
            validType = "Телефон";
        if ("laptop".equals(type))
            validType = "Ноутбук";
        if ("tv_set".equals(type))
            validType = "Телевизор";
        return validType;
    }

}
