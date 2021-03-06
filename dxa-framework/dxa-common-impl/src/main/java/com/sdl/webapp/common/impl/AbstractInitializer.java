package com.sdl.webapp.common.impl;

import com.google.common.base.Strings;
import com.sdl.webapp.common.api.model.EntityModel;
import com.sdl.webapp.common.api.model.MvcDataImpl;
import com.sdl.webapp.common.api.model.RegionModel;
import com.sdl.webapp.common.api.model.ViewModel;
import com.sdl.webapp.common.api.model.ViewModelRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractInitializer {

    @Autowired
    private ViewModelRegistry viewModelRegistry;

    protected void registerViewModel(String viewName, Class<? extends ViewModel> entityClass) {
        registerViewModel(viewName, entityClass, null);
    }

    protected void registerViewModel(String viewName, Class<? extends ViewModel> entityClass, String controllerName) {
        if (Strings.isNullOrEmpty(controllerName)) {
            if (EntityModel.class.isAssignableFrom(entityClass)) {
                controllerName = "Entity";
            } else if (RegionModel.class.isAssignableFrom(entityClass)) {
                controllerName = "Region";
            } else {
                controllerName = "Page";
            }
        }

        MvcDataImpl mvcData = new MvcDataImpl();
        mvcData.setAreaName(getAreaName());
        mvcData.setViewName(viewName);
        mvcData.setControllerName(controllerName);

        viewModelRegistry.registerViewModel(mvcData, entityClass);
    }

    protected abstract String getAreaName();
}
