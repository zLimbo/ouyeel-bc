package com.ouyeel.obfm.sql.exception;


//import com.baosight.obmp.chain.sql.basic.ExceptionCode;

import com.ouyeel.obfm.sql.basic.ExceptionCode;

public class BusinessException extends RuntimeException {

    public BusinessException(ExceptionCode businessErrorCode) {
        super(businessErrorCode.toString());
    }
}
