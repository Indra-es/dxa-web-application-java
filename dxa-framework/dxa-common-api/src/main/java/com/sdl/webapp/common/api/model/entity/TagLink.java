package com.sdl.webapp.common.api.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sdl.webapp.common.api.mapping.annotations.SemanticEntity;
import com.sdl.webapp.common.api.mapping.annotations.SemanticProperties;
import com.sdl.webapp.common.api.mapping.annotations.SemanticProperty;

import static com.sdl.webapp.common.api.mapping.config.SemanticVocabulary.SDL_CORE;

@SemanticEntity(entityName = "SocialLink", vocabulary = SDL_CORE, prefix = "s")
public class TagLink extends AbstractEntityModel {

    @SemanticProperties({
            @SemanticProperty("internalLink"),
            @SemanticProperty("externalLink"),
            @SemanticProperty("s:internalLink"),
            @SemanticProperty("s:externalLink")
    })
    @JsonProperty("Url")
    private String url;

    @SemanticProperty("s:tag")
    @JsonProperty("Tag")
    private Tag tag;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "TagLink{" +
                "url='" + url + '\'' +
                ", tag=" + tag +
                '}';
    }
}
