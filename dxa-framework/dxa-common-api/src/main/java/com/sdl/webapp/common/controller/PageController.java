package com.sdl.webapp.common.controller;

import static com.sdl.webapp.common.controller.ControllerUtils.INCLUDE_PATH_PREFIX;
import static com.sdl.webapp.common.controller.ControllerUtils.SECTION_ERROR_VIEW;
import static com.sdl.webapp.common.controller.ControllerUtils.SERVER_ERROR_VIEW;
import static com.sdl.webapp.common.controller.RequestAttributeNames.MARKUP;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.sdl.webapp.common.api.PageRetriever;
import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.content.LinkResolver;
import com.sdl.webapp.common.api.content.NavigationProvider;
import com.sdl.webapp.common.api.content.NavigationProviderException;
import com.sdl.webapp.common.api.formats.DataFormatter;
import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.EntityModel;
import com.sdl.webapp.common.api.model.PageModel;
import com.sdl.webapp.common.api.model.RegionModel;
import com.sdl.webapp.common.api.model.ViewModel;
import com.sdl.webapp.common.api.model.entity.SitemapItem;
import com.sdl.webapp.common.controller.exception.BadRequestException;
import com.sdl.webapp.common.controller.exception.NotFoundException;
import com.sdl.webapp.common.exceptions.DxaException;
import com.sdl.webapp.common.markup.Markup;

/**
 * Main controller. This handles requests that come from the client.
 */
@Controller
public class PageController extends BaseController {

    // TODO: Move this to common-impl or core-module

    private static final Logger LOG = LoggerFactory.getLogger(PageController.class);

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    private final LinkResolver linkResolver;
    private final WebRequestContext webRequestContext;
    private final Markup markup;
    private final PageRetriever pageRetriever;
    private final DataFormatter dataFormatters;
    @Value("#{environment.getProperty('AllowJsonResponse', 'false')}")
    private boolean allowJsonResponse;
    @Autowired
    private NavigationProvider navigationProvider;

    @Autowired
    public PageController(LinkResolver linkResolver,  WebRequestContext webRequestContext, Markup markup, 
                           PageRetriever pageRetriever, DataFormatter dataFormatter) {
    	
        this.linkResolver = linkResolver;
        this.webRequestContext = webRequestContext;
        this.markup = markup;
        this.pageRetriever = pageRetriever;
        this.dataFormatters = dataFormatter;
    }

    /**
     * Gets a page requested by a client. This is the main handler method which gets called when a client sends a
     * request for a page.
     *
     * @param request The request.
     * @return The view name of the page.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/**", produces = {MediaType.TEXT_HTML_VALUE})
    public String handleGetPage(HttpServletRequest request) throws Exception {
    	
        final String requestPath = webRequestContext.getRequestPath();
        LOG.trace("handleGetPage: requestPath={}", requestPath);

        final Localization localization = webRequestContext.getLocalization();

        final PageModel originalPageModel = pageRetriever.getPage(requestPath, localization);
        final ViewModel enrichedPageModel = enrichModel(originalPageModel);
        final PageModel page = enrichedPageModel instanceof PageModel ? (PageModel) enrichedPageModel : originalPageModel;

        return pageRetriever.getPageViewFromPage(request, localization, page);
    }

	
    @RequestMapping(method = RequestMethod.GET, value = "/**", produces = {"application/rss+xml", MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public ModelAndView handleGetPageFormatted() {

        final String requestPath = webRequestContext.getRequestPath();
        LOG.trace("handleGetPageFormatted: requestPath={}", requestPath);

        final Localization localization = webRequestContext.getLocalization();
        final PageModel page = pageRetriever.getPage(requestPath, localization);
        enrichEmbeddedModels(page);
        LOG.trace("handleGetPageFormatted: page={}", page);
        return dataFormatters.view(page);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/resolve/{itemId}")
    public String handleResolve(@PathVariable String itemId, @RequestParam String localizationId,
                                @RequestParam(required = false) String defaultPath,
                                @RequestParam(required = false) String defaultItem) throws DxaException {
        if (StringUtils.isEmpty(localizationId)) {
            throw new DxaException(new IllegalArgumentException("Localization ID is null while it shouldn't be"));
        }

        String url = linkResolver.resolveLink(itemId, localizationId);
        if (StringUtils.isEmpty(url) && !StringUtils.isEmpty(defaultItem)) {
            url = linkResolver.resolveLink(defaultItem, localizationId);
        }
        if (StringUtils.isEmpty(url)) {
            url = StringUtils.isEmpty(defaultPath) ? "/" : defaultPath;
        }
        return "redirect:" + url;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{locPath}/resolve/{itemId}")
    public String handleResolveLoc(@PathVariable String itemId,
                                   @RequestParam String localizationId, @RequestParam String defaultPath,
                                   @RequestParam(required = false) String defaultItem) throws DxaException {
        return handleResolve(itemId, localizationId, defaultPath, defaultItem);
    }

    // Blank page for XPM
    @RequestMapping(method = RequestMethod.GET, value = "/se_blank.html", produces = "text/html")
    @ResponseBody
    public String blankPage() {
        return "";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/navigation.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    String handleGetNavigationJson() throws NavigationProviderException, JsonProcessingException {
        LOG.trace("handleGetNavigationJson");


        SitemapItem model = navigationProvider.getNavigationModel(webRequestContext.getLocalization());

        return new ObjectMapper()
                .registerModule(new JodaModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .writeValueAsString(model);
    }

    /**
     * Throws a {@code BadRequestException} when a request is made to an URL under /system/mvc which is not handled
     * by another controller.
     *
     * @param request The request.
     */
    @RequestMapping(method = RequestMethod.GET, value = INCLUDE_PATH_PREFIX + "**")
    public void handleGetUnknownAction(HttpServletRequest request) {
        throw new BadRequestException("Request to unknown action: " + urlPathHelper.getRequestUri(request));
    }

