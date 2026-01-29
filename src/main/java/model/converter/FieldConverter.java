package model.converter;

import model.bean.FieldBean;
import model.domain.Field;

public class FieldConverter {

    private FieldConverter() {
        // Private constructor to prevent instantiation
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
        field.setManagerId(fieldBean.getManagerId());

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
        fieldBean.setManagerId(field.getManagerId());

        return fieldBean;
    }
}
