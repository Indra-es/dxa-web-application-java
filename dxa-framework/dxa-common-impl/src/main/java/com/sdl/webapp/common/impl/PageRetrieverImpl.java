package com.sdl.webapp.common.impl;

import static com.sdl.webapp.common.controller.RequestAttributeNames.CONTEXTENGINE;
import static com.sdl.webapp.common.controller.RequestAttributeNames.LOCALIZATION;
import static com.sdl.webapp.common.controller.RequestAttributeNames.MARKUP;
import static com.sdl.webapp.common.controller.RequestAttributeNames.MEDIAHELPER;
import static com.sdl.webapp.common.controller.RequestAttributeNames.PAGE_ID;
import static com.sdl.webapp.common.controller.RequestAttributeNames.PAGE_MODEL;
import static com.sdl.webapp.common.controller.RequestAttributeNames.SCREEN_WIDTH;
import static com.sdl.webapp.common.controller.RequestAttributeNames.SOCIALSHARE_URL;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.sdl.webapp.common.api.MediaHelper;
import com.sdl.webapp.common.api.PageRetriever;
import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.content.ContentProvider;
import com.sdl.webapp.common.api.content.ContentProviderException;
import com.sdl.webapp.common.api.content.PageNotFoundException;
import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.MvcData;
import com.sdl.webapp.common.api.model.PageModel;
import com.sdl.webapp.common.controller.ViewResolver;
import com.sdl.webapp.common.controller.exception.InternalServerErrorException;
import com.sdl.webapp.common.controller.exception.NotFoundException;
import com.sdl.webapp.common.markup.Markup;

/**
 * Helper class to retrieve Page from Tridion DB.
 * 
 * @author Indra-es
 *
 */
@Component
public class PageRetrieverImpl implements PageRetriever {
	
	  private static final Logger LOG = LoggerFactory.getLogger(PageRetrieverImpl.class);
	  
	  private final ContentProvider contentProvider;
	  private final MediaHelper mediaHelper;
	  private final Markup markup;
	  protected final ViewResolver viewResolver;
	  private final WebRequestContext webRequestContext;
	  
	  @Autowired
	   public PageRetrieverImpl(ContentProvider contentProvider, MediaHelper mediaHelper, 
			   ViewResolver viewResolver, Markup markup, WebRequestContext webRequestContext) {
		  
	        this.contentProvider = contentProvider;
	        this.mediaHelper = mediaHelper;
	        this.markup = markup;
	        this.viewResolver = viewResolver;
	        this.webRequestContext = webRequestContext;
	   }
	  
	
	/* (non-Javadoc)
	 * @see com.sdl.webapp.common.api.PageRetriever#getPageView(javax.servlet.http.HttpServletRequest, com.sdl.webapp.common.api.localization.Localization, java.lang.String)
	 */
    @Override
	public String getPageView( HttpServletRequest request, Localization localization , String path){
 
        return getViewFromPage(request, localization, getPage(path, localization) );
    }
    
    @Override
	public String getPageViewFromPage(HttpServletRequest request, Localization localization, PageModel page) {

		return getViewFromPage(request, localization, page);
	}

    
    /**
     * Make Page object ready for display 
     * @param page
     * @return
     */
    @Override
    public String getViewFromPage(HttpServletRequest request, Localization localization, PageModel page){
    	
    	 LOG.debug("handleGetPage: page={}", page);
         
         LOG.debug("PageRetriever-Page Name: " + page.getName());

        this.setRequestAttributes(request, page);

         final MvcData mvcData = page.getMvcData();
         LOG.debug("Page MvcData: {}", mvcData);

         String view = viewResolver.resolveView(mvcData, "Page", request);
         LOG.debug("PageRetriever-Returning Page View  " + view);
         return view;
    }
    
    /**
	 * @param request
	 * @param localization
	 * @param page
	 */
	private void setRequestAttributes(HttpServletRequest request, final PageModel page) {
		
		if (!isIncludeRequest(request)) {
            request.setAttribute(PAGE_ID, page.getId());
        }

        request.setAttribute(PAGE_MODEL, page);
        request.setAttribute(LOCALIZATION, webRequestContext.getLocalization());
        request.setAttribute(MARKUP, markup);
        request.setAttribute(MEDIAHELPER, mediaHelper);
        request.setAttribute(SCREEN_WIDTH, mediaHelper.getScreenWidth());
        request.setAttribute(SOCIALSHARE_URL, webRequestContext.getFullUrl());
        request.setAttribute(CONTEXTENGINE, webRequestContext.getContextEngine());
	}
    
    /* (non-Javadoc)
	 * @see com.sdl.webapp.common.api.PageRetriever#isIncludeRequest(javax.servlet.http.HttpServletRequest)
	 */
    @Override
	public boolean isIncludeRequest(HttpServletRequest request) {
        return request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE) != null;
    }
    

	@Override
	public PageModel getPage(String path, Localization localization) {
    	
		 try {
	            return contentProvider.getPageModel(path, localization);
	        } 
		    catch (PageNotFoundException e) {
	            LOG.error("Page not found: {}", path, e);
	            throw new NotFoundException("Page not found: " + path, e);
	        } 
		    catch (ContentProviderException e) {
	            LOG.error("An unexpected error occurred", e);
	            throw new InternalServerErrorException("An unexpected error occurred", e);
	        }
    }
    
    /* (non-Javadoc)
	 * @see com.sdl.webapp.common.api.PageRetriever#getPageContent(java.lang.String, com.sdl.webapp.common.api.localization.Localization)
	 */
    @Override
	public InputStream getPageContent(String path, Localization localization) {
    	
        try {
            return contentProvider.getPageContent(path, localization);
        } 
        catch (PageNotFoundException e) {
            LOG.error("Page not found: {}", path, e);
            throw new NotFoundException("Page not found: " + path, e);
        } 
        catch (ContentProviderException e) {
            LOG.error("An unexpected error occurred", e);
            throw new InternalServerErrorException("An unexpected error occurred", e);
        }
    }

    /* (non-Javadoc)
	 * @see com.sdl.webapp.common.api.PageRetriever#getMarkup()
	 */
    @Override
	public Markup getMarkup(){
    	return markup;
    }


}
