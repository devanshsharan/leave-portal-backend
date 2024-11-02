package com.example.leavePortal.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.leavePortal.service.LeaveRequestServiceImplementation.*(..))")
    public void logMethodCall(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        Object[] methodArgs = jp.getArgs();
        LOGGER.info("Method called: {} with arguments: {}", methodName, methodArgs);
    }

    @After("execution(* com.example.leavePortal.service.LeaveRequestServiceImplementation.*(..))")
    public void logMethodExecuted(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        LOGGER.info("Method executed: {}", methodName);
    }
}
