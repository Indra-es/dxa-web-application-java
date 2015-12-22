package com.sdl.webapp.common.api.content;

import java.io.InputStream;

import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.EntityModel;
import com.sdl.webapp.common.api.model.PageModel;
import com.sdl.webapp.common.api.model.entity.ContentList;
import com.sdl.webapp.common.exceptions.DxaException;

/**
 * Content provider.
 */
public interface ContentProvider {

    /**
     * Gets a page by path for a specific localization.
     *
     * @param path         The path of the page.
     * @param localization The localization.
     * @return The {@code Page}.
     * @throws ContentProviderException If an error occurred so that the content of the page could be retrieved.
     */
    PageModel getPageModel(String path, Localization localization) throws ContentProviderException;

    /**
     * Get entity model by TCM URI
     *
     * @return the {@code Entity}
     * @throws ContentProviderException
     */
    EntityModel getEntityModel(String id, Localization localization) throws ContentProviderException, DxaException;

    /**
     * Populates a dynamic list.
     *
     * @param contentList  The list to populate.
     * @param localization The localization.
     * @throws ContentProviderException If an error occurred so that the content of the list could not be retrieved.
     */
    void populateDynamicList(ContentList contentList, Localization localization) throws ContentProviderException;

    /**
     * Gets a static content item by path for a specific localization.
     *
     * @param path             The path of the static content item.
     * @param localizationId   The localization ID.
     * @param localizationPath The localization path.
     * @return The {@code StaticContentItem}.
     * @throws ContentProviderException If an error occurred so that the static content item could not be retrieved.
     */
    StaticContentItem getStaticContent(String path, String localizationId, String localizationPath)
            throws ContentProviderException;
    
    /**
     * 
     * @param path
     * @param localization
     * @return
     * @throws ContentProviderException
     */
    public InputStream getPageContent(String path, Localization localization) 
    		throws ContentProviderException;
}
