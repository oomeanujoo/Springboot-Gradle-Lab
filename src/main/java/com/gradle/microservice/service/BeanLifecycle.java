package com.gradle.microservice.service;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class demonstrates the Bean Lifecycle in Spring.
 *
 * Order of execution:
 * 1. Constructor       → Bean is being created
 * 2. @PostConstruct    → Bean is fully created and dependencies injected
 * 3. BeanInAction()    → Bean is being used by application (called manually)
 * 4. @PreDestroy       → Bean is about to be destroyed (app shutdown)
 */
@Component
public class BeanLifecycle {

    // Logger should use its OWN class name — BeanLifecycle.class ✅
    private static final Logger log = LoggerFactory.getLogger(BeanLifecycle.class);

    /**
     * STEP 1 — Constructor
     * Called FIRST when Spring creates this bean.
     * Note: @Autowired dependencies are NOT available here yet!
     */
    public BeanLifecycle() {
        log.info("STEP 1 — Bean Initialised");
        System.out.println("Bean Initialised");
    }

    /**
     * STEP 2 — @PostConstruct
     * Called AFTER constructor and AFTER all dependencies are injected.
     * Best place for initialization logic — like loading cache, opening connections.
     */
    @PostConstruct
    public void BeanCreation() {
        log.info("STEP 2 — Bean Created");
        System.out.println("Bean Created");
    }

    /**
     * STEP 3 — Normal Method (Bean in Action)
     * This is where bean does its actual work.
     * This method is NOT called automatically —
     * it must be called manually from controller or service.
     */
    public void BeanInAction() {
        log.info("STEP 3 — Bean in Action — Registered in IOC");
        System.out.println("Registered in IOC");
    }

    /**
     * STEP 4 — @PreDestroy
     * Called just BEFORE Spring destroys this bean (app shutdown).
     * Best place for cleanup — like closing connections, releasing resources.
     */
    @PreDestroy
    public void Cleanup() {
        log.info("STEP 4 — Bean Ready to Destroy");
        System.out.println("Bean Ready to Destroy");
    }
}
