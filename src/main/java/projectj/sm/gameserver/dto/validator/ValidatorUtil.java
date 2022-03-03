package projectj.sm.gameserver.dto.validator;

import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class ValidatorUtil<T, U> {
    public static void addValidationError(String field, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(field)
                .addConstraintViolation();
    }

    public static <T, U> U getFieldValue(T object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field f = findField(object.getClass(), fieldName);
        f.setAccessible(true);
        return (U) f.get(object);
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        if (clazz.equals(Object.class))
            throw new NoSuchFieldException();
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return findField(clazz.getSuperclass(), fieldName);
        }
    }
}
