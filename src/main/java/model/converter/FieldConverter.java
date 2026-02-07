package model.converter;

import model.bean.FieldBean;
import model.domain.Field;
import model.domain.User;

public class FieldConverter {
    private FieldConverter() {
    }

    public static Field toEntity(FieldBean fieldBean) {
        if (fieldBean == null) {
            return null;
        }
        Field field = new Field();
        field.setId(fieldBean.getFieldId());
        field.setName(fieldBean.getName());
        field.setSport(fieldBean.getSport());
        field.setCity(fieldBean.getCity());
        field.setAddress(fieldBean.getAddress());
        field.setPricePerHour(fieldBean.getPricePerHour());
        User manager = new User();
        manager.setId(fieldBean.getManagerId());
        field.setManager(manager);
        return field;
    }

    public static FieldBean toBean(Field field) {
        if (field == null) {
            return null;
        }
        FieldBean fieldBean = new FieldBean();
        fieldBean.setFieldId(field.getId());
        fieldBean.setName(field.getName());
        fieldBean.setSport(field.getSport());
        fieldBean.setCity(field.getCity());
        fieldBean.setAddress(field.getAddress());
        fieldBean.setPricePerHour(field.getPricePerHour());
        fieldBean.setManagerId(field.getManager() != null ? field.getManager().getId() : 0);
        return fieldBean;
    }
}