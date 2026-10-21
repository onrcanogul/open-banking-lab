package io.github.onrcanogul.openbanking.hhsbank.api.error;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

/**
 * Validation reports Java property paths ({@code scope.permission.types[0]}); the API contract is the Turkish
 * JSON names ({@code hspBlg.iznBlg.iznTur[0]}). Walks the request record's {@link JsonProperty} annotations
 * to translate one into the other, and falls back to the Java name wherever the shape is not a record.
 */
final class JsonFieldNames {

    private JsonFieldNames() {}

    static String resolve(Class<?> root, String javaPath) {
        String[] segments = javaPath.split("\\.");
        StringBuilder out = new StringBuilder();
        Class<?> current = root;

        for (int i = 0; i < segments.length; i++) {
            int bracket = segments[i].indexOf('[');
            String name = bracket < 0 ? segments[i] : segments[i].substring(0, bracket);
            String index = bracket < 0 ? "" : segments[i].substring(bracket);

            RecordComponent component = current == null ? null : component(current, name);
            if (i > 0) out.append('.');
            if (component == null) {
                out.append(String.join(".", Arrays.copyOfRange(segments, i, segments.length)));
                return out.toString();
            }
            out.append(jsonName(component)).append(index);
            current = elementType(component);
        }
        return out.toString();
    }

    private static RecordComponent component(Class<?> type, String name) {
        if (!type.isRecord()) return null;
        return Arrays.stream(type.getRecordComponents())
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static String jsonName(RecordComponent component) {
        JsonProperty property = component.getAccessor().getAnnotation(JsonProperty.class);
        return property != null && !property.value().isEmpty() ? property.value() : component.getName();
    }

    private static Class<?> elementType(RecordComponent component) {
        Type type = component.getGenericType();
        if (type instanceof Class<?> plain) return plain;
        if (type instanceof ParameterizedType parameterized && parameterized.getRawType() instanceof Class<?> raw) {
            Type argument = parameterized.getActualTypeArguments()[0];
            if (Collection.class.isAssignableFrom(raw) && argument instanceof Class<?> element) return element;
            return raw;
        }
        return null;
    }
}
