package com.sdl.webapp.common.api.mapping.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sdl.webapp.common.api.localization.Localization;

import java.util.Map;
import java.util.Set;

/**
 * Semantic schema.
 */
public final class SemanticSchema {

    private final long id;

    private final String rootElement;

    private final Set<EntitySemantics> entitySemantics;

    private final Map<FieldSemantics, SemanticField> semanticFields;
    private Localization localization;

    public SemanticSchema(long id, String rootElement, Set<EntitySemantics> entitySemantics,
                          Map<FieldSemantics, SemanticField> semanticFields) {
        this.id = id;
        this.rootElement = rootElement;
        this.entitySemantics = ImmutableSet.copyOf(entitySemantics);
        this.semanticFields = ImmutableMap.copyOf(semanticFields);
    }

    public Localization getLocalization() {
        return localization;
    }

    public void setLocalization(Localization localization) {
        this.localization = localization;
    }

    public long getId() {
        return id;
    }

    public String getRootElement() {
        return rootElement;
    }

    public Set<EntitySemantics> getEntitySemantics() {
        return entitySemantics;
    }

    public Map<FieldSemantics, SemanticField> getSemanticFields() {
        return semanticFields;
    }

    @Override
    public String toString() {
        return "SemanticSchema{" +
                "id=" + id +
                ", rootElement='" + rootElement + '\'' +
                ", entitySemantics=" + entitySemantics +
                ", semanticFields=" + semanticFields +
                '}';
    }
}
