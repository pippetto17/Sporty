package model.converter;

import model.bean.FieldBean;
import model.domain.Field;

public class FieldConverter {

    private FieldConverter() {
        // Private constructor to prevent instantiation
    }

    public static Field toField(FieldBean fieldBean) {
        if (fieldBean == null) {
            return null;
        }

        Field field = new Field();
        field.setFieldId(fieldBean.getFieldId());
        field.setName(fieldBean.getName());
        field.setSport(fieldBean.getSport());
        field.setAddress(fieldBean.getAddress());
        field.setCity(fieldBean.getCity());
        field.setPricePerHour(fieldBean.getPricePerHour());
        field.setIndoor(fieldBean.isIndoor());
        field.setManagerId(fieldBean.getManagerId());
        field.setStructureName(fieldBean.getStructureName());
        field.setAutoApprove(fieldBean.isAutoApprove());

        return field;
    }

    public static FieldBean toFieldBean(Field field) {
        if (field == null) {
            return null;
        }

        FieldBean fieldBean = new FieldBean();
        fieldBean.setFieldId(field.getFieldId());
        fieldBean.setName(field.getName());
        fieldBean.setSport(field.getSport());
        fieldBean.setAddress(field.getAddress());
        fieldBean.setCity(field.getCity());
        fieldBean.setPricePerHour(field.getPricePerHour());
        fieldBean.setIndoor(field.isIndoor());
        fieldBean.setManagerId(field.getManagerId());
        fieldBean.setStructureName(field.getStructureName());
        fieldBean.setAutoApprove(field.getAutoApprove());

        return fieldBean;
    }

    public static FieldBean toFieldBeanWithPricePerPerson(Field field, int participants) {
        FieldBean fieldBean = toFieldBean(field);
        if (fieldBean != null && field.getPricePerHour() != null) {
            // Calculate price per person for 2 hours booking
            double pricePerPerson = model.utils.Utils.calculatePricePerPerson(
                    field.getPricePerHour(), 2.0, participants);
            fieldBean.setPricePerPerson(pricePerPerson);
        }
        return fieldBean;
    }

    // Alias methods for consistency
    public static Field toEntity(FieldBean bean) {
        return toField(bean);
    }

    public static FieldBean toBean(Field entity) {
        return toFieldBean(entity);
    }
}
