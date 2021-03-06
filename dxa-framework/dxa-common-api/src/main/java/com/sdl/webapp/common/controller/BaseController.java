package com.sdl.webapp.common.controller;

import com.sdl.webapp.common.api.model.EntityModel;
import com.sdl.webapp.common.api.model.MvcData;
import com.sdl.webapp.common.api.model.RegionModel;
import com.sdl.webapp.common.api.model.ViewModel;
import com.sdl.webapp.common.api.model.entity.ExceptionEntity;
import com.sdl.webapp.common.controller.exception.NotFoundException;
import com.sdl.webapp.common.util.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract superclass for controllers with utility methods and exception handling.
 */
public abstract class BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    protected ViewResolver viewResolver;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private static Boolean isCustomAction(MvcData mvcData) {
        return !Objects.equals(mvcData.getActionName(), "Entity")
                || !Objects.equals(mvcData.getControllerName(), "Entity")
                || !Objects.equals(mvcData.getControllerAreaName(), "Core");
    }

    protected RegionModel getRegionFromRequest(HttpServletRequest request, String regionName) {
        RegionModel region = (RegionModel) request.getAttribute("_region_");
        if (region == null) {
            LOG.error("Region not found on page: {}", regionName);
            throw new NotFoundException("Region not found on page: " + regionName);
        }
        return region;
    }

    protected EntityModel getEntityFromRequest(HttpServletRequest request, String entityId) {
        final EntityModel entity = (EntityModel) request.getAttribute("_entity_");
        if (entity == null) {
            LOG.error("Entity not found in request: {}", entityId);
            throw new NotFoundException("Entity not found in request: " + entityId);
        }
        return entity;
    }

    @RequestMapping(value = ControllerUtils.INCLUDE_PATH_PREFIX + ControllerUtils.SECTION_ERROR_VIEW)
    public String handleJspIncludesErrors() {
        LOG.error("Unhandled exception from JSP include action");
        return ControllerUtils.SECTION_ERROR_VIEW;
    }


    @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, Exception exception) {
        LOG.error("Exception while processing request for: {}", request.getRequestURL(), exception);
        return ControllerUtils.SECTION_ERROR_VIEW;
    }

    protected String resolveView(MvcData mvcData, String type, HttpServletRequest request) {
        return this.viewResolver.resolveView(mvcData, type, request);
    }

    protected String resolveView(String viewBaseDir, String view, MvcData mvcData, HttpServletRequest request) {
        return this.viewResolver.resolveView(viewBaseDir, view, mvcData, request);
    }

    /**
     * This is the method to override if you need to add custom model population logic,
     * first calling the base class and then adding your own logic
     *
     * @param model The model which you wish to add data to.
     * @return A fully populated view model combining CMS content with other data
     */
    protected ViewModel enrichModel(ViewModel model) throws Exception {
        //Check if an exception was generated when creating the model, so now is the time to throw it
        // TODO: shouldn't we just render the ExceptionEntity using an Exception View?
        if (model.getClass().isAssignableFrom(ExceptionEntity.class)) {
            ExceptionEntity exceptionEntity = (ExceptionEntity) model;
            throw exceptionEntity.getException();
        }

        return (ViewModel) processModel(model, model.getClass());
    }

    /**
     * This is the method to override if you need to add custom model population logic, first calling the base class and then adding your own logic
     *
     * @param sourceModel The model to process
     * @param type        The type of view model required
     * @return A processed view model
     * @deprecated Deprecated in DXA 1.1. Override EnrichModel instead.
     */
    @Deprecated
    protected Object processModel(Object sourceModel, Class type) {
        // NOTE: Intentionally loosely typed for backwards compatibility; this was part of the V1.0 (semi-)public API
        return sourceModel;
    }

    /**
     * Enriches a given Entity Model using an appropriate (custom) Controller.
     *
     * @param entity The Entity Model to enrich.
     * @return The enriched Entity Model.
     * <p/>
     * This method is different from EnrichModel in that it doesn't expect the current Controller to be able to enrich the Entity Model;
     * it creates a Controller associated with the Entity Model for that purpose.
     * It is used by PageController.enrichEmbeddedModels.
     */
    protected EntityModel enrichEntityModel(EntityModel entity) {
        if (entity == null || entity.getMvcData() == null || !isCustomAction(entity.getMvcData())) {
            return entity;
        }

        MvcData mvcData = entity.getMvcData();

        String controllerName = mvcData.getControllerName() != null ? mvcData.getControllerName() : "Entity";
        String controllerAreaName = mvcData.getControllerAreaName() != null ? mvcData.getControllerAreaName() : "Core";


        Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                this.requestMappingHandlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> item : handlerMethods.entrySet()) {
            RequestMappingInfo mapping = item.getKey();
            HandlerMethod method = item.getValue();

            for (String urlPattern : mapping.getPatternsCondition().getPatterns()) {
                if (urlPattern.contains("/" + controllerAreaName + "/" + controllerName)) {
                    HandlerMethod controllerMethod = handlerMethods.get(mapping);
                    BaseController controller = (BaseController) ApplicationContextHolder.getContext().getBean(controllerMethod.getBean().toString());
                    try {
                        controller.enrichModel(entity);
                        return entity;
                    } catch (Exception e) {
                        LOG.error("Error in EnrichModel", e);
                        return new ExceptionEntity(e); // TODO: What about MvcData?
                    }
                }
            }
        }
        return entity;
    }
}