    /**
     * Handles a {@code NotFoundException}.
     *
     * @param request The request.
     * @return The name of the view that renders the "not found" page.
     */
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(HttpServletRequest request, HttpServletResponse response) 
    		throws Exception {
    	
        // TODO TSI-775: No need to prefix with WebRequestContext.Localization.Path here (?)
        String path = webRequestContext.getLocalization().getPath();
        String notFoundPageUrl = (path.endsWith("/") ? path : path + "/") + "error-404";

        PageModel originalPageModel = pageRetriever.getPage(notFoundPageUrl ,
        		                                    webRequestContext.getLocalization());

        final ViewModel enrichedPageModel = enrichModel(originalPageModel);
        final PageModel pageModel = enrichedPageModel instanceof PageModel ? (PageModel) enrichedPageModel : originalPageModel;
              
        response.setStatus(SC_NOT_FOUND);
        return pageRetriever.getViewFromPage(request, webRequestContext.getLocalization(), pageModel);
    }

    /**
     * Handles non-specific exceptions.
     *
     * @param request   The request.
     * @param response   The response
     * @param exception The exception.
     * @return The name of the view that renders the "internal server error" page.
     */
    @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        LOG.error("Exception while processing request for: {}", urlPathHelper.getRequestUri(request), exception);
        request.setAttribute(MARKUP, markup);
          
        // set appropriate response status code 500, except if it's an include
        if( ! pageRetriever.isIncludeRequest(request)){
	    	final ServletServerHttpResponse res = new ServletServerHttpResponse(response);
	    	res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
	        res.close();
        }
        return pageRetriever.isIncludeRequest(request) ? SECTION_ERROR_VIEW : SERVER_ERROR_VIEW;
    }



    /**
     * Enriches all the Region/Entity Models embedded in the given Page Model.
     * Used by <see cref="FormatDataAttribute"/> to get all embedded Models enriched without rendering any Views.
     *
     * @param model The Page Model to enrich.
     */
    private void enrichEmbeddedModels(PageModel model) {
        if (model == null) {
            return;
        }

        for (RegionModel region : model.getRegions()) {
            // NOTE: Currently not enriching the Region Model itself, because we don't support custom Region Controllers (yet).
            for (int i = 0; i < region.getEntities().size(); i++) {
                EntityModel entity = region.getEntities().get(i);
                if (entity != null && entity.getMvcData() != null) {
                    region.getEntities().set(i, enrichEntityModel(entity));
                }
            }
        }
    }

}
