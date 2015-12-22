package com.sdl.webapp.common.api;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.PageModel;
import com.sdl.webapp.common.markup.Markup;

/**
 * To be implemented by classes which aim to retrieve a Page from Tridion DB.
 * 
 * @author Indra-es
 *
 */
public interface PageRetriever {

	/**
	 * Get the view corresponding to the Tridion Page.
	 * @param request
	 * @param localization
	 * @param path
	 * @return
	 */
	public abstract String getPageView(HttpServletRequest request,
			Localization localization, String path);
	
	/**
	 * Get the view corresponding to the Tridion Page.
	 * @param request
	 * @param localization
	 * @param page
	 * @return
	 */
	public abstract String getPageViewFromPage(HttpServletRequest request,
			Localization localization, PageModel page);
	
	 /**
     *  Get a Page requested by a client
     * @param path
     * @param localization
     * @return
     */
	public abstract PageModel getPage(String path, Localization localization);
	
	/**
	 * 
	 * @param request
	 * @param localization
	 * @param page
	 * @return
	 */
	public abstract String getViewFromPage(HttpServletRequest request, Localization localization, 
			                               PageModel page);

	/**
	 * Determine if the request is for an include (a section of a bigger page)
	 * @param request
	 * @return
	 */
	public abstract boolean isIncludeRequest(HttpServletRequest request);

	/**
	 * Get a Page in JSON format
	 * @param path
	 * @param localization
	 * @return
	 */
	public abstract InputStream getPageContent(String path,
			Localization localization);

	/**
	 * 
	 * @return
	 */
	public abstract Markup getMarkup();

}