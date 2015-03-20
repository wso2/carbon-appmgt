package org.wso2.carbon.appmgt.mobile.mdm;

/**
 * Created by dilan on 3/20/15.
 */
public class MDMServiceReferenceHolder {

    private  static MDMServiceReferenceHolder mdmServiceReferenceHolder;

    private MDMOperations mdmOperations;

    private MDMServiceReferenceHolder(){

    }

    public static MDMServiceReferenceHolder getInstance(){
        if(mdmServiceReferenceHolder == null){
            mdmServiceReferenceHolder = new MDMServiceReferenceHolder();
        }
        return mdmServiceReferenceHolder;
    }

    public MDMOperations getMDMOperation(){
            return mdmOperations;
    }

    public void setMDMOperation(MDMOperations mdmOperations){
        this.mdmOperations = mdmOperations;
    }
}
