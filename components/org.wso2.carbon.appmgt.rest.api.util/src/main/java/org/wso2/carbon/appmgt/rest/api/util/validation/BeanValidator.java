package org.wso2.carbon.appmgt.rest.api.util.validation;


import org.wso2.carbon.appmgt.rest.api.util.utils.RestApiUtil;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
public class BeanValidator {

    private Validator validator;

    public BeanValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public void validate(Object object) throws ValidationException {
        if (object == null) {
            throw new ValidationException("Payload cannot be null");
        }
        //contains internal error message
        StringBuilder internalErrMsg = null;
        //validate request payload
        Set<ConstraintViolation<Object>> constraintViolations =
                validator.validate(object);
        //check if there are bean validation violations
        if (!constraintViolations.isEmpty()) {
            internalErrMsg = new StringBuilder();
            //check through each property in the bean class for validation violation
            for (ConstraintViolation<Object> violation :
                    constraintViolations) {
                // fail
                //Appending property name and violated validation
                internalErrMsg.append(violation.getPropertyPath().toString()).append(" ").append(
                        violation.getMessage()).append("; ");
            }
            throw new RestApiUtil().buildBadRequestException(internalErrMsg.toString());
        }
    }
}
