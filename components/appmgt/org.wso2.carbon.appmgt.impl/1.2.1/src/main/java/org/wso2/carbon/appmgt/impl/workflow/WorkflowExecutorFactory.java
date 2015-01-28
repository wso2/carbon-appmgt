/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.appmgt.impl.workflow;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.impl.AppMConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.securevault.SecretResolver;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;

public class WorkflowExecutorFactory {

    private static final Log log = LogFactory.getLog(WorkflowExecutorFactory.class);

    private static final QName PROP_Q = new QName("Property");

    private static final QName ATT_NAME = new QName("name");

    private SecretResolver secretResolver;

    private boolean initialized;


    private static WorkflowExecutorFactory instance;

    private WorkflowExecutorFactory(){
    }

    public static synchronized WorkflowExecutorFactory getInstance(){
        if(instance == null){
            instance = new WorkflowExecutorFactory();
        }
        return instance;
    }


    public TenantWorkflowConfigHolder getWorkflowConfigurations() throws WorkflowException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String cacheName = tenantDomain + "_" + AppMConstants.WORKFLOW_CACHE_NAME;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        //synchronized (cacheName.intern()){
        Cache workflowCache = Caching.getCacheManager(AppMConstants.APP_MANAGER_CACHE_MANAGER).getCache(AppMConstants.WORKFLOW_CACHE_NAME);
        TenantWorkflowConfigHolder workflowConfig = (TenantWorkflowConfigHolder) workflowCache.get(cacheName);

        if (workflowConfig != null) {
            return workflowConfig;
        } else {
            TenantWorkflowConfigHolder configHolder = new TenantWorkflowConfigHolder(tenantDomain,tenantId);
            try {
                configHolder.load();
                workflowCache.put(cacheName, configHolder);
                return configHolder;
            } catch (WorkflowException e) {
                handleException("Error occurred while creating workflow configurations for tenant " + tenantDomain, e);
            }
        }
        // }

        return null;
    }

    public WorkflowExecutor getWorkflowExecutor(String workflowExecutorType) throws WorkflowException {
        TenantWorkflowConfigHolder holder = null;
        try {
            holder = this.getWorkflowConfigurations();
            if (holder != null) {
                return holder.getWorkflowExecutor(workflowExecutorType);
            }
        } catch (WorkflowException e) {
            handleException("Error while creating WorkFlowDTO for " + workflowExecutorType);
        }
        return null;
    }

    /**
     * Find and invoke the setter method with the name of form setXXX passing in the value given
     * on the POJO object
     *
     * @param name name of the setter field
     * @param val  value to be set
     * @param obj  POJO instance
     */
    public void setInstanceProperty(String name, Object val, Object obj) throws WorkflowException {

        String mName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Method method;

        try {
            Method[] methods = obj.getClass().getMethods();
            boolean invoked = false;

            for (Method method1 : methods) {
                if (mName.equals(method1.getName())) {
                    Class[] params = method1.getParameterTypes();
                    if (params.length != 1) {
                        handleException("Did not find a setter method named : " + mName +
                                "() that takes a single String, int, long, float, double ," +
                                "OMElement or boolean parameter");
                    } else if (val instanceof String) {
                        String value = (String) val;
                        if (String.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, String.class);
                            method.invoke(obj, new String[]{value});
                        } else if (int.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, int.class);
                            method.invoke(obj, new Integer[]{new Integer(value)});
                        } else if (long.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, long.class);
                            method.invoke(obj, new Long[]{new Long(value)});
                        } else if (float.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, float.class);
                            method.invoke(obj, new Float[]{new Float(value)});
                        } else if (double.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, double.class);
                            method.invoke(obj, new Double[]{new Double(value)});
                        } else if (boolean.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, boolean.class);
                            method.invoke(obj, new Boolean[]{Boolean.valueOf(value)});
                        } else {
                            continue;
                        }
                    } else if (val instanceof OMElement && OMElement.class.equals(params[0])) {
                        method = obj.getClass().getMethod(mName, OMElement.class);
                        method.invoke(obj, new OMElement[]{(OMElement) val});
                    } else {
                        continue;
                    }
                    invoked = true;
                    break;
                }
            }

            if (!invoked) {
                handleException("Did not find a setter method named : " + mName +
                        "() that takes a single String, int, long, float, double " +
                        "or boolean parameter");
            }

        } catch (Exception e) {
            handleException("Error invoking setter method named : " + mName +
                    "() that takes a single String, int, long, float, double " +
                    "or boolean parameter", e);
        }
    }

    private static void handleException(String msg) throws WorkflowException{
        log.error(msg);
        throw new WorkflowException(msg);
    }

    private static void handleException(String msg, Exception e) throws WorkflowException{
        log.error(msg, e);
        throw new WorkflowException(msg, e);
    }

}
